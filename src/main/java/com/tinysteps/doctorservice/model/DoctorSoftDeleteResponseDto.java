package com.tinysteps.doctorservice.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * Response DTO for doctor soft delete operations.
 * Provides detailed information about the operation result.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response for doctor soft delete operations")
public class DoctorSoftDeleteResponseDto {

    @Schema(description = "ID of the doctor that was processed", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID doctorId;

    @Schema(description = "Whether the operation was successful", example = "true")
    private Boolean success;

    @Schema(description = "Descriptive message about the operation", example = "Doctor successfully deactivated from 2 branches")
    private String message;

    @Schema(description = "List of branch IDs that were affected", example = "[\"550e8400-e29b-41d4-a716-446655440000\", \"550e8400-e29b-41d4-a716-446655440001\"]")
    private List<UUID> affectedBranches;

    @Schema(description = "Whether the doctor's global status was changed", example = "true")
    private Boolean globalStatusChanged;

    @Schema(description = "The doctor's new global status", example = "INACTIVE")
    private String newGlobalStatus;

    @Schema(description = "Number of branches where the doctor is still active", example = "3")
    private Integer remainingActiveBranches;

    @Schema(description = "Total number of branches the doctor is associated with", example = "5")
    private Integer totalBranches;

    @Schema(description = "Type of operation performed", example = "BRANCH_SPECIFIC_DEACTIVATION")
    private String operationType;
}