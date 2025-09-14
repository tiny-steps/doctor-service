package com.tinysteps.doctorservice.repository;

import com.tinysteps.doctorservice.entity.Doctor;
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
public interface DoctorRepository extends JpaRepository<Doctor, UUID> {

        // Find by user ID
        Optional<Doctor> findByUserId(UUID userId);

        // Find by slug
        Optional<Doctor> findBySlug(String slug);

        // Find by name (case-insensitive)
        List<Doctor> findByNameContainingIgnoreCase(String name);

        // Find by status
        List<Doctor> findByStatus(String status);

        // Find by verification status
        List<Doctor> findByIsVerified(Boolean isVerified);

        // Find by gender
        List<Doctor> findByGender(String gender);

        // Find by experience years range
        List<Doctor> findByExperienceYearsBetween(Integer minYears, Integer maxYears);

        // Find by minimum rating
        List<Doctor> findByRatingAverageGreaterThanEqual(BigDecimal minRating);

        // Find verified doctors with minimum rating
        @Query("SELECT d FROM Doctor d WHERE d.isVerified = true AND d.ratingAverage >= :minRating ORDER BY d.ratingAverage DESC")
        List<Doctor> findVerifiedDoctorsWithMinRating(@Param("minRating") BigDecimal minRating);

        // Find top-rated doctors
        @Query("SELECT d FROM Doctor d WHERE d.isVerified = true ORDER BY d.ratingAverage DESC, d.reviewCount DESC")
        List<Doctor> findTopRatedDoctors();

        // Find doctors by specialization
        @Query("SELECT DISTINCT d FROM Doctor d JOIN d.specializations s WHERE s.speciality = :speciality")
        List<Doctor> findBySpeciality(@Param("speciality") String speciality);

        // Find doctors by address location using DoctorAddress relationship
        @Query("SELECT DISTINCT d FROM Doctor d JOIN d.doctorAddresses da WHERE da.addressId = :addressId")
        List<Doctor> findByAddressLocation(@Param("addressId") UUID addressId);

        // Find doctors by address location with pagination
        @Query("SELECT DISTINCT d FROM Doctor d JOIN d.doctorAddresses da WHERE da.addressId = :addressId")
        Page<Doctor> findByAddressLocation(@Param("addressId") UUID addressId, Pageable pageable);

        // Find doctors by address location and practice role
        @Query("SELECT DISTINCT d FROM Doctor d JOIN d.doctorAddresses da WHERE da.addressId = :addressId AND da.practiceRole = :practiceRole")
        List<Doctor> findByAddressLocationAndPracticeRole(@Param("addressId") UUID addressId,
                        @Param("practiceRole") String practiceRole);

        // Find doctors by address location and practice role with pagination
        @Query("SELECT DISTINCT d FROM Doctor d JOIN d.doctorAddresses da WHERE da.addressId = :addressId AND da.practiceRole = :practiceRole")
        Page<Doctor> findByAddressLocationAndPracticeRole(@Param("addressId") UUID addressId,
                        @Param("practiceRole") String practiceRole, Pageable pageable);

        // Search doctors by multiple criteria
        @Query("SELECT DISTINCT d FROM Doctor d " +
                        "LEFT JOIN d.specializations s " +
                        "WHERE (:name IS NULL OR LOWER(d.name) LIKE LOWER(CONCAT('%', :name, '%'))) " +
                        "AND (:speciality IS NULL OR s.speciality = :speciality) " +
                        "AND (:isVerified IS NULL OR d.isVerified = :isVerified) " +
                        "AND (:minRating IS NULL OR d.ratingAverage >= :minRating) " +
                        "AND d.status = 'ACTIVE'")
        List<Doctor> searchDoctors(@Param("name") String name,
                        @Param("speciality") String speciality,
                        @Param("isVerified") Boolean isVerified,
                        @Param("minRating") BigDecimal minRating);

        // Count doctors by status
        long countByStatus(String status);

        // Count verified doctors
        long countByIsVerified(Boolean isVerified);

        // Check if slug exists
        boolean existsBySlug(String slug);

        // Check if user ID exists
        boolean existsByUserId(UUID userId);

        // Pageable versions
        Page<Doctor> findByStatus(String status, Pageable pageable);

        Page<Doctor> findByIsVerified(Boolean isVerified, Pageable pageable);

        Page<Doctor> findByGender(String gender, Pageable pageable);

        Page<Doctor> findByExperienceYearsBetween(Integer minYears, Integer maxYears, Pageable pageable);

        Page<Doctor> findByRatingAverageGreaterThanEqual(BigDecimal minRating, Pageable pageable);

        Page<Doctor> findAllByOrderByRatingAverageDesc(Pageable pageable);

        Page<Doctor> findByIsVerifiedAndRatingAverageGreaterThanEqual(Boolean isVerified, BigDecimal minRating,
                        Pageable pageable);

        // Branch-based queries
        List<Doctor> findByPrimaryBranchId(UUID primaryBranchId);

        Page<Doctor> findByPrimaryBranchId(UUID primaryBranchId, Pageable pageable);

        // Find doctors by primary branch and status
        List<Doctor> findByPrimaryBranchIdAndStatus(UUID primaryBranchId, String status);

        Page<Doctor> findByPrimaryBranchIdAndStatus(UUID primaryBranchId, String status, Pageable pageable);

        // Find multi-branch doctors
        List<Doctor> findByIsMultiBranch(Boolean isMultiBranch);

        Page<Doctor> findByIsMultiBranch(Boolean isMultiBranch, Pageable pageable);

        // Combined branch and verification queries
        Page<Doctor> findByPrimaryBranchIdAndIsVerified(UUID primaryBranchId, Boolean isVerified, Pageable pageable);

        // Count by branch
        long countByPrimaryBranchId(UUID primaryBranchId);

        long countByPrimaryBranchIdAndStatus(UUID primaryBranchId, String status);

        // Additional branch-based count methods
        long countByPrimaryBranchIdAndIsVerified(UUID primaryBranchId, Boolean isVerified);

        long countByPrimaryBranchIdAndGender(UUID primaryBranchId, String gender);

        long countByPrimaryBranchIdAndExperienceYearsGreaterThanEqual(UUID primaryBranchId, Integer minYears);

        long countByPrimaryBranchIdAndExperienceYearsLessThan(UUID primaryBranchId, Integer maxYears);

        long countByPrimaryBranchIdAndExperienceYearsBetween(UUID primaryBranchId, Integer minYears, Integer maxYears);

        // Methods for statistics across multiple branches
        long countByPrimaryBranchIdIn(List<UUID> branchIds);

        long countByPrimaryBranchIdInAndIsVerified(List<UUID> branchIds, Boolean isVerified);

        long countByPrimaryBranchIdInAndStatus(List<UUID> branchIds, String status);

        long countByPrimaryBranchIdInAndGender(List<UUID> branchIds, String gender);

        // Average rating queries
        @Query("SELECT AVG(d.ratingAverage) FROM Doctor d WHERE d.primaryBranchId = :branchId")
        Double findAverageRatingByPrimaryBranchId(@Param("branchId") UUID branchId);

        @Query("SELECT AVG(d.ratingAverage) FROM Doctor d WHERE d.primaryBranchId IN :branchIds")
        Double findAverageRatingByPrimaryBranchIdIn(@Param("branchIds") List<UUID> branchIds);

        @Query("SELECT DISTINCT d FROM Doctor d LEFT JOIN d.doctorAddresses da WHERE d.primaryBranchId = :branchId OR da.addressId = :branchId")
        Page<Doctor> findByPrimaryOrAssociatedBranch(@Param("branchId") UUID branchId, Pageable pageable);

        @Query("SELECT DISTINCT d FROM Doctor d LEFT JOIN d.doctorAddresses da WHERE d.primaryBranchId = :branchId OR da.addressId = :branchId")
        List<Doctor> findByPrimaryOrAssociatedBranch(@Param("branchId") UUID branchId);

        // Include doctors with NULL primaryBranchId (legacy doctors) in branch-specific
        // queries - but only when querying for a specific branch
        @Query("SELECT DISTINCT d FROM Doctor d LEFT JOIN d.doctorAddresses da WHERE d.primaryBranchId = :branchId OR da.addressId = :branchId OR d.primaryBranchId IS NULL")
        Page<Doctor> findByPrimaryOrAssociatedBranchOrNull(@Param("branchId") UUID branchId, Pageable pageable);

        @Query("SELECT DISTINCT d FROM Doctor d LEFT JOIN d.doctorAddresses da WHERE d.primaryBranchId = :branchId OR da.addressId = :branchId OR d.primaryBranchId IS NULL")
        List<Doctor> findByPrimaryOrAssociatedBranchOrNull(@Param("branchId") UUID branchId);
}
