package com.tinysteps.doctorsevice.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record PhotoRequestDto(
        @NotBlank(message = "Photo URL is required")
        @Size(max = 255, message = "Photo URL must not exceed 255 characters")
        @Pattern(regexp = "^(https?|ftp)://[^\\s/$.?#].[^\\s]*$", message = "Photo URL must be a valid URL")
        String photoUrl,

        Boolean isDefault
) {
}
