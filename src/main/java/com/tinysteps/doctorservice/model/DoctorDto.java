package com.tinysteps.doctorservice.model;

import jakarta.validation.constraints.*;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record DoctorDto(
                @NotBlank(message = "Doctor name is required") @Size(max = 200, message = "Doctor name must not exceed 200 characters") String name,

                @NotBlank(message = "Email is required") @Email(message = "Email should be valid") String email,

                String password,

                @NotBlank(message = "Phone is required") @Size(max = 20, message = "Phone must not exceed 20 characters") String phone,

                @Size(max = 200, message = "Slug must not exceed 200 characters") @Pattern(regexp = "^[a-z0-9-]+$", message = "Slug must contain only lowercase letters, numbers, and hyphens") String slug,

                @Pattern(regexp = "^(MALE|FEMALE|OTHER)$", message = "Gender must be MALE, FEMALE, or OTHER") String gender,

                String summary,

                String about,

                @Pattern(regexp = "^(https?|ftp)://[^\\s/$.?#].[^\\s]*$", message = "Image URL must be a valid URL") String imageUrl,

                @Min(value = 0, message = "Experience years must be non-negative") @Max(value = 100, message = "Experience years must not exceed 100") Integer experienceYears,

                Boolean isVerified,

                @DecimalMin(value = "0.0", message = "Rating average must be at least 0.0") @DecimalMax(value = "5.0", message = "Rating average must not exceed 5.0") BigDecimal ratingAverage,

                @Min(value = 0, message = "Review count must be non-negative") Integer reviewCount,

                @Pattern(regexp = "^(ACTIVE|INACTIVE|SUSPENDED)$", message = "Status must be ACTIVE, INACTIVE, or SUSPENDED") String status,

                String branchId) {
}
