package com.tinysteps.doctorsevice.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record RegistrationRequestDto(
        @Size(max = 255, message = "Registration council name must not exceed 255 characters")
        String registrationCouncilName,

        @Size(max = 100, message = "Registration number must not exceed 100 characters")
        String registrationNumber,

        @Min(value = 1900, message = "Registration year must be after 1900")
        @Max(value = 2100, message = "Registration year must be before 2100")
        Integer registrationYear
) {
}
