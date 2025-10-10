-- Fix status column type in doctor_addresses table
-- Convert from any existing type to SMALLINT with proper ordinal values
-- This migration ensures the column type matches the entity configuration

-- First check if status column exists and what type it is
-- If it's already SMALLINT, this will be a no-op
-- If it's a PostgreSQL enum or other type, convert it

DO $$
BEGIN
    -- Check if the column exists and get its data type
    IF EXISTS (
        SELECT 1 
        FROM information_schema.columns 
        WHERE table_name = 'doctor_addresses' 
        AND column_name = 'status'
    ) THEN
        -- Get the current data type
        DECLARE
            current_type text;
        BEGIN
            SELECT data_type INTO current_type
            FROM information_schema.columns 
            WHERE table_name = 'doctor_addresses' 
            AND column_name = 'status';
            
            -- If it's not already smallint, convert it
            IF current_type != 'smallint' THEN
                -- If it's a custom enum type or text, convert string values to ordinals
                -- ACTIVE -> 0, INACTIVE -> 1
                ALTER TABLE doctor_addresses 
                ADD COLUMN IF NOT EXISTS status_temp SMALLINT;
                
                -- Convert existing data if any
                UPDATE doctor_addresses 
                SET status_temp = CASE 
                    WHEN status::text = 'ACTIVE' OR status::text = '0' THEN 0
                    WHEN status::text = 'INACTIVE' OR status::text = '1' THEN 1
                    ELSE 0  -- Default to ACTIVE if unknown
                END
                WHERE status_temp IS NULL;
                
                -- Drop the old column and rename temp column
                ALTER TABLE doctor_addresses DROP COLUMN IF EXISTS status;
                ALTER TABLE doctor_addresses RENAME COLUMN status_temp TO status;
                
                -- Set default value
                ALTER TABLE doctor_addresses ALTER COLUMN status SET DEFAULT 0;
                
                -- Add NOT NULL constraint if needed
                ALTER TABLE doctor_addresses ALTER COLUMN status SET NOT NULL;
                
                RAISE NOTICE 'Converted status column from % to smallint', current_type;
            ELSE
                RAISE NOTICE 'Status column is already smallint, no conversion needed';
            END IF;
        END;
    ELSE
        -- Column doesn't exist, create it
        ALTER TABLE doctor_addresses 
        ADD COLUMN status SMALLINT DEFAULT 0 NOT NULL;
        
        RAISE NOTICE 'Created status column as smallint';
    END IF;
END $$;

-- Add comment for clarity
COMMENT ON COLUMN doctor_addresses.status IS 'Status of doctor-address relationship: 0=ACTIVE, 1=INACTIVE (enum ordinal values)';

-- Create index for efficient querying by status
CREATE INDEX IF NOT EXISTS idx_doctor_addresses_status ON doctor_addresses(status);
CREATE INDEX IF NOT EXISTS idx_doctor_addresses_doctor_status ON doctor_addresses(doctor_id, status);
CREATE INDEX IF NOT EXISTS idx_doctor_addresses_address_status ON doctor_addresses(address_id, status);