package com.tinysteps.doctorservice.service;

import com.tinysteps.doctorservice.dto.DoctorTransferRequestDto;
import com.tinysteps.doctorservice.dto.DoctorTransferResponseDto;

import java.util.UUID;

public interface DoctorTransferService {

    /**
     * Transfer a doctor from one branch to another
     * This involves updating the doctor-address relationships
     */
    DoctorTransferResponseDto transferDoctor(UUID doctorId, DoctorTransferRequestDto requestDto);

    /**
     * Check if a doctor can be transferred to a target branch
     */
    boolean canTransferDoctor(UUID doctorId, UUID targetBranchId);

    /**
     * Get current branches for a doctor
     */
    DoctorTransferResponseDto getCurrentBranches(UUID doctorId);

    /**
     * Emergency transfer - removes doctor from source and adds to target immediately
     */
    DoctorTransferResponseDto emergencyTransfer(UUID doctorId, UUID sourceBranchId, UUID targetBranchId, String reason);
}
