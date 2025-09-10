package com.tinysteps.doctorservice.model;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record PricingResponseDto(
        String id,
        String doctorId,
        String sessionTypeId,
        BigDecimal customPrice,
        Boolean isActive
) {
}
