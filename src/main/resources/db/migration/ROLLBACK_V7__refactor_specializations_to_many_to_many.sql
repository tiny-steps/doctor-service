-- ROLLBACK_V7__refactor_specializations_to_many_to_many.sql
-- Rollback script for V7 migration
-- WARNING: This is a manual rollback script. Run only if you need to revert the changes.
-- Step 1: Ensure speciality column has the data (copy from master table if needed)
UPDATE doctor_specializations ds
SET speciality = s.name
FROM specializations s
WHERE ds.specialization_id = s.id
    AND (
        ds.speciality IS NULL
        OR ds.speciality = ''
    );
-- Step 2: Drop the unique constraint
ALTER TABLE doctor_specializations DROP CONSTRAINT IF EXISTS uk_doctor_specializations_doctor_specialization;
-- Step 3: Drop the foreign key constraint
ALTER TABLE doctor_specializations DROP CONSTRAINT IF EXISTS fk_doctor_specializations_specialization;
-- Step 4: Drop the new index
DROP INDEX IF EXISTS idx_doctor_specializations_specialization_id;
-- Step 5: Drop the specialization_id column
ALTER TABLE doctor_specializations DROP COLUMN IF EXISTS specialization_id;
-- Step 6: Drop the specializations master table
DROP TABLE IF EXISTS specializations CASCADE;
-- Step 7: Remove comment from speciality column
COMMENT ON COLUMN doctor_specializations.speciality IS NULL;
-- Note: After rollback, the old structure will be restored
-- Multiple doctors can have duplicate "Cardiology" text entries again


