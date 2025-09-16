package com.tinysteps.doctorservice.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * Request DTO for branch-specific doctor deactivation operations.
 * Used when deactivating a doctor from specific branches rather than globally.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request for deactivating a doctor from specific branches")
public class DoctorBranchDeactivationRequestDto {

    @NotNull(message = "Branch IDs cannot be null")
    @NotEmpty(message = "At least one branch ID must be provided")
    @Schema(description = "List of branch IDs to deactivate the doctor from", example = "[\"550e8400-e29b-41d4-a716-446655440000\", \"550e8400-e29b-41d4-a716-446655440001\"]", required = true)
    private List<UUID> branchIds;

    @Schema(description = "Optional reason for deactivation", example = "Doctor requested to be removed from these branches")
    private String reason;

    @Schema(description = "Whether to set the doctor's global status to INACTIVE if they become inactive in all branches", example = "true", defaultValue = "true")
    @Builder.Default
    private Boolean updateGlobalStatus = true;
}