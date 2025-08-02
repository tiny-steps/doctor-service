package com.tintsteps.doctorsevice.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record MembershipRequestDto(
        @NotBlank(message = "Membership council name is required")
        @Size(max = 255, message = "Membership council name must not exceed 255 characters")
        String membershipCouncilName
) {
}
