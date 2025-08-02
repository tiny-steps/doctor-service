package com.tinysteps.doctorsevice.model;

import lombok.Builder;

@Builder
public record PracticeResponseDto(
        String id,
        String doctorId,
        String practiceName,
        String practiceType,
        String addressId,
        String slug,
        Integer practicePosition,
        String createdAt
) {
}
