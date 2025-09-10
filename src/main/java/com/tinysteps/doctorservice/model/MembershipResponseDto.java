package com.tinysteps.doctorservice.model;

import lombok.Builder;

@Builder
public record MembershipResponseDto(
        String id,
        String doctorId,
        String membershipCouncilName
) {
}
