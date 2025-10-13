package com.tinysteps.doctorservice.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record OrganizationRequestDto(
                @NotBlank(message = "Organization name is required") @Size(max = 255, message = "Organization name must not exceed 255 characters") String organizationName,

                @Size(max = 100, message = "Role must not exceed 100 characters") String role,

                @Size(max = 100, message = "City must not exceed 100 characters") String city,

                @Size(max = 100, message = "State must not exceed 100 characters") String state,

                @Size(max = 100, message = "Country must not exceed 100 characters") String country,

                String tenureStart,

                String tenureEnd,

                String summary,

                // Optional doctor ID to allow reassigning organization to different doctor
                String doctorId) {
}
