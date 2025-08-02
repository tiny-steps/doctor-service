package com.tinysteps.doctorsevice.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record AwardRequestDto(
        @NotBlank(message = "Award title is required")
        @Size(max = 255, message = "Award title must not exceed 255 characters")
        String title,

        @Min(value = 1900, message = "Awarded year must be after 1900")
        @Max(value = 2100, message = "Awarded year must be before 2100")
        Integer awardedYear,

        @Size(max = 255, message = "Award summary must not exceed 255 characters")
        String summary
) {
}
