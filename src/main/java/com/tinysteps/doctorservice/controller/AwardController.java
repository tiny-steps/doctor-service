package com.tinysteps.doctorservice.controller;

import com.tinysteps.doctorservice.model.AwardRequestDto;
import com.tinysteps.doctorservice.model.AwardResponseDto;
import com.tinysteps.doctorservice.model.ResponseModel;
import com.tinysteps.doctorservice.service.AwardService;
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
@RequestMapping("/api/v1/awards")
@RequiredArgsConstructor
@Tag(name = "Award Management", description = "APIs for managing doctor awards and recognitions")
@SecurityRequirement(name = "Bearer Authentication")
public class AwardController {

    private final AwardService awardService;

    @Operation(summary = "Create a new award", description = "Creates a new award for a doctor")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Award created successfully",
                    content = @Content(schema = @Schema(implementation = AwardResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PostMapping("/doctor/{doctorId}")
    @PreAuthorize("@doctorSecurity.isDoctorOwner(authentication, #doctorId) or hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<AwardResponseDto>> createAward(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID doctorId,
            @Valid @RequestBody AwardRequestDto requestDto) {
        AwardResponseDto award = awardService.create(doctorId, requestDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseModel.<AwardResponseDto>builder()
                        .status(HttpStatus.CREATED)
                        .message("Award created successfully")
                        .data(award)
                        .build());
    }

    @Operation(summary = "Get award by ID", description = "Retrieves an award by its unique identifier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Award found",
                    content = @Content(schema = @Schema(implementation = AwardResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Award not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ResponseModel<AwardResponseDto>> getAwardById(
            @Parameter(description = "Award ID", required = true) @PathVariable UUID id) {
        AwardResponseDto award = awardService.findById(id);
        return ResponseEntity.ok(ResponseModel.<AwardResponseDto>builder()
                .status(HttpStatus.OK)
                .message("Award retrieved successfully")
                .data(award)
                .build());
    }

    @Operation(summary = "Get all awards", description = "Retrieves a paginated list of all awards")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Awards retrieved successfully")
    })
    @GetMapping
    public ResponseEntity<ResponseModel<Page<AwardResponseDto>>> getAllAwards(
            @Parameter(description = "Pagination information") Pageable pageable) {
        Page<AwardResponseDto> awards = awardService.findAll(pageable);
        return ResponseEntity.ok(ResponseModel.<Page<AwardResponseDto>>builder()
                .status(HttpStatus.OK)
                .message("Awards retrieved successfully")
                .data(awards)
                .build());
    }

    @Operation(summary = "Get awards by doctor", description = "Retrieves all awards for a specific doctor")
    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<ResponseModel<List<AwardResponseDto>>> getAwardsByDoctor(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID doctorId) {
        List<AwardResponseDto> awards = awardService.findByDoctorId(doctorId);
        return ResponseEntity.ok(ResponseModel.<List<AwardResponseDto>>builder()
                .status(HttpStatus.OK)
                .message("Doctor awards retrieved successfully")
                .data(awards)
                .build());
    }

    @Operation(summary = "Get awards by doctor (ordered by year)", description = "Retrieves awards for a doctor ordered by year")
    @GetMapping("/doctor/{doctorId}/ordered")
    public ResponseEntity<ResponseModel<List<AwardResponseDto>>> getAwardsByDoctorOrderedByYear(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID doctorId) {
        List<AwardResponseDto> awards = awardService.findByDoctorIdOrderByYear(doctorId);
        return ResponseEntity.ok(ResponseModel.<List<AwardResponseDto>>builder()
                .status(HttpStatus.OK)
                .message("Doctor awards retrieved successfully")
                .data(awards)
                .build());
    }

    @Operation(summary = "Update award", description = "Updates an award with new information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Award updated successfully"),
            @ApiResponse(responseCode = "404", description = "Award not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PutMapping("/{id}")
    @PreAuthorize("@doctorSecurity.isAwardOwner(authentication, #id) or hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<AwardResponseDto>> updateAward(
            @Parameter(description = "Award ID", required = true) @PathVariable UUID id,
            @Valid @RequestBody AwardRequestDto requestDto) {
        AwardResponseDto award = awardService.update(id, requestDto);
        return ResponseEntity.ok(ResponseModel.<AwardResponseDto>builder()
                .status(HttpStatus.OK)
                .message("Award updated successfully")
                .data(award)
                .build());
    }

    @Operation(summary = "Partially update award", description = "Partially updates an award")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Award updated successfully"),
            @ApiResponse(responseCode = "404", description = "Award not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PatchMapping("/{id}")
    @PreAuthorize("@doctorSecurity.isAwardOwner(authentication, #id) or hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<AwardResponseDto>> partialUpdateAward(
            @Parameter(description = "Award ID", required = true) @PathVariable UUID id,
            @RequestBody AwardRequestDto requestDto) {
        AwardResponseDto award = awardService.partialUpdate(id, requestDto);
        return ResponseEntity.ok(ResponseModel.<AwardResponseDto>builder()
                .status(HttpStatus.OK)
                .message("Award updated successfully")
                .data(award)
                .build());
    }

    @Operation(summary = "Delete award", description = "Deletes an award")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Award deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Award not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("@doctorSecurity.isAwardOwner(authentication, #id) or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteAward(
            @Parameter(description = "Award ID", required = true) @PathVariable UUID id) {
        awardService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Search awards by title", description = "Searches for awards by title")
    @GetMapping("/search/title")
    public ResponseEntity<ResponseModel<Page<AwardResponseDto>>> searchAwardsByTitle(
            @Parameter(description = "Award title to search") @RequestParam String title,
            @Parameter(description = "Pagination information") Pageable pageable) {
        Page<AwardResponseDto> awards = awardService.findByTitle(title, pageable);
        return ResponseEntity.ok(ResponseModel.<Page<AwardResponseDto>>builder()
                .status(HttpStatus.OK)
                .message("Awards found")
                .data(awards)
                .build());
    }

    @Operation(summary = "Search awards by year", description = "Searches for awards by year")
    @GetMapping("/search/year/{year}")
    public ResponseEntity<ResponseModel<Page<AwardResponseDto>>> searchAwardsByYear(
            @Parameter(description = "Award year") @PathVariable Integer year,
            @Parameter(description = "Pagination information") Pageable pageable) {
        Page<AwardResponseDto> awards = awardService.findByAwardedYear(year, pageable);
        return ResponseEntity.ok(ResponseModel.<Page<AwardResponseDto>>builder()
                .status(HttpStatus.OK)
                .message("Awards found")
                .data(awards)
                .build());
    }

    @Operation(summary = "Search awards by year range", description = "Searches for awards within a year range")
    @GetMapping("/search/year-range")
    public ResponseEntity<ResponseModel<Page<AwardResponseDto>>> searchAwardsByYearRange(
            @Parameter(description = "Start year") @RequestParam Integer startYear,
            @Parameter(description = "End year") @RequestParam Integer endYear,
            @Parameter(description = "Pagination information") Pageable pageable) {
        Page<AwardResponseDto> awards = awardService.findByAwardedYearRange(startYear, endYear, pageable);
        return ResponseEntity.ok(ResponseModel.<Page<AwardResponseDto>>builder()
                .status(HttpStatus.OK)
                .message("Awards found")
                .data(awards)
                .build());
    }

    @Operation(summary = "Get recent awards", description = "Retrieves recent awards from a specific year onwards")
    @GetMapping("/recent")
    public ResponseEntity<ResponseModel<Page<AwardResponseDto>>> getRecentAwards(
            @Parameter(description = "Start year for recent awards") @RequestParam Integer startYear,
            @Parameter(description = "Pagination information") Pageable pageable) {
        Page<AwardResponseDto> awards = awardService.findRecentAwards(startYear, pageable);
        return ResponseEntity.ok(ResponseModel.<Page<AwardResponseDto>>builder()
                .status(HttpStatus.OK)
                .message("Recent awards retrieved successfully")
                .data(awards)
                .build());
    }

    @Operation(summary = "Create multiple awards", description = "Creates multiple awards for a doctor in batch")
    @PostMapping("/doctor/{doctorId}/batch")
    @PreAuthorize("@doctorSecurity.isDoctorOwner(authentication, #doctorId) or hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<List<AwardResponseDto>>> createAwardsBatch(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID doctorId,
            @Valid @RequestBody List<AwardRequestDto> requestDtos) {
        List<AwardResponseDto> awards = awardService.createBatch(doctorId, requestDtos);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseModel.<List<AwardResponseDto>>builder()
                        .status(HttpStatus.CREATED)
                        .message("Awards created successfully")
                        .data(awards)
                        .build());
    }

    @Operation(summary = "Delete awards by doctor", description = "Deletes all awards for a specific doctor")
    @DeleteMapping("/doctor/{doctorId}")
    @PreAuthorize("@doctorSecurity.isDoctorOwner(authentication, #doctorId) or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteAwardsByDoctor(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID doctorId) {
        awardService.deleteByDoctorId(doctorId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get award statistics", description = "Gets statistics about awards")
    @GetMapping("/statistics/count")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<Long>> getAwardCount() {
        long count = awardService.countAll();
        return ResponseEntity.ok(ResponseModel.<Long>builder()
                .status(HttpStatus.OK)
                .message("Award count retrieved successfully")
                .data(count)
                .build());
    }

    @Operation(summary = "Check if doctor has award in year", description = "Checks if a doctor received an award in a specific year")
    @GetMapping("/doctor/{doctorId}/year/{year}/exists")
    public ResponseEntity<ResponseModel<Boolean>> hasDoctorReceivedAwardInYear(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID doctorId,
            @Parameter(description = "Year", required = true) @PathVariable Integer year) {
        boolean hasAward = awardService.hasDoctorReceivedAwardInYear(doctorId, year);
        return ResponseEntity.ok(ResponseModel.<Boolean>builder()
                .status(HttpStatus.OK)
                .message("Award check completed")
                .data(hasAward)
                .build());
    }
}
