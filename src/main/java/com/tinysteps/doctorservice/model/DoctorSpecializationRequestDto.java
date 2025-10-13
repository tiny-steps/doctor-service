package com.tinysteps.doctorservice.model;

import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for assigning specializations to a doctor
 * 
 * Uses specialization ID (not name) to reference existing specializations
 */
public record DoctorSpecializationRequestDto(
        @NotNull(message = "Specialization ID is required") String specializationId, // UUID of existing specialization
                                                                                     // from specializations table

        String subspecialization // Optional: specific area within the specialization
) {
}



