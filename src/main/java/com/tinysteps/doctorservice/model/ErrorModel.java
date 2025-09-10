package com.tinysteps.doctorservice.model;

import lombok.Builder;

@Builder
public record ErrorModel(
        String message,
        String details
) {
}
