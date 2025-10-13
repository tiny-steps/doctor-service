package com.tinysteps.doctorservice.model;

/**
 * Response DTO for Specialization Master
 */
public record SpecializationMasterResponseDto(
        String id,
        String name,
        String description,
        Boolean isActive) {
}



