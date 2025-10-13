package com.tinysteps.doctorservice.controller;

import com.tinysteps.doctorservice.entity.SpecializationMaster;
import com.tinysteps.doctorservice.model.SpecializationMasterRequestDto;
import com.tinysteps.doctorservice.model.SpecializationMasterResponseDto;
import com.tinysteps.doctorservice.model.ResponseModel;
import com.tinysteps.doctorservice.service.SpecializationMasterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST API for Managing Specializations (Independent of Doctors)
 * 
 * Purpose: Admins can CRUD specializations without needing a doctor
 */
@RestController
@RequestMapping("/api/v1/specializations")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Specializations", description = "Manage specializations independently")
public class SpecializationMasterController {

    private final SpecializationMasterService specializationMasterService;

    @PostMapping
    @Operation(summary = "Create a new specialization", description = "Create a specialization that can be assigned to doctors later")
    public ResponseEntity<ResponseModel<SpecializationMasterResponseDto>> createSpecialization(
            @Valid @RequestBody SpecializationMasterRequestDto request) {

        log.info("Creating new specialization: {}", request.name());

        SpecializationMaster created = specializationMasterService.create(
                request.name(),
                request.description());

        ResponseModel<SpecializationMasterResponseDto> response = ResponseModel
                .<SpecializationMasterResponseDto>builder()
                .status(HttpStatus.CREATED)
                .code(HttpStatus.CREATED.value())
                .message("Specialization created successfully")
                .data(toResponseDto(created))
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Get all active specializations")
    public ResponseEntity<ResponseModel<List<SpecializationMasterResponseDto>>> getAllActiveSpecializations() {
        log.info("Fetching all active specializations");

        List<SpecializationMaster> specializations = specializationMasterService.getAllActive();
        List<SpecializationMasterResponseDto> data = specializations.stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());

        ResponseModel<List<SpecializationMasterResponseDto>> response = ResponseModel
                .<List<SpecializationMasterResponseDto>>builder()
                .status(HttpStatus.OK)
                .code(HttpStatus.OK.value())
                .message("Specializations retrieved successfully")
                .data(data)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/names")
    @Operation(summary = "Get all specialization names", description = "Returns just the names for dropdown/autocomplete")
    public ResponseEntity<ResponseModel<List<String>>> getAllSpecializationNames() {
        log.info("Fetching all specialization names");
        List<String> names = specializationMasterService.getAllDistinctNames();

        ResponseModel<List<String>> response = ResponseModel.<List<String>>builder()
                .status(HttpStatus.OK)
                .code(HttpStatus.OK.value())
                .message("Specialization names retrieved successfully")
                .data(names)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get specialization by ID")
    public ResponseEntity<ResponseModel<SpecializationMasterResponseDto>> getSpecializationById(@PathVariable UUID id) {
        log.info("Fetching specialization with ID: {}", id);

        SpecializationMaster specialization = specializationMasterService.findById(id);

        ResponseModel<SpecializationMasterResponseDto> response = ResponseModel
                .<SpecializationMasterResponseDto>builder()
                .status(HttpStatus.OK)
                .code(HttpStatus.OK.value())
                .message("Specialization retrieved successfully")
                .data(toResponseDto(specialization))
                .build();

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a specialization")
    public ResponseEntity<ResponseModel<SpecializationMasterResponseDto>> updateSpecialization(
            @PathVariable UUID id,
            @Valid @RequestBody SpecializationMasterRequestDto request) {

        log.info("Updating specialization {}: {}", id, request.name());

        SpecializationMaster updated = specializationMasterService.update(
                id,
                request.name(),
                request.description());

        ResponseModel<SpecializationMasterResponseDto> response = ResponseModel
                .<SpecializationMasterResponseDto>builder()
                .status(HttpStatus.OK)
                .code(HttpStatus.OK.value())
                .message("Specialization updated successfully")
                .data(toResponseDto(updated))
                .build();

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate a specialization", description = "Deactivated specializations won't appear in active lists")
    public ResponseEntity<Void> deactivateSpecialization(@PathVariable UUID id) {
        log.info("Deactivating specialization: {}", id);
        specializationMasterService.deactivate(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/activate")
    @Operation(summary = "Activate a specialization")
    public ResponseEntity<Void> activateSpecialization(@PathVariable UUID id) {
        log.info("Activating specialization: {}", id);
        specializationMasterService.activate(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a specialization", description = "WARNING: This will remove the specialization from all doctors")
    public ResponseEntity<Void> deleteSpecialization(@PathVariable UUID id) {
        log.warn("Attempting to delete specialization: {}", id);
        // TODO: Implement soft delete or prevent deletion if assigned to doctors
        return ResponseEntity.noContent().build();
    }

    // Helper method to convert entity to DTO
    private SpecializationMasterResponseDto toResponseDto(SpecializationMaster entity) {
        return new SpecializationMasterResponseDto(
                entity.getId().toString(),
                entity.getName(),
                entity.getDescription(),
                entity.getIsActive());
    }
}
