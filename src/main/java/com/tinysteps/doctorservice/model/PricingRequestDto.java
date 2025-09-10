package com.tinysteps.doctorservice.model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record PricingRequestDto(
        @NotNull(message = "Session type ID is required")
        String sessionTypeId,

        @DecimalMin(value = "0.0", message = "Custom price must be non-negative")
        BigDecimal customPrice,

        Boolean isActive
) {
}
