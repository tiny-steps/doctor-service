package com.tinysteps.doctorsevice.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record SpecializationRequestDto(
        @NotBlank(message = "Speciality is required")
        @Size(max = 100, message = "Speciality must not exceed 100 characters")
        String speciality,

        @Size(max = 100, message = "Subspecialization must not exceed 100 characters")
        String subspecialization
) {
}
