package com.tinysteps.doctorservice.repository;

import com.tinysteps.doctorservice.entity.DoctorSpecialization;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SpecializationRepository extends JpaRepository<DoctorSpecialization, UUID> {

    // Find specializations by doctor ID
    List<DoctorSpecialization> findByDoctorId(UUID doctorId);

    // Find specializations by speciality (case-insensitive) - DEPRECATED: use
    // SpecializationMaster
    @Deprecated
    List<DoctorSpecialization> findBySpecialityContainingIgnoreCase(String speciality);

    // Find specializations by exact speciality - DEPRECATED: use
    // SpecializationMaster
    @Deprecated
    List<DoctorSpecialization> findBySpeciality(String speciality);

    // Find specializations by subspecialization (case-insensitive)
    List<DoctorSpecialization> findBySubspecializationContainingIgnoreCase(String subspecialization);

    // Find specializations by exact subspecialization
    List<DoctorSpecialization> findBySubspecialization(String subspecialization);

    // Find specializations by doctor and speciality - DEPRECATED: use
    // SpecializationMaster
    @Deprecated
    List<DoctorSpecialization> findByDoctorIdAndSpecialityContainingIgnoreCase(UUID doctorId, String speciality);

    // Find specializations by doctor and subspecialization
    List<DoctorSpecialization> findByDoctorIdAndSubspecializationContainingIgnoreCase(UUID doctorId,
            String subspecialization);

    // Count specializations by doctor
    long countByDoctorId(UUID doctorId);

    // Count doctors by speciality
    long countBySpeciality(String speciality);

    // Count doctors by subspecialization
    long countBySubspecialization(String subspecialization);

    // Check if doctor has specific speciality - DEPRECATED: use
    // SpecializationMaster
    @Deprecated
    @Query("SELECT COUNT(s) > 0 FROM DoctorSpecialization s WHERE s.doctor.id = :doctorId AND LOWER(s.speciality) = LOWER(:speciality)")
    boolean existsByDoctorIdAndSpeciality(@Param("doctorId") UUID doctorId, @Param("speciality") String speciality);

    // Check if doctor has specific subspecialization
    @Query("SELECT COUNT(s) > 0 FROM DoctorSpecialization s WHERE s.doctor.id = :doctorId AND LOWER(s.subspecialization) = LOWER(:subspecialization)")
    boolean existsByDoctorIdAndSubspecialization(@Param("doctorId") UUID doctorId,
            @Param("subspecialization") String subspecialization);

    // Find all unique specialities - DEPRECATED: use SpecializationMasterRepository
    @Deprecated
    @Query("SELECT DISTINCT s.speciality FROM DoctorSpecialization s WHERE s.speciality IS NOT NULL ORDER BY s.speciality")
    List<String> findAllUniqueSpecialities();

    // Find all unique subspecializations
    @Query("SELECT DISTINCT s.subspecialization FROM DoctorSpecialization s WHERE s.subspecialization IS NOT NULL ORDER BY s.subspecialization")
    List<String> findAllUniqueSubspecializations();

    // Find subspecializations for a specific speciality - DEPRECATED
    @Deprecated
    @Query("SELECT DISTINCT s.subspecialization FROM DoctorSpecialization s WHERE LOWER(s.speciality) = LOWER(:speciality) AND s.subspecialization IS NOT NULL ORDER BY s.subspecialization")
    List<String> findSubspecializationsBySpeciality(@Param("speciality") String speciality);

    // Find doctors with multiple specializations
    @Query("SELECT s.doctor.id FROM DoctorSpecialization s GROUP BY s.doctor.id HAVING COUNT(s) > 1")
    List<UUID> findDoctorsWithMultipleSpecializations();

    // Find most common specialities - DEPRECATED: use
    // SpecializationMasterRepository
    @Deprecated
    @Query("SELECT s.speciality, COUNT(s) as count FROM DoctorSpecialization s WHERE s.speciality IS NOT NULL GROUP BY s.speciality ORDER BY count DESC")
    List<Object[]> findMostCommonSpecialities();

    // Find specializations with both speciality and subspecialization
    @Query("SELECT s FROM DoctorSpecialization s WHERE s.specializationMaster IS NOT NULL AND s.subspecialization IS NOT NULL")
    List<DoctorSpecialization> findSpecializationsWithSubspecialization();

    // Additional missing methods
    Page<DoctorSpecialization> findByDoctorId(UUID doctorId, Pageable pageable);

    @Deprecated
    Page<DoctorSpecialization> findBySpecialityContainingIgnoreCase(String speciality, Pageable pageable);

    Page<DoctorSpecialization> findBySubspecializationContainingIgnoreCase(String subSpecialization, Pageable pageable);

    boolean existsByDoctorId(UUID doctorId);

    @Deprecated
    long countBySpecialityContainingIgnoreCase(String speciality);

    void deleteByDoctorId(UUID doctorId);

    @Deprecated
    @Query("SELECT DISTINCT s.speciality FROM DoctorSpecialization s WHERE s.speciality IS NOT NULL ORDER BY s.speciality")
    List<String> findDistinctSpecialities();

    @Query("SELECT DISTINCT s.subspecialization FROM DoctorSpecialization s WHERE s.subspecialization IS NOT NULL ORDER BY s.subspecialization")
    List<String> findDistinctSubSpecializations();

    @Deprecated
    boolean existsByDoctorIdAndSpecialityContainingIgnoreCase(UUID doctorId, String speciality);

    long countBySubspecializationContainingIgnoreCase(String subSpecialization);

    @Deprecated
    @Query("SELECT COUNT(DISTINCT s.speciality) FROM DoctorSpecialization s WHERE s.speciality IS NOT NULL")
    long countDistinctSpecialities();

    @Deprecated
    @Query("SELECT DISTINCT s.doctor.id FROM DoctorSpecialization s WHERE LOWER(s.speciality) = LOWER(:speciality)")
    List<UUID> findDoctorIdsBySpeciality(@Param("speciality") String speciality);

    @Query("SELECT DISTINCT s.doctor.id FROM DoctorSpecialization s WHERE LOWER(s.subspecialization) = LOWER(:subspecialization)")
    List<UUID> findDoctorIdsBySubspecialization(@Param("subspecialization") String subspecialization);

    @Query("SELECT d.id FROM Doctor d WHERE d.id NOT IN (SELECT DISTINCT s.doctor.id FROM DoctorSpecialization s)")
    List<UUID> findDoctorsWithoutSpecializations();

    // Find specialization by ID with doctor eagerly loaded
    @Query("SELECT s FROM DoctorSpecialization s JOIN FETCH s.doctor WHERE s.id = :id")
    java.util.Optional<DoctorSpecialization> findByIdWithDoctor(@Param("id") UUID id);
}
