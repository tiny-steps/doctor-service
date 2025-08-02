package com.tinysteps.doctorsevice.repository;

import com.tinysteps.doctorsevice.entity.Registration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RegistrationRepository extends JpaRepository<Registration, UUID> {

    // Find registrations by doctor ID
    List<Registration> findByDoctorId(UUID doctorId);

    // Find registrations by doctor ID ordered by registration year (most recent first)
    List<Registration> findByDoctorIdOrderByRegistrationYearDesc(UUID doctorId);

    // Find registrations by council name (case-insensitive)
    List<Registration> findByRegistrationCouncilNameContainingIgnoreCase(String councilName);

    // Find registrations by exact council name
    List<Registration> findByRegistrationCouncilName(String councilName);

    // Find registration by registration number
    Optional<Registration> findByRegistrationNumber(String registrationNumber);

    // Find registrations by registration year
    List<Registration> findByRegistrationYear(Integer year);

    // Find registrations by year range
    List<Registration> findByRegistrationYearBetween(Integer startYear, Integer endYear);

    // Find registrations by doctor and council
    List<Registration> findByDoctorIdAndRegistrationCouncilNameContainingIgnoreCase(UUID doctorId, String councilName);

    // Find registrations by doctor and registration number
    Optional<Registration> findByDoctorIdAndRegistrationNumber(UUID doctorId, String registrationNumber);

    // Count registrations by doctor
    long countByDoctorId(UUID doctorId);

    // Count registrations by council
    long countByRegistrationCouncilName(String councilName);

    // Check if registration number exists
    boolean existsByRegistrationNumber(String registrationNumber);

    // Check if doctor has registration with specific council
    @Query("SELECT COUNT(r) > 0 FROM Registration r WHERE r.doctor.id = :doctorId AND LOWER(r.registrationCouncilName) = LOWER(:councilName)")
    boolean existsByDoctorIdAndCouncilName(@Param("doctorId") UUID doctorId, @Param("councilName") String councilName);

    // Find all unique council names
    @Query("SELECT DISTINCT r.registrationCouncilName FROM Registration r WHERE r.registrationCouncilName IS NOT NULL ORDER BY r.registrationCouncilName")
    List<String> findAllUniqueCouncilNames();

    // Find recent registrations (last 10 years)
    @Query("SELECT r FROM Registration r WHERE r.registrationYear >= :startYear ORDER BY r.registrationYear DESC")
    List<Registration> findRecentRegistrations(@Param("startYear") Integer startYear);

    // Find registrations by doctor with year range
    @Query("SELECT r FROM Registration r WHERE r.doctor.id = :doctorId AND r.registrationYear BETWEEN :startYear AND :endYear ORDER BY r.registrationYear DESC")
    List<Registration> findByDoctorIdAndYearRange(@Param("doctorId") UUID doctorId,
                                                 @Param("startYear") Integer startYear,
                                                 @Param("endYear") Integer endYear);

    long countByRegistrationYear(Integer year);

    // Additional missing methods
    Page<Registration> findByDoctorId(UUID doctorId, Pageable pageable);
    Page<Registration> findByRegistrationCouncilNameContainingIgnoreCase(String councilName, Pageable pageable);
    Page<Registration> findByRegistrationYear(Integer year, Pageable pageable);
    Page<Registration> findByRegistrationYearBetween(Integer startYear, Integer endYear, Pageable pageable);

    Page<Registration> findByRegistrationYearGreaterThanEqual(Integer startYear, Pageable pageable);
    boolean existsByDoctorId(UUID doctorId);
    boolean existsByDoctorIdAndRegistrationCouncilNameContainingIgnoreCase(UUID doctorId, String councilName);
    long countByRegistrationCouncilNameContainingIgnoreCase(String councilName);
    void deleteByDoctorId(UUID doctorId);

    @Query("SELECT DISTINCT r.registrationCouncilName FROM Registration r WHERE r.registrationCouncilName IS NOT NULL ORDER BY r.registrationCouncilName")
    List<String> findDistinctCouncilNames();

    List<Registration> findByDoctorIdAndRegistrationYearBetween(UUID doctorId, Integer startYear, Integer endYear);

}
