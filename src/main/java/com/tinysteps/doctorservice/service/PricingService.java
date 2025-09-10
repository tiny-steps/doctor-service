package com.tinysteps.doctorservice.service;

import com.tinysteps.doctorservice.model.PricingRequestDto;
import com.tinysteps.doctorservice.model.PricingResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Service interface for Pricing operations
 */
public interface PricingService {

    // CRUD Operations
    PricingResponseDto create(UUID doctorId, PricingRequestDto requestDto);
    PricingResponseDto findById(UUID id);
    Page<PricingResponseDto> findAll(Pageable pageable);
    PricingResponseDto update(UUID id, PricingRequestDto requestDto);
    PricingResponseDto partialUpdate(UUID id, PricingRequestDto requestDto);
    void delete(UUID id);

    // Doctor-specific Operations
    List<PricingResponseDto> findByDoctorId(UUID doctorId);
    List<PricingResponseDto> findActivePricingByDoctorId(UUID doctorId);
    Page<PricingResponseDto> findByDoctorId(UUID doctorId, Pageable pageable);

    // Session Type Operations
    List<PricingResponseDto> findBySessionTypeId(UUID sessionTypeId);
    List<PricingResponseDto> findActiveBySessionTypeId(UUID sessionTypeId);
    PricingResponseDto findByDoctorIdAndSessionTypeId(UUID doctorId, UUID sessionTypeId);
    PricingResponseDto findActiveByDoctorIdAndSessionTypeId(UUID doctorId, UUID sessionTypeId);

    // Price Range Operations
    Page<PricingResponseDto> findByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);
    Page<PricingResponseDto> findActivePricingByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);
    Page<PricingResponseDto> findByMinPrice(BigDecimal minPrice, Pageable pageable);
    Page<PricingResponseDto> findByMaxPrice(BigDecimal maxPrice, Pageable pageable);

    // Active Status Operations
    Page<PricingResponseDto> findActivePricing(Pageable pageable);
    Page<PricingResponseDto> findInactivePricing(Pageable pageable);

    // Validation Operations
    boolean existsById(UUID id);
    boolean existsByDoctorId(UUID doctorId);
    boolean hasDoctorPricingForSessionType(UUID doctorId, UUID sessionTypeId);
    boolean hasActivePricingForSessionType(UUID doctorId, UUID sessionTypeId);

    // Statistics Operations
    long countByDoctorId(UUID doctorId);
    long countActivePricingByDoctorId(UUID doctorId);
    long countBySessionTypeId(UUID sessionTypeId);
    BigDecimal findAveragePriceForSessionType(UUID sessionTypeId);
    BigDecimal findMinPriceForSessionType(UUID sessionTypeId);
    BigDecimal findMaxPriceForSessionType(UUID sessionTypeId);
    long countAll();

    // Bulk Operations
    List<PricingResponseDto> createBatch(UUID doctorId, List<PricingRequestDto> requestDtos);
    void deleteByDoctorId(UUID doctorId);
    void deleteBatch(List<UUID> ids);

    // Business Operations
    List<UUID> getActiveSessionTypeIds();
    List<PricingResponseDto> findCheapestPricingForSessionType(UUID sessionTypeId);
    List<PricingResponseDto> findMostExpensivePricingForSessionType(UUID sessionTypeId);
    PricingResponseDto activatePricing(UUID id);
    PricingResponseDto deactivatePricing(UUID id);
    Object[] getPricingStatsByDoctorId(UUID doctorId);
}
