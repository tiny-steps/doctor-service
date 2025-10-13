-- Rename summary column to remarks in doctors table
-- This migration renames the 'summary' column to 'remarks' to better reflect its purpose
ALTER TABLE doctors
    RENAME COLUMN summary TO remarks;
