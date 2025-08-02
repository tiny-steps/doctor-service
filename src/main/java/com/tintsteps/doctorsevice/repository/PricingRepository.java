package com.tintsteps.doctorsevice.repository;

import com.tintsteps.doctorsevice.entity.Pricing;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PricingRepository extends JpaRepository<Pricing, UUID> {
    
    // Find pricing by doctor ID
    List<Pricing> findByDoctorId(UUID doctorId);
    
    // Find active pricing by doctor ID
    List<Pricing> findByDoctorIdAndIsActive(UUID doctorId, Boolean isActive);
    
    // Find pricing by session type ID
    List<Pricing> findBySessionTypeId(UUID sessionTypeId);
    
    // Find active pricing by session type ID
    List<Pricing> findBySessionTypeIdAndIsActive(UUID sessionTypeId, Boolean isActive);
    
    // Find pricing by doctor and session type
    Optional<Pricing> findByDoctorIdAndSessionTypeId(UUID doctorId, UUID sessionTypeId);
    
    // Find active pricing by doctor and session type
    Optional<Pricing> findByDoctorIdAndSessionTypeIdAndIsActive(UUID doctorId, UUID sessionTypeId, Boolean isActive);
    
    // Find pricing by price range
    List<Pricing> findByCustomPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);
    
    // Find active pricing by price range
    List<Pricing> findByCustomPriceBetweenAndIsActive(BigDecimal minPrice, BigDecimal maxPrice, Boolean isActive);
    
    // Find pricing by minimum price
    List<Pricing> findByCustomPriceGreaterThanEqual(BigDecimal minPrice);
    
    // Find pricing by maximum price
    List<Pricing> findByCustomPriceLessThanEqual(BigDecimal maxPrice);
    
    // Count pricing by doctor
    long countByDoctorId(UUID doctorId);
    
    // Count active pricing by doctor
    long countByDoctorIdAndIsActive(UUID doctorId, Boolean isActive);
    
    // Find all active pricing
    List<Pricing> findByIsActive(Boolean isActive);
    
    // Check if doctor has pricing for session type
    boolean existsByDoctorIdAndSessionTypeId(UUID doctorId, UUID sessionTypeId);

    // Check if doctor has active pricing for session type
    boolean existsByDoctorIdAndSessionTypeIdAndIsActive(UUID doctorId, UUID sessionTypeId, Boolean isActive);
    
    // Find doctors with custom pricing for specific session type
    @Query("SELECT p FROM Pricing p WHERE p.sessionTypeId = :sessionTypeId AND p.customPrice IS NOT NULL AND p.isActive = true")
    List<Pricing> findCustomPricingForSessionType(@Param("sessionTypeId") UUID sessionTypeId);
    
    // Find cheapest pricing for session type
    @Query("SELECT p FROM Pricing p WHERE p.sessionTypeId = :sessionTypeId AND p.customPrice IS NOT NULL AND p.isActive = true ORDER BY p.customPrice ASC")
    List<Pricing> findCheapestPricingForSessionType(@Param("sessionTypeId") UUID sessionTypeId);
    
    // Find most expensive pricing for session type
    @Query("SELECT p FROM Pricing p WHERE p.sessionTypeId = :sessionTypeId AND p.customPrice IS NOT NULL AND p.isActive = true ORDER BY p.customPrice DESC")
    List<Pricing> findMostExpensivePricingForSessionType(@Param("sessionTypeId") UUID sessionTypeId);
    
    // Find average price for session type
    @Query("SELECT AVG(p.customPrice) FROM Pricing p WHERE p.sessionTypeId = :sessionTypeId AND p.customPrice IS NOT NULL AND p.isActive = true")
    BigDecimal findAveragePriceForSessionType(@Param("sessionTypeId") UUID sessionTypeId);
    
    // Find all unique session type IDs
    @Query("SELECT DISTINCT p.sessionTypeId FROM Pricing p WHERE p.isActive = true")
    List<UUID> findAllActiveSessionTypeIds();
    
    // Find pricing statistics for doctor
    @Query("SELECT MIN(p.customPrice), MAX(p.customPrice), AVG(p.customPrice) FROM Pricing p WHERE p.doctor.id = :doctorId AND p.customPrice IS NOT NULL AND p.isActive = true")
    Object[] findPricingStatsByDoctorId(@Param("doctorId") UUID doctorId);
    
    // Additional missing methods
    Page<Pricing> findByDoctorId(UUID doctorId, Pageable pageable);
    Page<Pricing> findByCustomPriceBetween(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);
    Page<Pricing> findByCustomPriceBetweenAndIsActive(BigDecimal minPrice, BigDecimal maxPrice, boolean isActive, Pageable pageable);
    Page<Pricing> findByCustomPriceGreaterThanEqual(BigDecimal minPrice, Pageable pageable);
    Page<Pricing> findByCustomPriceLessThanEqual(BigDecimal maxPrice, Pageable pageable);
    Page<Pricing> findByIsActive(boolean isActive, Pageable pageable);
    boolean existsByDoctorId(UUID doctorId);
    void deleteByDoctorId(UUID doctorId);

    @Query("SELECT DISTINCT p.sessionTypeId FROM Pricing p WHERE p.sessionTypeId IS NOT NULL")
    List<UUID> findDistinctSessionTypeIds();

    // Missing methods for compilation errors
    long countBySessionTypeId(UUID sessionTypeId);

    @Query("SELECT MIN(p.customPrice) FROM Pricing p WHERE p.sessionTypeId = :sessionTypeId AND p.customPrice IS NOT NULL AND p.isActive = true")
    BigDecimal findMinPriceForSessionType(@Param("sessionTypeId") UUID sessionTypeId);

    @Query("SELECT MAX(p.customPrice) FROM Pricing p WHERE p.sessionTypeId = :sessionTypeId AND p.customPrice IS NOT NULL AND p.isActive = true")
    BigDecimal findMaxPriceForSessionType(@Param("sessionTypeId") UUID sessionTypeId);

    @Query("SELECT DISTINCT p.sessionTypeId FROM Pricing p WHERE p.isActive = :isActive")
    List<UUID> findDistinctSessionTypeIdsByIsActive(@Param("isActive") boolean isActive);
}
