package com.tinysteps.doctorsevice.controller;

import com.tinysteps.doctorsevice.model.DoctorDto;
import com.tinysteps.doctorsevice.model.DoctorRequestDto;
import com.tinysteps.doctorsevice.model.DoctorResponseDto;
import com.tinysteps.doctorsevice.model.ResponseModel;
import com.tinysteps.doctorsevice.service.DoctorService;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/doctors")
@RequiredArgsConstructor
@Tag(name = "Doctor Management", description = "APIs for managing doctor profiles and information")
@SecurityRequirement(name = "Bearer Authentication")
public class DoctorController {

    private final DoctorService doctorService;

    @Operation(summary = "Create a new doctor profile", description = "Creates a new doctor profile with the provided information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Doctor profile created successfully",
                    content = @Content(schema = @Schema(implementation = DoctorResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "409", description = "Doctor already exists")
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('PATIENT','DOCTOR','ADMIN')")
    public ResponseEntity<ResponseModel<DoctorResponseDto>> createDoctor(
            @Valid @RequestBody DoctorRequestDto requestDto) {
        DoctorResponseDto doctor = doctorService.create(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseModel.<DoctorResponseDto>builder()
                        .status(HttpStatus.CREATED)
                        .message("Doctor profile created successfully")
                        .data(doctor)
                        .build());
    }

    @PostMapping("/register")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<ResponseModel<DoctorResponseDto>> registerDoctor(
            @Valid @RequestBody DoctorDto requestDto) {
        DoctorResponseDto createdDoctor = doctorService.registerDoctor(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseModel.<DoctorResponseDto>builder()
                        .status(HttpStatus.CREATED)
                        .message("Doctor profile created successfully")
                        .data(createdDoctor)
                        .build());
    }

    @Operation(summary = "Get doctor by ID", description = "Retrieves a doctor profile by their unique identifier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Doctor found",
                    content = @Content(schema = @Schema(implementation = DoctorResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Doctor not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ResponseModel<DoctorResponseDto>> getDoctorById(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID id) {
        DoctorResponseDto doctor = doctorService.findById(id);
        return ResponseEntity.ok(ResponseModel.<DoctorResponseDto>builder()
                .status(HttpStatus.OK)
                .message("Doctor retrieved successfully")
                .data(doctor)
                .build());
    }

    @Operation(summary = "Get all doctors", description = "Retrieves a paginated list of all doctors")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Doctors retrieved successfully")
    })
    @GetMapping
    public ResponseEntity<ResponseModel<Page<DoctorResponseDto>>> getAllDoctors(
            @Parameter(description = "Pagination information") Pageable pageable) {
        Page<DoctorResponseDto> doctors = doctorService.findAll(pageable);
        return ResponseEntity.ok(ResponseModel.<Page<DoctorResponseDto>>builder()
                .status(HttpStatus.OK)
                .message("Doctors retrieved successfully")
                .data(doctors)
                .build());
    }

    @Operation(summary = "Update doctor profile", description = "Updates a doctor profile with new information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Doctor updated successfully"),
            @ApiResponse(responseCode = "404", description = "Doctor not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PutMapping("/{id}")
    @PreAuthorize("@doctorSecurity.isDoctorOwner(authentication, #id) or hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<DoctorResponseDto>> updateDoctor(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID id,
            @Valid @RequestBody DoctorRequestDto requestDto) {
        DoctorResponseDto doctor = doctorService.update(id, requestDto);
        return ResponseEntity.ok(ResponseModel.<DoctorResponseDto>builder()
                .status(HttpStatus.OK)
                .message("Doctor updated successfully")
                .data(doctor)
                .build());
    }

    @Operation(summary = "Partially update doctor profile", description = "Partially updates a doctor profile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Doctor updated successfully"),
            @ApiResponse(responseCode = "404", description = "Doctor not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PatchMapping("/{id}")
    @PreAuthorize("@doctorSecurity.isDoctorOwner(authentication, #id) or hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<DoctorResponseDto>> partialUpdateDoctor(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID id,
            @RequestBody DoctorRequestDto requestDto) {
        DoctorResponseDto doctor = doctorService.partialUpdate(id, requestDto);
        return ResponseEntity.ok(ResponseModel.<DoctorResponseDto>builder()
                .status(HttpStatus.OK)
                .message("Doctor updated successfully")
                .data(doctor)
                .build());
    }

    @Operation(summary = "Delete doctor profile", description = "Deletes a doctor profile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Doctor deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Doctor not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("@doctorSecurity.isDoctorOwner(authentication, #id) or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteDoctor(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID id) {
        doctorService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get doctor by slug", description = "Retrieves a doctor profile by their unique slug")
    @GetMapping("/slug/{slug}")
    public ResponseEntity<ResponseModel<DoctorResponseDto>> getDoctorBySlug(
            @Parameter(description = "Doctor slug", required = true) @PathVariable String slug) {
        DoctorResponseDto doctor = doctorService.findBySlug(slug);
        return ResponseEntity.ok(ResponseModel.<DoctorResponseDto>builder()
                .status(HttpStatus.OK)
                .message("Doctor retrieved successfully")
                .data(doctor)
                .build());
    }

    @Operation(summary = "Get doctor by user ID", description = "Retrieves a doctor profile by their user ID")
    @GetMapping("/user/{userId}")
    public ResponseEntity<ResponseModel<DoctorResponseDto>> getDoctorByUserId(
            @Parameter(description = "User ID", required = true) @PathVariable UUID userId) {
        DoctorResponseDto doctor = doctorService.findByUserId(userId);
        return ResponseEntity.ok(ResponseModel.<DoctorResponseDto>builder()
                .status(HttpStatus.OK)
                .message("Doctor retrieved successfully")
                .data(doctor)
                .build());
    }

    @Operation(summary = "Search doctors by name", description = "Searches for doctors by name")
    @GetMapping("/search/name")
    public ResponseEntity<ResponseModel<List<DoctorResponseDto>>> searchDoctorsByName(
            @Parameter(description = "Doctor name to search") @RequestParam String name) {
        List<DoctorResponseDto> doctors = doctorService.findByName(name);
        return ResponseEntity.ok(ResponseModel.<List<DoctorResponseDto>>builder()
                .status(HttpStatus.OK)
                .message("Doctors found")
                .data(doctors)
                .build());
    }

    @Operation(summary = "Search doctors", description = "Advanced search for doctors with multiple criteria")
    @GetMapping("/search")
    public ResponseEntity<ResponseModel<Page<DoctorResponseDto>>> searchDoctors(
            @Parameter(description = "Doctor name") @RequestParam(required = false) String name,
            @Parameter(description = "Speciality") @RequestParam(required = false) String speciality,
            @Parameter(description = "Verification status") @RequestParam(required = false) Boolean isVerified,
            @Parameter(description = "Minimum rating") @RequestParam(required = false) BigDecimal minRating,
            @Parameter(description = "Pagination information") Pageable pageable) {
        Page<DoctorResponseDto> doctors = doctorService.searchDoctors(name, speciality, isVerified, minRating, pageable);
        return ResponseEntity.ok(ResponseModel.<Page<DoctorResponseDto>>builder()
                .status(HttpStatus.OK)
                .message("Search completed successfully")
                .data(doctors)
                .build());
    }

    @Operation(summary = "Get top rated doctors", description = "Retrieves top rated doctors")
    @GetMapping("/top-rated")
    public ResponseEntity<ResponseModel<Page<DoctorResponseDto>>> getTopRatedDoctors(
            @Parameter(description = "Pagination information") Pageable pageable) {
        Page<DoctorResponseDto> doctors = doctorService.findTopRatedDoctors(pageable);
        return ResponseEntity.ok(ResponseModel.<Page<DoctorResponseDto>>builder()
                .status(HttpStatus.OK)
                .message("Top rated doctors retrieved successfully")
                .data(doctors)
                .build());
    }


    @Operation(summary = "Get profile completeness", description = "Calculates the profile completeness percentage")
    @GetMapping("/{id}/profile-completeness")
    @PreAuthorize("@doctorSecurity.isDoctorOwner(authentication, #id) or hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<Integer>> getProfileCompleteness(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID id) {
        int completeness = doctorService.calculateProfileCompleteness(id);
        return ResponseEntity.ok(ResponseModel.<Integer>builder()
                .status(HttpStatus.OK)
                .message("Profile completeness calculated successfully")
                .data(completeness)
                .build());
    }

    @Operation(summary = "Get missing profile fields", description = "Gets list of missing profile fields")
    @GetMapping("/{id}/missing-fields")
    @PreAuthorize("@doctorSecurity.isDoctorOwner(authentication, #id) or hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<List<String>>> getMissingProfileFields(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID id) {
        List<String> missingFields = doctorService.getMissingProfileFields(id);
        return ResponseEntity.ok(ResponseModel.<List<String>>builder()
                .status(HttpStatus.OK)
                .message("Missing profile fields retrieved successfully")
                .data(missingFields)
                .build());
    }

    @Operation(summary = "Get doctor statistics", description = "Gets various statistics about doctors")
    @GetMapping("/statistics/count")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<Long>> getDoctorCount() {
        long count = doctorService.countAll();
        return ResponseEntity.ok(ResponseModel.<Long>builder()
                .status(HttpStatus.OK)
                .message("Doctor count retrieved successfully")
                .data(count)
                .build());
    }


    @Operation(summary = "Get doctors by status", description = "Retrieves doctors by status")
    @GetMapping("/status/{status}")
    public ResponseEntity<ResponseModel<Page<DoctorResponseDto>>> getDoctorsByStatus(
            @Parameter(description = "Doctor status", required = true) @PathVariable String status,
            @Parameter(description = "Pagination information") Pageable pageable) {
        Page<DoctorResponseDto> doctors = doctorService.findByStatus(status, pageable);
        return ResponseEntity.ok(ResponseModel.<Page<DoctorResponseDto>>builder()
                .status(HttpStatus.OK)
                .message("Doctors retrieved successfully")
                .data(doctors)
                .build());
    }

    @Operation(summary = "Get doctors by verification status", description = "Retrieves doctors by verification status")
    @GetMapping("/verification/{isVerified}")
    public ResponseEntity<ResponseModel<Page<DoctorResponseDto>>> getDoctorsByVerificationStatus(
            @Parameter(description = "Verification status", required = true) @PathVariable Boolean isVerified,
            @Parameter(description = "Pagination information") Pageable pageable) {
        Page<DoctorResponseDto> doctors = doctorService.findByVerificationStatus(isVerified, pageable);
        return ResponseEntity.ok(ResponseModel.<Page<DoctorResponseDto>>builder()
                .status(HttpStatus.OK)
                .message("Doctors retrieved successfully")
                .data(doctors)
                .build());
    }

    @Operation(summary = "Get doctors by gender", description = "Retrieves doctors by gender")
    @GetMapping("/gender/{gender}")
    public ResponseEntity<ResponseModel<Page<DoctorResponseDto>>> getDoctorsByGender(
            @Parameter(description = "Doctor gender", required = true) @PathVariable String gender,
            @Parameter(description = "Pagination information") Pageable pageable) {
        Page<DoctorResponseDto> doctors = doctorService.findByGender(gender, pageable);
        return ResponseEntity.ok(ResponseModel.<Page<DoctorResponseDto>>builder()
                .status(HttpStatus.OK)
                .message("Doctors retrieved successfully")
                .data(doctors)
                .build());
    }

    @Operation(summary = "Get doctors by experience range", description = "Retrieves doctors by experience range")
    @GetMapping("/experience-range")
    public ResponseEntity<ResponseModel<Page<DoctorResponseDto>>> getDoctorsByExperienceRange(
            @Parameter(description = "Minimum years of experience") @RequestParam Integer minYears,
            @Parameter(description = "Maximum years of experience") @RequestParam Integer maxYears,
            @Parameter(description = "Pagination information") Pageable pageable) {
        Page<DoctorResponseDto> doctors = doctorService.findByExperienceRange(minYears, maxYears, pageable);
        return ResponseEntity.ok(ResponseModel.<Page<DoctorResponseDto>>builder()
                .status(HttpStatus.OK)
                .message("Doctors retrieved successfully")
                .data(doctors)
                .build());
    }

    @Operation(summary = "Get doctors by minimum rating", description = "Retrieves doctors with minimum rating")
    @GetMapping("/min-rating/{minRating}")
    public ResponseEntity<ResponseModel<Page<DoctorResponseDto>>> getDoctorsByMinRating(
            @Parameter(description = "Minimum rating", required = true) @PathVariable BigDecimal minRating,
            @Parameter(description = "Pagination information") Pageable pageable) {
        Page<DoctorResponseDto> doctors = doctorService.findByMinRating(minRating, pageable);
        return ResponseEntity.ok(ResponseModel.<Page<DoctorResponseDto>>builder()
                .status(HttpStatus.OK)
                .message("Doctors retrieved successfully")
                .data(doctors)
                .build());
    }

    @Operation(summary = "Get doctors by speciality", description = "Retrieves doctors by speciality")
    @GetMapping("/speciality/{speciality}")
    public ResponseEntity<ResponseModel<Page<DoctorResponseDto>>> getDoctorsBySpeciality(
            @Parameter(description = "Doctor speciality", required = true) @PathVariable String speciality,
            @Parameter(description = "Pagination information") Pageable pageable) {
        Page<DoctorResponseDto> doctors = doctorService.findBySpeciality(speciality, pageable);
        return ResponseEntity.ok(ResponseModel.<Page<DoctorResponseDto>>builder()
                .status(HttpStatus.OK)
                .message("Doctors retrieved successfully")
                .data(doctors)
                .build());
    }

    @Operation(summary = "Get doctors by location", description = "Retrieves doctors by location")
    @GetMapping("/location/{addressId}")
    public ResponseEntity<ResponseModel<Page<DoctorResponseDto>>> getDoctorsByLocation(
            @Parameter(description = "Address ID", required = true) @PathVariable UUID addressId,
            @Parameter(description = "Pagination information") Pageable pageable) {
        Page<DoctorResponseDto> doctors = doctorService.findByLocation(addressId, pageable);
        return ResponseEntity.ok(ResponseModel.<Page<DoctorResponseDto>>builder()
                .status(HttpStatus.OK)
                .message("Doctors retrieved successfully")
                .data(doctors)
                .build());
    }

    @Operation(summary = "Get doctors by location and practice role", description = "Retrieves doctors by location and practice role")
    @GetMapping("/location/{addressId}/role/{practiceRole}")
    public ResponseEntity<ResponseModel<Page<DoctorResponseDto>>> getDoctorsByLocationAndPracticeRole(
            @Parameter(description = "Address ID", required = true) @PathVariable UUID addressId,
            @Parameter(description = "Practice role", required = true) @PathVariable String practiceRole,
            @Parameter(description = "Pagination information") Pageable pageable) {
        Page<DoctorResponseDto> doctors = doctorService.findByLocationAndPracticeRole(addressId, practiceRole, pageable);
        return ResponseEntity.ok(ResponseModel.<Page<DoctorResponseDto>>builder()
                .status(HttpStatus.OK)
                .message("Doctors retrieved successfully")
                .data(doctors)
                .build());
    }

    @Operation(summary = "Get verified doctors with minimum rating", description = "Retrieves verified doctors with minimum rating")
    @GetMapping("/verified/min-rating/{minRating}")
    public ResponseEntity<ResponseModel<Page<DoctorResponseDto>>> getVerifiedDoctorsWithMinRating(
            @Parameter(description = "Minimum rating", required = true) @PathVariable BigDecimal minRating,
            @Parameter(description = "Pagination information") Pageable pageable) {
        Page<DoctorResponseDto> doctors = doctorService.findVerifiedDoctorsWithMinRating(minRating, pageable);
        return ResponseEntity.ok(ResponseModel.<Page<DoctorResponseDto>>builder()
                .status(HttpStatus.OK)
                .message("Verified doctors with minimum rating retrieved successfully")
                .data(doctors)
                .build());
    }

    @Operation(summary = "Verify doctor", description = "Verifies a doctor")
    @PostMapping("/{id}/verify")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<DoctorResponseDto>> verifyDoctor(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID id) {
        DoctorResponseDto doctor = doctorService.verifyDoctor(id);
        return ResponseEntity.ok(ResponseModel.<DoctorResponseDto>builder()
                .status(HttpStatus.OK)
                .message("Doctor verified successfully")
                .data(doctor)
                .build());
    }

    @Operation(summary = "Unverify doctor", description = "Removes verification from a doctor")
    @PostMapping("/{id}/unverify")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<DoctorResponseDto>> unverifyDoctor(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID id) {
        DoctorResponseDto doctor = doctorService.unverifyDoctor(id);
        return ResponseEntity.ok(ResponseModel.<DoctorResponseDto>builder()
                .status(HttpStatus.OK)
                .message("Doctor unverified successfully")
                .data(doctor)
                .build());
    }

    @Operation(summary = "Activate doctor", description = "Activates a doctor")
    @PostMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<DoctorResponseDto>> activateDoctor(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID id) {
        DoctorResponseDto doctor = doctorService.activateDoctor(id);
        return ResponseEntity.ok(ResponseModel.<DoctorResponseDto>builder()
                .status(HttpStatus.OK)
                .message("Doctor activated successfully")
                .data(doctor)
                .build());
    }

    @Operation(summary = "Deactivate doctor", description = "Deactivates a doctor")
    @PostMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<DoctorResponseDto>> deactivateDoctor(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID id) {
        DoctorResponseDto doctor = doctorService.deactivateDoctor(id);
        return ResponseEntity.ok(ResponseModel.<DoctorResponseDto>builder()
                .status(HttpStatus.OK)
                .message("Doctor deactivated successfully")
                .data(doctor)
                .build());
    }

    @Operation(summary = "Update doctor rating and review count", description = "Updates doctor's rating and review count")
    @PostMapping("/{id}/update-rating")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<Void>> updateDoctorRatingAndReviewCount(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID id,
            @Parameter(description = "New rating", required = true) @RequestParam BigDecimal newRating,
            @Parameter(description = "Review count", required = true) @RequestParam Integer reviewCount) {
        doctorService.updateRatingAndReviewCount(id, newRating, reviewCount);
        return ResponseEntity.ok(ResponseModel.<Void>builder()
                .status(HttpStatus.OK)
                .message("Doctor rating and review count updated successfully")
                .data(null)
                .build());
    }

    @Operation(summary = "Check if doctor exists", description = "Checks if a doctor exists by ID")
    @GetMapping("/{id}/exists")
    public ResponseEntity<ResponseModel<Boolean>> checkDoctorExists(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID id) {
        boolean exists = doctorService.existsById(id);
        return ResponseEntity.ok(ResponseModel.<Boolean>builder()
                .status(HttpStatus.OK)
                .message("Doctor existence checked")
                .data(exists)
                .build());
    }

    @Operation(summary = "Check if slug exists", description = "Checks if a doctor slug exists")
    @GetMapping("/slug/{slug}/exists")
    public ResponseEntity<ResponseModel<Boolean>> checkSlugExists(
            @Parameter(description = "Doctor slug", required = true) @PathVariable String slug) {
        boolean exists = doctorService.existsBySlug(slug);
        return ResponseEntity.ok(ResponseModel.<Boolean>builder()
                .status(HttpStatus.OK)
                .message("Slug existence checked")
                .data(exists)
                .build());
    }

    @Operation(summary = "Check if user has doctor profile", description = "Checks if a user has a doctor profile")
    @GetMapping("/user/{userId}/exists")
    public ResponseEntity<ResponseModel<Boolean>> checkUserHasDoctorProfile(
            @Parameter(description = "User ID", required = true) @PathVariable UUID userId) {
        boolean exists = doctorService.existsByUserId(userId);
        return ResponseEntity.ok(ResponseModel.<Boolean>builder()
                .status(HttpStatus.OK)
                .message("User doctor profile existence checked")
                .data(exists)
                .build());
    }

    @Operation(summary = "Check if slug is available", description = "Checks if a doctor slug is available")
    @GetMapping("/slug/{slug}/available")
    public ResponseEntity<ResponseModel<Boolean>> isSlugAvailable(
            @Parameter(description = "Doctor slug", required = true) @PathVariable String slug) {
        boolean isAvailable = doctorService.isSlugAvailable(slug);
        return ResponseEntity.ok(ResponseModel.<Boolean>builder()
                .status(HttpStatus.OK)
                .message("Slug availability checked")
                .data(isAvailable)
                .build());
    }

    @Operation(summary = "Check if doctor is verified", description = "Checks if a doctor is verified")
    @GetMapping("/{id}/verified")
    public ResponseEntity<ResponseModel<Boolean>> isDoctorVerified(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID id) {
        boolean isVerified = doctorService.isDoctorVerified(id);
        return ResponseEntity.ok(ResponseModel.<Boolean>builder()
                .status(HttpStatus.OK)
                .message("Doctor verification status checked")
                .data(isVerified)
                .build());
    }

    @Operation(summary = "Check if doctor is active", description = "Checks if a doctor is active")
    @GetMapping("/{id}/active")
    public ResponseEntity<ResponseModel<Boolean>> isDoctorActive(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID id) {
        boolean isActive = doctorService.isDoctorActive(id);
        return ResponseEntity.ok(ResponseModel.<Boolean>builder()
                .status(HttpStatus.OK)
                .message("Doctor active status checked")
                .data(isActive)
                .build());
    }

    @Operation(summary = "Count doctors by status", description = "Gets the count of doctors by status")
    @GetMapping("/statistics/count/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<Long>> countDoctorsByStatus(
            @Parameter(description = "Doctor status", required = true) @PathVariable String status) {
        long count = doctorService.countByStatus(status);
        return ResponseEntity.ok(ResponseModel.<Long>builder()
                .status(HttpStatus.OK)
                .message("Doctors count by status retrieved successfully")
                .data(count)
                .build());
    }

    @Operation(summary = "Count doctors by verification status", description = "Gets the count of doctors by verification status")
    @GetMapping("/statistics/count/verification/{isVerified}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<Long>> countDoctorsByVerificationStatus(
            @Parameter(description = "Verification status", required = true) @PathVariable Boolean isVerified) {
        long count = doctorService.countByVerificationStatus(isVerified);
        return ResponseEntity.ok(ResponseModel.<Long>builder()
                .status(HttpStatus.OK)
                .message("Doctors count by verification status retrieved successfully")
                .data(count)
                .build());
    }

    @Operation(summary = "Count doctors by speciality", description = "Gets the count of doctors by speciality")
    @GetMapping("/statistics/count/speciality/{speciality}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<Long>> countDoctorsBySpeciality(
            @Parameter(description = "Doctor speciality", required = true) @PathVariable String speciality) {
        long count = doctorService.countBySpeciality(speciality);
        return ResponseEntity.ok(ResponseModel.<Long>builder()
                .status(HttpStatus.OK)
                .message("Doctors count by speciality retrieved successfully")
                .data(count)
                .build());
    }

    @Operation(summary = "Create batch doctors", description = "Creates multiple doctors")
    @PostMapping("/batch")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<List<DoctorResponseDto>>> createBatchDoctors(
            @Parameter(description = "List of doctor details", required = true) @Valid @RequestBody List<DoctorRequestDto> requestDtos) {
        List<DoctorResponseDto> doctors = doctorService.createBatch(requestDtos);
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseModel.<List<DoctorResponseDto>>builder()
                .status(HttpStatus.CREATED)
                .message("Batch doctors created successfully")
                .data(doctors)
                .build());
    }

    @Operation(summary = "Delete batch doctors", description = "Deletes multiple doctors by IDs")
    @DeleteMapping("/batch")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<Void>> deleteBatchDoctors(
            @Parameter(description = "List of doctor IDs", required = true) @RequestBody List<UUID> ids) {
        doctorService.deleteBatch(ids);
        return ResponseEntity.ok(ResponseModel.<Void>builder()
                .status(HttpStatus.NO_CONTENT)
                .message("Batch doctors deleted successfully")
                .data(null)
                .build());
    }



    @Operation(summary = "Check if profile is complete", description = "Checks if a doctor's profile is complete")
    @GetMapping("/{id}/profile-complete")
    @PreAuthorize("@doctorSecurity.isDoctorOwner(authentication, #id) or hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<Boolean>> isProfileComplete(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID id) {
        boolean isComplete = doctorService.isProfileComplete(id);
        return ResponseEntity.ok(ResponseModel.<Boolean>builder()
                .status(HttpStatus.OK)
                .message("Profile completeness checked")
                .data(isComplete)
                .build());
    }

    // Branch-based endpoints
    @Operation(summary = "Get doctors by branch", description = "Retrieves doctors by branch ID")
    @GetMapping("/branch/{branchId}")
    @PreAuthorize("@securityService.hasBranchAccess(#branchId) or hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<Page<DoctorResponseDto>>> getDoctorsByBranch(
            @Parameter(description = "Branch ID", required = true) @PathVariable UUID branchId,
            @Parameter(description = "Pagination information") Pageable pageable) {
        Page<DoctorResponseDto> doctors = doctorService.findByBranch(branchId, pageable);
        return ResponseEntity.ok(ResponseModel.<Page<DoctorResponseDto>>builder()
                .status(HttpStatus.OK)
                .message("Doctors retrieved successfully")
                .data(doctors)
                .build());
    }

    @Operation(summary = "Get doctors by branch and status", description = "Retrieves doctors by branch ID and status")
    @GetMapping("/branch/{branchId}/status/{status}")
    @PreAuthorize("@securityService.hasBranchAccess(#branchId) or hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<Page<DoctorResponseDto>>> getDoctorsByBranchAndStatus(
            @Parameter(description = "Branch ID", required = true) @PathVariable UUID branchId,
            @Parameter(description = "Doctor status", required = true) @PathVariable String status,
            @Parameter(description = "Pagination information") Pageable pageable) {
        Page<DoctorResponseDto> doctors = doctorService.findByBranchAndStatus(branchId, status, pageable);
        return ResponseEntity.ok(ResponseModel.<Page<DoctorResponseDto>>builder()
                .status(HttpStatus.OK)
                .message("Doctors retrieved successfully")
                .data(doctors)
                .build());
    }

    @Operation(summary = "Get doctors by branch and verification status", description = "Retrieves doctors by branch ID and verification status")
    @GetMapping("/branch/{branchId}/verification/{isVerified}")
    @PreAuthorize("@securityService.hasBranchAccess(#branchId) or hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<List<DoctorResponseDto>>> getDoctorsByBranchAndVerificationStatus(
            @Parameter(description = "Branch ID", required = true) @PathVariable UUID branchId,
            @Parameter(description = "Verification status", required = true) @PathVariable Boolean isVerified) {
        List<DoctorResponseDto> doctors = doctorService.findByBranchAndVerificationStatus(branchId, isVerified);
        return ResponseEntity.ok(ResponseModel.<List<DoctorResponseDto>>builder()
                .status(HttpStatus.OK)
                .message("Doctors retrieved successfully")
                .data(doctors)
                .build());
    }

    @Operation(summary = "Get multi-branch doctors", description = "Retrieves doctors who work across multiple branches")
    @GetMapping("/multi-branch")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<Page<DoctorResponseDto>>> getMultiBranchDoctors(
            @Parameter(description = "Pagination information") Pageable pageable) {
        Page<DoctorResponseDto> doctors = doctorService.findMultiBranchDoctors(pageable);
        return ResponseEntity.ok(ResponseModel.<Page<DoctorResponseDto>>builder()
                .status(HttpStatus.OK)
                .message("Multi-branch doctors retrieved successfully")
                .data(doctors)
                .build());
    }

    @Operation(summary = "Get doctors by current user's branch", description = "Retrieves doctors from the current user's primary branch")
    @GetMapping("/my-branch")
    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN','STAFF')")
    public ResponseEntity<ResponseModel<Page<DoctorResponseDto>>> getDoctorsByCurrentUserBranch(
            @Parameter(description = "Pagination information") Pageable pageable) {
        Page<DoctorResponseDto> doctors = doctorService.findDoctorsByCurrentUserBranch(pageable);
        return ResponseEntity.ok(ResponseModel.<Page<DoctorResponseDto>>builder()
                .status(HttpStatus.OK)
                .message("Doctors from your branch retrieved successfully")
                .data(doctors)
                .build());
    }

    @Operation(summary = "Count doctors by branch", description = "Gets the count of doctors by branch ID")
    @GetMapping("/statistics/count/branch/{branchId}")
    @PreAuthorize("@securityService.hasBranchAccess(#branchId) or hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<Long>> countDoctorsByBranch(
            @Parameter(description = "Branch ID", required = true) @PathVariable UUID branchId) {
        long count = doctorService.countByBranch(branchId);
        return ResponseEntity.ok(ResponseModel.<Long>builder()
                .status(HttpStatus.OK)
                .message("Doctor count by branch retrieved successfully")
                .data(count)
                .build());
    }

    @Operation(summary = "Count doctors by branch and status", description = "Gets the count of doctors by branch ID and status")
    @GetMapping("/statistics/count/branch/{branchId}/status/{status}")
    @PreAuthorize("@securityService.hasBranchAccess(#branchId) or hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<Long>> countDoctorsByBranchAndStatus(
            @Parameter(description = "Branch ID", required = true) @PathVariable UUID branchId,
            @Parameter(description = "Doctor status", required = true) @PathVariable String status) {
        long count = doctorService.countByBranchAndStatus(branchId, status);
        return ResponseEntity.ok(ResponseModel.<Long>builder()
                .status(HttpStatus.OK)
                .message("Doctor count by branch and status retrieved successfully")
                .data(count)
                .build());
    }
}
