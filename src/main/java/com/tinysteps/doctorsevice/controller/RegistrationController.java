package com.tinysteps.doctorsevice.controller;

import com.tinysteps.doctorsevice.model.RegistrationRequestDto;
import com.tinysteps.doctorsevice.model.RegistrationResponseDto;
import com.tinysteps.doctorsevice.model.ResponseModel;
import com.tinysteps.doctorsevice.service.RegistrationService;
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
@RequestMapping("/api/v1/registrations")
@RequiredArgsConstructor
@Tag(name = "Registration Management", description = "APIs for managing doctor registrations")
@SecurityRequirement(name = "Bearer Authentication")
public class RegistrationController {

    private final RegistrationService registrationService;

    @Operation(summary = "Create registration", description = "Creates a new registration for a doctor")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Registration created successfully",
                    content = @Content(schema = @Schema(implementation = RegistrationResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Doctor not found")
    })
    @PostMapping("/doctor/{doctorId}")
    @PreAuthorize("@doctorSecurity.isDoctorOwner(authentication, #doctorId) or hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<RegistrationResponseDto>> createRegistration(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID doctorId,
            @Parameter(description = "Registration details", required = true) @Valid @RequestBody RegistrationRequestDto requestDto) {
        RegistrationResponseDto registration = registrationService.create(doctorId, requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseModel.<RegistrationResponseDto>builder()
                .status(HttpStatus.CREATED)
                .message("Registration created successfully")
                .data(registration)
                .build());
    }

    @Operation(summary = "Get registration by ID", description = "Retrieves a registration by its unique identifier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Registration found",
                    content = @Content(schema = @Schema(implementation = RegistrationResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Registration not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ResponseModel<RegistrationResponseDto>> getRegistrationById(
            @Parameter(description = "Registration ID", required = true) @PathVariable UUID id) {
        RegistrationResponseDto registration = registrationService.findById(id);
        return ResponseEntity.ok(ResponseModel.<RegistrationResponseDto>builder()
                .status(HttpStatus.OK)
                .message("Registration retrieved successfully")
                .data(registration)
                .build());
    }

    @Operation(summary = "Get all registrations", description = "Retrieves a paginated list of all registrations")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Registrations retrieved successfully")
    })
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<Page<RegistrationResponseDto>>> getAllRegistrations(
            @Parameter(description = "Pagination information") Pageable pageable) {
        Page<RegistrationResponseDto> registrations = registrationService.findAll(pageable);
        return ResponseEntity.ok(ResponseModel.<Page<RegistrationResponseDto>>builder()
                .status(HttpStatus.OK)
                .message("Registrations retrieved successfully")
                .data(registrations)
                .build());
    }

    @Operation(summary = "Update registration", description = "Updates an existing registration")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Registration updated successfully"),
            @ApiResponse(responseCode = "404", description = "Registration not found")
    })
    @PutMapping("/{id}")
    @PreAuthorize("@registrationSecurity.isRegistrationOwner(authentication, #id) or hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<RegistrationResponseDto>> updateRegistration(
            @Parameter(description = "Registration ID", required = true) @PathVariable UUID id,
            @Parameter(description = "Updated registration details", required = true) @Valid @RequestBody RegistrationRequestDto requestDto) {
        RegistrationResponseDto registration = registrationService.update(id, requestDto);
        return ResponseEntity.ok(ResponseModel.<RegistrationResponseDto>builder()
                .status(HttpStatus.OK)
                .message("Registration updated successfully")
                .data(registration)
                .build());
    }

    @Operation(summary = "Partially update registration", description = "Partially updates an existing registration")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Registration updated successfully"),
            @ApiResponse(responseCode = "404", description = "Registration not found")
    })
    @PatchMapping("/{id}")
    @PreAuthorize("@registrationSecurity.isRegistrationOwner(authentication, #id) or hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<RegistrationResponseDto>> partialUpdateRegistration(
            @Parameter(description = "Registration ID", required = true) @PathVariable UUID id,
            @Parameter(description = "Partial registration details", required = true) @Valid @RequestBody RegistrationRequestDto requestDto) {
        RegistrationResponseDto registration = registrationService.partialUpdate(id, requestDto);
        return ResponseEntity.ok(ResponseModel.<RegistrationResponseDto>builder()
                .status(HttpStatus.OK)
                .message("Registration updated successfully")
                .data(registration)
                .build());
    }

    @Operation(summary = "Delete registration", description = "Deletes a registration")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Registration deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Registration not found")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("@registrationSecurity.isRegistrationOwner(authentication, #id) or hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<Void>> deleteRegistration(
            @Parameter(description = "Registration ID", required = true) @PathVariable UUID id) {
        registrationService.delete(id);
        return ResponseEntity.ok(ResponseModel.<Void>builder()
                .status(HttpStatus.NO_CONTENT)
                .message("Registration deleted successfully")
                .data(null)
                .build());
    }

    @Operation(summary = "Get registrations by doctor", description = "Retrieves all registrations for a specific doctor")
    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<ResponseModel<List<RegistrationResponseDto>>> getRegistrationsByDoctor(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID doctorId) {
        List<RegistrationResponseDto> registrations = registrationService.findByDoctorId(doctorId);
        return ResponseEntity.ok(ResponseModel.<List<RegistrationResponseDto>>builder()
                .status(HttpStatus.OK)
                .message("Doctor registrations retrieved successfully")
                .data(registrations)
                .build());
    }

    @Operation(summary = "Get registrations by doctor ordered by year", description = "Retrieves registrations for a doctor ordered by year")
    @GetMapping("/doctor/{doctorId}/ordered-by-year")
    public ResponseEntity<ResponseModel<List<RegistrationResponseDto>>> getRegistrationsByDoctorOrderedByYear(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID doctorId) {
        List<RegistrationResponseDto> registrations = registrationService.findByDoctorIdOrderByYear(doctorId);
        return ResponseEntity.ok(ResponseModel.<List<RegistrationResponseDto>>builder()
                .status(HttpStatus.OK)
                .message("Doctor registrations retrieved successfully")
                .data(registrations)
                .build());
    }

    @Operation(summary = "Get registrations by doctor (paginated)", description = "Retrieves paginated registrations for a specific doctor")
    @GetMapping("/doctor/{doctorId}/paginated")
    public ResponseEntity<ResponseModel<Page<RegistrationResponseDto>>> getRegistrationsByDoctorPaginated(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID doctorId,
            @Parameter(description = "Pagination information") Pageable pageable) {
        Page<RegistrationResponseDto> registrations = registrationService.findByDoctorId(doctorId, pageable);
        return ResponseEntity.ok(ResponseModel.<Page<RegistrationResponseDto>>builder()
                .status(HttpStatus.OK)
                .message("Doctor registrations retrieved successfully")
                .data(registrations)
                .build());
    }

    @Operation(summary = "Search registrations by council name", description = "Searches registrations by council name")
    @GetMapping("/search/council")
    public ResponseEntity<ResponseModel<Page<RegistrationResponseDto>>> searchRegistrationsByCouncilName(
            @Parameter(description = "Council name to search") @RequestParam String councilName,
            @Parameter(description = "Pagination information") Pageable pageable) {
        Page<RegistrationResponseDto> registrations = registrationService.findByCouncilName(councilName, pageable);
        return ResponseEntity.ok(ResponseModel.<Page<RegistrationResponseDto>>builder()
                .status(HttpStatus.OK)
                .message("Registrations found")
                .data(registrations)
                .build());
    }

    @Operation(summary = "Get registration by registration number", description = "Retrieves registration by registration number")
    @GetMapping("/number/{registrationNumber}")
    public ResponseEntity<ResponseModel<RegistrationResponseDto>> getRegistrationByNumber(
            @Parameter(description = "Registration number", required = true) @PathVariable String registrationNumber) {
        RegistrationResponseDto registration = registrationService.findByRegistrationNumber(registrationNumber);
        return ResponseEntity.ok(ResponseModel.<RegistrationResponseDto>builder()
                .status(HttpStatus.OK)
                .message("Registration found")
                .data(registration)
                .build());
    }

    @Operation(summary = "Get registrations by year", description = "Retrieves registrations by specific year")
    @GetMapping("/year/{year}")
    public ResponseEntity<ResponseModel<Page<RegistrationResponseDto>>> getRegistrationsByYear(
            @Parameter(description = "Registration year", required = true) @PathVariable Integer year,
            @Parameter(description = "Pagination information") Pageable pageable) {
        Page<RegistrationResponseDto> registrations = registrationService.findByRegistrationYear(year, pageable);
        return ResponseEntity.ok(ResponseModel.<Page<RegistrationResponseDto>>builder()
                .status(HttpStatus.OK)
                .message("Registrations retrieved successfully")
                .data(registrations)
                .build());
    }

    @Operation(summary = "Get doctor registrations by council", description = "Retrieves registrations for a doctor by council name")
    @GetMapping("/doctor/{doctorId}/council")
    public ResponseEntity<ResponseModel<List<RegistrationResponseDto>>> getDoctorRegistrationsByCouncil(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID doctorId,
            @Parameter(description = "Council name") @RequestParam String councilName) {
        List<RegistrationResponseDto> registrations = registrationService.findByDoctorIdAndCouncilName(doctorId, councilName);
        return ResponseEntity.ok(ResponseModel.<List<RegistrationResponseDto>>builder()
                .status(HttpStatus.OK)
                .message("Doctor registrations by council retrieved successfully")
                .data(registrations)
                .build());
    }

    @Operation(summary = "Get recent registrations", description = "Retrieves recent registrations from specified start year")
    @GetMapping("/recent")
    public ResponseEntity<ResponseModel<Page<RegistrationResponseDto>>> getRecentRegistrations(
            @Parameter(description = "Start year for recent registrations") @RequestParam Integer startYear,
            @Parameter(description = "Pagination information") Pageable pageable) {
        Page<RegistrationResponseDto> registrations = registrationService.findRecentRegistrations(startYear, pageable);
        return ResponseEntity.ok(ResponseModel.<Page<RegistrationResponseDto>>builder()
                .status(HttpStatus.OK)
                .message("Recent registrations retrieved successfully")
                .data(registrations)
                .build());
    }

    @Operation(summary = "Check if registration exists", description = "Checks if a registration exists by ID")
    @GetMapping("/{id}/exists")
    public ResponseEntity<ResponseModel<Boolean>> checkRegistrationExists(
            @Parameter(description = "Registration ID", required = true) @PathVariable UUID id) {
        boolean exists = registrationService.existsById(id);
        return ResponseEntity.ok(ResponseModel.<Boolean>builder()
                .status(HttpStatus.OK)
                .message("Registration existence checked")
                .data(exists)
                .build());
    }

    @Operation(summary = "Check if doctor has registrations", description = "Checks if a doctor has any registrations")
    @GetMapping("/doctor/{doctorId}/exists")
    public ResponseEntity<ResponseModel<Boolean>> checkDoctorHasRegistrations(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID doctorId) {
        boolean exists = registrationService.existsByDoctorId(doctorId);
        return ResponseEntity.ok(ResponseModel.<Boolean>builder()
                .status(HttpStatus.OK)
                .message("Doctor registrations existence checked")
                .data(exists)
                .build());
    }

    @Operation(summary = "Check if registration number exists", description = "Checks if a registration number exists")
    @GetMapping("/number/exists")
    public ResponseEntity<ResponseModel<Boolean>> checkRegistrationNumberExists(
            @Parameter(description = "Registration number", required = true) @RequestParam String registrationNumber) {
        boolean exists = registrationService.existsByRegistrationNumber(registrationNumber);
        return ResponseEntity.ok(ResponseModel.<Boolean>builder()
                .status(HttpStatus.OK)
                .message("Registration number existence checked")
                .data(exists)
                .build());
    }

    @Operation(summary = "Check if doctor has registration with council", description = "Checks if a doctor has registration with specific council")
    @GetMapping("/doctor/{doctorId}/council/{councilName}/exists")
    public ResponseEntity<ResponseModel<Boolean>> checkDoctorHasRegistrationWithCouncil(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID doctorId,
            @Parameter(description = "Council name", required = true) @PathVariable String councilName) {
        boolean hasRegistration = registrationService.hasDoctorRegistrationWithCouncil(doctorId, councilName);
        return ResponseEntity.ok(ResponseModel.<Boolean>builder()
                .status(HttpStatus.OK)
                .message("Doctor registration with council checked")
                .data(hasRegistration)
                .build());
    }

    @Operation(summary = "Check if registration number is unique", description = "Checks if a registration number is unique")
    @GetMapping("/number/unique")
    public ResponseEntity<ResponseModel<Boolean>> checkRegistrationNumberUnique(
            @Parameter(description = "Registration number", required = true) @RequestParam String registrationNumber) {
        boolean isUnique = registrationService.isRegistrationNumberUnique(registrationNumber);
        return ResponseEntity.ok(ResponseModel.<Boolean>builder()
                .status(HttpStatus.OK)
                .message("Registration number uniqueness checked")
                .data(isUnique)
                .build());
    }

    @Operation(summary = "Count registrations by doctor", description = "Gets the count of registrations for a doctor")
    @GetMapping("/doctor/{doctorId}/count")
    public ResponseEntity<ResponseModel<Long>> countRegistrationsByDoctor(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID doctorId) {
        long count = registrationService.countByDoctorId(doctorId);
        return ResponseEntity.ok(ResponseModel.<Long>builder()
                .status(HttpStatus.OK)
                .message("Doctor registrations count retrieved successfully")
                .data(count)
                .build());
    }

    @Operation(summary = "Count registrations by council", description = "Gets the count of registrations for specific council")
    @GetMapping("/council/{councilName}/count")
    public ResponseEntity<ResponseModel<Long>> countRegistrationsByCouncil(
            @Parameter(description = "Council name", required = true) @PathVariable String councilName) {
        long count = registrationService.countByCouncilName(councilName);
        return ResponseEntity.ok(ResponseModel.<Long>builder()
                .status(HttpStatus.OK)
                .message("Registrations count by council retrieved successfully")
                .data(count)
                .build());
    }

    @Operation(summary = "Count registrations by year", description = "Gets the count of registrations for specific year")
    @GetMapping("/year/{year}/count")
    public ResponseEntity<ResponseModel<Long>> countRegistrationsByYear(
            @Parameter(description = "Registration year", required = true) @PathVariable Integer year) {
        long count = registrationService.countByRegistrationYear(year);
        return ResponseEntity.ok(ResponseModel.<Long>builder()
                .status(HttpStatus.OK)
                .message("Registrations count by year retrieved successfully")
                .data(count)
                .build());
    }

    @Operation(summary = "Get total registrations count", description = "Gets the total count of all registrations")
    @GetMapping("/count")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<Long>> getTotalRegistrationsCount() {
        long count = registrationService.countAll();
        return ResponseEntity.ok(ResponseModel.<Long>builder()
                .status(HttpStatus.OK)
                .message("Total registrations count retrieved successfully")
                .data(count)
                .build());
    }

    @Operation(summary = "Create batch registrations", description = "Creates multiple registrations for a doctor")
    @PostMapping("/doctor/{doctorId}/batch")
    @PreAuthorize("@doctorSecurity.isDoctorOwner(authentication, #doctorId) or hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<List<RegistrationResponseDto>>> createBatchRegistrations(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID doctorId,
            @Parameter(description = "List of registration details", required = true) @Valid @RequestBody List<RegistrationRequestDto> requestDtos) {
        List<RegistrationResponseDto> registrations = registrationService.createBatch(doctorId, requestDtos);
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseModel.<List<RegistrationResponseDto>>builder()
                .status(HttpStatus.CREATED)
                .message("Batch registrations created successfully")
                .data(registrations)
                .build());
    }

    @Operation(summary = "Delete registrations by doctor", description = "Deletes all registrations for a doctor")
    @DeleteMapping("/doctor/{doctorId}")
    @PreAuthorize("@doctorSecurity.isDoctorOwner(authentication, #doctorId) or hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<Void>> deleteRegistrationsByDoctor(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID doctorId) {
        registrationService.deleteByDoctorId(doctorId);
        return ResponseEntity.ok(ResponseModel.<Void>builder()
                .status(HttpStatus.NO_CONTENT)
                .message("Doctor registrations deleted successfully")
                .data(null)
                .build());
    }

    @Operation(summary = "Delete batch registrations", description = "Deletes multiple registrations by IDs")
    @DeleteMapping("/batch")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<Void>> deleteBatchRegistrations(
            @Parameter(description = "List of registration IDs", required = true) @RequestBody List<UUID> ids) {
        registrationService.deleteBatch(ids);
        return ResponseEntity.ok(ResponseModel.<Void>builder()
                .status(HttpStatus.NO_CONTENT)
                .message("Batch registrations deleted successfully")
                .data(null)
                .build());
    }

    @Operation(summary = "Get unique council names", description = "Retrieves all unique council names")
    @GetMapping("/unique/councils")
    public ResponseEntity<ResponseModel<List<String>>> getUniqueCouncilNames() {
        List<String> councils = registrationService.getUniqueCouncilNames();
        return ResponseEntity.ok(ResponseModel.<List<String>>builder()
                .status(HttpStatus.OK)
                .message("Unique council names retrieved successfully")
                .data(councils)
                .build());
    }

    @Operation(summary = "Get doctor registrations in year range", description = "Retrieves registrations for a doctor within year range")
    @GetMapping("/doctor/{doctorId}/year-range")
    public ResponseEntity<ResponseModel<List<RegistrationResponseDto>>> getDoctorRegistrationsInYearRange(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID doctorId,
            @Parameter(description = "Start year") @RequestParam Integer startYear,
            @Parameter(description = "End year") @RequestParam Integer endYear) {
        List<RegistrationResponseDto> registrations = registrationService.findDoctorRegistrationsInYearRange(doctorId, startYear, endYear);
        return ResponseEntity.ok(ResponseModel.<List<RegistrationResponseDto>>builder()
                .status(HttpStatus.OK)
                .message("Doctor registrations in year range retrieved successfully")
                .data(registrations)
                .build());
    }

    @Operation(summary = "Check if doctor is registered with council", description = "Checks if a doctor is registered with specific council")
    @GetMapping("/doctor/{doctorId}/council/{councilName}/registered")
    public ResponseEntity<ResponseModel<Boolean>> checkDoctorRegisteredWithCouncil(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID doctorId,
            @Parameter(description = "Council name", required = true) @PathVariable String councilName) {
        boolean isRegistered = registrationService.isDoctorRegisteredWithCouncil(doctorId, councilName);
        return ResponseEntity.ok(ResponseModel.<Boolean>builder()
                .status(HttpStatus.OK)
                .message("Doctor registration with council checked")
                .data(isRegistered)
                .build());
    }

    @Operation(summary = "Validate registration number", description = "Validates a registration number")
    @GetMapping("/number/validate")
    public ResponseEntity<ResponseModel<Boolean>> validateRegistrationNumber(
            @Parameter(description = "Registration number", required = true) @RequestParam String registrationNumber) {
        boolean isValid = registrationService.validateRegistrationNumber(registrationNumber);
        return ResponseEntity.ok(ResponseModel.<Boolean>builder()
                .status(HttpStatus.OK)
                .message("Registration number validated")
                .data(isValid)
                .build());
    }
}
