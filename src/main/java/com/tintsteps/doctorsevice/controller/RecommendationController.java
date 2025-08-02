package com.tintsteps.doctorsevice.controller;

import com.tintsteps.doctorsevice.model.RecommendationRequestDto;
import com.tintsteps.doctorsevice.model.RecommendationResponseDto;
import com.tintsteps.doctorsevice.model.ResponseModel;
import com.tintsteps.doctorsevice.service.RecommendationService;
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
@RequestMapping("/api/v1/recommendations")
@RequiredArgsConstructor
@Tag(name = "Recommendation Management", description = "APIs for managing doctor recommendations and reviews")
@SecurityRequirement(name = "Bearer Authentication")
public class RecommendationController {

    private final RecommendationService recommendationService;

    @Operation(summary = "Create recommendation", description = "Creates a new recommendation for a doctor")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Recommendation created successfully",
                    content = @Content(schema = @Schema(implementation = RecommendationResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Doctor not found")
    })
    @PostMapping("/doctor/{doctorId}")
    @PreAuthorize("hasRole('PATIENT') or hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<RecommendationResponseDto>> createRecommendation(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID doctorId,
            @Parameter(description = "Recommendation details", required = true) @Valid @RequestBody RecommendationRequestDto requestDto) {
        RecommendationResponseDto recommendation = recommendationService.create(doctorId, requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseModel.<RecommendationResponseDto>builder()
                .status(HttpStatus.CREATED)
                .message("Recommendation created successfully")
                .data(recommendation)
                .build());
    }

    @Operation(summary = "Get recommendation by ID", description = "Retrieves a recommendation by its unique identifier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Recommendation found",
                    content = @Content(schema = @Schema(implementation = RecommendationResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Recommendation not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ResponseModel<RecommendationResponseDto>> getRecommendationById(
            @Parameter(description = "Recommendation ID", required = true) @PathVariable UUID id) {
        RecommendationResponseDto recommendation = recommendationService.findById(id);
        return ResponseEntity.ok(ResponseModel.<RecommendationResponseDto>builder()
                .status(HttpStatus.OK)
                .message("Recommendation retrieved successfully")
                .data(recommendation)
                .build());
    }

    @Operation(summary = "Get all recommendations", description = "Retrieves a paginated list of all recommendations")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Recommendations retrieved successfully")
    })
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<Page<RecommendationResponseDto>>> getAllRecommendations(
            @Parameter(description = "Pagination information") Pageable pageable) {
        Page<RecommendationResponseDto> recommendations = recommendationService.findAll(pageable);
        return ResponseEntity.ok(ResponseModel.<Page<RecommendationResponseDto>>builder()
                .status(HttpStatus.OK)
                .message("Recommendations retrieved successfully")
                .data(recommendations)
                .build());
    }

    @Operation(summary = "Update recommendation", description = "Updates an existing recommendation")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Recommendation updated successfully"),
            @ApiResponse(responseCode = "404", description = "Recommendation not found")
    })
    @PutMapping("/{id}")
    @PreAuthorize("@recommendationSecurity.isRecommendationOwner(authentication, #id) or hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<RecommendationResponseDto>> updateRecommendation(
            @Parameter(description = "Recommendation ID", required = true) @PathVariable UUID id,
            @Parameter(description = "Updated recommendation details", required = true) @Valid @RequestBody RecommendationRequestDto requestDto) {
        RecommendationResponseDto recommendation = recommendationService.update(id, requestDto);
        return ResponseEntity.ok(ResponseModel.<RecommendationResponseDto>builder()
                .status(HttpStatus.OK)
                .message("Recommendation updated successfully")
                .data(recommendation)
                .build());
    }

    @Operation(summary = "Partially update recommendation", description = "Partially updates an existing recommendation")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Recommendation updated successfully"),
            @ApiResponse(responseCode = "404", description = "Recommendation not found")
    })
    @PatchMapping("/{id}")
    @PreAuthorize("@recommendationSecurity.isRecommendationOwner(authentication, #id) or hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<RecommendationResponseDto>> partialUpdateRecommendation(
            @Parameter(description = "Recommendation ID", required = true) @PathVariable UUID id,
            @Parameter(description = "Partial recommendation details", required = true) @Valid @RequestBody RecommendationRequestDto requestDto) {
        RecommendationResponseDto recommendation = recommendationService.partialUpdate(id, requestDto);
        return ResponseEntity.ok(ResponseModel.<RecommendationResponseDto>builder()
                .status(HttpStatus.OK)
                .message("Recommendation updated successfully")
                .data(recommendation)
                .build());
    }

    @Operation(summary = "Delete recommendation", description = "Deletes a recommendation")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Recommendation deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Recommendation not found")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("@recommendationSecurity.isRecommendationOwner(authentication, #id) or hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<Void>> deleteRecommendation(
            @Parameter(description = "Recommendation ID", required = true) @PathVariable UUID id) {
        recommendationService.delete(id);
        return ResponseEntity.ok(ResponseModel.<Void>builder()
                .status(HttpStatus.NO_CONTENT)
                .message("Recommendation deleted successfully")
                .data(null)
                .build());
    }

    @Operation(summary = "Get recommendations by doctor", description = "Retrieves all recommendations for a specific doctor")
    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<ResponseModel<List<RecommendationResponseDto>>> getRecommendationsByDoctor(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID doctorId) {
        List<RecommendationResponseDto> recommendations = recommendationService.findByDoctorId(doctorId);
        return ResponseEntity.ok(ResponseModel.<List<RecommendationResponseDto>>builder()
                .status(HttpStatus.OK)
                .message("Doctor recommendations retrieved successfully")
                .data(recommendations)
                .build());
    }

    @Operation(summary = "Get recommendations by doctor (paginated)", description = "Retrieves paginated recommendations for a specific doctor")
    @GetMapping("/doctor/{doctorId}/paginated")
    public ResponseEntity<ResponseModel<Page<RecommendationResponseDto>>> getRecommendationsByDoctorPaginated(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID doctorId,
            @Parameter(description = "Pagination information") Pageable pageable) {
        Page<RecommendationResponseDto> recommendations = recommendationService.findByDoctorId(doctorId, pageable);
        return ResponseEntity.ok(ResponseModel.<Page<RecommendationResponseDto>>builder()
                .status(HttpStatus.OK)
                .message("Doctor recommendations retrieved successfully")
                .data(recommendations)
                .build());
    }

    @Operation(summary = "Get recommendations by doctor ordered by rating", description = "Retrieves recommendations for a doctor ordered by rating")
    @GetMapping("/doctor/{doctorId}/ordered-by-rating")
    public ResponseEntity<ResponseModel<List<RecommendationResponseDto>>> getRecommendationsByDoctorOrderedByRating(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID doctorId) {
        List<RecommendationResponseDto> recommendations = recommendationService.findByDoctorIdOrderByRating(doctorId);
        return ResponseEntity.ok(ResponseModel.<List<RecommendationResponseDto>>builder()
                .status(HttpStatus.OK)
                .message("Doctor recommendations retrieved successfully")
                .data(recommendations)
                .build());
    }

    @Operation(summary = "Get recommendations by doctor ordered by recommendation count", description = "Retrieves recommendations for a doctor ordered by recommendation count")
    @GetMapping("/doctor/{doctorId}/ordered-by-count")
    public ResponseEntity<ResponseModel<List<RecommendationResponseDto>>> getRecommendationsByDoctorOrderedByCount(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID doctorId) {
        List<RecommendationResponseDto> recommendations = recommendationService.findByDoctorIdOrderByRecommendationCount(doctorId);
        return ResponseEntity.ok(ResponseModel.<List<RecommendationResponseDto>>builder()
                .status(HttpStatus.OK)
                .message("Doctor recommendations retrieved successfully")
                .data(recommendations)
                .build());
    }

    @Operation(summary = "Get recommendations by rating", description = "Retrieves recommendations by specific rating")
    @GetMapping("/rating/{rating}")
    public ResponseEntity<ResponseModel<Page<RecommendationResponseDto>>> getRecommendationsByRating(
            @Parameter(description = "Rating value", required = true) @PathVariable BigDecimal rating,
            @Parameter(description = "Pagination information") Pageable pageable) {
        Page<RecommendationResponseDto> recommendations = recommendationService.findByRating(rating, pageable);
        return ResponseEntity.ok(ResponseModel.<Page<RecommendationResponseDto>>builder()
                .status(HttpStatus.OK)
                .message("Recommendations retrieved successfully")
                .data(recommendations)
                .build());
    }

    @Operation(summary = "Get recommendations by minimum rating", description = "Retrieves recommendations with minimum rating")
    @GetMapping("/min-rating/{minRating}")
    public ResponseEntity<ResponseModel<Page<RecommendationResponseDto>>> getRecommendationsByMinRating(
            @Parameter(description = "Minimum rating", required = true) @PathVariable BigDecimal minRating,
            @Parameter(description = "Pagination information") Pageable pageable) {
        Page<RecommendationResponseDto> recommendations = recommendationService.findByMinRating(minRating, pageable);
        return ResponseEntity.ok(ResponseModel.<Page<RecommendationResponseDto>>builder()
                .status(HttpStatus.OK)
                .message("Recommendations retrieved successfully")
                .data(recommendations)
                .build());
    }

    @Operation(summary = "Get recommendations by maximum rating", description = "Retrieves recommendations with maximum rating")
    @GetMapping("/max-rating/{maxRating}")
    public ResponseEntity<ResponseModel<Page<RecommendationResponseDto>>> getRecommendationsByMaxRating(
            @Parameter(description = "Maximum rating", required = true) @PathVariable BigDecimal maxRating,
            @Parameter(description = "Pagination information") Pageable pageable) {
        Page<RecommendationResponseDto> recommendations = recommendationService.findByMaxRating(maxRating, pageable);
        return ResponseEntity.ok(ResponseModel.<Page<RecommendationResponseDto>>builder()
                .status(HttpStatus.OK)
                .message("Recommendations retrieved successfully")
                .data(recommendations)
                .build());
    }

    @Operation(summary = "Get recommendations by rating range", description = "Retrieves recommendations within rating range")
    @GetMapping("/rating-range")
    public ResponseEntity<ResponseModel<Page<RecommendationResponseDto>>> getRecommendationsByRatingRange(
            @Parameter(description = "Minimum rating") @RequestParam BigDecimal minRating,
            @Parameter(description = "Maximum rating") @RequestParam BigDecimal maxRating,
            @Parameter(description = "Pagination information") Pageable pageable) {
        Page<RecommendationResponseDto> recommendations = recommendationService.findByRatingRange(minRating, maxRating, pageable);
        return ResponseEntity.ok(ResponseModel.<Page<RecommendationResponseDto>>builder()
                .status(HttpStatus.OK)
                .message("Recommendations retrieved successfully")
                .data(recommendations)
                .build());
    }

    @Operation(summary = "Get doctor recommendations by minimum rating", description = "Retrieves doctor recommendations with minimum rating")
    @GetMapping("/doctor/{doctorId}/min-rating/{minRating}")
    public ResponseEntity<ResponseModel<List<RecommendationResponseDto>>> getDoctorRecommendationsByMinRating(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID doctorId,
            @Parameter(description = "Minimum rating", required = true) @PathVariable BigDecimal minRating) {
        List<RecommendationResponseDto> recommendations = recommendationService.findByDoctorIdAndMinRating(doctorId, minRating);
        return ResponseEntity.ok(ResponseModel.<List<RecommendationResponseDto>>builder()
                .status(HttpStatus.OK)
                .message("Doctor recommendations retrieved successfully")
                .data(recommendations)
                .build());
    }

    @Operation(summary = "Get recommendations by minimum recommendation count", description = "Retrieves recommendations with minimum recommendation count")
    @GetMapping("/min-count/{minCount}")
    public ResponseEntity<ResponseModel<Page<RecommendationResponseDto>>> getRecommendationsByMinCount(
            @Parameter(description = "Minimum recommendation count", required = true) @PathVariable Integer minCount,
            @Parameter(description = "Pagination information") Pageable pageable) {
        Page<RecommendationResponseDto> recommendations = recommendationService.findByMinRecommendationCount(minCount, pageable);
        return ResponseEntity.ok(ResponseModel.<Page<RecommendationResponseDto>>builder()
                .status(HttpStatus.OK)
                .message("Recommendations retrieved successfully")
                .data(recommendations)
                .build());
    }

    @Operation(summary = "Search recommendations by review text", description = "Searches recommendations by review text")
    @GetMapping("/search/review")
    public ResponseEntity<ResponseModel<Page<RecommendationResponseDto>>> searchRecommendationsByReviewText(
            @Parameter(description = "Review text to search") @RequestParam String reviewText,
            @Parameter(description = "Pagination information") Pageable pageable) {
        Page<RecommendationResponseDto> recommendations = recommendationService.findByReviewText(reviewText, pageable);
        return ResponseEntity.ok(ResponseModel.<Page<RecommendationResponseDto>>builder()
                .status(HttpStatus.OK)
                .message("Recommendations found")
                .data(recommendations)
                .build());
    }

    @Operation(summary = "Get recommendations with reviews", description = "Retrieves recommendations that have review text")
    @GetMapping("/with-reviews")
    public ResponseEntity<ResponseModel<Page<RecommendationResponseDto>>> getRecommendationsWithReviews(
            @Parameter(description = "Pagination information") Pageable pageable) {
        Page<RecommendationResponseDto> recommendations = recommendationService.findRecommendationsWithReviews(pageable);
        return ResponseEntity.ok(ResponseModel.<Page<RecommendationResponseDto>>builder()
                .status(HttpStatus.OK)
                .message("Recommendations with reviews retrieved successfully")
                .data(recommendations)
                .build());
    }

    @Operation(summary = "Get recommendations without reviews", description = "Retrieves recommendations that don't have review text")
    @GetMapping("/without-reviews")
    public ResponseEntity<ResponseModel<Page<RecommendationResponseDto>>> getRecommendationsWithoutReviews(
            @Parameter(description = "Pagination information") Pageable pageable) {
        Page<RecommendationResponseDto> recommendations = recommendationService.findRecommendationsWithoutReviews(pageable);
        return ResponseEntity.ok(ResponseModel.<Page<RecommendationResponseDto>>builder()
                .status(HttpStatus.OK)
                .message("Recommendations without reviews retrieved successfully")
                .data(recommendations)
                .build());
    }

    @Operation(summary = "Get highest rated recommendations", description = "Retrieves highest rated recommendations")
    @GetMapping("/highest-rated")
    public ResponseEntity<ResponseModel<Page<RecommendationResponseDto>>> getHighestRatedRecommendations(
            @Parameter(description = "Pagination information") Pageable pageable) {
        Page<RecommendationResponseDto> recommendations = recommendationService.findHighestRatedRecommendations(pageable);
        return ResponseEntity.ok(ResponseModel.<Page<RecommendationResponseDto>>builder()
                .status(HttpStatus.OK)
                .message("Highest rated recommendations retrieved successfully")
                .data(recommendations)
                .build());
    }

    @Operation(summary = "Get most recommended", description = "Retrieves most recommended doctors")
    @GetMapping("/most-recommended")
    public ResponseEntity<ResponseModel<Page<RecommendationResponseDto>>> getMostRecommended(
            @Parameter(description = "Pagination information") Pageable pageable) {
        Page<RecommendationResponseDto> recommendations = recommendationService.findMostRecommended(pageable);
        return ResponseEntity.ok(ResponseModel.<Page<RecommendationResponseDto>>builder()
                .status(HttpStatus.OK)
                .message("Most recommended retrieved successfully")
                .data(recommendations)
                .build());
    }

    @Operation(summary = "Check if recommendation exists", description = "Checks if a recommendation exists by ID")
    @GetMapping("/{id}/exists")
    public ResponseEntity<ResponseModel<Boolean>> checkRecommendationExists(
            @Parameter(description = "Recommendation ID", required = true) @PathVariable UUID id) {
        boolean exists = recommendationService.existsById(id);
        return ResponseEntity.ok(ResponseModel.<Boolean>builder()
                .status(HttpStatus.OK)
                .message("Recommendation existence checked")
                .data(exists)
                .build());
    }

    @Operation(summary = "Check if doctor has recommendations", description = "Checks if a doctor has any recommendations")
    @GetMapping("/doctor/{doctorId}/exists")
    public ResponseEntity<ResponseModel<Boolean>> checkDoctorHasRecommendations(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID doctorId) {
        boolean exists = recommendationService.existsByDoctorId(doctorId);
        return ResponseEntity.ok(ResponseModel.<Boolean>builder()
                .status(HttpStatus.OK)
                .message("Doctor recommendations existence checked")
                .data(exists)
                .build());
    }

    @Operation(summary = "Count recommendations by doctor", description = "Gets the count of recommendations for a doctor")
    @GetMapping("/doctor/{doctorId}/count")
    public ResponseEntity<ResponseModel<Long>> countRecommendationsByDoctor(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID doctorId) {
        long count = recommendationService.countByDoctorId(doctorId);
        return ResponseEntity.ok(ResponseModel.<Long>builder()
                .status(HttpStatus.OK)
                .message("Doctor recommendations count retrieved successfully")
                .data(count)
                .build());
    }

    @Operation(summary = "Count recommendations by rating", description = "Gets the count of recommendations with specific rating")
    @GetMapping("/rating/{rating}/count")
    public ResponseEntity<ResponseModel<Long>> countRecommendationsByRating(
            @Parameter(description = "Rating value", required = true) @PathVariable BigDecimal rating) {
        long count = recommendationService.countByRating(rating);
        return ResponseEntity.ok(ResponseModel.<Long>builder()
                .status(HttpStatus.OK)
                .message("Recommendations count by rating retrieved successfully")
                .data(count)
                .build());
    }

    @Operation(summary = "Count recommendations by minimum rating", description = "Gets the count of recommendations with minimum rating")
    @GetMapping("/min-rating/{minRating}/count")
    public ResponseEntity<ResponseModel<Long>> countRecommendationsByMinRating(
            @Parameter(description = "Minimum rating", required = true) @PathVariable BigDecimal minRating) {
        long count = recommendationService.countByMinRating(minRating);
        return ResponseEntity.ok(ResponseModel.<Long>builder()
                .status(HttpStatus.OK)
                .message("Recommendations count by minimum rating retrieved successfully")
                .data(count)
                .build());
    }

    @Operation(summary = "Get average rating by doctor", description = "Gets the average rating for a doctor")
    @GetMapping("/doctor/{doctorId}/average-rating")
    public ResponseEntity<ResponseModel<BigDecimal>> getAverageRatingByDoctor(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID doctorId) {
        BigDecimal averageRating = recommendationService.findAverageRatingByDoctorId(doctorId);
        return ResponseEntity.ok(ResponseModel.<BigDecimal>builder()
                .status(HttpStatus.OK)
                .message("Doctor average rating retrieved successfully")
                .data(averageRating)
                .build());
    }

    @Operation(summary = "Get total recommendation count by doctor", description = "Gets the total recommendation count for a doctor")
    @GetMapping("/doctor/{doctorId}/total-count")
    public ResponseEntity<ResponseModel<Long>> getTotalRecommendationCountByDoctor(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID doctorId) {
        Long totalCount = recommendationService.findTotalRecommendationCountByDoctorId(doctorId);
        return ResponseEntity.ok(ResponseModel.<Long>builder()
                .status(HttpStatus.OK)
                .message("Doctor total recommendation count retrieved successfully")
                .data(totalCount)
                .build());
    }

    @Operation(summary = "Get rating statistics by doctor", description = "Gets rating statistics for a doctor")
    @GetMapping("/doctor/{doctorId}/rating-stats")
    public ResponseEntity<ResponseModel<Object[]>> getRatingStatsByDoctor(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID doctorId) {
        Object[] stats = recommendationService.findRatingStatsByDoctorId(doctorId);
        return ResponseEntity.ok(ResponseModel.<Object[]>builder()
                .status(HttpStatus.OK)
                .message("Doctor rating statistics retrieved successfully")
                .data(stats)
                .build());
    }

    @Operation(summary = "Get recommendation count statistics by doctor", description = "Gets recommendation count statistics for a doctor")
    @GetMapping("/doctor/{doctorId}/count-stats")
    public ResponseEntity<ResponseModel<Object[]>> getRecommendationCountStatsByDoctor(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID doctorId) {
        Object[] stats = recommendationService.findRecommendationCountStatsByDoctorId(doctorId);
        return ResponseEntity.ok(ResponseModel.<Object[]>builder()
                .status(HttpStatus.OK)
                .message("Doctor recommendation count statistics retrieved successfully")
                .data(stats)
                .build());
    }

    @Operation(summary = "Get total recommendations count", description = "Gets the total count of all recommendations")
    @GetMapping("/count")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<Long>> getTotalRecommendationsCount() {
        long count = recommendationService.countAll();
        return ResponseEntity.ok(ResponseModel.<Long>builder()
                .status(HttpStatus.OK)
                .message("Total recommendations count retrieved successfully")
                .data(count)
                .build());
    }

    @Operation(summary = "Create batch recommendations", description = "Creates multiple recommendations for a doctor")
    @PostMapping("/doctor/{doctorId}/batch")
    @PreAuthorize("hasRole('PATIENT') or hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<List<RecommendationResponseDto>>> createBatchRecommendations(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID doctorId,
            @Parameter(description = "List of recommendation details", required = true) @Valid @RequestBody List<RecommendationRequestDto> requestDtos) {
        List<RecommendationResponseDto> recommendations = recommendationService.createBatch(doctorId, requestDtos);
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseModel.<List<RecommendationResponseDto>>builder()
                .status(HttpStatus.CREATED)
                .message("Batch recommendations created successfully")
                .data(recommendations)
                .build());
    }

    @Operation(summary = "Delete recommendations by doctor", description = "Deletes all recommendations for a doctor")
    @DeleteMapping("/doctor/{doctorId}")
    @PreAuthorize("@doctorSecurity.isDoctorOwner(authentication, #doctorId) or hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<Void>> deleteRecommendationsByDoctor(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID doctorId) {
        recommendationService.deleteByDoctorId(doctorId);
        return ResponseEntity.ok(ResponseModel.<Void>builder()
                .status(HttpStatus.NO_CONTENT)
                .message("Doctor recommendations deleted successfully")
                .data(null)
                .build());
    }

    @Operation(summary = "Delete batch recommendations", description = "Deletes multiple recommendations by IDs")
    @DeleteMapping("/batch")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<Void>> deleteBatchRecommendations(
            @Parameter(description = "List of recommendation IDs", required = true) @RequestBody List<UUID> ids) {
        recommendationService.deleteBatch(ids);
        return ResponseEntity.ok(ResponseModel.<Void>builder()
                .status(HttpStatus.NO_CONTENT)
                .message("Batch recommendations deleted successfully")
                .data(null)
                .build());
    }

    @Operation(summary = "Get doctors with highest average ratings", description = "Gets doctors with highest average ratings")
    @GetMapping("/doctors/highest-average-ratings")
    public ResponseEntity<ResponseModel<List<Object[]>>> getDoctorsWithHighestAverageRatings() {
        List<Object[]> doctors = recommendationService.findDoctorsWithHighestAverageRatings();
        return ResponseEntity.ok(ResponseModel.<List<Object[]>>builder()
                .status(HttpStatus.OK)
                .message("Doctors with highest average ratings retrieved successfully")
                .data(doctors)
                .build());
    }

    @Operation(summary = "Get doctors with most recommendations", description = "Gets doctors with most recommendations")
    @GetMapping("/doctors/most-recommendations")
    public ResponseEntity<ResponseModel<List<Object[]>>> getDoctorsWithMostRecommendations() {
        List<Object[]> doctors = recommendationService.findDoctorsWithMostRecommendations();
        return ResponseEntity.ok(ResponseModel.<List<Object[]>>builder()
                .status(HttpStatus.OK)
                .message("Doctors with most recommendations retrieved successfully")
                .data(doctors)
                .build());
    }

    @Operation(summary = "Get rating distribution", description = "Gets the distribution of ratings")
    @GetMapping("/rating-distribution")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<List<Object[]>>> getRatingDistribution() {
        List<Object[]> distribution = recommendationService.getRatingDistribution();
        return ResponseEntity.ok(ResponseModel.<List<Object[]>>builder()
                .status(HttpStatus.OK)
                .message("Rating distribution retrieved successfully")
                .data(distribution)
                .build());
    }

    @Operation(summary = "Update doctor rating and review count", description = "Updates doctor's rating and review count based on recommendations")
    @PostMapping("/doctor/{doctorId}/update-rating")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<Void>> updateDoctorRatingAndReviewCount(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID doctorId) {
        recommendationService.updateDoctorRatingAndReviewCount(doctorId);
        return ResponseEntity.ok(ResponseModel.<Void>builder()
                .status(HttpStatus.OK)
                .message("Doctor rating and review count updated successfully")
                .data(null)
                .build());
    }

    @Operation(summary = "Calculate doctor average rating", description = "Calculates the average rating for a doctor")
    @GetMapping("/doctor/{doctorId}/calculate-average-rating")
    public ResponseEntity<ResponseModel<BigDecimal>> calculateDoctorAverageRating(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID doctorId) {
        BigDecimal averageRating = recommendationService.calculateDoctorAverageRating(doctorId);
        return ResponseEntity.ok(ResponseModel.<BigDecimal>builder()
                .status(HttpStatus.OK)
                .message("Doctor average rating calculated successfully")
                .data(averageRating)
                .build());
    }

    @Operation(summary = "Calculate doctor total recommendations", description = "Calculates the total recommendations for a doctor")
    @GetMapping("/doctor/{doctorId}/calculate-total-recommendations")
    public ResponseEntity<ResponseModel<Integer>> calculateDoctorTotalRecommendations(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID doctorId) {
        Integer totalRecommendations = recommendationService.calculateDoctorTotalRecommendations(doctorId);
        return ResponseEntity.ok(ResponseModel.<Integer>builder()
                .status(HttpStatus.OK)
                .message("Doctor total recommendations calculated successfully")
                .data(totalRecommendations)
                .build());
    }
}
