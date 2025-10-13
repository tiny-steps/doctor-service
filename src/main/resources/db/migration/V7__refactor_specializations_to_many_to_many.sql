-- V7__refactor_specializations_to_many_to_many.sql
-- Refactor specialization structure to prevent duplicates
-- Creates a master specializations table and refactors doctor_specializations to a proper junction table
-- Step 1: Create the specializations master table
CREATE TABLE IF NOT EXISTS specializations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    deleted BOOLEAN DEFAULT false,
    deleted_at TIMESTAMP,
    deleted_by UUID
);
-- Create index on name for faster lookups
CREATE INDEX idx_specializations_name ON specializations(name);
CREATE INDEX idx_specializations_is_active ON specializations(is_active);
-- Step 2: Add new foreign key column to doctor_specializations (nullable initially for migration)
ALTER TABLE doctor_specializations
ADD COLUMN IF NOT EXISTS specialization_id UUID;
-- Step 3: Migrate existing data
-- Insert unique specialization names into the master table
INSERT INTO specializations (name, is_active)
SELECT DISTINCT TRIM(speciality) as name,
    true as is_active
FROM doctor_specializations
WHERE speciality IS NOT NULL
    AND TRIM(speciality) != '' ON CONFLICT (name) DO NOTHING;
-- Step 4: Update doctor_specializations to reference the master table
UPDATE doctor_specializations ds
SET specialization_id = s.id
FROM specializations s
WHERE TRIM(ds.speciality) = s.name
    AND ds.specialization_id IS NULL;
-- Step 5: Add constraints
-- Make specialization_id NOT NULL (after data migration)
ALTER TABLE doctor_specializations
ALTER COLUMN specialization_id
SET NOT NULL;
-- Add foreign key constraint
ALTER TABLE doctor_specializations
ADD CONSTRAINT fk_doctor_specializations_specialization FOREIGN KEY (specialization_id) REFERENCES specializations(id) ON DELETE CASCADE;
-- Add unique constraint to prevent duplicate doctor-specialization combinations
ALTER TABLE doctor_specializations
ADD CONSTRAINT uk_doctor_specializations_doctor_specialization UNIQUE (doctor_id, specialization_id);
-- Create index for better join performance
CREATE INDEX IF NOT EXISTS idx_doctor_specializations_specialization_id ON doctor_specializations(specialization_id);
-- Step 6: Mark old speciality column as deprecated (keep for rollback safety)
-- We're not dropping it immediately to allow for rollback if needed
-- The application will now use specialization_id instead
COMMENT ON COLUMN doctor_specializations.speciality IS 'DEPRECATED: Use specialization_id foreign key instead. Kept temporarily for rollback safety.';
-- Step 7: Add helpful indexes
CREATE INDEX IF NOT EXISTS idx_doctor_specializations_doctor_id ON doctor_specializations(doctor_id);
-- Note: To fully complete the migration and drop the old column, run this later:
-- ALTER TABLE doctor_specializations DROP COLUMN speciality;


