package com.tinysteps.doctorservice.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for creating/updating a Specialization (independent of doctors)
 */
public record SpecializationMasterRequestDto(
        @NotBlank(message = "Specialization name is required") @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters") String name,

        @Size(max = 500, message = "Description must not exceed 500 characters") String description) {
}



