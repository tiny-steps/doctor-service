package com.tinysteps.doctorservice.controller;

import com.tinysteps.doctorservice.model.QualificationRequestDto;
import com.tinysteps.doctorservice.model.QualificationResponseDto;
import com.tinysteps.doctorservice.model.ResponseModel;
import com.tinysteps.doctorservice.service.QualificationService;
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
@RequestMapping("/api/v1/qualifications")
@RequiredArgsConstructor
@Tag(name = "Qualification Management", description = "APIs for managing doctor qualifications and education")
@SecurityRequirement(name = "Bearer Authentication")
public class QualificationController {

    private final QualificationService qualificationService;

    @Operation(summary = "Create a new qualification", description = "Creates a new qualification for a doctor")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Qualification created successfully",
                    content = @Content(schema = @Schema(implementation = QualificationResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PostMapping("/doctor/{doctorId}")
    @PreAuthorize("@doctorSecurity.isDoctorOwner(authentication, #doctorId) or hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<QualificationResponseDto>> createQualification(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID doctorId,
            @Valid @RequestBody QualificationRequestDto requestDto) {
        QualificationResponseDto qualification = qualificationService.create(doctorId, requestDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseModel.<QualificationResponseDto>builder()
                        .status(HttpStatus.CREATED)
                        .message("Qualification created successfully")
                        .data(qualification)
                        .build());
    }

    @Operation(summary = "Get qualification by ID", description = "Retrieves a qualification by its unique identifier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Qualification found",
                    content = @Content(schema = @Schema(implementation = QualificationResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Qualification not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ResponseModel<QualificationResponseDto>> getQualificationById(
            @Parameter(description = "Qualification ID", required = true) @PathVariable UUID id) {
        QualificationResponseDto qualification = qualificationService.findById(id);
        return ResponseEntity.ok(ResponseModel.<QualificationResponseDto>builder()
                .status(HttpStatus.OK)
                .message("Qualification retrieved successfully")
                .data(qualification)
                .build());
    }

    @Operation(summary = "Get all qualifications", description = "Retrieves a paginated list of all qualifications")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Qualifications retrieved successfully")
    })
    @GetMapping
    public ResponseEntity<ResponseModel<Page<QualificationResponseDto>>> getAllQualifications(
            @Parameter(description = "Pagination information") Pageable pageable) {
        Page<QualificationResponseDto> qualifications = qualificationService.findAll(pageable);
        return ResponseEntity.ok(ResponseModel.<Page<QualificationResponseDto>>builder()
                .status(HttpStatus.OK)
                .message("Qualifications retrieved successfully")
                .data(qualifications)
                .build());
    }

    @Operation(summary = "Get qualifications by doctor", description = "Retrieves all qualifications for a specific doctor")
    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<ResponseModel<List<QualificationResponseDto>>> getQualificationsByDoctor(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID doctorId) {
        List<QualificationResponseDto> qualifications = qualificationService.findByDoctorId(doctorId);
        return ResponseEntity.ok(ResponseModel.<List<QualificationResponseDto>>builder()
                .status(HttpStatus.OK)
                .message("Doctor qualifications retrieved successfully")
                .data(qualifications)
                .build());
    }

    @Operation(summary = "Get qualifications by doctor (ordered by year)", description = "Retrieves qualifications for a doctor ordered by year")
    @GetMapping("/doctor/{doctorId}/ordered")
    public ResponseEntity<ResponseModel<List<QualificationResponseDto>>> getQualificationsByDoctorOrderedByYear(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID doctorId) {
        List<QualificationResponseDto> qualifications = qualificationService.findByDoctorIdOrderByYear(doctorId);
        return ResponseEntity.ok(ResponseModel.<List<QualificationResponseDto>>builder()
                .status(HttpStatus.OK)
                .message("Doctor qualifications retrieved successfully")
                .data(qualifications)
                .build());
    }

    @Operation(summary = "Update qualification", description = "Updates a qualification with new information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Qualification updated successfully"),
            @ApiResponse(responseCode = "404", description = "Qualification not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PutMapping("/{id}")
    @PreAuthorize("@doctorSecurity.isQualificationOwner(authentication, #id) or hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<QualificationResponseDto>> updateQualification(
            @Parameter(description = "Qualification ID", required = true) @PathVariable UUID id,
            @Valid @RequestBody QualificationRequestDto requestDto) {
        QualificationResponseDto qualification = qualificationService.update(id, requestDto);
        return ResponseEntity.ok(ResponseModel.<QualificationResponseDto>builder()
                .status(HttpStatus.OK)
                .message("Qualification updated successfully")
                .data(qualification)
                .build());
    }

    @Operation(summary = "Partially update qualification", description = "Partially updates a qualification")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Qualification updated successfully"),
            @ApiResponse(responseCode = "404", description = "Qualification not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PatchMapping("/{id}")
    @PreAuthorize("@doctorSecurity.isQualificationOwner(authentication, #id) or hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<QualificationResponseDto>> partialUpdateQualification(
            @Parameter(description = "Qualification ID", required = true) @PathVariable UUID id,
            @RequestBody QualificationRequestDto requestDto) {
        QualificationResponseDto qualification = qualificationService.partialUpdate(id, requestDto);
        return ResponseEntity.ok(ResponseModel.<QualificationResponseDto>builder()
                .status(HttpStatus.OK)
                .message("Qualification updated successfully")
                .data(qualification)
                .build());
    }

    @Operation(summary = "Delete qualification", description = "Deletes a qualification")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Qualification deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Qualification not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("@doctorSecurity.isQualificationOwner(authentication, #id) or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteQualification(
            @Parameter(description = "Qualification ID", required = true) @PathVariable UUID id) {
        qualificationService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Search qualifications by name", description = "Searches for qualifications by qualification name")
    @GetMapping("/search/name")
    public ResponseEntity<ResponseModel<Page<QualificationResponseDto>>> searchQualificationsByName(
            @Parameter(description = "Qualification name to search") @RequestParam String qualificationName,
            @Parameter(description = "Pagination information") Pageable pageable) {
        Page<QualificationResponseDto> qualifications = qualificationService.findByQualificationName(qualificationName, pageable);
        return ResponseEntity.ok(ResponseModel.<Page<QualificationResponseDto>>builder()
                .status(HttpStatus.OK)
                .message("Qualifications found")
                .data(qualifications)
                .build());
    }

    @Operation(summary = "Search qualifications by college", description = "Searches for qualifications by college name")
    @GetMapping("/search/college")
    public ResponseEntity<ResponseModel<Page<QualificationResponseDto>>> searchQualificationsByCollege(
            @Parameter(description = "College name to search") @RequestParam String collegeName,
            @Parameter(description = "Pagination information") Pageable pageable) {
        Page<QualificationResponseDto> qualifications = qualificationService.findByCollegeName(collegeName, pageable);
        return ResponseEntity.ok(ResponseModel.<Page<QualificationResponseDto>>builder()
                .status(HttpStatus.OK)
                .message("Qualifications found")
                .data(qualifications)
                .build());
    }

    @Operation(summary = "Search qualifications by year", description = "Searches for qualifications by year")
    @GetMapping("/search/year/{year}")
    public ResponseEntity<ResponseModel<Page<QualificationResponseDto>>> searchQualificationsByYear(
            @Parameter(description = "Qualification year") @PathVariable Integer year,
            @Parameter(description = "Pagination information") Pageable pageable) {
        Page<QualificationResponseDto> qualifications = qualificationService.findByCompletionYear(year, pageable);
        return ResponseEntity.ok(ResponseModel.<Page<QualificationResponseDto>>builder()
                .status(HttpStatus.OK)
                .message("Qualifications found")
                .data(qualifications)
                .build());
    }

    @Operation(summary = "Get unique qualification names", description = "Retrieves all unique qualification names")
    @GetMapping("/unique/names")
    public ResponseEntity<ResponseModel<List<String>>> getUniqueQualificationNames() {
        List<String> names = qualificationService.getUniqueQualificationNames();
        return ResponseEntity.ok(ResponseModel.<List<String>>builder()
                .status(HttpStatus.OK)
                .message("Unique qualification names retrieved successfully")
                .data(names)
                .build());
    }

    @Operation(summary = "Get unique college names", description = "Retrieves all unique college names")
    @GetMapping("/unique/colleges")
    public ResponseEntity<ResponseModel<List<String>>> getUniqueCollegeNames() {
        List<String> colleges = qualificationService.getUniqueCollegeNames();
        return ResponseEntity.ok(ResponseModel.<List<String>>builder()
                .status(HttpStatus.OK)
                .message("Unique college names retrieved successfully")
                .data(colleges)
                .build());
    }

    @Operation(summary = "Create multiple qualifications", description = "Creates multiple qualifications for a doctor in batch")
    @PostMapping("/doctor/{doctorId}/batch")
    @PreAuthorize("@doctorSecurity.isDoctorOwner(authentication, #doctorId) or hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<List<QualificationResponseDto>>> createQualificationsBatch(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID doctorId,
            @Valid @RequestBody List<QualificationRequestDto> requestDtos) {
        List<QualificationResponseDto> qualifications = qualificationService.createBatch(doctorId, requestDtos);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseModel.<List<QualificationResponseDto>>builder()
                        .status(HttpStatus.CREATED)
                        .message("Qualifications created successfully")
                        .data(qualifications)
                        .build());
    }

    @Operation(summary = "Delete qualifications by doctor", description = "Deletes all qualifications for a specific doctor")
    @DeleteMapping("/doctor/{doctorId}")
    @PreAuthorize("@doctorSecurity.isDoctorOwner(authentication, #doctorId) or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteQualificationsByDoctor(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID doctorId) {
        qualificationService.deleteByDoctorId(doctorId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get qualification statistics", description = "Gets statistics about qualifications")
    @GetMapping("/statistics/count")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<Long>> getQualificationCount() {
        long count = qualificationService.countAll();
        return ResponseEntity.ok(ResponseModel.<Long>builder()
                .status(HttpStatus.OK)
                .message("Qualification count retrieved successfully")
                .data(count)
                .build());
    }

    @Operation(summary = "Check if doctor has qualification", description = "Checks if a doctor has a specific qualification")
    @GetMapping("/doctor/{doctorId}/has-qualification")
    public ResponseEntity<ResponseModel<Boolean>> hasDoctorQualification(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID doctorId,
            @Parameter(description = "Qualification name", required = true) @RequestParam String qualificationName) {
        boolean hasQualification = qualificationService.hasDoctorQualification(doctorId, qualificationName);
        return ResponseEntity.ok(ResponseModel.<Boolean>builder()
                .status(HttpStatus.OK)
                .message("Qualification check completed")
                .data(hasQualification)
                .build());
    }

    @Operation(summary = "Check if doctor is qualified in field", description = "Checks if a doctor is qualified in a specific field")
    @GetMapping("/doctor/{doctorId}/qualified-in-field")
    public ResponseEntity<ResponseModel<Boolean>> isDoctorQualifiedInField(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID doctorId,
            @Parameter(description = "Field name", required = true) @RequestParam String field) {
        boolean isQualified = qualificationService.isDoctorQualifiedInField(doctorId, field);
        return ResponseEntity.ok(ResponseModel.<Boolean>builder()
                .status(HttpStatus.OK)
                .message("Field qualification check completed")
                .data(isQualified)
                .build());
    }
}
