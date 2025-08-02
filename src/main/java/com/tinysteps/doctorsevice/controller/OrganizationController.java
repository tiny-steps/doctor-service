package com.tinysteps.doctorsevice.controller;

import com.tinysteps.doctorsevice.model.OrganizationRequestDto;
import com.tinysteps.doctorsevice.model.OrganizationResponseDto;
import com.tinysteps.doctorsevice.model.ResponseModel;
import com.tinysteps.doctorsevice.service.OrganizationService;
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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/organizations")
@RequiredArgsConstructor
@Tag(name = "Organization Management", description = "APIs for managing doctor organizations")
@SecurityRequirement(name = "Bearer Authentication")
public class OrganizationController {

    private final OrganizationService organizationService;

    @Operation(summary = "Create organization", description = "Creates a new organization for a doctor")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Organization created successfully",
                    content = @Content(schema = @Schema(implementation = OrganizationResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Doctor not found")
    })
    @PostMapping("/doctor/{doctorId}")
    @PreAuthorize("@doctorSecurity.isDoctorOwner(authentication, #doctorId) or hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<OrganizationResponseDto>> createOrganization(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID doctorId,
            @Parameter(description = "Organization details", required = true) @Valid @RequestBody OrganizationRequestDto requestDto) {
        OrganizationResponseDto organization = organizationService.create(doctorId, requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseModel.<OrganizationResponseDto>builder()
                .status(HttpStatus.CREATED)
                .message("Organization created successfully")
                .data(organization)
                .build());
    }

    @Operation(summary = "Get organization by ID", description = "Retrieves an organization by its unique identifier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Organization found",
                    content = @Content(schema = @Schema(implementation = OrganizationResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Organization not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ResponseModel<OrganizationResponseDto>> getOrganizationById(
            @Parameter(description = "Organization ID", required = true) @PathVariable UUID id) {
        OrganizationResponseDto organization = organizationService.findById(id);
        return ResponseEntity.ok(ResponseModel.<OrganizationResponseDto>builder()
                .status(HttpStatus.OK)
                .message("Organization retrieved successfully")
                .data(organization)
                .build());
    }

    @Operation(summary = "Get all organizations", description = "Retrieves a paginated list of all organizations")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Organizations retrieved successfully")
    })
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<Page<OrganizationResponseDto>>> getAllOrganizations(
            @Parameter(description = "Pagination information") Pageable pageable) {
        Page<OrganizationResponseDto> organizations = organizationService.findAll(pageable);
        return ResponseEntity.ok(ResponseModel.<Page<OrganizationResponseDto>>builder()
                .status(HttpStatus.OK)
                .message("Organizations retrieved successfully")
                .data(organizations)
                .build());
    }

    @Operation(summary = "Update organization", description = "Updates an existing organization")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Organization updated successfully"),
            @ApiResponse(responseCode = "404", description = "Organization not found")
    })
    @PutMapping("/{id}")
    @PreAuthorize("@doctorSecurity.isDoctorOwner(authentication, #id) or hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<OrganizationResponseDto>> updateOrganization(
            @Parameter(description = "Organization ID", required = true) @PathVariable UUID id,
            @Parameter(description = "Updated organization details", required = true) @Valid @RequestBody OrganizationRequestDto requestDto) {
        OrganizationResponseDto organization = organizationService.update(id, requestDto);
        return ResponseEntity.ok(ResponseModel.<OrganizationResponseDto>builder()
                .status(HttpStatus.OK)
                .message("Organization updated successfully")
                .data(organization)
                .build());
    }

    @Operation(summary = "Partially update organization", description = "Partially updates an existing organization")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Organization updated successfully"),
            @ApiResponse(responseCode = "404", description = "Organization not found")
    })
    @PatchMapping("/{id}")
    @PreAuthorize("@doctorSecurity.isDoctorOwner(authentication, #id) or hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<OrganizationResponseDto>> partialUpdateOrganization(
            @Parameter(description = "Organization ID", required = true) @PathVariable UUID id,
            @Parameter(description = "Partial organization details", required = true) @Valid @RequestBody OrganizationRequestDto requestDto) {
        OrganizationResponseDto organization = organizationService.partialUpdate(id, requestDto);
        return ResponseEntity.ok(ResponseModel.<OrganizationResponseDto>builder()
                .status(HttpStatus.OK)
                .message("Organization updated successfully")
                .data(organization)
                .build());
    }

    @Operation(summary = "Delete organization", description = "Deletes an organization")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Organization deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Organization not found")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("@doctorSecurity.isDoctorOwner(authentication, #id) or hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<Void>> deleteOrganization(
            @Parameter(description = "Organization ID", required = true) @PathVariable UUID id) {
        organizationService.delete(id);
        return ResponseEntity.ok(ResponseModel.<Void>builder()
                .status(HttpStatus.NO_CONTENT)
                .message("Organization deleted successfully")
                .data(null)
                .build());
    }

    @Operation(summary = "Get organizations by doctor", description = "Retrieves all organizations for a specific doctor")
    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<ResponseModel<List<OrganizationResponseDto>>> getOrganizationsByDoctor(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID doctorId) {
        List<OrganizationResponseDto> organizations = organizationService.findByDoctorId(doctorId);
        return ResponseEntity.ok(ResponseModel.<List<OrganizationResponseDto>>builder()
                .status(HttpStatus.OK)
                .message("Doctor organizations retrieved successfully")
                .data(organizations)
                .build());
    }

    @Operation(summary = "Get organizations by doctor (paginated)", description = "Retrieves paginated organizations for a specific doctor")
    @GetMapping("/doctor/{doctorId}/paginated")
    public ResponseEntity<ResponseModel<Page<OrganizationResponseDto>>> getOrganizationsByDoctorPaginated(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID doctorId,
            @Parameter(description = "Pagination information") Pageable pageable) {
        Page<OrganizationResponseDto> organizations = organizationService.findByDoctorId(doctorId, pageable);
        return ResponseEntity.ok(ResponseModel.<Page<OrganizationResponseDto>>builder()
                .status(HttpStatus.OK)
                .message("Doctor organizations retrieved successfully")
                .data(organizations)
                .build());
    }

    @Operation(summary = "Search organizations by name", description = "Searches organizations by organization name")
    @GetMapping("/search/name")
    public ResponseEntity<ResponseModel<Page<OrganizationResponseDto>>> searchOrganizationsByName(
            @Parameter(description = "Organization name to search") @RequestParam String organizationName,
            @Parameter(description = "Pagination information") Pageable pageable) {
        Page<OrganizationResponseDto> organizations = organizationService.findByOrganizationName(organizationName, pageable);
        return ResponseEntity.ok(ResponseModel.<Page<OrganizationResponseDto>>builder()
                .status(HttpStatus.OK)
                .message("Organizations found")
                .data(organizations)
                .build());
    }

    @Operation(summary = "Search organizations by role", description = "Searches organizations by role")
    @GetMapping("/search/role")
    public ResponseEntity<ResponseModel<Page<OrganizationResponseDto>>> searchOrganizationsByRole(
            @Parameter(description = "Role to search") @RequestParam String role,
            @Parameter(description = "Pagination information") Pageable pageable) {
        Page<OrganizationResponseDto> organizations = organizationService.findByRole(role, pageable);
        return ResponseEntity.ok(ResponseModel.<Page<OrganizationResponseDto>>builder()
                .status(HttpStatus.OK)
                .message("Organizations found")
                .data(organizations)
                .build());
    }

    @Operation(summary = "Check if doctor is employed at organization", description = "Checks if a doctor is employed at specific organization")
    @GetMapping("/doctor/{doctorId}/organization/employed")
    public ResponseEntity<ResponseModel<Boolean>> checkDoctorEmployedAtOrganization(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID doctorId,
            @Parameter(description = "Organization name") @RequestParam String organizationName) {
        boolean isEmployed = organizationService.isDoctorEmployedAtOrganization(doctorId, organizationName);
        return ResponseEntity.ok(ResponseModel.<Boolean>builder()
                .status(HttpStatus.OK)
                .message("Doctor employment status checked")
                .data(isEmployed)
                .build());
    }



    @Operation(summary = "Get current organizations", description = "Retrieves all current organizations")
    @GetMapping("/current")
    public ResponseEntity<ResponseModel<List<OrganizationResponseDto>>> getCurrentOrganizations() {
        List<OrganizationResponseDto> organizations = organizationService.findCurrentOrganizations();
        return ResponseEntity.ok(ResponseModel.<List<OrganizationResponseDto>>builder()
                .status(HttpStatus.OK)
                .message("Current organizations retrieved successfully")
                .data(organizations)
                .build());
    }

    @Operation(summary = "Get current organizations by doctor", description = "Retrieves current organizations for a doctor")
    @GetMapping("/doctor/{doctorId}/current")
    public ResponseEntity<ResponseModel<List<OrganizationResponseDto>>> getCurrentOrganizationsByDoctor(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID doctorId) {
        List<OrganizationResponseDto> organizations = organizationService.findCurrentOrganizationsByDoctorId(doctorId);
        return ResponseEntity.ok(ResponseModel.<List<OrganizationResponseDto>>builder()
                .status(HttpStatus.OK)
                .message("Current doctor organizations retrieved successfully")
                .data(organizations)
                .build());
    }

    @Operation(summary = "Get past organizations by doctor", description = "Retrieves past organizations for a doctor")
    @GetMapping("/doctor/{doctorId}/past")
    public ResponseEntity<ResponseModel<List<OrganizationResponseDto>>> getPastOrganizationsByDoctor(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID doctorId) {
        List<OrganizationResponseDto> organizations = organizationService.findPastOrganizationsByDoctorId(doctorId);
        return ResponseEntity.ok(ResponseModel.<List<OrganizationResponseDto>>builder()
                .status(HttpStatus.OK)
                .message("Past doctor organizations retrieved successfully")
                .data(organizations)
                .build());
    }

    @Operation(summary = "Get organizations by tenure period", description = "Retrieves organizations within tenure period")
    @GetMapping("/tenure-period")
    public ResponseEntity<ResponseModel<Page<OrganizationResponseDto>>> getOrganizationsByTenurePeriod(
            @Parameter(description = "Start date") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @Parameter(description = "End date") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
            @Parameter(description = "Pagination information") Pageable pageable) {
        Page<OrganizationResponseDto> organizations = organizationService.findByTenurePeriod(startDate, endDate, pageable);
        return ResponseEntity.ok(ResponseModel.<Page<OrganizationResponseDto>>builder()
                .status(HttpStatus.OK)
                .message("Organizations by tenure period retrieved successfully")
                .data(organizations)
                .build());
    }

    @Operation(summary = "Check if organization exists", description = "Checks if an organization exists by ID")
    @GetMapping("/{id}/exists")
    public ResponseEntity<ResponseModel<Boolean>> checkOrganizationExists(
            @Parameter(description = "Organization ID", required = true) @PathVariable UUID id) {
        boolean exists = organizationService.existsById(id);
        return ResponseEntity.ok(ResponseModel.<Boolean>builder()
                .status(HttpStatus.OK)
                .message("Organization existence checked")
                .data(exists)
                .build());
    }

    @Operation(summary = "Check if doctor has organizations", description = "Checks if a doctor has any organizations")
    @GetMapping("/doctor/{doctorId}/exists")
    public ResponseEntity<ResponseModel<Boolean>> checkDoctorHasOrganizations(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID doctorId) {
        boolean exists = organizationService.existsByDoctorId(doctorId);
        return ResponseEntity.ok(ResponseModel.<Boolean>builder()
                .status(HttpStatus.OK)
                .message("Doctor organizations existence checked")
                .data(exists)
                .build());
    }

    @Operation(summary = "Check if doctor is currently employed", description = "Checks if a doctor is currently employed")
    @GetMapping("/doctor/{doctorId}/currently-employed")
    public ResponseEntity<ResponseModel<Boolean>> checkDoctorCurrentlyEmployed(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID doctorId) {
        boolean isEmployed = organizationService.isDoctorCurrentlyEmployed(doctorId);
        return ResponseEntity.ok(ResponseModel.<Boolean>builder()
                .status(HttpStatus.OK)
                .message("Doctor employment status checked")
                .data(isEmployed)
                .build());
    }

    @Operation(summary = "Count organizations by doctor", description = "Gets the count of organizations for a doctor")
    @GetMapping("/doctor/{doctorId}/count")
    public ResponseEntity<ResponseModel<Long>> countOrganizationsByDoctor(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID doctorId) {
        long count = organizationService.countByDoctorId(doctorId);
        return ResponseEntity.ok(ResponseModel.<Long>builder()
                .status(HttpStatus.OK)
                .message("Doctor organizations count retrieved successfully")
                .data(count)
                .build());
    }

    @Operation(summary = "Count current organizations by doctor", description = "Gets the count of current organizations for a doctor")
    @GetMapping("/doctor/{doctorId}/current/count")
    public ResponseEntity<ResponseModel<Long>> countCurrentOrganizationsByDoctor(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID doctorId) {
        long count = organizationService.countCurrentOrganizationsByDoctorId(doctorId);
        return ResponseEntity.ok(ResponseModel.<Long>builder()
                .status(HttpStatus.OK)
                .message("Doctor current organizations count retrieved successfully")
                .data(count)
                .build());
    }

    @Operation(summary = "Count organizations by name", description = "Gets the count of organizations by organization name")
    @GetMapping("/name/{organizationName}/count")
    public ResponseEntity<ResponseModel<Long>> countOrganizationsByName(
            @Parameter(description = "Organization name", required = true) @PathVariable String organizationName) {
        long count = organizationService.countByOrganizationName(organizationName);
        return ResponseEntity.ok(ResponseModel.<Long>builder()
                .status(HttpStatus.OK)
                .message("Organizations count by name retrieved successfully")
                .data(count)
                .build());
    }

    @Operation(summary = "Get total organizations count", description = "Gets the total count of all organizations")
    @GetMapping("/count")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<Long>> getTotalOrganizationsCount() {
        long count = organizationService.countAll();
        return ResponseEntity.ok(ResponseModel.<Long>builder()
                .status(HttpStatus.OK)
                .message("Total organizations count retrieved successfully")
                .data(count)
                .build());
    }

    @Operation(summary = "Create batch organizations", description = "Creates multiple organizations for a doctor")
    @PostMapping("/doctor/{doctorId}/batch")
    @PreAuthorize("@doctorSecurity.isDoctorOwner(authentication, #doctorId) or hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<List<OrganizationResponseDto>>> createBatchOrganizations(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID doctorId,
            @Parameter(description = "List of organization details", required = true) @Valid @RequestBody List<OrganizationRequestDto> requestDtos) {
        List<OrganizationResponseDto> organizations = organizationService.createBatch(doctorId, requestDtos);
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseModel.<List<OrganizationResponseDto>>builder()
                .status(HttpStatus.CREATED)
                .message("Batch organizations created successfully")
                .data(organizations)
                .build());
    }

    @Operation(summary = "Delete organizations by doctor", description = "Deletes all organizations for a doctor")
    @DeleteMapping("/doctor/{doctorId}")
    @PreAuthorize("@doctorSecurity.isDoctorOwner(authentication, #doctorId) or hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<Void>> deleteOrganizationsByDoctor(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID doctorId) {
        organizationService.deleteByDoctorId(doctorId);
        return ResponseEntity.ok(ResponseModel.<Void>builder()
                .status(HttpStatus.NO_CONTENT)
                .message("Doctor organizations deleted successfully")
                .data(null)
                .build());
    }

    @Operation(summary = "Delete batch organizations", description = "Deletes multiple organizations by IDs")
    @DeleteMapping("/batch")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<Void>> deleteBatchOrganizations(
            @Parameter(description = "List of organization IDs", required = true) @RequestBody List<UUID> ids) {
        organizationService.deleteBatch(ids);
        return ResponseEntity.ok(ResponseModel.<Void>builder()
                .status(HttpStatus.NO_CONTENT)
                .message("Batch organizations deleted successfully")
                .data(null)
                .build());
    }

    @Operation(summary = "Get unique organization names", description = "Retrieves all unique organization names")
    @GetMapping("/unique/names")
    public ResponseEntity<ResponseModel<List<String>>> getUniqueOrganizationNames() {
        List<String> names = organizationService.getUniqueOrganizationNames();
        return ResponseEntity.ok(ResponseModel.<List<String>>builder()
                .status(HttpStatus.OK)
                .message("Unique organization names retrieved successfully")
                .data(names)
                .build());
    }

    @Operation(summary = "Get unique roles", description = "Retrieves all unique roles")
    @GetMapping("/unique/roles")
    public ResponseEntity<ResponseModel<List<String>>> getUniqueRoles() {
        List<String> roles = organizationService.getUniqueRoles();
        return ResponseEntity.ok(ResponseModel.<List<String>>builder()
                .status(HttpStatus.OK)
                .message("Unique roles retrieved successfully")
                .data(roles)
                .build());
    }
}
