package com.tinysteps.doctorsevice.controller;

import com.tinysteps.doctorsevice.model.PracticeRequestDto;
import com.tinysteps.doctorsevice.model.PracticeResponseDto;
import com.tinysteps.doctorsevice.model.ResponseModel;
import com.tinysteps.doctorsevice.service.PracticeService;
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
@RequestMapping("/api/v1/practices")
@RequiredArgsConstructor
@Tag(name = "Practice Management", description = "APIs for managing doctor practices and clinic information")
@SecurityRequirement(name = "Bearer Authentication")
public class PracticeController {

    private final PracticeService practiceService;

    @Operation(summary = "Create a new practice", description = "Creates a new practice for a doctor")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Practice created successfully",
                    content = @Content(schema = @Schema(implementation = PracticeResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PostMapping("/doctor/{doctorId}")
    @PreAuthorize("@doctorSecurity.isDoctorOwner(authentication, #doctorId) or hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<PracticeResponseDto>> createPractice(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID doctorId,
            @Valid @RequestBody PracticeRequestDto requestDto) {
        PracticeResponseDto practice = practiceService.create(doctorId, requestDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseModel.<PracticeResponseDto>builder()
                        .status(HttpStatus.CREATED)
                        .message("Practice created successfully")
                        .data(practice)
                        .build());
    }

    @Operation(summary = "Get practice by ID", description = "Retrieves a practice by its unique identifier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Practice found",
                    content = @Content(schema = @Schema(implementation = PracticeResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Practice not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ResponseModel<PracticeResponseDto>> getPracticeById(
            @Parameter(description = "Practice ID", required = true) @PathVariable UUID id) {
        PracticeResponseDto practice = practiceService.findById(id);
        return ResponseEntity.ok(ResponseModel.<PracticeResponseDto>builder()
                .status(HttpStatus.OK)
                .message("Practice retrieved successfully")
                .data(practice)
                .build());
    }

    @Operation(summary = "Get all practices", description = "Retrieves a paginated list of all practices")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Practices retrieved successfully")
    })
    @GetMapping
    public ResponseEntity<ResponseModel<Page<PracticeResponseDto>>> getAllPractices(
            @Parameter(description = "Pagination information") Pageable pageable) {
        Page<PracticeResponseDto> practices = practiceService.findAll(pageable);
        return ResponseEntity.ok(ResponseModel.<Page<PracticeResponseDto>>builder()
                .status(HttpStatus.OK)
                .message("Practices retrieved successfully")
                .data(practices)
                .build());
    }

    @Operation(summary = "Get practices by doctor", description = "Retrieves all practices for a specific doctor")
    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<ResponseModel<List<PracticeResponseDto>>> getPracticesByDoctor(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID doctorId) {
        List<PracticeResponseDto> practices = practiceService.findByDoctorId(doctorId);
        return ResponseEntity.ok(ResponseModel.<List<PracticeResponseDto>>builder()
                .status(HttpStatus.OK)
                .message("Doctor practices retrieved successfully")
                .data(practices)
                .build());
    }

    @Operation(summary = "Get practices by doctor (ordered by position)", description = "Retrieves practices for a doctor ordered by position")
    @GetMapping("/doctor/{doctorId}/ordered-by-position")
    public ResponseEntity<ResponseModel<List<PracticeResponseDto>>> getPracticesByDoctorOrderedByPosition(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID doctorId) {
        List<PracticeResponseDto> practices = practiceService.findByDoctorIdOrderByPosition(doctorId);
        return ResponseEntity.ok(ResponseModel.<List<PracticeResponseDto>>builder()
                .status(HttpStatus.OK)
                .message("Doctor practices retrieved successfully")
                .data(practices)
                .build());
    }

    @Operation(summary = "Update practice", description = "Updates a practice with new information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Practice updated successfully"),
            @ApiResponse(responseCode = "404", description = "Practice not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PutMapping("/{id}")
    @PreAuthorize("@doctorSecurity.isPracticeOwner(authentication, #id) or hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<PracticeResponseDto>> updatePractice(
            @Parameter(description = "Practice ID", required = true) @PathVariable UUID id,
            @Valid @RequestBody PracticeRequestDto requestDto) {
        PracticeResponseDto practice = practiceService.update(id, requestDto);
        return ResponseEntity.ok(ResponseModel.<PracticeResponseDto>builder()
                .status(HttpStatus.OK)
                .message("Practice updated successfully")
                .data(practice)
                .build());
    }

    @Operation(summary = "Partially update practice", description = "Partially updates a practice")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Practice updated successfully"),
            @ApiResponse(responseCode = "404", description = "Practice not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PatchMapping("/{id}")
    @PreAuthorize("@doctorSecurity.isPracticeOwner(authentication, #id) or hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<PracticeResponseDto>> partialUpdatePractice(
            @Parameter(description = "Practice ID", required = true) @PathVariable UUID id,
            @RequestBody PracticeRequestDto requestDto) {
        PracticeResponseDto practice = practiceService.partialUpdate(id, requestDto);
        return ResponseEntity.ok(ResponseModel.<PracticeResponseDto>builder()
                .status(HttpStatus.OK)
                .message("Practice updated successfully")
                .data(practice)
                .build());
    }

    @Operation(summary = "Delete practice", description = "Deletes a practice")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Practice deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Practice not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("@doctorSecurity.isPracticeOwner(authentication, #id) or hasRole('ADMIN')")
    public ResponseEntity<Void> deletePractice(
            @Parameter(description = "Practice ID", required = true) @PathVariable UUID id) {
        practiceService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get practice by slug", description = "Retrieves a practice by its unique slug")
    @GetMapping("/slug/{slug}")
    public ResponseEntity<ResponseModel<PracticeResponseDto>> getPracticeBySlug(
            @Parameter(description = "Practice slug", required = true) @PathVariable String slug) {
        PracticeResponseDto practice = practiceService.findBySlug(slug);
        return ResponseEntity.ok(ResponseModel.<PracticeResponseDto>builder()
                .status(HttpStatus.OK)
                .message("Practice retrieved successfully")
                .data(practice)
                .build());
    }

    @Operation(summary = "Search practices by name", description = "Searches for practices by practice name")
    @GetMapping("/search/name")
    public ResponseEntity<ResponseModel<Page<PracticeResponseDto>>> searchPracticesByName(
            @Parameter(description = "Practice name to search") @RequestParam String practiceName,
            @Parameter(description = "Pagination information") Pageable pageable) {
        Page<PracticeResponseDto> practices = practiceService.findByPracticeName(practiceName, pageable);
        return ResponseEntity.ok(ResponseModel.<Page<PracticeResponseDto>>builder()
                .status(HttpStatus.OK)
                .message("Practices found")
                .data(practices)
                .build());
    }

    @Operation(summary = "Search practices by type", description = "Searches for practices by practice type")
    @GetMapping("/search/type")
    public ResponseEntity<ResponseModel<Page<PracticeResponseDto>>> searchPracticesByType(
            @Parameter(description = "Practice type to search") @RequestParam String practiceType,
            @Parameter(description = "Pagination information") Pageable pageable) {
        Page<PracticeResponseDto> practices = practiceService.findByPracticeType(practiceType, pageable);
        return ResponseEntity.ok(ResponseModel.<Page<PracticeResponseDto>>builder()
                .status(HttpStatus.OK)
                .message("Practices found")
                .data(practices)
                .build());
    }

    @Operation(summary = "Search practices by address", description = "Searches for practices by address ID")
    @GetMapping("/search/address/{addressId}")
    public ResponseEntity<ResponseModel<Page<PracticeResponseDto>>> searchPracticesByAddress(
            @Parameter(description = "Address ID") @PathVariable UUID addressId,
            @Parameter(description = "Pagination information") Pageable pageable) {
        Page<PracticeResponseDto> practices = practiceService.findByAddressId(addressId, pageable);
        return ResponseEntity.ok(ResponseModel.<Page<PracticeResponseDto>>builder()
                .status(HttpStatus.OK)
                .message("Practices found")
                .data(practices)
                .build());
    }

    @Operation(summary = "Get unique practice types", description = "Retrieves all unique practice types")
    @GetMapping("/unique/types")
    public ResponseEntity<ResponseModel<List<String>>> getUniquePracticeTypes() {
        List<String> types = practiceService.getUniquePracticeTypes();
        return ResponseEntity.ok(ResponseModel.<List<String>>builder()
                .status(HttpStatus.OK)
                .message("Unique practice types retrieved successfully")
                .data(types)
                .build());
    }

    @Operation(summary = "Get most common practice types", description = "Retrieves the most common practice types with counts")
    @GetMapping("/statistics/most-common-types")
    public ResponseEntity<ResponseModel<List<Object[]>>> getMostCommonPracticeTypes() {
        List<Object[]> commonTypes = practiceService.getMostCommonPracticeTypes();
        return ResponseEntity.ok(ResponseModel.<List<Object[]>>builder()
                .status(HttpStatus.OK)
                .message("Most common practice types retrieved successfully")
                .data(commonTypes)
                .build());
    }

    @Operation(summary = "Find doctors with multiple practices", description = "Finds doctors who have multiple practices")
    @GetMapping("/doctors/multiple-practices")
    public ResponseEntity<ResponseModel<List<UUID>>> findDoctorsWithMultiplePractices() {
        List<UUID> doctorIds = practiceService.findDoctorsWithMultiplePractices();
        return ResponseEntity.ok(ResponseModel.<List<UUID>>builder()
                .status(HttpStatus.OK)
                .message("Doctors with multiple practices found")
                .data(doctorIds)
                .build());
    }

    @Operation(summary = "Update practice position", description = "Updates the position of a practice")
    @PatchMapping("/{id}/position")
    @PreAuthorize("@doctorSecurity.isPracticeOwner(authentication, #id) or hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<PracticeResponseDto>> updatePracticePosition(
            @Parameter(description = "Practice ID", required = true) @PathVariable UUID id,
            @Parameter(description = "New position", required = true) @RequestParam Integer newPosition) {
        PracticeResponseDto practice = practiceService.updatePracticePosition(id, newPosition);
        return ResponseEntity.ok(ResponseModel.<PracticeResponseDto>builder()
                .status(HttpStatus.OK)
                .message("Practice position updated successfully")
                .data(practice)
                .build());
    }

    @Operation(summary = "Reorder practices", description = "Reorders practices for a doctor")
    @PutMapping("/doctor/{doctorId}/reorder")
    @PreAuthorize("@doctorSecurity.isDoctorOwner(authentication, #doctorId) or hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<String>> reorderPractices(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID doctorId,
            @Parameter(description = "Ordered list of practice IDs", required = true) @RequestBody List<UUID> practiceIds) {
        practiceService.reorderPractices(doctorId, practiceIds);
        return ResponseEntity.ok(ResponseModel.<String>builder()
                .status(HttpStatus.OK)
                .message("Practices reordered successfully")
                .data("Reorder completed")
                .build());
    }

    @Operation(summary = "Create multiple practices", description = "Creates multiple practices for a doctor in batch")
    @PostMapping("/doctor/{doctorId}/batch")
    @PreAuthorize("@doctorSecurity.isDoctorOwner(authentication, #doctorId) or hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<List<PracticeResponseDto>>> createPracticesBatch(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID doctorId,
            @Valid @RequestBody List<PracticeRequestDto> requestDtos) {
        List<PracticeResponseDto> practices = practiceService.createBatch(doctorId, requestDtos);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseModel.<List<PracticeResponseDto>>builder()
                        .status(HttpStatus.CREATED)
                        .message("Practices created successfully")
                        .data(practices)
                        .build());
    }

    @Operation(summary = "Delete practices by doctor", description = "Deletes all practices for a specific doctor")
    @DeleteMapping("/doctor/{doctorId}")
    @PreAuthorize("@doctorSecurity.isDoctorOwner(authentication, #doctorId) or hasRole('ADMIN')")
    public ResponseEntity<Void> deletePracticesByDoctor(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID doctorId) {
        practiceService.deleteByDoctorId(doctorId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get practice statistics", description = "Gets statistics about practices")
    @GetMapping("/statistics/count")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<Long>> getPracticeCount() {
        long count = practiceService.countAll();
        return ResponseEntity.ok(ResponseModel.<Long>builder()
                .status(HttpStatus.OK)
                .message("Practice count retrieved successfully")
                .data(count)
                .build());
    }

    @Operation(summary = "Check if slug is available", description = "Checks if a practice slug is available")
    @GetMapping("/slug/{slug}/available")
    public ResponseEntity<ResponseModel<Boolean>> isSlugAvailable(
            @Parameter(description = "Practice slug", required = true) @PathVariable String slug) {
        boolean isAvailable = practiceService.isSlugAvailable(slug);
        return ResponseEntity.ok(ResponseModel.<Boolean>builder()
                .status(HttpStatus.OK)
                .message("Slug availability checked")
                .data(isAvailable)
                .build());
    }
}
