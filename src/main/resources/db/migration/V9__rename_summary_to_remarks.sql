-- Rename summary column to remarks in doctors table
-- This migration renames the 'summary' column to 'remarks' to better reflect its purpose
-- Handle case where remarks column might already exist
-- Check if summary column exists and remarks doesn't, then rename
DO $$ BEGIN -- Check if summary column exists and remarks doesn't
IF EXISTS (
    SELECT 1
    FROM information_schema.columns
    WHERE table_name = 'doctors'
        AND column_name = 'summary'
)
AND NOT EXISTS (
    SELECT 1
    FROM information_schema.columns
    WHERE table_name = 'doctors'
        AND column_name = 'remarks'
) THEN
ALTER TABLE doctors
    RENAME COLUMN summary TO remarks;
ELSIF EXISTS (
    SELECT 1
    FROM information_schema.columns
    WHERE table_name = 'doctors'
        AND column_name = 'summary'
)
AND EXISTS (
    SELECT 1
    FROM information_schema.columns
    WHERE table_name = 'doctors'
        AND column_name = 'remarks'
) THEN -- Both columns exist, drop summary column
ALTER TABLE doctors DROP COLUMN summary;
END IF;
END $$;
