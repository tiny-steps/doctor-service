package com.tinysteps.doctorsevice.model;

import lombok.Builder;

@Builder
public record AwardResponseDto(
        String id,
        String doctorId,
        String title,
        Integer awardedYear,
        String summary
) {
}
