-- V10__fix_schema_migration_issues.sql
-- Fix database schema migration issues for doctor service
-- This migration addresses the following problems:
-- 1. Status column type casting issue in doctor_addresses
-- 2. Specialization_id column addition issue in doctor_specializations
-- ============================================================================
-- PART 1: Fix status column type casting issue in doctor_addresses
-- ============================================================================
DO $$
DECLARE current_type text;
column_exists boolean;
BEGIN -- Check if status column exists
SELECT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'doctor_addresses'
            AND column_name = 'status'
    ) INTO column_exists;
IF column_exists THEN -- Get the current data type
SELECT data_type INTO current_type
FROM information_schema.columns
WHERE table_name = 'doctor_addresses'
    AND column_name = 'status';
-- If it's not already smallint, convert it properly
IF current_type != 'smallint' THEN RAISE NOTICE 'Converting status column from % to smallint',
current_type;
-- Create a temporary column with the correct type
ALTER TABLE doctor_addresses
ADD COLUMN status_new SMALLINT;
-- Convert existing data based on current type
IF current_type = 'character varying'
OR current_type = 'text' THEN -- Handle string values
UPDATE doctor_addresses
SET status_new = CASE
        WHEN UPPER(TRIM(status::text)) = 'ACTIVE' THEN 0
        WHEN UPPER(TRIM(status::text)) = 'INACTIVE' THEN 1
        WHEN status::text = '0' THEN 0
        WHEN status::text = '1' THEN 1
        ELSE 0 -- Default to ACTIVE
    END;
ELSIF current_type = 'integer' THEN -- Handle integer values
UPDATE doctor_addresses
SET status_new = CASE
        WHEN status::integer = 0 THEN 0
        WHEN status::integer = 1 THEN 1
        ELSE 0 -- Default to ACTIVE
    END;
ELSE -- For other types, try to convert to integer first, then map
UPDATE doctor_addresses
SET status_new = CASE
        WHEN status::text ~ '^[0-9]+$'
        AND status::integer = 0 THEN 0
        WHEN status::text ~ '^[0-9]+$'
        AND status::integer = 1 THEN 1
        WHEN UPPER(TRIM(status::text)) = 'ACTIVE' THEN 0
        WHEN UPPER(TRIM(status::text)) = 'INACTIVE' THEN 1
        ELSE 0 -- Default to ACTIVE
    END;
END IF;
-- Drop the old column and rename the new one
ALTER TABLE doctor_addresses DROP COLUMN status;
ALTER TABLE doctor_addresses
    RENAME COLUMN status_new TO status;
-- Set constraints
ALTER TABLE doctor_addresses
ALTER COLUMN status
SET NOT NULL;
ALTER TABLE doctor_addresses
ALTER COLUMN status
SET DEFAULT 0;
RAISE NOTICE 'Successfully converted status column to smallint';
ELSE RAISE NOTICE 'Status column is already smallint, no conversion needed';
END IF;
ELSE -- Column doesn't exist, create it
ALTER TABLE doctor_addresses
ADD COLUMN status SMALLINT DEFAULT 0 NOT NULL;
RAISE NOTICE 'Created status column as smallint';
END IF;
END $$;
-- Add comment for clarity
COMMENT ON COLUMN doctor_addresses.status IS 'Status of doctor-address relationship: 0=ACTIVE, 1=INACTIVE (enum ordinal values)';
-- ============================================================================
-- PART 2: Fix specialization_id column addition issue in doctor_specializations
-- ============================================================================
DO $$
DECLARE column_exists boolean;
has_data boolean;
BEGIN -- Check if specialization_id column exists
SELECT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'doctor_specializations'
            AND column_name = 'specialization_id'
    ) INTO column_exists;
-- Check if table has data
SELECT EXISTS (
        SELECT 1
        FROM doctor_specializations
        LIMIT 1
    ) INTO has_data;
IF NOT column_exists THEN RAISE NOTICE 'Adding specialization_id column to doctor_specializations';
-- Add the column as nullable first
ALTER TABLE doctor_specializations
ADD COLUMN specialization_id UUID;
-- If there's existing data, we need to handle it
IF has_data THEN RAISE NOTICE 'Found existing data in doctor_specializations, migrating...';
-- Ensure specializations table exists and has data
IF NOT EXISTS (
    SELECT 1
    FROM specializations
    LIMIT 1
) THEN -- Create default specializations if none exist
INSERT INTO specializations (name, is_active)
VALUES ('General Medicine', true),
    ('Cardiology', true),
    ('Dermatology', true),
    ('Pediatrics', true),
    ('Orthopedics', true) ON CONFLICT (name) DO NOTHING;
END IF;
-- Migrate existing data
-- First, try to match existing speciality values to specializations
UPDATE doctor_specializations ds
SET specialization_id = s.id
FROM specializations s
WHERE ds.speciality IS NOT NULL
    AND TRIM(ds.speciality) != ''
    AND TRIM(ds.speciality) = s.name
    AND ds.specialization_id IS NULL;
-- For any remaining null values, assign to 'General Medicine'
UPDATE doctor_specializations ds
SET specialization_id = s.id
FROM specializations s
WHERE ds.specialization_id IS NULL
    AND s.name = 'General Medicine';
RAISE NOTICE 'Migration completed for existing data';
END IF;
-- Now make the column NOT NULL
ALTER TABLE doctor_specializations
ALTER COLUMN specialization_id
SET NOT NULL;
-- Add foreign key constraint
ALTER TABLE doctor_specializations
ADD CONSTRAINT fk_doctor_specializations_specialization FOREIGN KEY (specialization_id) REFERENCES specializations(id) ON DELETE CASCADE;
-- Add unique constraint
ALTER TABLE doctor_specializations
ADD CONSTRAINT uk_doctor_specializations_doctor_specialization UNIQUE (doctor_id, specialization_id);
RAISE NOTICE 'Successfully added specialization_id column with constraints';
ELSE RAISE NOTICE 'specialization_id column already exists';
-- Check if constraints exist and add them if missing
IF NOT EXISTS (
    SELECT 1
    FROM information_schema.table_constraints
    WHERE table_name = 'doctor_specializations'
        AND constraint_name = 'fk_doctor_specializations_specialization'
) THEN
ALTER TABLE doctor_specializations
ADD CONSTRAINT fk_doctor_specializations_specialization FOREIGN KEY (specialization_id) REFERENCES specializations(id) ON DELETE CASCADE;
RAISE NOTICE 'Added missing foreign key constraint';
END IF;
IF NOT EXISTS (
    SELECT 1
    FROM information_schema.table_constraints
    WHERE table_name = 'doctor_specializations'
        AND constraint_name = 'uk_doctor_specializations_doctor_specialization'
) THEN
ALTER TABLE doctor_specializations
ADD CONSTRAINT uk_doctor_specializations_doctor_specialization UNIQUE (doctor_id, specialization_id);
RAISE NOTICE 'Added missing unique constraint';
END IF;
END IF;
END $$;
-- ============================================================================
-- PART 3: Create necessary indexes for performance
-- ============================================================================
-- Indexes for doctor_addresses
CREATE INDEX IF NOT EXISTS idx_doctor_addresses_status ON doctor_addresses(status);
CREATE INDEX IF NOT EXISTS idx_doctor_addresses_doctor_status ON doctor_addresses(doctor_id, status);
CREATE INDEX IF NOT EXISTS idx_doctor_addresses_address_status ON doctor_addresses(address_id, status);
-- Indexes for doctor_specializations
CREATE INDEX IF NOT EXISTS idx_doctor_specializations_specialization_id ON doctor_specializations(specialization_id);
CREATE INDEX IF NOT EXISTS idx_doctor_specializations_doctor_id ON doctor_specializations(doctor_id);
-- Indexes for specializations
CREATE INDEX IF NOT EXISTS idx_specializations_name ON specializations(name);
CREATE INDEX IF NOT EXISTS idx_specializations_is_active ON specializations(is_active);
-- ============================================================================
-- PART 4: Verify the migration
-- ============================================================================
DO $$ BEGIN -- Verify status column
IF EXISTS (
    SELECT 1
    FROM information_schema.columns
    WHERE table_name = 'doctor_addresses'
        AND column_name = 'status'
        AND data_type = 'smallint'
) THEN RAISE NOTICE '✓ Status column is correctly configured as smallint';
ELSE RAISE WARNING '✗ Status column configuration issue detected';
END IF;
-- Verify specialization_id column
IF EXISTS (
    SELECT 1
    FROM information_schema.columns
    WHERE table_name = 'doctor_specializations'
        AND column_name = 'specialization_id'
        AND is_nullable = 'NO'
) THEN RAISE NOTICE '✓ Specialization_id column is correctly configured as NOT NULL';
ELSE RAISE WARNING '✗ Specialization_id column configuration issue detected';
END IF;
-- Verify foreign key constraint
IF EXISTS (
    SELECT 1
    FROM information_schema.table_constraints
    WHERE table_name = 'doctor_specializations'
        AND constraint_name = 'fk_doctor_specializations_specialization'
) THEN RAISE NOTICE '✓ Foreign key constraint is properly configured';
ELSE RAISE WARNING '✗ Foreign key constraint missing';
END IF;
RAISE NOTICE 'Migration verification completed';
END $$;

