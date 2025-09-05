package com.tinysteps.doctorservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO for doctor transfer requests between branches
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorTransferRequestDto {

    @NotNull(message = "Source branch ID is required")
    private UUID sourceBranchId;

    @NotNull(message = "Target branch ID is required")
    private UUID targetBranchId;

    @NotNull(message = "Transfer type is required")
    private TransferType transferType;

    private String reason;
    private String notes;
    
    // Transfer options
    private boolean maintainExistingAssignments = true;
    private boolean validateTargetBranchCapacity = true;
    private boolean notifyDoctor = true;

    public enum TransferType {
        BRANCH_TRANSFER,
        EMERGENCY_TRANSFER,
        TEMPORARY_ASSIGNMENT,
        PERMANENT_RELOCATION
    }
}