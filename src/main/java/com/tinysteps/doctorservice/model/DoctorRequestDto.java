package com.tinysteps.doctorservice.model;

import jakarta.validation.constraints.*;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record DoctorRequestDto(
        @NotNull(message = "User ID is required") String userId,

        String name,

        @Size(max = 200, message = "Slug must not exceed 200 characters") @Pattern(regexp = "^[a-z0-9-]+$", message = "Slug must contain only lowercase letters, numbers, and hyphens") String slug,

        @Pattern(regexp = "^(MALE|FEMALE|OTHER)$", message = "Gender must be MALE, FEMALE, or OTHER") String gender,

        String summary,

        String about,

        // Either imageUrl (public URL, relative path, or base64 data URL) or imageData can be provided
        @Pattern(regexp = "^((https?|ftp)://[^\\s/$.?#].[^\\s]*|/[^\\s]*)?$", message = "Image URL must be a valid URL, relative path, or empty") String imageUrl,

        // Optional base64 data URL (e.g., data:image/jpeg;base64,....)
        String imageData,

        @Min(value = 0, message = "Experience years must be non-negative") @Max(value = 100, message = "Experience years must not exceed 100") Integer experienceYears,

        Boolean isVerified,

        @DecimalMin(value = "0.0", message = "Rating average must be at least 0.0") @DecimalMax(value = "5.0", message = "Rating average must not exceed 5.0") BigDecimal ratingAverage,

        @Min(value = 0, message = "Review count must be non-negative") Integer reviewCount,

        @Pattern(regexp = "^(ACTIVE|INACTIVE|SUSPENDED)$", message = "Status must be ACTIVE, INACTIVE, or SUSPENDED") String status,

        String primaryBranchId,

        Boolean isMultiBranch) {
}
