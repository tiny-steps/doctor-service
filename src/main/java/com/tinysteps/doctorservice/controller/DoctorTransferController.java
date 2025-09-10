package com.tinysteps.doctorservice.controller;

import com.tinysteps.doctorservice.dto.DoctorTransferRequestDto;
import com.tinysteps.doctorservice.dto.DoctorTransferResponseDto;
import com.tinysteps.doctorservice.model.ResponseModel;
import com.tinysteps.doctorservice.service.DoctorTransferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/doctors")
@RequiredArgsConstructor
@Tag(name = "Doctor Transfer Management", description = "APIs for transferring doctors between branches")
@SecurityRequirement(name = "Bearer Authentication")
public class DoctorTransferController {

    private final DoctorTransferService doctorTransferService;

    @Operation(summary = "Transfer doctor between branches", description = "Transfer a doctor from one branch to another")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Doctor transferred successfully",
                    content = @Content(schema = @Schema(implementation = DoctorTransferResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid transfer request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Doctor not found")
    })
    @PostMapping("/{doctorId}/transfer")
    @PreAuthorize("hasRole('ADMIN') or hasRole('BRANCH_MANAGER')")
    public ResponseEntity<ResponseModel<DoctorTransferResponseDto>> transferDoctor(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID doctorId,
            @Valid @RequestBody DoctorTransferRequestDto requestDto) {

        log.info("Transfer request received for doctor {} from branch {} to branch {}",
                doctorId, requestDto.getSourceBranchId(), requestDto.getTargetBranchId());

        DoctorTransferResponseDto response = doctorTransferService.transferDoctor(doctorId, requestDto);

        if (DoctorTransferResponseDto.TransferStatus.SUCCESS.equals(response.getStatus())) {
            return ResponseEntity.ok(ResponseModel.success("Doctor transferred successfully", response));
        } else {
            return ResponseEntity.badRequest()
                    .body(ResponseModel.error(HttpStatus.BAD_REQUEST, "Transfer failed: " + response.getMessage(), null));
        }
    }

    @Operation(summary = "Check transfer eligibility", description = "Check if a doctor can be transferred to a target branch")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transfer eligibility checked successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/{doctorId}/transfer/eligibility")
    @PreAuthorize("hasRole('ADMIN') or hasRole('BRANCH_MANAGER')")
    public ResponseEntity<ResponseModel<Boolean>> checkTransferEligibility(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID doctorId,
            @Parameter(description = "Target branch ID", required = true) @RequestParam UUID targetBranchId) {

        boolean canTransfer = doctorTransferService.canTransferDoctor(doctorId, targetBranchId);
        return ResponseEntity.ok(ResponseModel.success(
                "Transfer eligibility checked successfully", canTransfer));
    }

    @Operation(summary = "Get current branches", description = "Get all current branch assignments for a doctor")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Current branches retrieved successfully",
                    content = @Content(schema = @Schema(implementation = DoctorTransferResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Doctor not found")
    })
    @GetMapping("/{doctorId}/branches")
    @PreAuthorize("@doctorSecurity.isDoctorOwner(authentication, #doctorId) or hasRole('ADMIN') or hasRole('BRANCH_MANAGER')")
    public ResponseEntity<ResponseModel<DoctorTransferResponseDto>> getCurrentBranches(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID doctorId) {

        DoctorTransferResponseDto response = doctorTransferService.getCurrentBranches(doctorId);

        if (DoctorTransferResponseDto.TransferStatus.SUCCESS.equals(response.getStatus())) {
            return ResponseEntity.ok(ResponseModel.success("Current branches retrieved successfully", response));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ResponseModel.error(HttpStatus.NOT_FOUND, "Failed to retrieve branches: " + response.getMessage(), null));
        }
    }

    @Operation(summary = "Emergency transfer", description = "Perform an emergency transfer of a doctor between branches")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Emergency transfer completed",
                    content = @Content(schema = @Schema(implementation = DoctorTransferResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid emergency transfer request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PostMapping("/{doctorId}/transfer/emergency")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<DoctorTransferResponseDto>> emergencyTransfer(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID doctorId,
            @Parameter(description = "Source branch ID", required = true) @RequestParam UUID sourceBranchId,
            @Parameter(description = "Target branch ID", required = true) @RequestParam UUID targetBranchId,
            @Parameter(description = "Emergency reason", required = true) @RequestParam String reason) {

        log.warn("Emergency transfer request for doctor {} from branch {} to branch {}. Reason: {}",
                doctorId, sourceBranchId, targetBranchId, reason);

        DoctorTransferResponseDto response = doctorTransferService.emergencyTransfer(
                doctorId, sourceBranchId, targetBranchId, reason);

        if (DoctorTransferResponseDto.TransferStatus.SUCCESS.equals(response.getStatus())) {
            return ResponseEntity.ok(ResponseModel.success("Emergency transfer completed successfully", response));
        } else {
            return ResponseEntity.badRequest()
                    .body(ResponseModel.error(HttpStatus.BAD_REQUEST, "Emergency transfer failed: " + response.getMessage(), null));
        }
    }
}
