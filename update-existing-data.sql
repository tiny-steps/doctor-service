-- Update Existing Data for Robust Soft Delete Testing
-- This script uses existing data in your database and modifies it to create test scenarios
-- =====================================
-- STEP 1: CHECK EXISTING DATA
-- =====================================
-- Run these queries first to see what data you have
-- Check existing doctors
SELECT d.id,
    d.first_name,
    d.last_name,
    d.email,
    d.status as global_status,
    COUNT(da.id) as total_branches
FROM doctors d
    LEFT JOIN doctor_addresses da ON d.id = da.doctor_id
GROUP BY d.id,
    d.first_name,
    d.last_name,
    d.email,
    d.status
ORDER BY d.first_name
LIMIT 10;
-- Check existing branches
SELECT id,
    name,
    address,
    status
FROM branches
ORDER BY name
LIMIT 10;
-- Check doctor-branch relationships
SELECT d.first_name || ' ' || d.last_name as doctor_name,
    d.status as doctor_global_status,
    b.name as branch_name,
    da.practice_role,
    da.status as branch_status,
    d.id as doctor_id,
    b.id as branch_id,
    da.id as doctor_address_id
FROM doctors d
    JOIN doctor_addresses da ON d.id = da.doctor_id
    JOIN branches b ON da.address_id = b.id
ORDER BY d.first_name,
    b.name
LIMIT 20;
-- =====================================
-- STEP 2: UPDATE QUERIES (Replace with actual IDs from your database)
-- =====================================
-- SCENARIO 1: Single Branch Doctor
-- Replace 'DOCTOR_ID_1' with an actual doctor ID who should work at only one branch
-- Replace 'BRANCH_ID_1' with an actual branch ID
-- Make sure this doctor is active globally
UPDATE doctors
SET status = 'ACTIVE',
    updated_at = NOW()
WHERE id = 'DOCTOR_ID_1';
-- Make sure this doctor has only one active branch association
-- First, make all other branches inactive for this doctor
UPDATE doctor_addresses
SET status = 'INACTIVE',
    updated_at = NOW()
WHERE doctor_id = 'DOCTOR_ID_1';
-- Then activate only one branch for this doctor
UPDATE doctor_addresses
SET status = 'ACTIVE',
    updated_at = NOW()
WHERE doctor_id = 'DOCTOR_ID_1'
    AND address_id = 'BRANCH_ID_1';
-- =====================================
-- SCENARIO 2: Multi-Branch Doctor
-- Replace 'DOCTOR_ID_2' with an actual doctor ID who should work at multiple branches
-- Replace 'BRANCH_ID_2', 'BRANCH_ID_3', 'BRANCH_ID_4' with actual branch IDs
-- Make sure this doctor is active globally
UPDATE doctors
SET status = 'ACTIVE',
    updated_at = NOW()
WHERE id = 'DOCTOR_ID_2';
-- Make sure this doctor has multiple active branch associations
-- First, make all branches inactive for this doctor
UPDATE doctor_addresses
SET status = 'INACTIVE',
    updated_at = NOW()
WHERE doctor_id = 'DOCTOR_ID_2';
-- Then activate multiple branches for this doctor
UPDATE doctor_addresses
SET status = 'ACTIVE',
    updated_at = NOW()
WHERE doctor_id = 'DOCTOR_ID_2'
    AND address_id IN ('BRANCH_ID_2', 'BRANCH_ID_3', 'BRANCH_ID_4');
-- If the doctor doesn't have associations with these branches, create them
-- (Replace with actual IDs)
INSERT INTO doctor_addresses (
        id,
        doctor_id,
        address_id,
        practice_role,
        status,
        created_at,
        updated_at
    )
SELECT gen_random_uuid(),
    'DOCTOR_ID_2',
    branch_id,
    'CONSULTANT',
    'ACTIVE',
    NOW(),
    NOW()
FROM (
        VALUES ('BRANCH_ID_2'::uuid),
            ('BRANCH_ID_3'::uuid),
            ('BRANCH_ID_4'::uuid)
    ) AS branches(branch_id)
WHERE NOT EXISTS (
        SELECT 1
        FROM doctor_addresses
        WHERE doctor_id = 'DOCTOR_ID_2'
            AND address_id = branch_id
    );
-- =====================================
-- SCENARIO 3: Inactive Doctor for Reactivation Testing
-- Replace 'DOCTOR_ID_3' with an actual doctor ID who should be inactive
-- Replace 'BRANCH_ID_5', 'BRANCH_ID_6' with actual branch IDs
-- Make this doctor inactive globally
UPDATE doctors
SET status = 'INACTIVE',
    updated_at = NOW()
WHERE id = 'DOCTOR_ID_3';
-- Make all branch associations inactive for this doctor
UPDATE doctor_addresses
SET status = 'INACTIVE',
    updated_at = NOW()
WHERE doctor_id = 'DOCTOR_ID_3';
-- Ensure this doctor has at least 2 branch associations for testing
INSERT INTO doctor_addresses (
        id,
        doctor_id,
        address_id,
        practice_role,
        status,
        created_at,
        updated_at
    )
SELECT gen_random_uuid(),
    'DOCTOR_ID_3',
    branch_id,
    'CONSULTANT',
    'INACTIVE',
    NOW(),
    NOW()
FROM (
        VALUES ('BRANCH_ID_5'::uuid),
            ('BRANCH_ID_6'::uuid)
    ) AS branches(branch_id)
WHERE NOT EXISTS (
        SELECT 1
        FROM doctor_addresses
        WHERE doctor_id = 'DOCTOR_ID_3'
            AND address_id = branch_id
    );
-- =====================================
-- STEP 3: EXAMPLE WITH SAMPLE IDS (if you want to use these)
-- =====================================
-- These are example queries - replace the UUIDs with actual ones from your database
/*
 -- Example: Using first 3 doctors from your database
 WITH sample_doctors AS (
 SELECT id, ROW_NUMBER() OVER (ORDER BY created_at) as rn
 FROM doctors 
 LIMIT 3
 ),
 sample_branches AS (
 SELECT id, ROW_NUMBER() OVER (ORDER BY created_at) as rn
 FROM branches 
 LIMIT 4
 )
 
 -- SCENARIO 1: First doctor - single branch
 UPDATE doctors 
 SET status = 'ACTIVE', updated_at = NOW()
 WHERE id = (SELECT id FROM sample_doctors WHERE rn = 1);
 
 -- Clear existing associations for first doctor
 DELETE FROM doctor_addresses 
 WHERE doctor_id = (SELECT id FROM sample_doctors WHERE rn = 1);
 
 -- Add single branch association
 INSERT INTO doctor_addresses (id, doctor_id, address_id, practice_role, status, created_at, updated_at)
 SELECT 
 gen_random_uuid(),
 sd.id,
 sb.id,
 'CONSULTANT',
 'ACTIVE',
 NOW(),
 NOW()
 FROM sample_doctors sd, sample_branches sb
 WHERE sd.rn = 1 AND sb.rn = 1;
 
 -- SCENARIO 2: Second doctor - multiple branches
 UPDATE doctors 
 SET status = 'ACTIVE', updated_at = NOW()
 WHERE id = (SELECT id FROM sample_doctors WHERE rn = 2);
 
 -- Clear existing associations for second doctor
 DELETE FROM doctor_addresses 
 WHERE doctor_id = (SELECT id FROM sample_doctors WHERE rn = 2);
 
 -- Add multiple branch associations
 INSERT INTO doctor_addresses (id, doctor_id, address_id, practice_role, status, created_at, updated_at)
 SELECT 
 gen_random_uuid(),
 sd.id,
 sb.id,
 'CONSULTANT',
 'ACTIVE',
 NOW(),
 NOW()
 FROM sample_doctors sd, sample_branches sb
 WHERE sd.rn = 2 AND sb.rn IN (1, 2, 3);
 
 -- SCENARIO 3: Third doctor - inactive
 UPDATE doctors 
 SET status = 'INACTIVE', updated_at = NOW()
 WHERE id = (SELECT id FROM sample_doctors WHERE rn = 3);
 
 -- Clear existing associations for third doctor
 DELETE FROM doctor_addresses 
 WHERE doctor_id = (SELECT id FROM sample_doctors WHERE rn = 3);
 
 -- Add inactive branch associations
 INSERT INTO doctor_addresses (id, doctor_id, address_id, practice_role, status, created_at, updated_at)
 SELECT 
 gen_random_uuid(),
 sd.id,
 sb.id,
 'CONSULTANT',
 'INACTIVE',
 NOW(),
 NOW()
 FROM sample_doctors sd, sample_branches sb
 WHERE sd.rn = 3 AND sb.rn IN (1, 2);
 */
-- =====================================
-- STEP 4: VERIFICATION QUERIES
-- =====================================
-- Run these after your updates to verify the setup
-- Check updated doctors and their status
SELECT d.id,
    d.first_name || ' ' || d.last_name as doctor_name,
    d.status as global_status,
    COUNT(da.id) as total_branches,
    COUNT(
        CASE
            WHEN da.status = 'ACTIVE' THEN 1
        END
    ) as active_branches,
    COUNT(
        CASE
            WHEN da.status = 'INACTIVE' THEN 1
        END
    ) as inactive_branches
FROM doctors d
    LEFT JOIN doctor_addresses da ON d.id = da.doctor_id
GROUP BY d.id,
    d.first_name,
    d.last_name,
    d.status
ORDER BY d.first_name;
-- Check detailed doctor-branch relationships
SELECT d.first_name || ' ' || d.last_name as doctor_name,
    d.status as doctor_global_status,
    b.name as branch_name,
    da.practice_role,
    da.status as branch_status,
    d.id as doctor_id,
    b.id as branch_id
FROM doctors d
    JOIN doctor_addresses da ON d.id = da.doctor_id
    JOIN branches b ON da.address_id = b.id
ORDER BY d.first_name,
    b.name;
-- =====================================
-- STEP 5: TESTING COMMANDS
-- =====================================
-- Use these curl commands to test each scenario (replace IDs with actual ones)
/*
 -- SCENARIO 1 TEST: Deactivate single-branch doctor
 curl -X PUT "http://localhost:8084/api/v1/doctors/DOCTOR_ID_1/deactivate-branches" \
 -H "Content-Type: application/json" \
 -d '{"branchIds": ["BRANCH_ID_1"]}'
 
 -- SCENARIO 2A TEST: Deactivate multi-branch doctor from one branch
 curl -X PUT "http://localhost:8084/api/v1/doctors/DOCTOR_ID_2/deactivate-branches" \
 -H "Content-Type: application/json" \
 -d '{"branchIds": ["BRANCH_ID_2"]}'
 
 -- SCENARIO 2B TEST: Deactivate multi-branch doctor from all branches
 curl -X PUT "http://localhost:8084/api/v1/doctors/DOCTOR_ID_2/deactivate-branches" \
 -H "Content-Type: application/json" \
 -d '{"branchIds": ["BRANCH_ID_2", "BRANCH_ID_3", "BRANCH_ID_4"]}'
 
 -- SCENARIO 3 TEST: Reactivate inactive doctor
 curl -X PUT "http://localhost:8084/api/v1/doctors/DOCTOR_ID_3/activate-branch/BRANCH_ID_5"
 */
-- =====================================
-- STEP 6: RESET QUERIES (if needed)
-- =====================================
-- Run these if you need to reset the test data to original state
/*
 -- Reset all doctors to active
 UPDATE doctors SET status = 'ACTIVE', updated_at = NOW();
 
 -- Reset all doctor addresses to active
 UPDATE doctor_addresses SET status = 'ACTIVE', updated_at = NOW();
 */