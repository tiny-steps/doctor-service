package com.tinysteps.doctorservice.model;

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
