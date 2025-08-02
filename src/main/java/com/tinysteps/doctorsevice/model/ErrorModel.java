package com.tinysteps.doctorsevice.model;

import lombok.Builder;

@Builder
public record ErrorModel(
        String message,
        String details
) {
}
