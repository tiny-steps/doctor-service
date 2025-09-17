-- Create enum type for doctor status if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'doctor_status') THEN
        CREATE TYPE doctor_status AS ENUM ('ACTIVE', 'INACTIVE');
    END IF;
END $$;

-- Add status column to doctor_addresses table
ALTER TABLE doctor_addresses 
ADD COLUMN IF NOT EXISTS status doctor_status DEFAULT 'ACTIVE';