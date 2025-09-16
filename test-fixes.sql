-- Test script to verify the fixes for robust soft delete issues
-- =====================================
-- ISSUE 1 FIX VERIFICATION: Check doctor-address creation
-- =====================================
-- Check if doctor_addresses entries exist for newly created doctors
SELECT d.id as doctor_id,
    d.name as doctor_name,
    d.status as global_status,
    da.address_id as branch_id,
    da.practice_role,
    da.status as branch_status_ordinal,
    CASE
        WHEN da.status = 0 THEN 'ACTIVE'
        WHEN da.status = 1 THEN 'INACTIVE'
        ELSE 'UNKNOWN'
    END as branch_status_text
FROM doctors d
    LEFT JOIN doctor_addresses da ON d.id = da.doctor_id
ORDER BY d.created_at DESC,
    d.name;
-- =====================================
-- ISSUE 2 & 3 FIX VERIFICATION: Test branch filtering
-- =====================================
-- Test with existing doctor ID and branch ID
-- Replace these with actual IDs from your database
SET @test_doctor_id = '2afeb80b-7e3d-44f9-a13e-401e4a5bf7d2';
SET @test_branch_id = '1ac004b5-be67-4010-a9d1-e15be70bfba7';
-- Check current status
SELECT 'Current Status Check' as test_type,
    d.id as doctor_id,
    d.name,
    d.status as global_status,
    da.address_id,
    da.status as branch_status_ordinal,
    CASE
        WHEN da.status = 0 THEN 'ACTIVE'
        WHEN da.status = 1 THEN 'INACTIVE'
        ELSE 'NO_ASSOCIATION'
    END as branch_status_text
FROM doctors d
    LEFT JOIN doctor_addresses da ON d.id = da.doctor_id
    AND da.address_id = @test_branch_id::uuid
WHERE d.id = @test_doctor_id::uuid;
-- Test different status scenarios
-- Scenario 1: Make doctor active in branch
UPDATE doctor_addresses
SET status = 0 -- ACTIVE
WHERE doctor_id = @test_doctor_id::uuid
    AND address_id = @test_branch_id::uuid;
-- Verify after activation
SELECT 'After Activation' as test_type,
    d.id as doctor_id,
    d.name,
    d.status as global_status,
    da.status as branch_status_ordinal,
    CASE
        WHEN da.status = 0 THEN 'ACTIVE'
        WHEN da.status = 1 THEN 'INACTIVE'
        ELSE 'NO_ASSOCIATION'
    END as branch_status_text
FROM doctors d
    LEFT JOIN doctor_addresses da ON d.id = da.doctor_id
    AND da.address_id = @test_branch_id::uuid
WHERE d.id = @test_doctor_id::uuid;
-- Test API endpoint expectations
-- The API should now correctly show:
-- 1. Active doctors when includeInactive=false
-- 2. Both active and inactive when includeInactive=true
-- =====================================
-- QUICK TEST SETUP
-- =====================================
-- Create a test doctor-address relationship if it doesn't exist
-- (Replace with your actual doctor and branch IDs)
INSERT INTO doctor_addresses (
        doctor_id,
        address_id,
        practice_role,
        status,
        created_at,
        updated_at
    )
VALUES (
        '2afeb80b-7e3d-44f9-a13e-401e4a5bf7d2'::uuid,
        '1ac004b5-be67-4010-a9d1-e15be70bfba7'::uuid,
        'CONSULTANT',
        0,
        -- ACTIVE
        NOW(),
        NOW()
    ) ON CONFLICT (doctor_id, address_id, practice_role) DO
UPDATE
SET status = 0,
    updated_at = NOW();
-- =====================================
-- API TEST COMMANDS
-- =====================================
/*
 After running the SQL fixes above, test these API endpoints:
 
 1. Test including inactive doctors:
 curl -X GET "http://localhost:8084/api/v1/doctors/branch/1ac004b5-be67-4010-a9d1-e15be70bfba7/with-status?includeInactive=true&page=0&size=1000"
 
 2. Test excluding inactive doctors:
 curl -X GET "http://localhost:8084/api/v1/doctors/branch/1ac004b5-be67-4010-a9d1-e15be70bfba7/with-status?includeInactive=false&page=0&size=1000"
 
 3. Test the regular branch endpoint (should show active only):
 curl -X GET "http://localhost:8084/api/v1/doctors/branch/1ac004b5-be67-4010-a9d1-e15be70bfba7?page=0&size=1000"
 
 Expected Results:
 - When includeInactive=true: Should show both active and inactive doctors
 - When includeInactive=false: Should show only active doctors  
 - Doctor status should reflect branch-specific status, not just global status
 */
-- =====================================
-- VERIFICATION QUERIES
-- =====================================
-- Check all doctors and their branch associations
SELECT d.id,
    d.name,
    d.status as global_status,
    COUNT(da.address_id) as total_branches,
    COUNT(
        CASE
            WHEN da.status = 0 THEN 1
        END
    ) as active_branches,
    COUNT(
        CASE
            WHEN da.status = 1 THEN 1
        END
    ) as inactive_branches
FROM doctors d
    LEFT JOIN doctor_addresses da ON d.id = da.doctor_id
GROUP BY d.id,
    d.name,
    d.status
ORDER BY d.name;
-- Check specific branch associations
SELECT d.name as doctor_name,
    b.name as branch_name,
    d.status as doctor_global_status,
    CASE
        WHEN da.status = 0 THEN 'ACTIVE'
        WHEN da.status = 1 THEN 'INACTIVE'
        ELSE 'NO_ASSOCIATION'
    END as branch_status,
    da.practice_role
FROM doctors d
    CROSS JOIN branches b
    LEFT JOIN doctor_addresses da ON d.id = da.doctor_id
    AND b.id = da.address_id
WHERE b.id = '1ac004b5-be67-4010-a9d1-e15be70bfba7'::uuid
ORDER BY d.name;