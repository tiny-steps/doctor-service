package com.tintsteps.doctorsevice.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record PracticeRequestDto(
        @NotBlank(message = "Practice name is required")
        @Size(max = 255, message = "Practice name must not exceed 255 characters")
        String practiceName,
        
        @Size(max = 30, message = "Practice type must not exceed 30 characters")
        @Pattern(regexp = "^(CLINIC|HOSPITAL|NURSING_HOME|DIAGNOSTIC_CENTER|OTHER)$", 
                message = "Practice type must be one of: CLINIC, HOSPITAL, NURSING_HOME, DIAGNOSTIC_CENTER, OTHER")
        String practiceType,
        
        @NotNull(message = "Address ID is required")
        String addressId,
        
        @Size(max = 200, message = "Slug must not exceed 200 characters")
        @Pattern(regexp = "^[a-z0-9-]+$", message = "Slug must contain only lowercase letters, numbers, and hyphens")
        String slug,
        
        Integer practicePosition
) {
}
