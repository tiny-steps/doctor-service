package com.tinysteps.doctorservice.service.impl;

import com.tinysteps.doctorservice.dto.DoctorTransferRequestDto;
import com.tinysteps.doctorservice.dto.DoctorTransferResponseDto;
import com.tinysteps.doctorservice.model.DoctorAddressRequestDto;
import com.tinysteps.doctorservice.model.DoctorAddressResponseDto;
import com.tinysteps.doctorservice.service.DoctorAddressService;
import com.tinysteps.doctorservice.service.DoctorTransferService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DoctorTransferServiceImpl implements DoctorTransferService {

    private final DoctorAddressService doctorAddressService;

    @Override
    @Transactional
    public DoctorTransferResponseDto transferDoctor(UUID doctorId, DoctorTransferRequestDto requestDto) {
        log.info("Starting doctor transfer for doctorId: {} from branch: {} to branch: {}",
                doctorId, requestDto.getSourceBranchId(), requestDto.getTargetBranchId());

        try {
            // Validate that doctor exists at source branch
            List<DoctorAddressResponseDto> currentAddresses = doctorAddressService.findByDoctorId(doctorId);
            boolean existsAtSource = currentAddresses.stream()
                    .anyMatch(addr -> addr.addressId().equals(requestDto.getSourceBranchId()));

            if (!existsAtSource) {
                return DoctorTransferResponseDto.builder()
                        .transferId(UUID.randomUUID())
                        .status(DoctorTransferResponseDto.TransferStatus.FAILED)
                        .message("Doctor not found at source branch")
                        .doctorId(doctorId)
                        .sourceBranchId(requestDto.getSourceBranchId())
                        .targetBranchId(requestDto.getTargetBranchId())
                        .transferredAt(LocalDateTime.now())
                        .build();
            }

            // Check if doctor already exists at target branch
            boolean existsAtTarget = currentAddresses.stream()
                    .anyMatch(addr -> addr.addressId().equals(requestDto.getTargetBranchId()));

            if (existsAtTarget && !requestDto.isMaintainExistingAssignments()) {
                return DoctorTransferResponseDto.builder()
                        .transferId(UUID.randomUUID())
                        .status(DoctorTransferResponseDto.TransferStatus.FAILED)
                        .message("Doctor already exists at target branch")
                        .doctorId(doctorId)
                        .sourceBranchId(requestDto.getSourceBranchId())
                        .targetBranchId(requestDto.getTargetBranchId())
                        .transferredAt(LocalDateTime.now())
                        .build();
            }

            // Get the practice roles from source branch
            List<DoctorAddressResponseDto> sourceRoles = currentAddresses.stream()
                    .filter(addr -> addr.addressId().equals(requestDto.getSourceBranchId().toString()))
                    .collect(Collectors.toList());

            // Add doctor to target branch with same roles
            for (DoctorAddressResponseDto sourceRole : sourceRoles) {
                if (!existsAtTarget || !doctorAddressService.existsDoctorAddress(
                        doctorId, requestDto.getTargetBranchId(), sourceRole.practiceRole())) {

                    DoctorAddressRequestDto addRequest = new DoctorAddressRequestDto(
                            requestDto.getTargetBranchId(),
                            sourceRole.practiceRole(),
                            "ACTIVE"
                    );
                    doctorAddressService.addDoctorAddress(doctorId, addRequest);
                }
            }

            // Remove from source branch if not maintaining existing assignments
            if (!requestDto.isMaintainExistingAssignments()) {
                for (DoctorAddressResponseDto sourceRole : sourceRoles) {
                    doctorAddressService.removeDoctorAddress(
                            doctorId, requestDto.getSourceBranchId(), sourceRole.practiceRole());
                }
            }

            // Get updated addresses
            List<DoctorAddressResponseDto> updatedAddresses = doctorAddressService.findByDoctorId(doctorId);

            log.info("Successfully transferred doctor {} from branch {} to branch {}",
                    doctorId, requestDto.getSourceBranchId(), requestDto.getTargetBranchId());

            return DoctorTransferResponseDto.builder()
                    .transferId(UUID.randomUUID())
                    .status(DoctorTransferResponseDto.TransferStatus.SUCCESS)
                    .message("Doctor transferred successfully")
                    .doctorId(doctorId)
                    .sourceBranchId(requestDto.getSourceBranchId())
                    .targetBranchId(requestDto.getTargetBranchId())
                    .transferredAt(LocalDateTime.now())
                    .currentAssignments(null) // TODO: Convert DoctorAddressResponseDto to BranchAssignment
                    .build();

        } catch (Exception e) {
            log.error("Error transferring doctor {}: {}", doctorId, e.getMessage(), e);
            return DoctorTransferResponseDto.builder()
                    .transferId(UUID.randomUUID())
                    .status(DoctorTransferResponseDto.TransferStatus.FAILED)
                    .message("Transfer failed: " + e.getMessage())
                    .doctorId(doctorId)
                    .sourceBranchId(requestDto.getSourceBranchId())
                    .targetBranchId(requestDto.getTargetBranchId())
                    .transferredAt(LocalDateTime.now())
                    .build();
        }
    }

    @Override
    public boolean canTransferDoctor(UUID doctorId, UUID targetBranchId) {
        log.debug("Checking if doctor {} can be transferred to branch {}", doctorId, targetBranchId);

        try {
            // Check if doctor exists
            List<DoctorAddressResponseDto> addresses = doctorAddressService.findByDoctorId(doctorId);
            if (addresses.isEmpty()) {
                return false;
            }

            // Check if target branch already has this doctor
            boolean existsAtTarget = addresses.stream()
                    .anyMatch(addr -> addr.addressId().equals(targetBranchId));

            // Can transfer if doctor doesn't exist at target or if maintaining existing assignments is allowed
            return !existsAtTarget;
        } catch (Exception e) {
            log.error("Error checking transfer eligibility for doctor {}: {}", doctorId, e.getMessage());
            return false;
        }
    }

    @Override
    public DoctorTransferResponseDto getCurrentBranches(UUID doctorId) {
        log.debug("Getting current branches for doctor {}", doctorId);

        try {
            List<DoctorAddressResponseDto> addresses = doctorAddressService.findByDoctorId(doctorId);

            // Convert DoctorAddressResponseDto to BranchAssignment
            List<DoctorTransferResponseDto.BranchAssignment> currentAssignments = addresses.stream()
                    .map(address -> DoctorTransferResponseDto.BranchAssignment.builder()
                            .branchId(UUID.fromString(address.addressId()))
                            .branchName("Branch " + address.addressId()) // TODO: Get actual branch name from address service
                            .role(address.practiceRole())
                            .isPrimary(false) // TODO: Determine primary branch logic
                            .assignedAt(address.createdAt())
                            .build())
                    .collect(Collectors.toList());

            return DoctorTransferResponseDto.builder()
                    .transferId(UUID.randomUUID())
                    .status(DoctorTransferResponseDto.TransferStatus.SUCCESS)
                    .message("Current branches retrieved successfully")
                    .doctorId(doctorId)
                    .currentAssignments(currentAssignments)
                    .transferredAt(LocalDateTime.now())
                    .build();
        } catch (Exception e) {
            log.error("Error getting current branches for doctor {}: {}", doctorId, e.getMessage());
            return DoctorTransferResponseDto.builder()
                    .transferId(UUID.randomUUID())
                    .status(DoctorTransferResponseDto.TransferStatus.FAILED)
                    .message("Failed to retrieve current branches: " + e.getMessage())
                    .doctorId(doctorId)
                    .transferredAt(LocalDateTime.now())
                    .build();
        }
    }

    @Override
    @Transactional
    public DoctorTransferResponseDto emergencyTransfer(UUID doctorId, UUID sourceBranchId, UUID targetBranchId, String reason) {
        log.warn("Emergency transfer initiated for doctor {} from branch {} to branch {}. Reason: {}",
                doctorId, sourceBranchId, targetBranchId, reason);

        DoctorTransferRequestDto emergencyRequest = DoctorTransferRequestDto.builder()
                .sourceBranchId(sourceBranchId)
                .targetBranchId(targetBranchId)
                .transferType(DoctorTransferRequestDto.TransferType.EMERGENCY_TRANSFER)
                .reason(reason)
                .notes("Emergency transfer - immediate action required")
                .maintainExistingAssignments(false) // Don't maintain existing assignments in emergency
                .notifyDoctor(true)   // Notify doctor
                .validateTargetBranchCapacity(true)
                .build();

        DoctorTransferResponseDto result = transferDoctor(doctorId, emergencyRequest);

        // Log emergency transfer for audit purposes
        if (DoctorTransferResponseDto.TransferStatus.SUCCESS.equals(result.getStatus())) {
            log.warn("Emergency transfer completed successfully for doctor {} - Reason: {}", doctorId, reason);
        } else {
            log.error("Emergency transfer failed for doctor {} - Reason: {} - Error: {}",
                    doctorId, reason, result.getMessage());
        }

        return result;
    }
}
