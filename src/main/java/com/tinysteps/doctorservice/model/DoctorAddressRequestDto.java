package com.tinysteps.doctorservice.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record DoctorAddressRequestDto(
        @NotNull(message = "Address ID is required")
        UUID addressId,
        
        @NotNull(message = "Practice role is required")
        @Size(min = 1, max = 100, message = "Practice role must be between 1 and 100 characters")
        String practiceRole
) {
}
