package com.tinysteps.doctorservice.repository;

import com.tinysteps.doctorservice.entity.DoctorSpecialization;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DoctorSpecializationRepository extends JpaRepository<DoctorSpecialization, UUID> {

    // Find specializations by doctor ID
    List<DoctorSpecialization> findByDoctorId(UUID doctorId);

    // Find specializations by specialization master
    List<DoctorSpecialization> findBySpecializationMasterId(UUID specializationId);

    // Count specializations by doctor
    long countByDoctorId(UUID doctorId);

    // Check if doctor has specializations
    boolean existsByDoctorId(UUID doctorId);

    // Delete all specializations for a doctor (with flush for updates)
    @Modifying
    @Query("DELETE FROM DoctorSpecialization ds WHERE ds.doctor.id = :doctorId")
    void deleteByDoctorId(@Param("doctorId") UUID doctorId);

    // Find all specializations with pagination
    Page<DoctorSpecialization> findAll(Pageable pageable);

    // Find specializations by doctor with pagination
    Page<DoctorSpecialization> findByDoctorId(UUID doctorId, Pageable pageable);
}

