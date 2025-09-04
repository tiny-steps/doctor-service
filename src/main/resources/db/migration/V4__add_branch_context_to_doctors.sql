-- Add branch context fields to doctors table
ALTER TABLE doctors 
ADD COLUMN primary_branch_id UUID,
ADD COLUMN is_multi_branch BOOLEAN DEFAULT FALSE;

-- Add index for better query performance
CREATE INDEX idx_doctors_primary_branch_id ON doctors(primary_branch_id);
CREATE INDEX idx_doctors_is_multi_branch ON doctors(is_multi_branch);
CREATE INDEX idx_doctors_primary_branch_status ON doctors(primary_branch_id, status);
CREATE INDEX idx_doctors_primary_branch_verified ON doctors(primary_branch_id, is_verified);

-- Add comments for documentation
COMMENT ON COLUMN doctors.primary_branch_id IS 'Primary branch ID where the doctor is primarily associated';
COMMENT ON COLUMN doctors.is_multi_branch IS 'Indicates if the doctor works across multiple branches';