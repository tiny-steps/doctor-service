package com.tinysteps.doctorservice.integration.model;

import lombok.Builder;

/**
 * Error model for external service integrations
 */
@Builder
public record IntegrationErrorModel(
        String message,
        String details
) {
}
