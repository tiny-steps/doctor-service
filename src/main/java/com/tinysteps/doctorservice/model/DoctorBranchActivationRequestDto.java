package com.tinysteps.doctorservice.model;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.UUID;

/**
 * DTO for requesting doctor activation in multiple branches
 */
public record DoctorBranchActivationRequestDto(
        @NotEmpty(message = "Branch IDs cannot be empty") List<UUID> branchIds,
        String reason,
        String practiceRole) {
}
