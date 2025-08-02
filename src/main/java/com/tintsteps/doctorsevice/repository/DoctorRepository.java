package com.tintsteps.doctorsevice.repository;

import com.tintsteps.doctorsevice.entity.Doctor;
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
    
    // Find doctors by location (through practices)
    @Query("SELECT DISTINCT d FROM Doctor d JOIN d.practices p WHERE p.addressId = :addressId")
    List<Doctor> findByPracticeLocation(@Param("addressId") UUID addressId);
    
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
    Page<Doctor> findByIsVerifiedAndRatingAverageGreaterThanEqual(Boolean isVerified, BigDecimal minRating, Pageable pageable);
}
