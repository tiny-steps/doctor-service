package com.tinysteps.doctorsevice.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record QualificationRequestDto(
        @NotBlank(message = "Qualification name is required")
        @Size(max = 100, message = "Qualification name must not exceed 100 characters")
        String qualificationName,

        @Size(max = 255, message = "College name must not exceed 255 characters")
        String collegeName,

        @Min(value = 1900, message = "Completion year must be after 1900")
        @Max(value = 2100, message = "Completion year must be before 2100")
        Integer completionYear
) {
}
