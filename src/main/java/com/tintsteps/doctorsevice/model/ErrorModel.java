package com.tintsteps.doctorsevice.model;

import lombok.Builder;

@Builder
public record ErrorModel(
        String message,
        String details
) {
}
