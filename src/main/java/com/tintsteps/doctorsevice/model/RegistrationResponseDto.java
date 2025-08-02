package com.tintsteps.doctorsevice.model;

import lombok.Builder;

@Builder
public record RegistrationResponseDto(
        String id,
        String doctorId,
        String registrationCouncilName,
        String registrationNumber,
        Integer registrationYear
) {
}
