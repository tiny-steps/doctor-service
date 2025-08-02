package com.tinysteps.doctorsevice.controller;

import com.tinysteps.doctorsevice.model.MembershipRequestDto;
import com.tinysteps.doctorsevice.model.MembershipResponseDto;
import com.tinysteps.doctorsevice.model.ResponseModel;
import com.tinysteps.doctorsevice.service.MembershipService;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/memberships")
@RequiredArgsConstructor
@Tag(name = "Membership Management", description = "APIs for managing doctor memberships")
@SecurityRequirement(name = "Bearer Authentication")
public class MembershipController {

    private final MembershipService membershipService;

    @Operation(summary = "Create membership", description = "Creates a new membership for a doctor")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Membership created successfully",
                    content = @Content(schema = @Schema(implementation = MembershipResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Doctor not found")
    })
    @PostMapping("/doctor/{doctorId}")
    @PreAuthorize("@doctorSecurity.isDoctorOwner(authentication, #doctorId) or hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<MembershipResponseDto>> createMembership(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID doctorId,
            @Parameter(description = "Membership details", required = true) @Valid @RequestBody MembershipRequestDto requestDto) {
        MembershipResponseDto membership = membershipService.create(doctorId, requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseModel.<MembershipResponseDto>builder()
                .status(HttpStatus.CREATED)
                .message("Membership created successfully")
                .data(membership)
                .build());
    }

    @Operation(summary = "Get membership by ID", description = "Retrieves a membership by its unique identifier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Membership found",
                    content = @Content(schema = @Schema(implementation = MembershipResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Membership not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ResponseModel<MembershipResponseDto>> getMembershipById(
            @Parameter(description = "Membership ID", required = true) @PathVariable UUID id) {
        MembershipResponseDto membership = membershipService.findById(id);
        return ResponseEntity.ok(ResponseModel.<MembershipResponseDto>builder()
                .status(HttpStatus.OK)
                .message("Membership retrieved successfully")
                .data(membership)
                .build());
    }

    @Operation(summary = "Get all memberships", description = "Retrieves a paginated list of all memberships")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Memberships retrieved successfully")
    })
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<Page<MembershipResponseDto>>> getAllMemberships(
            @Parameter(description = "Pagination information") Pageable pageable) {
        Page<MembershipResponseDto> memberships = membershipService.findAll(pageable);
        return ResponseEntity.ok(ResponseModel.<Page<MembershipResponseDto>>builder()
                .status(HttpStatus.OK)
                .message("Memberships retrieved successfully")
                .data(memberships)
                .build());
    }

    @Operation(summary = "Update membership", description = "Updates an existing membership")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Membership updated successfully"),
            @ApiResponse(responseCode = "404", description = "Membership not found")
    })
    @PutMapping("/{id}")
    @PreAuthorize("@membershipSecurity.isMembershipOwner(authentication, #id) or hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<MembershipResponseDto>> updateMembership(
            @Parameter(description = "Membership ID", required = true) @PathVariable UUID id,
            @Parameter(description = "Updated membership details", required = true) @Valid @RequestBody MembershipRequestDto requestDto) {
        MembershipResponseDto membership = membershipService.update(id, requestDto);
        return ResponseEntity.ok(ResponseModel.<MembershipResponseDto>builder()
                .status(HttpStatus.OK)
                .message("Membership updated successfully")
                .data(membership)
                .build());
    }

    @Operation(summary = "Partially update membership", description = "Partially updates an existing membership")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Membership updated successfully"),
            @ApiResponse(responseCode = "404", description = "Membership not found")
    })
    @PatchMapping("/{id}")
    @PreAuthorize("@membershipSecurity.isMembershipOwner(authentication, #id) or hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<MembershipResponseDto>> partialUpdateMembership(
            @Parameter(description = "Membership ID", required = true) @PathVariable UUID id,
            @Parameter(description = "Partial membership details", required = true) @Valid @RequestBody MembershipRequestDto requestDto) {
        MembershipResponseDto membership = membershipService.partialUpdate(id, requestDto);
        return ResponseEntity.ok(ResponseModel.<MembershipResponseDto>builder()
                .status(HttpStatus.OK)
                .message("Membership updated successfully")
                .data(membership)
                .build());
    }

    @Operation(summary = "Delete membership", description = "Deletes a membership")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Membership deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Membership not found")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("@membershipSecurity.isMembershipOwner(authentication, #id) or hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<Void>> deleteMembership(
            @Parameter(description = "Membership ID", required = true) @PathVariable UUID id) {
        membershipService.delete(id);
        return ResponseEntity.ok(ResponseModel.<Void>builder()
                .status(HttpStatus.NO_CONTENT)
                .message("Membership deleted successfully")
                .data(null)
                .build());
    }

    @Operation(summary = "Get memberships by doctor", description = "Retrieves all memberships for a specific doctor")
    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<ResponseModel<List<MembershipResponseDto>>> getMembershipsByDoctor(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID doctorId) {
        List<MembershipResponseDto> memberships = membershipService.findByDoctorId(doctorId);
        return ResponseEntity.ok(ResponseModel.<List<MembershipResponseDto>>builder()
                .status(HttpStatus.OK)
                .message("Doctor memberships retrieved successfully")
                .data(memberships)
                .build());
    }

    @Operation(summary = "Get memberships by doctor (paginated)", description = "Retrieves paginated memberships for a specific doctor")
    @GetMapping("/doctor/{doctorId}/paginated")
    public ResponseEntity<ResponseModel<Page<MembershipResponseDto>>> getMembershipsByDoctorPaginated(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID doctorId,
            @Parameter(description = "Pagination information") Pageable pageable) {
        Page<MembershipResponseDto> memberships = membershipService.findByDoctorId(doctorId, pageable);
        return ResponseEntity.ok(ResponseModel.<Page<MembershipResponseDto>>builder()
                .status(HttpStatus.OK)
                .message("Doctor memberships retrieved successfully")
                .data(memberships)
                .build());
    }

    @Operation(summary = "Search memberships by council name", description = "Searches memberships by council name")
    @GetMapping("/search/council")
    public ResponseEntity<ResponseModel<Page<MembershipResponseDto>>> searchMembershipsByCouncilName(
            @Parameter(description = "Council name to search") @RequestParam String councilName,
            @Parameter(description = "Pagination information") Pageable pageable) {
        Page<MembershipResponseDto> memberships = membershipService.findByCouncilName(councilName, pageable);
        return ResponseEntity.ok(ResponseModel.<Page<MembershipResponseDto>>builder()
                .status(HttpStatus.OK)
                .message("Memberships found")
                .data(memberships)
                .build());
    }

    @Operation(summary = "Get doctor memberships by council", description = "Retrieves memberships for a doctor in specific council")
    @GetMapping("/doctor/{doctorId}/council")
    public ResponseEntity<ResponseModel<List<MembershipResponseDto>>> getDoctorMembershipsByCouncil(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID doctorId,
            @Parameter(description = "Council name") @RequestParam String councilName) {
        List<MembershipResponseDto> memberships = membershipService.findByDoctorIdAndCouncilName(doctorId, councilName);
        return ResponseEntity.ok(ResponseModel.<List<MembershipResponseDto>>builder()
                .status(HttpStatus.OK)
                .message("Doctor memberships by council retrieved successfully")
                .data(memberships)
                .build());
    }

    @Operation(summary = "Check if membership exists", description = "Checks if a membership exists by ID")
    @GetMapping("/{id}/exists")
    public ResponseEntity<ResponseModel<Boolean>> checkMembershipExists(
            @Parameter(description = "Membership ID", required = true) @PathVariable UUID id) {
        boolean exists = membershipService.existsById(id);
        return ResponseEntity.ok(ResponseModel.<Boolean>builder()
                .status(HttpStatus.OK)
                .message("Membership existence checked")
                .data(exists)
                .build());
    }

    @Operation(summary = "Check if doctor has memberships", description = "Checks if a doctor has any memberships")
    @GetMapping("/doctor/{doctorId}/exists")
    public ResponseEntity<ResponseModel<Boolean>> checkDoctorHasMemberships(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID doctorId) {
        boolean exists = membershipService.existsByDoctorId(doctorId);
        return ResponseEntity.ok(ResponseModel.<Boolean>builder()
                .status(HttpStatus.OK)
                .message("Doctor memberships existence checked")
                .data(exists)
                .build());
    }

    @Operation(summary = "Check if doctor has membership in council", description = "Checks if a doctor has membership in specific council")
    @GetMapping("/doctor/{doctorId}/council/{councilName}/exists")
    public ResponseEntity<ResponseModel<Boolean>> checkDoctorHasMembershipInCouncil(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID doctorId,
            @Parameter(description = "Council name", required = true) @PathVariable String councilName) {
        boolean hasMembership = membershipService.hasDoctorMembershipInCouncil(doctorId, councilName);
        return ResponseEntity.ok(ResponseModel.<Boolean>builder()
                .status(HttpStatus.OK)
                .message("Doctor membership in council checked")
                .data(hasMembership)
                .build());
    }

    @Operation(summary = "Count memberships by doctor", description = "Gets the count of memberships for a doctor")
    @GetMapping("/doctor/{doctorId}/count")
    public ResponseEntity<ResponseModel<Long>> countMembershipsByDoctor(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID doctorId) {
        long count = membershipService.countByDoctorId(doctorId);
        return ResponseEntity.ok(ResponseModel.<Long>builder()
                .status(HttpStatus.OK)
                .message("Doctor memberships count retrieved successfully")
                .data(count)
                .build());
    }

    @Operation(summary = "Count memberships by council", description = "Gets the count of memberships for specific council")
    @GetMapping("/council/{councilName}/count")
    public ResponseEntity<ResponseModel<Long>> countMembershipsByCouncil(
            @Parameter(description = "Council name", required = true) @PathVariable String councilName) {
        long count = membershipService.countByCouncilName(councilName);
        return ResponseEntity.ok(ResponseModel.<Long>builder()
                .status(HttpStatus.OK)
                .message("Memberships count by council retrieved successfully")
                .data(count)
                .build());
    }

    @Operation(summary = "Get total memberships count", description = "Gets the total count of all memberships")
    @GetMapping("/count")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<Long>> getTotalMembershipsCount() {
        long count = membershipService.countAll();
        return ResponseEntity.ok(ResponseModel.<Long>builder()
                .status(HttpStatus.OK)
                .message("Total memberships count retrieved successfully")
                .data(count)
                .build());
    }

    @Operation(summary = "Create batch memberships", description = "Creates multiple memberships for a doctor")
    @PostMapping("/doctor/{doctorId}/batch")
    @PreAuthorize("@doctorSecurity.isDoctorOwner(authentication, #doctorId) or hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<List<MembershipResponseDto>>> createBatchMemberships(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID doctorId,
            @Parameter(description = "List of membership details", required = true) @Valid @RequestBody List<MembershipRequestDto> requestDtos) {
        List<MembershipResponseDto> memberships = membershipService.createBatch(doctorId, requestDtos);
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseModel.<List<MembershipResponseDto>>builder()
                .status(HttpStatus.CREATED)
                .message("Batch memberships created successfully")
                .data(memberships)
                .build());
    }

    @Operation(summary = "Delete memberships by doctor", description = "Deletes all memberships for a doctor")
    @DeleteMapping("/doctor/{doctorId}")
    @PreAuthorize("@doctorSecurity.isDoctorOwner(authentication, #doctorId) or hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<Void>> deleteMembershipsByDoctor(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID doctorId) {
        membershipService.deleteByDoctorId(doctorId);
        return ResponseEntity.ok(ResponseModel.<Void>builder()
                .status(HttpStatus.NO_CONTENT)
                .message("Doctor memberships deleted successfully")
                .data(null)
                .build());
    }

    @Operation(summary = "Delete batch memberships", description = "Deletes multiple memberships by IDs")
    @DeleteMapping("/batch")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<Void>> deleteBatchMemberships(
            @Parameter(description = "List of membership IDs", required = true) @RequestBody List<UUID> ids) {
        membershipService.deleteBatch(ids);
        return ResponseEntity.ok(ResponseModel.<Void>builder()
                .status(HttpStatus.NO_CONTENT)
                .message("Batch memberships deleted successfully")
                .data(null)
                .build());
    }

    @Operation(summary = "Get unique council names", description = "Retrieves all unique council names")
    @GetMapping("/unique/councils")
    public ResponseEntity<ResponseModel<List<String>>> getUniqueCouncilNames() {
        List<String> councils = membershipService.getUniqueCouncilNames();
        return ResponseEntity.ok(ResponseModel.<List<String>>builder()
                .status(HttpStatus.OK)
                .message("Unique council names retrieved successfully")
                .data(councils)
                .build());
    }

    @Operation(summary = "Check if doctor is member of council", description = "Checks if a doctor is member of specific council")
    @GetMapping("/doctor/{doctorId}/council/{councilName}/member")
    public ResponseEntity<ResponseModel<Boolean>> checkDoctorIsMemberOfCouncil(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID doctorId,
            @Parameter(description = "Council name", required = true) @PathVariable String councilName) {
        boolean isMember = membershipService.isDoctorMemberOfCouncil(doctorId, councilName);
        return ResponseEntity.ok(ResponseModel.<Boolean>builder()
                .status(HttpStatus.OK)
                .message("Doctor council membership checked")
                .data(isMember)
                .build());
    }
}
