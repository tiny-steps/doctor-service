-- V3 Migration: Replace Practice entity with doctor_addresses junction table
-- This migration supports multi-location practices by allowing doctors to have multiple address IDs

-- Create the new doctor_addresses junction table
-- Supports multiple roles per doctor across different branches
CREATE TABLE doctor_addresses (
    doctor_id UUID NOT NULL REFERENCES doctors(id) ON DELETE CASCADE,
    address_id UUID NOT NULL,
    practice_role VARCHAR(50) NOT NULL DEFAULT 'CONSULTANT',
    PRIMARY KEY (doctor_id, address_id, practice_role),
    UNIQUE (doctor_id, address_id, practice_role)
);

-- Create index for efficient querying
CREATE INDEX idx_doctor_addresses_doctor_id ON doctor_addresses(doctor_id);
CREATE INDEX idx_doctor_addresses_address_id ON doctor_addresses(address_id);

-- No data migration needed since practice table is empty
-- Drop the old practice table immediately
DROP TABLE IF EXISTS doctor_practices;

-- Add comments for future reference
COMMENT ON TABLE doctor_addresses IS 'Junction table storing doctor-address-role relationships for multi-location practices';
COMMENT ON COLUMN doctor_addresses.doctor_id IS 'Foreign key to doctors table';
COMMENT ON COLUMN doctor_addresses.address_id IS 'Foreign key to address-service addresses table';
COMMENT ON COLUMN doctor_addresses.practice_role IS 'Doctor role at this location (CONSULTANT, VISITING_DOCTOR, HEAD_OF_DEPARTMENT, etc.)';

-- Practice role constants for reference:
-- CONSULTANT: Regular consulting doctor
-- VISITING_DOCTOR: Doctor who visits occasionally
-- HEAD_OF_DEPARTMENT: Department head
-- RESIDENT: Resident doctor
-- SPECIALIST: Specialist doctor
-- EMERGENCY_DOCTOR: Emergency department doctor