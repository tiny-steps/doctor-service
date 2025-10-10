-- Add status column to doctor_addresses table
-- Uses SMALLINT to store enum ordinal values: 0=ACTIVE, 1=INACTIVE
ALTER TABLE doctor_addresses
ADD COLUMN IF NOT EXISTS status SMALLINT DEFAULT 0;
-- Add comment for clarity
COMMENT ON COLUMN doctor_addresses.status IS 'Status of doctor-address relationship: 0=ACTIVE, 1=INACTIVE';