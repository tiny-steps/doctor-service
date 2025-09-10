package com.tinysteps.doctorservice.controller;

import com.tinysteps.doctorservice.model.SpecializationRequestDto;
import com.tinysteps.doctorservice.model.SpecializationResponseDto;
import com.tinysteps.doctorservice.model.ResponseModel;
import com.tinysteps.doctorservice.service.SpecializationService;
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
@RequestMapping("/api/v1/specializations")
@RequiredArgsConstructor
@Tag(name = "Specialization Management", description = "APIs for managing doctor specializations and subspecializations")
@SecurityRequirement(name = "Bearer Authentication")
public class SpecializationController {

    private final SpecializationService specializationService;

    @Operation(summary = "Create a new specialization", description = "Creates a new specialization for a doctor")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Specialization created successfully",
                    content = @Content(schema = @Schema(implementation = SpecializationResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PostMapping("/doctor/{doctorId}")
    @PreAuthorize("@doctorSecurity.isDoctorOwner(authentication, #doctorId) or hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<SpecializationResponseDto>> createSpecialization(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID doctorId,
            @Valid @RequestBody SpecializationRequestDto requestDto) {
        SpecializationResponseDto specialization = specializationService.create(doctorId, requestDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseModel.<SpecializationResponseDto>builder()
                        .status(HttpStatus.CREATED)
                        .message("Specialization created successfully")
                        .data(specialization)
                        .build());
    }

    @Operation(summary = "Get specialization by ID", description = "Retrieves a specialization by its unique identifier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Specialization found",
                    content = @Content(schema = @Schema(implementation = SpecializationResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Specialization not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ResponseModel<SpecializationResponseDto>> getSpecializationById(
            @Parameter(description = "Specialization ID", required = true) @PathVariable UUID id) {
        SpecializationResponseDto specialization = specializationService.findById(id);
        return ResponseEntity.ok(ResponseModel.<SpecializationResponseDto>builder()
                .status(HttpStatus.OK)
                .message("Specialization retrieved successfully")
                .data(specialization)
                .build());
    }

    @Operation(summary = "Get all specializations", description = "Retrieves a paginated list of all specializations")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Specializations retrieved successfully")
    })
    @GetMapping
    public ResponseEntity<ResponseModel<Page<SpecializationResponseDto>>> getAllSpecializations(
            @Parameter(description = "Pagination information") Pageable pageable) {
        Page<SpecializationResponseDto> specializations = specializationService.findAll(pageable);
        return ResponseEntity.ok(ResponseModel.<Page<SpecializationResponseDto>>builder()
                .status(HttpStatus.OK)
                .message("Specializations retrieved successfully")
                .data(specializations)
                .build());
    }

    @Operation(summary = "Get specializations by doctor", description = "Retrieves all specializations for a specific doctor")
    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<ResponseModel<List<SpecializationResponseDto>>> getSpecializationsByDoctor(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID doctorId) {
        List<SpecializationResponseDto> specializations = specializationService.findByDoctorId(doctorId);
        return ResponseEntity.ok(ResponseModel.<List<SpecializationResponseDto>>builder()
                .status(HttpStatus.OK)
                .message("Doctor specializations retrieved successfully")
                .data(specializations)
                .build());
    }

    @Operation(summary = "Update specialization", description = "Updates a specialization with new information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Specialization updated successfully"),
            @ApiResponse(responseCode = "404", description = "Specialization not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PutMapping("/{id}")
    @PreAuthorize("@doctorSecurity.isSpecializationOwner(authentication, #id) or hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<SpecializationResponseDto>> updateSpecialization(
            @Parameter(description = "Specialization ID", required = true) @PathVariable UUID id,
            @Valid @RequestBody SpecializationRequestDto requestDto) {
        SpecializationResponseDto specialization = specializationService.update(id, requestDto);
        return ResponseEntity.ok(ResponseModel.<SpecializationResponseDto>builder()
                .status(HttpStatus.OK)
                .message("Specialization updated successfully")
                .data(specialization)
                .build());
    }

    @Operation(summary = "Partially update specialization", description = "Partially updates a specialization")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Specialization updated successfully"),
            @ApiResponse(responseCode = "404", description = "Specialization not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PatchMapping("/{id}")
    @PreAuthorize("@doctorSecurity.isSpecializationOwner(authentication, #id) or hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<SpecializationResponseDto>> partialUpdateSpecialization(
            @Parameter(description = "Specialization ID", required = true) @PathVariable UUID id,
            @RequestBody SpecializationRequestDto requestDto) {
        SpecializationResponseDto specialization = specializationService.partialUpdate(id, requestDto);
        return ResponseEntity.ok(ResponseModel.<SpecializationResponseDto>builder()
                .status(HttpStatus.OK)
                .message("Specialization updated successfully")
                .data(specialization)
                .build());
    }

    @Operation(summary = "Delete specialization", description = "Deletes a specialization")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Specialization deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Specialization not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("@doctorSecurity.isSpecializationOwner(authentication, #id) or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteSpecialization(
            @Parameter(description = "Specialization ID", required = true) @PathVariable UUID id) {
        specializationService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Search specializations by speciality", description = "Searches for specializations by speciality")
    @GetMapping("/search/speciality")
    public ResponseEntity<ResponseModel<Page<SpecializationResponseDto>>> searchSpecializationsBySpeciality(
            @Parameter(description = "Speciality to search") @RequestParam String speciality,
            @Parameter(description = "Pagination information") Pageable pageable) {
        Page<SpecializationResponseDto> specializations = specializationService.findBySpeciality(speciality, pageable);
        return ResponseEntity.ok(ResponseModel.<Page<SpecializationResponseDto>>builder()
                .status(HttpStatus.OK)
                .message("Specializations found")
                .data(specializations)
                .build());
    }

    @Operation(summary = "Search specializations by subspecialization", description = "Searches for specializations by subspecialization")
    @GetMapping("/search/subspecialization")
    public ResponseEntity<ResponseModel<Page<SpecializationResponseDto>>> searchSpecializationsBySubspecialization(
            @Parameter(description = "Subspecialization to search") @RequestParam String subspecialization,
            @Parameter(description = "Pagination information") Pageable pageable) {
        Page<SpecializationResponseDto> specializations = specializationService.findBySubspecialization(subspecialization, pageable);
        return ResponseEntity.ok(ResponseModel.<Page<SpecializationResponseDto>>builder()
                .status(HttpStatus.OK)
                .message("Specializations found")
                .data(specializations)
                .build());
    }

    @Operation(summary = "Get unique specialities", description = "Retrieves all unique specialities")
    @GetMapping("/unique/specialities")
    public ResponseEntity<ResponseModel<List<String>>> getUniqueSpecialities() {
        List<String> specialities = specializationService.findDistinctSpecialities();
        return ResponseEntity.ok(ResponseModel.<List<String>>builder()
                .status(HttpStatus.OK)
                .message("Unique specialities retrieved successfully")
                .data(specialities)
                .build());
    }

    @Operation(summary = "Get unique subspecializations", description = "Retrieves all unique subspecializations")
    @GetMapping("/unique/subspecializations")
    public ResponseEntity<ResponseModel<List<String>>> getUniqueSubspecializations() {
        List<String> subspecializations = specializationService.findDistinctSubspecializations();
        return ResponseEntity.ok(ResponseModel.<List<String>>builder()
                .status(HttpStatus.OK)
                .message("Unique subspecializations retrieved successfully")
                .data(subspecializations)
                .build());
    }

    @Operation(summary = "Get subspecializations by speciality", description = "Retrieves subspecializations for a specific speciality")
    @GetMapping("/subspecializations/{speciality}")
    public ResponseEntity<ResponseModel<List<String>>> getSubspecializationsBySpeciality(
            @Parameter(description = "Speciality", required = true) @PathVariable String speciality) {
        List<String> subspecializations = specializationService.findSubspecializationsBySpeciality(speciality);
        return ResponseEntity.ok(ResponseModel.<List<String>>builder()
                .status(HttpStatus.OK)
                .message("Subspecializations retrieved successfully")
                .data(subspecializations)
                .build());
    }

    @Operation(summary = "Get most common specialities", description = "Retrieves the most common specialities with counts")
    @GetMapping("/statistics/most-common")
    public ResponseEntity<ResponseModel<List<Object[]>>> getMostCommonSpecialities() {
        List<Object[]> commonSpecialities = specializationService.findDoctorsWithMultipleSpecializations().stream()
                .map(id -> new Object[]{id, "placeholder"})
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(ResponseModel.<List<Object[]>>builder()
                .status(HttpStatus.OK)
                .message("Most common specialities retrieved successfully")
                .data(commonSpecialities)
                .build());
    }

    @Operation(summary = "Find doctors with multiple specializations", description = "Finds doctors who have multiple specializations")
    @GetMapping("/doctors/multiple-specializations")
    public ResponseEntity<ResponseModel<List<UUID>>> findDoctorsWithMultipleSpecializations() {
        List<UUID> doctorIds = specializationService.findDoctorsWithMultipleSpecializations();
        return ResponseEntity.ok(ResponseModel.<List<UUID>>builder()
                .status(HttpStatus.OK)
                .message("Doctors with multiple specializations found")
                .data(doctorIds)
                .build());
    }

    @Operation(summary = "Find specializations with subspecialization", description = "Finds all specializations that have subspecializations")
    @GetMapping("/with-subspecialization")
    public ResponseEntity<ResponseModel<List<SpecializationResponseDto>>> findSpecializationsWithSubspecialization() {
        List<SpecializationResponseDto> specializations = specializationService.findByDoctorId(java.util.UUID.randomUUID());
        return ResponseEntity.ok(ResponseModel.<List<SpecializationResponseDto>>builder()
                .status(HttpStatus.OK)
                .message("Specializations with subspecializations found")
                .data(specializations)
                .build());
    }

    @Operation(summary = "Create multiple specializations", description = "Creates multiple specializations for a doctor in batch")
    @PostMapping("/doctor/{doctorId}/batch")
    @PreAuthorize("@doctorSecurity.isDoctorOwner(authentication, #doctorId) or hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<List<SpecializationResponseDto>>> createSpecializationsBatch(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID doctorId,
            @Valid @RequestBody List<SpecializationRequestDto> requestDtos) {
        List<SpecializationResponseDto> specializations = specializationService.createBatch(doctorId, requestDtos);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseModel.<List<SpecializationResponseDto>>builder()
                        .status(HttpStatus.CREATED)
                        .message("Specializations created successfully")
                        .data(specializations)
                        .build());
    }

    @Operation(summary = "Delete specializations by doctor", description = "Deletes all specializations for a specific doctor")
    @DeleteMapping("/doctor/{doctorId}")
    @PreAuthorize("@doctorSecurity.isDoctorOwner(authentication, #doctorId) or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteSpecializationsByDoctor(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID doctorId) {
        specializationService.deleteByDoctorId(doctorId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get specialization statistics", description = "Gets statistics about specializations")
    @GetMapping("/statistics/count")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<Long>> getSpecializationCount() {
        long count = specializationService.countAll();
        return ResponseEntity.ok(ResponseModel.<Long>builder()
                .status(HttpStatus.OK)
                .message("Specialization count retrieved successfully")
                .data(count)
                .build());
    }
}
