package com.tintsteps.doctorsevice.model;

import lombok.Builder;

@Builder
public record PhotoResponseDto(
        String id,
        String doctorId,
        String photoUrl,
        Boolean isDefault
) {
}
