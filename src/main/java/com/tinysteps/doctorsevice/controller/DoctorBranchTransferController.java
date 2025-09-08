package com.tinysteps.doctorsevice.controller;

import com.tinysteps.doctorsevice.entity.DoctorAddress;
import com.tinysteps.doctorsevice.service.BranchTransferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/doctors/branch-transfer")
@RequiredArgsConstructor
@Tag(name = "Doctor Branch Transfer", description = "APIs for managing doctor assignments across branches")
public class DoctorBranchTransferController {

    private final BranchTransferService branchTransferService;

    @Operation(summary = "Transfer doctor between branches", description = "Move doctor from one branch to another, updating all relationships")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transfer completed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid transfer request"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Doctor not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/transfer/{doctorId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('BRANCH_MANAGER')")
    public ResponseEntity<Map<String, Object>> transferDoctorBetweenBranches(
            @Parameter(description = "Doctor ID to transfer") @PathVariable UUID doctorId,
            @Parameter(description = "Source branch ID") @RequestParam UUID sourceBranchId,
            @Parameter(description = "Target branch ID") @RequestParam UUID targetBranchId) {

        log.info("Received doctor transfer request: doctor={}, source={}, target={}",
                doctorId, sourceBranchId, targetBranchId);

        try {
            boolean success = branchTransferService.transferDoctorBetweenBranches(doctorId, sourceBranchId,
                    targetBranchId);

            if (success) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Doctor transferred successfully",
                        "doctorId", doctorId,
                        "sourceBranchId", sourceBranchId,
                        "targetBranchId", targetBranchId));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Failed to transfer doctor",
                        "doctorId", doctorId));
            }
        } catch (Exception e) {
            log.error("Error transferring doctor {}: {}", doctorId, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Internal server error: " + e.getMessage(),
                    "doctorId", doctorId));
        }
    }

    @Operation(summary = "Add doctor to branch", description = "Add doctor to additional branch without removing from current branches")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Doctor added to branch successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Doctor not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/add/{doctorId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('BRANCH_MANAGER')")
    public ResponseEntity<Map<String, Object>> addDoctorToBranch(
            @Parameter(description = "Doctor ID to add") @PathVariable UUID doctorId,
            @Parameter(description = "Branch ID to add doctor to") @RequestParam UUID branchId,
            @Parameter(description = "Practice role for the doctor at this branch") @RequestParam(defaultValue = "CONSULTANT") String role) {

        log.info("Received add doctor to branch request: doctor={}, branch={}, role={}",
                doctorId, branchId, role);

        try {
            DoctorAddress.PracticeRole practiceRole = DoctorAddress.PracticeRole.valueOf(role.toUpperCase());
            boolean success = branchTransferService.addDoctorToBranch(doctorId, branchId, practiceRole);

            if (success) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Doctor added to branch successfully",
                        "doctorId", doctorId,
                        "branchId", branchId,
                        "role", role));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Failed to add doctor to branch",
                        "doctorId", doctorId,
                        "branchId", branchId));
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Invalid role: " + role,
                    "doctorId", doctorId,
                    "branchId", branchId));
        } catch (Exception e) {
            log.error("Error adding doctor {} to branch {}: {}", doctorId, branchId, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Internal server error: " + e.getMessage(),
                    "doctorId", doctorId,
                    "branchId", branchId));
        }
    }

    @Operation(summary = "Remove doctor from branch", description = "Remove doctor from a specific branch")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Doctor removed from branch successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Doctor not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/remove/{doctorId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('BRANCH_MANAGER')")
    public ResponseEntity<Map<String, Object>> removeDoctorFromBranch(
            @Parameter(description = "Doctor ID to remove") @PathVariable UUID doctorId,
            @Parameter(description = "Branch ID to remove doctor from") @RequestParam UUID branchId) {

        log.info("Received remove doctor from branch request: doctor={}, branch={}",
                doctorId, branchId);

        try {
            boolean success = branchTransferService.removeDoctorFromBranch(doctorId, branchId);

            if (success) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Doctor removed from branch successfully",
                        "doctorId", doctorId,
                        "branchId", branchId));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Failed to remove doctor from branch",
                        "doctorId", doctorId,
                        "branchId", branchId));
            }
        } catch (Exception e) {
            log.error("Error removing doctor {} from branch {}: {}", doctorId, branchId, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Internal server error: " + e.getMessage(),
                    "doctorId", doctorId,
                    "branchId", branchId));
        }
    }

    @Operation(summary = "Get doctors by branch", description = "Get all doctors assigned to a specific branch")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Doctors retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/branch/{branchId}/doctors")
    @PreAuthorize("hasRole('ADMIN') or hasRole('BRANCH_MANAGER') or hasRole('DOCTOR') or hasRole('RECEPTIONIST')")
    public ResponseEntity<Map<String, Object>> getDoctorsByBranch(
            @Parameter(description = "Branch ID") @PathVariable UUID branchId) {

        log.info("Received get doctors by branch request: branch={}", branchId);

        try {
            List<UUID> doctorIds = branchTransferService.getDoctorsByBranch(branchId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Doctors retrieved successfully",
                    "branchId", branchId,
                    "doctorIds", doctorIds,
                    "count", doctorIds.size()));
        } catch (Exception e) {
            log.error("Error getting doctors for branch {}: {}", branchId, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Internal server error: " + e.getMessage(),
                    "branchId", branchId));
        }
    }

    @Operation(summary = "Get branches by doctor", description = "Get all branches where a doctor is assigned")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Branches retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Doctor not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/doctor/{doctorId}/branches")
    @PreAuthorize("hasRole('ADMIN') or hasRole('BRANCH_MANAGER') or hasRole('DOCTOR') or hasRole('RECEPTIONIST')")
    public ResponseEntity<Map<String, Object>> getBranchesByDoctor(
            @Parameter(description = "Doctor ID") @PathVariable UUID doctorId) {

        log.info("Received get branches by doctor request: doctor={}", doctorId);

        try {
            List<UUID> branchIds = branchTransferService.getBranchesByDoctor(doctorId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Branches retrieved successfully",
                    "doctorId", doctorId,
                    "branchIds", branchIds,
                    "count", branchIds.size()));
        } catch (Exception e) {
            log.error("Error getting branches for doctor {}: {}", doctorId, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Internal server error: " + e.getMessage(),
                    "doctorId", doctorId));
        }
    }
}