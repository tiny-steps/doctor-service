-- ROLLBACK_V10__fix_schema_migration_issues.sql
-- Rollback script for V10 migration
-- This script reverts the schema changes made in V10
-- ============================================================================
-- PART 1: Rollback specialization_id changes
-- ============================================================================
DO $$ BEGIN -- Drop foreign key constraint
IF EXISTS (
    SELECT 1
    FROM information_schema.table_constraints
    WHERE table_name = 'doctor_specializations'
        AND constraint_name = 'fk_doctor_specializations_specialization'
) THEN
ALTER TABLE doctor_specializations DROP CONSTRAINT fk_doctor_specializations_specialization;
RAISE NOTICE 'Dropped foreign key constraint';
END IF;
-- Drop unique constraint
IF EXISTS (
    SELECT 1
    FROM information_schema.table_constraints
    WHERE table_name = 'doctor_specializations'
        AND constraint_name = 'uk_doctor_specializations_doctor_specialization'
) THEN
ALTER TABLE doctor_specializations DROP CONSTRAINT uk_doctor_specializations_doctor_specialization;
RAISE NOTICE 'Dropped unique constraint';
END IF;
-- Drop specialization_id column
IF EXISTS (
    SELECT 1
    FROM information_schema.columns
    WHERE table_name = 'doctor_specializations'
        AND column_name = 'specialization_id'
) THEN
ALTER TABLE doctor_specializations DROP COLUMN specialization_id;
RAISE NOTICE 'Dropped specialization_id column';
END IF;
RAISE NOTICE 'Specialization_id rollback completed';
END $$;
-- ============================================================================
-- PART 2: Rollback status column changes (if needed)
-- ============================================================================
DO $$ BEGIN -- Only rollback if the status column was created/modified in V10
-- This is a conservative approach - we don't want to break existing data
-- Check if status column exists and is smallint
IF EXISTS (
    SELECT 1
    FROM information_schema.columns
    WHERE table_name = 'doctor_addresses'
        AND column_name = 'status'
        AND data_type = 'smallint'
) THEN RAISE NOTICE 'Status column exists as smallint - keeping it for data safety';
RAISE NOTICE 'If you need to remove it, do so manually after verifying no data loss';
ELSE RAISE NOTICE 'Status column not found or not smallint - no rollback needed';
END IF;
END $$;
-- ============================================================================
-- PART 3: Drop indexes that were created in V10
-- ============================================================================
-- Drop indexes for doctor_addresses (only if they were created in V10)
DROP INDEX IF EXISTS idx_doctor_addresses_status;
DROP INDEX IF EXISTS idx_doctor_addresses_doctor_status;
DROP INDEX IF EXISTS idx_doctor_addresses_address_status;
-- Drop indexes for doctor_specializations (only if they were created in V10)
DROP INDEX IF EXISTS idx_doctor_specializations_specialization_id;
DROP INDEX IF EXISTS idx_doctor_specializations_doctor_id;
-- Note: We don't drop specializations table indexes as they might be used elsewhere
RAISE NOTICE 'Rollback completed - please verify your application works correctly';

