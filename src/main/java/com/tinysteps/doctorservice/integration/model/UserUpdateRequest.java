package com.tinysteps.doctorservice.integration.model;

import lombok.Builder;

/**
 * Request model for updating user information
 */
@Builder
public record UserUpdateRequest(
        String name,
        String email,
        String phone,
        String avatar,
        String imageData, // Added imageData field
        String status) {
}
