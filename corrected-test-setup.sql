-- Corrected Update Queries for Robust Soft Delete Testing
-- The status column stores enum ordinal values, not strings:
-- Status.ACTIVE = 0
-- Status.INACTIVE = 1
-- =====================================
-- STEP 1: CHECK CURRENT STATUS VALUES
-- =====================================
-- First, let's see what values are currently in the status column
SELECT d.first_name || ' ' || d.last_name as doctor_name,
    d.status as doctor_global_status,
    b.name as branch_name,
    da.status as branch_status_ordinal,
    CASE
        WHEN da.status = 0 THEN 'ACTIVE'
        WHEN da.status = 1 THEN 'INACTIVE'
        ELSE 'UNKNOWN'
    END as branch_status_text,
    d.id as doctor_id,
    b.id as branch_id,
    da.doctor_id,
    da.address_id,
    da.practice_role
FROM doctors d
    JOIN doctor_addresses da ON d.id = da.doctor_id
    JOIN branches b ON da.address_id = b.id
ORDER BY d.first_name,
    b.name
LIMIT 20;
-- =====================================
-- STEP 2: CORRECTED UPDATE QUERIES
-- =====================================
-- SCENARIO 1: Single Branch Doctor
-- Replace these IDs with actual ones from your database
-- Make doctor active globally
UPDATE doctors
SET status = 'ACTIVE',
    updated_at = NOW()
WHERE id = '96dd7783-c021-4e8d-ae52-a716c24296bb';
-- Make all other branches inactive for this doctor (using ordinal value 1)
UPDATE doctor_addresses
SET status = 1,
    updated_at = NOW()
WHERE doctor_id = '96dd7783-c021-4e8d-ae52-a716c24296bb';
-- Then activate only one branch for this doctor (using ordinal value 0)
UPDATE doctor_addresses
SET status = 0,
    updated_at = NOW()
WHERE doctor_id = '96dd7783-c021-4e8d-ae52-a716c24296bb'
    AND address_id = '1ac004b5-be67-4010-a9d1-e15be70bfba7';
-- =====================================
-- STEP 3: SET UP MULTI-BRANCH DOCTOR (SCENARIO 2)
-- =====================================
-- Replace with another doctor ID for multi-branch testing
-- Make sure this doctor is active globally
UPDATE doctors
SET status = 'ACTIVE',
    updated_at = NOW()
WHERE id = 'REPLACE_WITH_SECOND_DOCTOR_ID';
-- Activate multiple branches for this doctor (use actual branch IDs)
UPDATE doctor_addresses
SET status = 0,
    updated_at = NOW()
WHERE doctor_id = 'REPLACE_WITH_SECOND_DOCTOR_ID'
    AND address_id IN (
        'REPLACE_WITH_BRANCH_ID_1',
        'REPLACE_WITH_BRANCH_ID_2',
        'REPLACE_WITH_BRANCH_ID_3'
    );
-- If the doctor doesn't have associations with multiple branches, create them
-- (Replace with actual IDs)
INSERT INTO doctor_addresses (
        doctor_id,
        address_id,
        practice_role,
        status,
        created_at,
        updated_at
    )
SELECT 'REPLACE_WITH_SECOND_DOCTOR_ID'::uuid,
    branch_id,
    'CONSULTANT',
    0,
    -- ACTIVE
    NOW(),
    NOW()
FROM (
        VALUES ('REPLACE_WITH_BRANCH_ID_1'::uuid),
            ('REPLACE_WITH_BRANCH_ID_2'::uuid),
            ('REPLACE_WITH_BRANCH_ID_3'::uuid)
    ) AS branches(branch_id)
WHERE NOT EXISTS (
        SELECT 1
        FROM doctor_addresses
        WHERE doctor_id = 'REPLACE_WITH_SECOND_DOCTOR_ID'
            AND address_id = branch_id
    );
-- =====================================
-- STEP 4: SET UP INACTIVE DOCTOR (SCENARIO 3)
-- =====================================
-- Replace with a third doctor ID for reactivation testing
-- Make this doctor inactive globally
UPDATE doctors
SET status = 'INACTIVE',
    updated_at = NOW()
WHERE id = 'REPLACE_WITH_THIRD_DOCTOR_ID';
-- Make all branch associations inactive for this doctor
UPDATE doctor_addresses
SET status = 1,
    updated_at = NOW() -- INACTIVE
WHERE doctor_id = 'REPLACE_WITH_THIRD_DOCTOR_ID';
-- =====================================
-- STEP 5: QUICK SETUP WITH YOUR EXISTING DATA
-- =====================================
-- Here's a version using the IDs you already tried
-- SCENARIO 1: Single branch doctor (using your existing IDs)
UPDATE doctors
SET status = 'ACTIVE',
    updated_at = NOW()
WHERE id = '96dd7783-c021-4e8d-ae52-a716c24296bb';
-- Make all branches inactive first
UPDATE doctor_addresses
SET status = 1,
    updated_at = NOW()
WHERE doctor_id = '96dd7783-c021-4e8d-ae52-a716c24296bb';
-- Then activate only one branch
UPDATE doctor_addresses
SET status = 0,
    updated_at = NOW()
WHERE doctor_id = '96dd7783-c021-4e8d-ae52-a716c24296bb'
    AND address_id = '1ac004b5-be67-4010-a9d1-e15be70bfba7';
-- =====================================
-- STEP 6: GET MORE DOCTORS AND BRANCHES FOR COMPLETE SETUP
-- =====================================
-- Run this to get additional IDs for the other scenarios
-- Get available doctors (excluding the one already used)
SELECT id as doctor_id,
    first_name || ' ' || last_name as doctor_name,
    status as current_status
FROM doctors
WHERE id != '96dd7783-c021-4e8d-ae52-a716c24296bb'
ORDER BY created_at
LIMIT 5;
-- Get available branches (excluding the one already used)
SELECT id as branch_id,
    name as branch_name,
    status as current_status
FROM branches
WHERE id != '1ac004b5-be67-4010-a9d1-e15be70bfba7'
ORDER BY created_at
LIMIT 5;
-- Check existing doctor-branch relationships
SELECT da.doctor_id,
    da.address_id,
    da.status,
    d.first_name || ' ' || d.last_name as doctor_name,
    b.name as branch_name
FROM doctor_addresses da
    JOIN doctors d ON da.doctor_id = d.id
    JOIN branches b ON da.address_id = b.id
WHERE da.doctor_id != '96dd7783-c021-4e8d-ae52-a716c24296bb'
ORDER BY da.doctor_id,
    b.name;
-- =====================================
-- STEP 7: VERIFICATION QUERY
-- =====================================
-- Run this after your updates to verify the setup
SELECT d.first_name || ' ' || d.last_name as doctor_name,
    d.status as doctor_global_status,
    b.name as branch_name,
    CASE
        WHEN da.status = 0 THEN 'ACTIVE'
        WHEN da.status = 1 THEN 'INACTIVE'
        ELSE 'UNKNOWN'
    END as branch_status,
    d.id as doctor_id,
    b.id as branch_id
FROM doctors d
    JOIN doctor_addresses da ON d.id = da.doctor_id
    JOIN branches b ON da.address_id = b.id
ORDER BY d.first_name,
    b.name;
-- =====================================
-- STEP 8: TEST COMMANDS
-- =====================================
-- After setting up the data, use these curl commands to test
-- Test Scenario 1: Single branch doctor deactivation
-- curl -X PUT "http://localhost:8084/api/v1/doctors/96dd7783-c021-4e8d-ae52-a716c24296bb/deactivate-branches" \
--   -H "Content-Type: application/json" \
--   -d '{"branchIds": ["1ac004b5-be67-4010-a9d1-e15be70bfba7"]}'
-- Test Scenario 2A: Multi-branch doctor partial deactivation
-- curl -X PUT "http://localhost:8084/api/v1/doctors/SECOND_DOCTOR_ID/deactivate-branches" \
--   -H "Content-Type: application/json" \
--   -d '{"branchIds": ["ONE_BRANCH_ID"]}'
-- Test Scenario 2B: Multi-branch doctor full deactivation
-- curl -X PUT "http://localhost:8084/api/v1/doctors/SECOND_DOCTOR_ID/deactivate-branches" \
--   -H "Content-Type: application/json" \
--   -d '{"branchIds": ["BRANCH_ID_1", "BRANCH_ID_2", "BRANCH_ID_3"]}'
-- Test Scenario 3: Doctor reactivation
-- curl -X PUT "http://localhost:8084/api/v1/doctors/THIRD_DOCTOR_ID/activate-branch/BRANCH_ID"
-- =====================================
-- NOTES:
-- =====================================
-- 1. The status column uses enum ordinals: 0 = ACTIVE, 1 = INACTIVE
-- 2. Make sure to replace placeholder IDs with actual ones from your database
-- 3. Run the verification query after each test to see the results
-- 4. The doctor service should be running on port 8084