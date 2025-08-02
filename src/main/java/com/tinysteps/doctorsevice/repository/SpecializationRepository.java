package com.tinysteps.doctorsevice.repository;

import com.tinysteps.doctorsevice.entity.Specialization;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SpecializationRepository extends JpaRepository<Specialization, UUID> {

    // Find specializations by doctor ID
    List<Specialization> findByDoctorId(UUID doctorId);

    // Find specializations by speciality (case-insensitive)
    List<Specialization> findBySpecialityContainingIgnoreCase(String speciality);

    // Find specializations by exact speciality
    List<Specialization> findBySpeciality(String speciality);

    // Find specializations by subspecialization (case-insensitive)
    List<Specialization> findBySubspecializationContainingIgnoreCase(String subspecialization);

    // Find specializations by exact subspecialization
    List<Specialization> findBySubspecialization(String subspecialization);

    // Find specializations by doctor and speciality
    List<Specialization> findByDoctorIdAndSpecialityContainingIgnoreCase(UUID doctorId, String speciality);

    // Find specializations by doctor and subspecialization
    List<Specialization> findByDoctorIdAndSubspecializationContainingIgnoreCase(UUID doctorId, String subspecialization);

    // Count specializations by doctor
    long countByDoctorId(UUID doctorId);

    // Count doctors by speciality
    long countBySpeciality(String speciality);

    // Count doctors by subspecialization
    long countBySubspecialization(String subspecialization);

    // Check if doctor has specific speciality
    @Query("SELECT COUNT(s) > 0 FROM Specialization s WHERE s.doctor.id = :doctorId AND LOWER(s.speciality) = LOWER(:speciality)")
    boolean existsByDoctorIdAndSpeciality(@Param("doctorId") UUID doctorId, @Param("speciality") String speciality);

    // Check if doctor has specific subspecialization
    @Query("SELECT COUNT(s) > 0 FROM Specialization s WHERE s.doctor.id = :doctorId AND LOWER(s.subspecialization) = LOWER(:subspecialization)")
    boolean existsByDoctorIdAndSubspecialization(@Param("doctorId") UUID doctorId, @Param("subspecialization") String subspecialization);

    // Find all unique specialities
    @Query("SELECT DISTINCT s.speciality FROM Specialization s ORDER BY s.speciality")
    List<String> findAllUniqueSpecialities();

    // Find all unique subspecializations
    @Query("SELECT DISTINCT s.subspecialization FROM Specialization s WHERE s.subspecialization IS NOT NULL ORDER BY s.subspecialization")
    List<String> findAllUniqueSubspecializations();

    // Find subspecializations for a specific speciality
    @Query("SELECT DISTINCT s.subspecialization FROM Specialization s WHERE LOWER(s.speciality) = LOWER(:speciality) AND s.subspecialization IS NOT NULL ORDER BY s.subspecialization")
    List<String> findSubspecializationsBySpeciality(@Param("speciality") String speciality);

    // Find doctors with multiple specializations
    @Query("SELECT s.doctor.id FROM Specialization s GROUP BY s.doctor.id HAVING COUNT(s) > 1")
    List<UUID> findDoctorsWithMultipleSpecializations();

    // Find most common specialities
    @Query("SELECT s.speciality, COUNT(s) as count FROM Specialization s GROUP BY s.speciality ORDER BY count DESC")
    List<Object[]> findMostCommonSpecialities();

    // Find specializations with both speciality and subspecialization
    @Query("SELECT s FROM Specialization s WHERE s.speciality IS NOT NULL AND s.subspecialization IS NOT NULL")
    List<Specialization> findSpecializationsWithSubspecialization();

    // Additional missing methods
    Page<Specialization> findByDoctorId(UUID doctorId, Pageable pageable);
    Page<Specialization> findBySpecialityContainingIgnoreCase(String speciality, Pageable pageable);
    Page<Specialization> findBySubspecializationContainingIgnoreCase(String subSpecialization, Pageable pageable);
    boolean existsByDoctorId(UUID doctorId);
    long countBySpecialityContainingIgnoreCase(String speciality);
    void deleteByDoctorId(UUID doctorId);

    @Query("SELECT DISTINCT s.speciality FROM Specialization s WHERE s.speciality IS NOT NULL ORDER BY s.speciality")
    List<String> findDistinctSpecialities();

    @Query("SELECT DISTINCT s.subspecialization FROM Specialization s WHERE s.subspecialization IS NOT NULL ORDER BY s.subspecialization")
    List<String> findDistinctSubSpecializations();

    boolean existsByDoctorIdAndSpecialityContainingIgnoreCase(UUID doctorId, String speciality);

    long countBySubspecializationContainingIgnoreCase(String subSpecialization);

    @Query("SELECT COUNT(DISTINCT s.speciality) FROM Specialization s WHERE s.speciality IS NOT NULL")
    long countDistinctSpecialities();

    @Query("SELECT DISTINCT s.doctor.id FROM Specialization s WHERE LOWER(s.speciality) = LOWER(:speciality)")
    List<UUID> findDoctorIdsBySpeciality(@Param("speciality") String speciality);

    @Query("SELECT DISTINCT s.doctor.id FROM Specialization s WHERE LOWER(s.subspecialization) = LOWER(:subspecialization)")
    List<UUID> findDoctorIdsBySubspecialization(@Param("subspecialization") String subspecialization);

    @Query("SELECT d.id FROM Doctor d WHERE d.id NOT IN (SELECT DISTINCT s.doctor.id FROM Specialization s)")
    List<UUID> findDoctorsWithoutSpecializations();
}
