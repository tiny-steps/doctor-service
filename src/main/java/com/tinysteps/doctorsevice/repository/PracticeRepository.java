package com.tinysteps.doctorsevice.repository;

import com.tinysteps.doctorsevice.entity.Practice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PracticeRepository extends JpaRepository<Practice, UUID> {

    // Find practices by doctor ID
    List<Practice> findByDoctorId(UUID doctorId);

    // Find practices by doctor ID ordered by position
    List<Practice> findByDoctorIdOrderByPracticePosition(UUID doctorId);

    // Find practices by doctor ID ordered by creation date (most recent first)
    List<Practice> findByDoctorIdOrderByCreatedAtDesc(UUID doctorId);

    // Find practices by practice name (case-insensitive)
    List<Practice> findByPracticeNameContainingIgnoreCase(String practiceName);

    // Find practices by exact practice name
    List<Practice> findByPracticeName(String practiceName);

    // Find practices by practice type
    List<Practice> findByPracticeType(String practiceType);

    // Find practices by address ID
    List<Practice> findByAddressId(UUID addressId);

    // Find practice by slug
    Optional<Practice> findBySlug(String slug);

    // Find practices by doctor and practice type
    List<Practice> findByDoctorIdAndPracticeType(UUID doctorId, String practiceType);

    // Find practices by doctor and address
    List<Practice> findByDoctorIdAndAddressId(UUID doctorId, UUID addressId);

    // Count practices by doctor
    long countByDoctorId(UUID doctorId);

    // Count practices by practice type
    long countByPracticeType(String practiceType);

    // Count practices by address
    long countByAddressId(UUID addressId);

    // Check if slug exists
    boolean existsBySlug(String slug);

    // Check if doctor has practice at specific address
    boolean existsByDoctorIdAndAddressId(UUID doctorId, UUID addressId);

    // Find practices by creation date range
    List<Practice> findByCreatedAtBetween(Timestamp startDate, Timestamp endDate);

    // Find recent practices (last 30 days)
    @Query("SELECT p FROM Practice p WHERE p.createdAt >= :startDate ORDER BY p.createdAt DESC")
    List<Practice> findRecentPractices(@Param("startDate") Timestamp startDate);

    // Find practices by multiple criteria
    @Query("SELECT p FROM Practice p WHERE " +
           "(:doctorId IS NULL OR p.doctor.id = :doctorId) AND " +
           "(:practiceType IS NULL OR p.practiceType = :practiceType) AND " +
           "(:addressId IS NULL OR p.addressId = :addressId)")
    List<Practice> findByCriteria(@Param("doctorId") UUID doctorId,
                                 @Param("practiceType") String practiceType,
                                 @Param("addressId") UUID addressId);

    // Find all unique practice types
    @Query("SELECT DISTINCT p.practiceType FROM Practice p WHERE p.practiceType IS NOT NULL ORDER BY p.practiceType")
    List<String> findAllUniquePracticeTypes();

    // Find all unique address IDs
    @Query("SELECT DISTINCT p.addressId FROM Practice p")
    List<UUID> findAllUniqueAddressIds();

    // Find doctors with multiple practices
    @Query("SELECT p.doctor.id FROM Practice p GROUP BY p.doctor.id HAVING COUNT(p) > 1")
    List<UUID> findDoctorsWithMultiplePractices();

    // Find most common practice types
    @Query("SELECT p.practiceType, COUNT(p) as count FROM Practice p WHERE p.practiceType IS NOT NULL GROUP BY p.practiceType ORDER BY count DESC")
    List<Object[]> findMostCommonPracticeTypes();

    // Find practices with highest positions by doctor
    @Query("SELECT p FROM Practice p WHERE p.doctor.id = :doctorId AND p.practicePosition = (SELECT MAX(p2.practicePosition) FROM Practice p2 WHERE p2.doctor.id = :doctorId)")
    List<Practice> findHighestPositionPracticesByDoctorId(@Param("doctorId") UUID doctorId);

    // Additional missing methods
    Page<Practice> findByDoctorId(UUID doctorId, Pageable pageable);
    Page<Practice> findByPracticeNameContainingIgnoreCase(String practiceName, Pageable pageable);
    Page<Practice> findByPracticeType(String practiceType, Pageable pageable);
    Page<Practice> findByAddressId(UUID addressId, Pageable pageable);
    Page<Practice> findByCreatedAtBetween(Timestamp startDate, Timestamp endDate, Pageable pageable);
    Page<Practice> findByCreatedAtGreaterThanEqual(Timestamp startDate, Pageable pageable);
    boolean existsByDoctorId(UUID doctorId);
    void deleteByDoctorId(UUID doctorId);

    @Query("SELECT DISTINCT p.practiceType FROM Practice p WHERE p.practiceType IS NOT NULL ORDER BY p.practiceType")
    List<String> findDistinctPracticeTypes();

    @Query("SELECT DISTINCT p.addressId FROM Practice p WHERE p.addressId IS NOT NULL")
    List<UUID> findDistinctAddressIds();
}
