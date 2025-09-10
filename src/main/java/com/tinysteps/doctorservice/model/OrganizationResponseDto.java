package com.tinysteps.doctorservice.model;

import lombok.Builder;

@Builder
public record OrganizationResponseDto(
        String id,
        String doctorId,
        String organizationName,
        String role,
        String city,
        String state,
        String country,
        String tenureStart,
        String tenureEnd,
        String summary
) {
}
