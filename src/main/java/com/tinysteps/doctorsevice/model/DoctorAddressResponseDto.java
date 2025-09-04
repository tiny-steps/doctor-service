package com.tinysteps.doctorsevice.model;

import java.time.LocalDateTime;
import java.util.UUID;

public record DoctorAddressResponseDto(
        String doctorId,
        String addressId,
        String practiceRole,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}