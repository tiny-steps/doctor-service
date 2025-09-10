package com.tinysteps.doctorservice.controller;

import com.tinysteps.doctorservice.model.PricingRequestDto;
import com.tinysteps.doctorservice.model.PricingResponseDto;
import com.tinysteps.doctorservice.model.ResponseModel;
import com.tinysteps.doctorservice.service.PricingService;
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
@RequestMapping("/api/v1/pricing")
@RequiredArgsConstructor
@Tag(name = "Pricing Management", description = "APIs for managing doctor pricing")
@SecurityRequirement(name = "Bearer Authentication")
public class PricingController {

    private final PricingService pricingService;

    @Operation(summary = "Create pricing", description = "Creates a new pricing for a doctor")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Pricing created successfully",
                    content = @Content(schema = @Schema(implementation = PricingResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Doctor not found")
    })
    @PostMapping("/doctor/{doctorId}")
    @PreAuthorize("@doctorSecurity.isDoctorOwner(authentication, #doctorId) or hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<PricingResponseDto>> createPricing(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID doctorId,
            @Parameter(description = "Pricing details", required = true) @Valid @RequestBody PricingRequestDto requestDto) {
        PricingResponseDto pricing = pricingService.create(doctorId, requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseModel.<PricingResponseDto>builder()
                .status(HttpStatus.CREATED)
                .message("Pricing created successfully")
                .data(pricing)
                .build());
    }

    @Operation(summary = "Get pricing by ID", description = "Retrieves a pricing by its unique identifier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pricing found",
                    content = @Content(schema = @Schema(implementation = PricingResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Pricing not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ResponseModel<PricingResponseDto>> getPricingById(
            @Parameter(description = "Pricing ID", required = true) @PathVariable UUID id) {
        PricingResponseDto pricing = pricingService.findById(id);
        return ResponseEntity.ok(ResponseModel.<PricingResponseDto>builder()
                .status(HttpStatus.OK)
                .message("Pricing retrieved successfully")
                .data(pricing)
                .build());
    }

    @Operation(summary = "Get all pricing", description = "Retrieves a paginated list of all pricing")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pricing retrieved successfully")
    })
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<Page<PricingResponseDto>>> getAllPricing(
            @Parameter(description = "Pagination information") Pageable pageable) {
        Page<PricingResponseDto> pricing = pricingService.findAll(pageable);
        return ResponseEntity.ok(ResponseModel.<Page<PricingResponseDto>>builder()
                .status(HttpStatus.OK)
                .message("Pricing retrieved successfully")
                .data(pricing)
                .build());
    }

    @Operation(summary = "Update pricing", description = "Updates an existing pricing")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pricing updated successfully"),
            @ApiResponse(responseCode = "404", description = "Pricing not found")
    })
    @PutMapping("/{id}")
    @PreAuthorize("@pricingSecurity.isPricingOwner(authentication, #id) or hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<PricingResponseDto>> updatePricing(
            @Parameter(description = "Pricing ID", required = true) @PathVariable UUID id,
            @Parameter(description = "Updated pricing details", required = true) @Valid @RequestBody PricingRequestDto requestDto) {
        PricingResponseDto pricing = pricingService.update(id, requestDto);
        return ResponseEntity.ok(ResponseModel.<PricingResponseDto>builder()
                .status(HttpStatus.OK)
                .message("Pricing updated successfully")
                .data(pricing)
                .build());
    }

    @Operation(summary = "Partially update pricing", description = "Partially updates an existing pricing")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pricing updated successfully"),
            @ApiResponse(responseCode = "404", description = "Pricing not found")
    })
    @PatchMapping("/{id}")
    @PreAuthorize("@pricingSecurity.isPricingOwner(authentication, #id) or hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<PricingResponseDto>> partialUpdatePricing(
            @Parameter(description = "Pricing ID", required = true) @PathVariable UUID id,
            @Parameter(description = "Partial pricing details", required = true) @Valid @RequestBody PricingRequestDto requestDto) {
        PricingResponseDto pricing = pricingService.partialUpdate(id, requestDto);
        return ResponseEntity.ok(ResponseModel.<PricingResponseDto>builder()
                .status(HttpStatus.OK)
                .message("Pricing updated successfully")
                .data(pricing)
                .build());
    }

    @Operation(summary = "Delete pricing", description = "Deletes a pricing")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Pricing deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Pricing not found")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("@pricingSecurity.isPricingOwner(authentication, #id) or hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<Void>> deletePricing(
            @Parameter(description = "Pricing ID", required = true) @PathVariable UUID id) {
        pricingService.delete(id);
        return ResponseEntity.ok(ResponseModel.<Void>builder()
                .status(HttpStatus.NO_CONTENT)
                .message("Pricing deleted successfully")
                .data(null)
                .build());
    }

    @Operation(summary = "Get pricing by doctor", description = "Retrieves all pricing for a specific doctor")
    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<ResponseModel<List<PricingResponseDto>>> getPricingByDoctor(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID doctorId) {
        List<PricingResponseDto> pricing = pricingService.findByDoctorId(doctorId);
        return ResponseEntity.ok(ResponseModel.<List<PricingResponseDto>>builder()
                .status(HttpStatus.OK)
                .message("Doctor pricing retrieved successfully")
                .data(pricing)
                .build());
    }

    @Operation(summary = "Get active pricing by doctor", description = "Retrieves active pricing for a specific doctor")
    @GetMapping("/doctor/{doctorId}/active")
    public ResponseEntity<ResponseModel<List<PricingResponseDto>>> getActivePricingByDoctor(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID doctorId) {
        List<PricingResponseDto> pricing = pricingService.findActivePricingByDoctorId(doctorId);
        return ResponseEntity.ok(ResponseModel.<List<PricingResponseDto>>builder()
                .status(HttpStatus.OK)
                .message("Doctor active pricing retrieved successfully")
                .data(pricing)
                .build());
    }

    @Operation(summary = "Get pricing by doctor (paginated)", description = "Retrieves paginated pricing for a specific doctor")
    @GetMapping("/doctor/{doctorId}/paginated")
    public ResponseEntity<ResponseModel<Page<PricingResponseDto>>> getPricingByDoctorPaginated(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID doctorId,
            @Parameter(description = "Pagination information") Pageable pageable) {
        Page<PricingResponseDto> pricing = pricingService.findByDoctorId(doctorId, pageable);
        return ResponseEntity.ok(ResponseModel.<Page<PricingResponseDto>>builder()
                .status(HttpStatus.OK)
                .message("Doctor pricing retrieved successfully")
                .data(pricing)
                .build());
    }

    @Operation(summary = "Get pricing by session type", description = "Retrieves pricing by session type")
    @GetMapping("/session-type/{sessionTypeId}")
    public ResponseEntity<ResponseModel<List<PricingResponseDto>>> getPricingBySessionType(
            @Parameter(description = "Session Type ID", required = true) @PathVariable UUID sessionTypeId) {
        List<PricingResponseDto> pricing = pricingService.findBySessionTypeId(sessionTypeId);
        return ResponseEntity.ok(ResponseModel.<List<PricingResponseDto>>builder()
                .status(HttpStatus.OK)
                .message("Pricing by session type retrieved successfully")
                .data(pricing)
                .build());
    }

    @Operation(summary = "Get active pricing by session type", description = "Retrieves active pricing by session type")
    @GetMapping("/session-type/{sessionTypeId}/active")
    public ResponseEntity<ResponseModel<List<PricingResponseDto>>> getActivePricingBySessionType(
            @Parameter(description = "Session Type ID", required = true) @PathVariable UUID sessionTypeId) {
        List<PricingResponseDto> pricing = pricingService.findActiveBySessionTypeId(sessionTypeId);
        return ResponseEntity.ok(ResponseModel.<List<PricingResponseDto>>builder()
                .status(HttpStatus.OK)
                .message("Active pricing by session type retrieved successfully")
                .data(pricing)
                .build());
    }

    @Operation(summary = "Get pricing by doctor and session type", description = "Retrieves pricing for specific doctor and session type")
    @GetMapping("/doctor/{doctorId}/session-type/{sessionTypeId}")
    public ResponseEntity<ResponseModel<PricingResponseDto>> getPricingByDoctorAndSessionType(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID doctorId,
            @Parameter(description = "Session Type ID", required = true) @PathVariable UUID sessionTypeId) {
        PricingResponseDto pricing = pricingService.findByDoctorIdAndSessionTypeId(doctorId, sessionTypeId);
        return ResponseEntity.ok(ResponseModel.<PricingResponseDto>builder()
                .status(HttpStatus.OK)
                .message("Pricing by doctor and session type retrieved successfully")
                .data(pricing)
                .build());
    }

    @Operation(summary = "Get active pricing by doctor and session type", description = "Retrieves active pricing for specific doctor and session type")
    @GetMapping("/doctor/{doctorId}/session-type/{sessionTypeId}/active")
    public ResponseEntity<ResponseModel<PricingResponseDto>> getActivePricingByDoctorAndSessionType(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID doctorId,
            @Parameter(description = "Session Type ID", required = true) @PathVariable UUID sessionTypeId) {
        PricingResponseDto pricing = pricingService.findActiveByDoctorIdAndSessionTypeId(doctorId, sessionTypeId);
        return ResponseEntity.ok(ResponseModel.<PricingResponseDto>builder()
                .status(HttpStatus.OK)
                .message("Active pricing by doctor and session type retrieved successfully")
                .data(pricing)
                .build());
    }

    @Operation(summary = "Get pricing by price range", description = "Retrieves pricing within price range")
    @GetMapping("/price-range")
    public ResponseEntity<ResponseModel<Page<PricingResponseDto>>> getPricingByPriceRange(
            @Parameter(description = "Minimum price") @RequestParam BigDecimal minPrice,
            @Parameter(description = "Maximum price") @RequestParam BigDecimal maxPrice,
            @Parameter(description = "Pagination information") Pageable pageable) {
        Page<PricingResponseDto> pricing = pricingService.findByPriceRange(minPrice, maxPrice, pageable);
        return ResponseEntity.ok(ResponseModel.<Page<PricingResponseDto>>builder()
                .status(HttpStatus.OK)
                .message("Pricing by price range retrieved successfully")
                .data(pricing)
                .build());
    }

    @Operation(summary = "Get active pricing by price range", description = "Retrieves active pricing within price range")
    @GetMapping("/price-range/active")
    public ResponseEntity<ResponseModel<Page<PricingResponseDto>>> getActivePricingByPriceRange(
            @Parameter(description = "Minimum price") @RequestParam BigDecimal minPrice,
            @Parameter(description = "Maximum price") @RequestParam BigDecimal maxPrice,
            @Parameter(description = "Pagination information") Pageable pageable) {
        Page<PricingResponseDto> pricing = pricingService.findActivePricingByPriceRange(minPrice, maxPrice, pageable);
        return ResponseEntity.ok(ResponseModel.<Page<PricingResponseDto>>builder()
                .status(HttpStatus.OK)
                .message("Active pricing by price range retrieved successfully")
                .data(pricing)
                .build());
    }

    @Operation(summary = "Get pricing by minimum price", description = "Retrieves pricing with minimum price")
    @GetMapping("/min-price/{minPrice}")
    public ResponseEntity<ResponseModel<Page<PricingResponseDto>>> getPricingByMinPrice(
            @Parameter(description = "Minimum price", required = true) @PathVariable BigDecimal minPrice,
            @Parameter(description = "Pagination information") Pageable pageable) {
        Page<PricingResponseDto> pricing = pricingService.findByMinPrice(minPrice, pageable);
        return ResponseEntity.ok(ResponseModel.<Page<PricingResponseDto>>builder()
                .status(HttpStatus.OK)
                .message("Pricing by minimum price retrieved successfully")
                .data(pricing)
                .build());
    }

    @Operation(summary = "Get pricing by maximum price", description = "Retrieves pricing with maximum price")
    @GetMapping("/max-price/{maxPrice}")
    public ResponseEntity<ResponseModel<Page<PricingResponseDto>>> getPricingByMaxPrice(
            @Parameter(description = "Maximum price", required = true) @PathVariable BigDecimal maxPrice,
            @Parameter(description = "Pagination information") Pageable pageable) {
        Page<PricingResponseDto> pricing = pricingService.findByMaxPrice(maxPrice, pageable);
        return ResponseEntity.ok(ResponseModel.<Page<PricingResponseDto>>builder()
                .status(HttpStatus.OK)
                .message("Pricing by maximum price retrieved successfully")
                .data(pricing)
                .build());
    }

    @Operation(summary = "Get active pricing", description = "Retrieves all active pricing")
    @GetMapping("/active")
    public ResponseEntity<ResponseModel<Page<PricingResponseDto>>> getActivePricing(
            @Parameter(description = "Pagination information") Pageable pageable) {
        Page<PricingResponseDto> pricing = pricingService.findActivePricing(pageable);
        return ResponseEntity.ok(ResponseModel.<Page<PricingResponseDto>>builder()
                .status(HttpStatus.OK)
                .message("Active pricing retrieved successfully")
                .data(pricing)
                .build());
    }

    @Operation(summary = "Get inactive pricing", description = "Retrieves all inactive pricing")
    @GetMapping("/inactive")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<Page<PricingResponseDto>>> getInactivePricing(
            @Parameter(description = "Pagination information") Pageable pageable) {
        Page<PricingResponseDto> pricing = pricingService.findInactivePricing(pageable);
        return ResponseEntity.ok(ResponseModel.<Page<PricingResponseDto>>builder()
                .status(HttpStatus.OK)
                .message("Inactive pricing retrieved successfully")
                .data(pricing)
                .build());
    }

    @Operation(summary = "Check if pricing exists", description = "Checks if pricing exists by ID")
    @GetMapping("/{id}/exists")
    public ResponseEntity<ResponseModel<Boolean>> checkPricingExists(
            @Parameter(description = "Pricing ID", required = true) @PathVariable UUID id) {
        boolean exists = pricingService.existsById(id);
        return ResponseEntity.ok(ResponseModel.<Boolean>builder()
                .status(HttpStatus.OK)
                .message("Pricing existence checked")
                .data(exists)
                .build());
    }

    @Operation(summary = "Check if doctor has pricing", description = "Checks if a doctor has any pricing")
    @GetMapping("/doctor/{doctorId}/exists")
    public ResponseEntity<ResponseModel<Boolean>> checkDoctorHasPricing(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID doctorId) {
        boolean exists = pricingService.existsByDoctorId(doctorId);
        return ResponseEntity.ok(ResponseModel.<Boolean>builder()
                .status(HttpStatus.OK)
                .message("Doctor pricing existence checked")
                .data(exists)
                .build());
    }

    @Operation(summary = "Check if doctor has pricing for session type", description = "Checks if doctor has pricing for specific session type")
    @GetMapping("/doctor/{doctorId}/session-type/{sessionTypeId}/exists")
    public ResponseEntity<ResponseModel<Boolean>> checkDoctorHasPricingForSessionType(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID doctorId,
            @Parameter(description = "Session Type ID", required = true) @PathVariable UUID sessionTypeId) {
        boolean hasPricing = pricingService.hasDoctorPricingForSessionType(doctorId, sessionTypeId);
        return ResponseEntity.ok(ResponseModel.<Boolean>builder()
                .status(HttpStatus.OK)
                .message("Doctor pricing for session type checked")
                .data(hasPricing)
                .build());
    }

    @Operation(summary = "Check if doctor has active pricing for session type", description = "Checks if doctor has active pricing for specific session type")
    @GetMapping("/doctor/{doctorId}/session-type/{sessionTypeId}/active/exists")
    public ResponseEntity<ResponseModel<Boolean>> checkDoctorHasActivePricingForSessionType(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID doctorId,
            @Parameter(description = "Session Type ID", required = true) @PathVariable UUID sessionTypeId) {
        boolean hasActivePricing = pricingService.hasActivePricingForSessionType(doctorId, sessionTypeId);
        return ResponseEntity.ok(ResponseModel.<Boolean>builder()
                .status(HttpStatus.OK)
                .message("Doctor active pricing for session type checked")
                .data(hasActivePricing)
                .build());
    }

    @Operation(summary = "Count pricing by doctor", description = "Gets the count of pricing for a doctor")
    @GetMapping("/doctor/{doctorId}/count")
    public ResponseEntity<ResponseModel<Long>> countPricingByDoctor(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID doctorId) {
        long count = pricingService.countByDoctorId(doctorId);
        return ResponseEntity.ok(ResponseModel.<Long>builder()
                .status(HttpStatus.OK)
                .message("Doctor pricing count retrieved successfully")
                .data(count)
                .build());
    }

    @Operation(summary = "Count active pricing by doctor", description = "Gets the count of active pricing for a doctor")
    @GetMapping("/doctor/{doctorId}/active/count")
    public ResponseEntity<ResponseModel<Long>> countActivePricingByDoctor(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID doctorId) {
        long count = pricingService.countActivePricingByDoctorId(doctorId);
        return ResponseEntity.ok(ResponseModel.<Long>builder()
                .status(HttpStatus.OK)
                .message("Doctor active pricing count retrieved successfully")
                .data(count)
                .build());
    }

    @Operation(summary = "Count pricing by session type", description = "Gets the count of pricing for session type")
    @GetMapping("/session-type/{sessionTypeId}/count")
    public ResponseEntity<ResponseModel<Long>> countPricingBySessionType(
            @Parameter(description = "Session Type ID", required = true) @PathVariable UUID sessionTypeId) {
        long count = pricingService.countBySessionTypeId(sessionTypeId);
        return ResponseEntity.ok(ResponseModel.<Long>builder()
                .status(HttpStatus.OK)
                .message("Pricing count by session type retrieved successfully")
                .data(count)
                .build());
    }

    @Operation(summary = "Get average price for session type", description = "Gets the average price for session type")
    @GetMapping("/session-type/{sessionTypeId}/average-price")
    public ResponseEntity<ResponseModel<BigDecimal>> getAveragePriceForSessionType(
            @Parameter(description = "Session Type ID", required = true) @PathVariable UUID sessionTypeId) {
        BigDecimal averagePrice = pricingService.findAveragePriceForSessionType(sessionTypeId);
        return ResponseEntity.ok(ResponseModel.<BigDecimal>builder()
                .status(HttpStatus.OK)
                .message("Average price for session type retrieved successfully")
                .data(averagePrice)
                .build());
    }

    @Operation(summary = "Get minimum price for session type", description = "Gets the minimum price for session type")
    @GetMapping("/session-type/{sessionTypeId}/min-price")
    public ResponseEntity<ResponseModel<BigDecimal>> getMinPriceForSessionType(
            @Parameter(description = "Session Type ID", required = true) @PathVariable UUID sessionTypeId) {
        BigDecimal minPrice = pricingService.findMinPriceForSessionType(sessionTypeId);
        return ResponseEntity.ok(ResponseModel.<BigDecimal>builder()
                .status(HttpStatus.OK)
                .message("Minimum price for session type retrieved successfully")
                .data(minPrice)
                .build());
    }

    @Operation(summary = "Get maximum price for session type", description = "Gets the maximum price for session type")
    @GetMapping("/session-type/{sessionTypeId}/max-price")
    public ResponseEntity<ResponseModel<BigDecimal>> getMaxPriceForSessionType(
            @Parameter(description = "Session Type ID", required = true) @PathVariable UUID sessionTypeId) {
        BigDecimal maxPrice = pricingService.findMaxPriceForSessionType(sessionTypeId);
        return ResponseEntity.ok(ResponseModel.<BigDecimal>builder()
                .status(HttpStatus.OK)
                .message("Maximum price for session type retrieved successfully")
                .data(maxPrice)
                .build());
    }

    @Operation(summary = "Get total pricing count", description = "Gets the total count of all pricing")
    @GetMapping("/count")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<Long>> getTotalPricingCount() {
        long count = pricingService.countAll();
        return ResponseEntity.ok(ResponseModel.<Long>builder()
                .status(HttpStatus.OK)
                .message("Total pricing count retrieved successfully")
                .data(count)
                .build());
    }

    @Operation(summary = "Create batch pricing", description = "Creates multiple pricing for a doctor")
    @PostMapping("/doctor/{doctorId}/batch")
    @PreAuthorize("@doctorSecurity.isDoctorOwner(authentication, #doctorId) or hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<List<PricingResponseDto>>> createBatchPricing(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID doctorId,
            @Parameter(description = "List of pricing details", required = true) @Valid @RequestBody List<PricingRequestDto> requestDtos) {
        List<PricingResponseDto> pricing = pricingService.createBatch(doctorId, requestDtos);
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseModel.<List<PricingResponseDto>>builder()
                .status(HttpStatus.CREATED)
                .message("Batch pricing created successfully")
                .data(pricing)
                .build());
    }

    @Operation(summary = "Delete pricing by doctor", description = "Deletes all pricing for a doctor")
    @DeleteMapping("/doctor/{doctorId}")
    @PreAuthorize("@doctorSecurity.isDoctorOwner(authentication, #doctorId) or hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<Void>> deletePricingByDoctor(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID doctorId) {
        pricingService.deleteByDoctorId(doctorId);
        return ResponseEntity.ok(ResponseModel.<Void>builder()
                .status(HttpStatus.NO_CONTENT)
                .message("Doctor pricing deleted successfully")
                .data(null)
                .build());
    }

    @Operation(summary = "Delete batch pricing", description = "Deletes multiple pricing by IDs")
    @DeleteMapping("/batch")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<Void>> deleteBatchPricing(
            @Parameter(description = "List of pricing IDs", required = true) @RequestBody List<UUID> ids) {
        pricingService.deleteBatch(ids);
        return ResponseEntity.ok(ResponseModel.<Void>builder()
                .status(HttpStatus.NO_CONTENT)
                .message("Batch pricing deleted successfully")
                .data(null)
                .build());
    }

    @Operation(summary = "Get active session type IDs", description = "Retrieves all active session type IDs")
    @GetMapping("/session-types/active")
    public ResponseEntity<ResponseModel<List<UUID>>> getActiveSessionTypeIds() {
        List<UUID> sessionTypeIds = pricingService.getActiveSessionTypeIds();
        return ResponseEntity.ok(ResponseModel.<List<UUID>>builder()
                .status(HttpStatus.OK)
                .message("Active session type IDs retrieved successfully")
                .data(sessionTypeIds)
                .build());
    }

    @Operation(summary = "Get cheapest pricing for session type", description = "Retrieves cheapest pricing for session type")
    @GetMapping("/session-type/{sessionTypeId}/cheapest")
    public ResponseEntity<ResponseModel<List<PricingResponseDto>>> getCheapestPricingForSessionType(
            @Parameter(description = "Session Type ID", required = true) @PathVariable UUID sessionTypeId) {
        List<PricingResponseDto> pricing = pricingService.findCheapestPricingForSessionType(sessionTypeId);
        return ResponseEntity.ok(ResponseModel.<List<PricingResponseDto>>builder()
                .status(HttpStatus.OK)
                .message("Cheapest pricing for session type retrieved successfully")
                .data(pricing)
                .build());
    }

    @Operation(summary = "Get most expensive pricing for session type", description = "Retrieves most expensive pricing for session type")
    @GetMapping("/session-type/{sessionTypeId}/most-expensive")
    public ResponseEntity<ResponseModel<List<PricingResponseDto>>> getMostExpensivePricingForSessionType(
            @Parameter(description = "Session Type ID", required = true) @PathVariable UUID sessionTypeId) {
        List<PricingResponseDto> pricing = pricingService.findMostExpensivePricingForSessionType(sessionTypeId);
        return ResponseEntity.ok(ResponseModel.<List<PricingResponseDto>>builder()
                .status(HttpStatus.OK)
                .message("Most expensive pricing for session type retrieved successfully")
                .data(pricing)
                .build());
    }

    @Operation(summary = "Activate pricing", description = "Activates a pricing")
    @PostMapping("/{id}/activate")
    @PreAuthorize("@pricingSecurity.isPricingOwner(authentication, #id) or hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<PricingResponseDto>> activatePricing(
            @Parameter(description = "Pricing ID", required = true) @PathVariable UUID id) {
        PricingResponseDto pricing = pricingService.activatePricing(id);
        return ResponseEntity.ok(ResponseModel.<PricingResponseDto>builder()
                .status(HttpStatus.OK)
                .message("Pricing activated successfully")
                .data(pricing)
                .build());
    }

    @Operation(summary = "Deactivate pricing", description = "Deactivates a pricing")
    @PostMapping("/{id}/deactivate")
    @PreAuthorize("@pricingSecurity.isPricingOwner(authentication, #id) or hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<PricingResponseDto>> deactivatePricing(
            @Parameter(description = "Pricing ID", required = true) @PathVariable UUID id) {
        PricingResponseDto pricing = pricingService.deactivatePricing(id);
        return ResponseEntity.ok(ResponseModel.<PricingResponseDto>builder()
                .status(HttpStatus.OK)
                .message("Pricing deactivated successfully")
                .data(pricing)
                .build());
    }

    @Operation(summary = "Get pricing statistics by doctor", description = "Gets pricing statistics for a doctor")
    @GetMapping("/doctor/{doctorId}/statistics")
    public ResponseEntity<ResponseModel<Object[]>> getPricingStatsByDoctor(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID doctorId) {
        Object[] stats = pricingService.getPricingStatsByDoctorId(doctorId);
        return ResponseEntity.ok(ResponseModel.<Object[]>builder()
                .status(HttpStatus.OK)
                .message("Doctor pricing statistics retrieved successfully")
                .data(stats)
                .build());
    }
}
