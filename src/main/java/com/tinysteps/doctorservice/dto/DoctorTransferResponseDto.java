package com.tinysteps.doctorservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO for doctor transfer responses
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorTransferResponseDto {

    private UUID transferId;
    private UUID doctorId;
    private TransferStatus status;
    private String message;
    private LocalDateTime transferredAt;
    
    // Branch information
    private UUID sourceBranchId;
    private UUID targetBranchId;
    
    // Transfer details
    private List<String> warnings;
    private List<String> errors;
    private boolean rollbackAvailable;
    private UUID rollbackId;
    
    // Doctor assignment details
    private List<BranchAssignment> currentAssignments;

    public enum TransferStatus {
        SUCCESS,
        FAILED,
        PENDING,
        ROLLED_BACK
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BranchAssignment {
        private UUID branchId;
        private String branchName;
        private String role;
        private boolean isPrimary;
        private LocalDateTime assignedAt;
    }
}