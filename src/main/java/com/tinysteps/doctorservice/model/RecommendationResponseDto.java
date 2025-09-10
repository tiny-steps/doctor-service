package com.tinysteps.doctorservice.model;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record RecommendationResponseDto(
        String id,
        String doctorId,
        BigDecimal rating,
        String review,
        Integer recommendationCount
) {
}
