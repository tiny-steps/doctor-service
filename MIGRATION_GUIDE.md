# Specialization Refactor - Migration Guide

## Overview

This guide helps migrate existing doctor specializations from the old format (text-based) to the new format (ID-based with master table).

## Pre-Migration Checklist

- [ ] Backup database
- [ ] Verify doctor-service is stopped
- [ ] Review existing specializations
- [ ] Test migration on staging first

---

## Step 1: Check Current Data

### Query existing specializations

```sql
-- Count total specializations
SELECT COUNT(*) FROM doctor_specializations;

-- View distinct speciality names
SELECT DISTINCT speciality, COUNT(*) as doctor_count
FROM doctor_specializations
WHERE speciality IS NOT NULL
GROUP BY speciality
ORDER BY doctor_count DESC;

-- Check if specializations master table exists
SELECT COUNT(*) FROM specializations;
```

**Expected Output:**

- If `specializations` table is empty → Need migration
- If V7 migration already ran → Already migrated

---

## Step 2: Automatic Migration (Flyway V7)

**Good News**: Migration script already exists!

**File**: `server/doctor-service/src/main/resources/db/migration/V7__refactor_specializations_to_many_to_many.sql`

**What it does:**

1. Creates `specializations` master table
2. Inserts distinct specialities from existing `doctor_specializations`
3. Renames old `doctor_specializations` → `old_doctor_specializations`
4. Creates new `doctor_specializations` with `specialization_id` FK
5. Migrates data from old to new structure
6. Drops `old_doctor_specializations` table

**To Run:**

```bash
cd /Users/apple/Freelance\ Projects/tiny-steps/server/doctor-service
mvn spring-boot:run
```

Flyway will automatically run V7 migration on startup if not already applied.

---

## Step 3: Verify Migration

### After Service Starts

```sql
-- 1. Check specializations master table
SELECT id, name, is_active FROM specializations ORDER BY name;

-- 2. Verify doctor-specialization mappings
SELECT
  ds.id as junction_id,
  d.name as doctor_name,
  sm.name as specialization_name,
  ds.subspecialization
FROM doctor_specializations ds
JOIN doctors d ON ds.doctor_id = d.id
JOIN specializations sm ON ds.specialization_id = sm.id
ORDER BY d.name, sm.name;

-- 3. Count before/after
SELECT
  (SELECT COUNT(*) FROM specializations) as master_count,
  (SELECT COUNT(*) FROM doctor_specializations) as junction_count;
```

**Expected Results:**

- `specializations` table has unique specialization names
- `doctor_specializations` has `specialization_id` (not `speciality` text)
- All doctors' specializations preserved

---

## Step 4: Manual Migration (If Needed)

### If V7 Migration Hasn't Run

**Option A: Let Flyway Handle It (Recommended)**

```bash
# Just start the service, Flyway will run V7 automatically
mvn spring-boot:run
```

**Option B: Manual SQL (Only if Flyway fails)**

```sql
-- WARNING: Run these queries in a transaction!
BEGIN;

-- 1. Create master specializations table
CREATE TABLE IF NOT EXISTS specializations (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  name VARCHAR(100) UNIQUE NOT NULL,
  description TEXT,
  is_active BOOLEAN DEFAULT true,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Insert distinct specializations from existing data
INSERT INTO specializations (name, is_active)
SELECT DISTINCT speciality, true
FROM doctor_specializations
WHERE speciality IS NOT NULL AND speciality <> ''
ON CONFLICT (name) DO NOTHING;

-- 3. Backup old table
ALTER TABLE doctor_specializations RENAME TO old_doctor_specializations;

-- 4. Create new junction table
CREATE TABLE doctor_specializations (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  doctor_id UUID NOT NULL REFERENCES doctors(id) ON DELETE CASCADE,
  specialization_id UUID NOT NULL REFERENCES specializations(id) ON DELETE CASCADE,
  subspecialization VARCHAR(100),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  UNIQUE(doctor_id, specialization_id)
);

-- 5. Migrate data to new structure
INSERT INTO doctor_specializations (doctor_id, specialization_id, subspecialization)
SELECT
  ods.doctor_id,
  sm.id,
  ods.subspecialization
FROM old_doctor_specializations ods
JOIN specializations sm ON sm.name = ods.speciality
WHERE ods.speciality IS NOT NULL;

-- 6. Verify counts match
SELECT
  'Old table count: ' || COUNT(*) FROM old_doctor_specializations
UNION ALL
SELECT
  'New table count: ' || COUNT(*) FROM doctor_specializations;

-- If counts match and data looks good:
DROP TABLE old_doctor_specializations;

COMMIT;
```

---

## Step 5: Rollback (Emergency Only)

**File**: `server/doctor-service/src/main/resources/db/migration/ROLLBACK_V7__refactor_specializations_to_many_to_many.sql`

```sql
-- WARNING: This will lose any NEW specializations created after migration
BEGIN;

-- 1. Rename current table
ALTER TABLE IF EXISTS doctor_specializations RENAME TO new_doctor_specializations;

-- 2. Restore old structure
CREATE TABLE doctor_specializations (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  doctor_id UUID NOT NULL REFERENCES doctors(id) ON DELETE CASCADE,
  speciality VARCHAR(100),
  subspecialization VARCHAR(100),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 3. Copy data back (from new to old format)
INSERT INTO doctor_specializations (doctor_id, speciality, subspecialization)
SELECT
  nds.doctor_id,
  sm.name,
  nds.subspecialization
FROM new_doctor_specializations nds
JOIN specializations sm ON sm.id = nds.specialization_id;

-- 4. Drop new tables
DROP TABLE new_doctor_specializations;
DROP TABLE specializations CASCADE;

COMMIT;
```

---

## Step 6: Post-Migration Tasks

### 1. Test API Endpoints

```bash
# Get all specializations
curl -X GET http://localhost:8084/api/v1/specializations/master \
  -H "Authorization: Bearer <token>"

# Expected: List of specializations with IDs

# Create new specialization
curl -X POST http://localhost:8084/api/v1/specializations/master \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"name": "Neurology", "description": "Brain and nervous system"}'

# Expected: 201 Created with new specialization object
```

### 2. Test Frontend

1. Login to application
2. Navigate to "Create Doctor" page
3. Check specializations dropdown
   - Should show existing specializations
   - Should NOT allow creating new ones (no "Add" option)
4. Select a specialization
5. Submit form
6. Verify doctor is created successfully

### 3. Verify No Duplicates

```sql
-- This should return 0 rows (no duplicates)
SELECT name, COUNT(*) as count
FROM specializations
GROUP BY name
HAVING COUNT(*) > 1;

-- Verify each specialization is used by multiple doctors
SELECT
  sm.name,
  COUNT(DISTINCT ds.doctor_id) as doctor_count
FROM specializations sm
LEFT JOIN doctor_specializations ds ON ds.specialization_id = sm.id
GROUP BY sm.name
ORDER BY doctor_count DESC;
```

---

## Troubleshooting

### Issue 1: Migration Fails - Foreign Key Constraint

**Symptom**: Error about foreign key constraint violation  
**Cause**: Orphaned records in `doctor_specializations`  
**Solution**:

```sql
-- Find orphaned records
SELECT * FROM doctor_specializations ds
WHERE NOT EXISTS (SELECT 1 FROM doctors d WHERE d.id = ds.doctor_id);

-- Delete orphaned records
DELETE FROM doctor_specializations ds
WHERE NOT EXISTS (SELECT 1 FROM doctors d WHERE d.id = ds.doctor_id);
```

### Issue 2: Duplicate Specialization Names (Different Case)

**Symptom**: Error "duplicate key value violates unique constraint"  
**Cause**: "Cardiology" and "cardiology" treated as different  
**Solution**: Normalize names before migration

```sql
-- Standardize to Title Case
UPDATE doctor_specializations
SET speciality = INITCAP(TRIM(speciality))
WHERE speciality IS NOT NULL;
```

### Issue 3: Null Specializations

**Symptom**: Some doctors have no specializations after migration  
**Cause**: Original `speciality` field was NULL or empty  
**Solution**: These doctors are skipped intentionally. Add specializations manually via admin UI.

---

## Migration Timeline

**Estimated Time**: 5-10 minutes (depending on data size)

| Step          | Duration | Notes                   |
| ------------- | -------- | ----------------------- |
| Backup        | 1-2 min  | Essential!              |
| Stop Service  | < 1 min  |                         |
| Run Migration | 1-5 min  | Depends on record count |
| Verify        | 1-2 min  | SQL queries             |
| Start Service | 1-2 min  |                         |
| Test          | 2-3 min  | API + Frontend          |

---

## Success Criteria

✅ Flyway shows V7 migration as "Success"  
✅ `specializations` table exists and has data  
✅ `doctor_specializations` has `specialization_id` column  
✅ No duplicate specialization names in `specializations`  
✅ All doctors' specializations preserved  
✅ API endpoints return 200 OK  
✅ Frontend dropdown shows specializations  
✅ Can create new doctor with specializations

---

## Emergency Contacts

If migration fails:

1. **Stop the service immediately**
2. **Do NOT attempt to fix in production**
3. **Restore database from backup**
4. **Test migration on staging first**
5. **Contact DevOps team**

---

## Post-Migration Monitoring

### First 24 Hours

- Monitor logs for errors related to specializations
- Check for 500 errors in API calls
- Verify new doctors are being created successfully

### First Week

- Review user feedback
- Monitor for duplicate specializations being reported
- Check database size (should be smaller due to deduplication)

---

**Created**: October 13, 2025  
**Status**: Ready for Migration  
**Risk Level**: LOW (Flyway handles automatically + rollback available)

