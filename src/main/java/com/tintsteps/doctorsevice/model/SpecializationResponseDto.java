package com.tintsteps.doctorsevice.model;

import lombok.Builder;

@Builder
public record SpecializationResponseDto(
        String id,
        String doctorId,
        String speciality,
        String subspecialization
) {
}
