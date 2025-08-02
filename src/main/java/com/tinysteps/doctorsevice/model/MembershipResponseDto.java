package com.tinysteps.doctorsevice.model;

import lombok.Builder;

@Builder
public record MembershipResponseDto(
        String id,
        String doctorId,
        String membershipCouncilName
) {
}
