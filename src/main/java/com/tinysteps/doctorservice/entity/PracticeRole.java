package com.tinysteps.doctorservice.entity;

/**
 * Top-level enum for doctor practice roles to avoid nested type resolution issues.
 */
public enum PracticeRole {
    CONSULTANT,
    VISITING_DOCTOR,
    HEAD_OF_DEPARTMENT,
    RESIDENT,
    SPECIALIST,
    EMERGENCY_DOCTOR
}

