package com.tinysteps.doctorservice.model;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record RecommendationRequestDto(
                @DecimalMin(value = "0.0", message = "Rating must be at least 0.0") @DecimalMax(value = "5.0", message = "Rating must not exceed 5.0") BigDecimal rating,

                @Size(max = 255, message = "Review must not exceed 255 characters") String review,

                @Min(value = 0, message = "Recommendation count must be non-negative") Integer recommendationCount,

                // Optional doctor ID to allow reassigning recommendation to different doctor
                String doctorId) {
}
