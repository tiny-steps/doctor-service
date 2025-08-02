package com.tintsteps.doctorsevice.service;

import com.tintsteps.doctorsevice.model.SpecializationRequestDto;
import com.tintsteps.doctorsevice.model.SpecializationResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for Specialization operations
 */
public interface SpecializationService {
    
    // CRUD Operations
    SpecializationResponseDto create(UUID doctorId, SpecializationRequestDto requestDto);
    SpecializationResponseDto findById(UUID id);
    Page<SpecializationResponseDto> findAll(Pageable pageable);
    SpecializationResponseDto update(UUID id, SpecializationRequestDto requestDto);
    SpecializationResponseDto partialUpdate(UUID id, SpecializationRequestDto requestDto);
    void delete(UUID id);
    
    // Doctor-specific Operations
    List<SpecializationResponseDto> findByDoctorId(UUID doctorId);
    Page<SpecializationResponseDto> findByDoctorId(UUID doctorId, Pageable pageable);
    
    // Search Operations
    Page<SpecializationResponseDto> findBySpeciality(String speciality, Pageable pageable);
    Page<SpecializationResponseDto> findBySubspecialization(String subspecialization, Pageable pageable);
    List<SpecializationResponseDto> findByDoctorIdAndSpeciality(UUID doctorId, String speciality);
    Page<SpecializationResponseDto> findBySpecialityPattern(String pattern, Pageable pageable);
    List<String> findDistinctSpecialities();
    List<String> findDistinctSubspecializations();
    List<String> findSubspecializationsBySpeciality(String speciality);
    
    // Validation Operations
    boolean existsById(UUID id);
    boolean existsByDoctorId(UUID doctorId);
    boolean existsByDoctorIdAndSpeciality(UUID doctorId, String speciality);
    boolean hasSpecialization(UUID doctorId, String speciality);
    
    // Statistics Operations
    long countByDoctorId(UUID doctorId);
    long countBySpeciality(String speciality);
    long countBySubspecialization(String subspecialization);
    long countAll();
    Object[] getSpecializationStatistics();
    
    // Bulk Operations
    List<SpecializationResponseDto> createBatch(UUID doctorId, List<SpecializationRequestDto> requestDtos);
    void deleteByDoctorId(UUID doctorId);
    void deleteBatch(List<UUID> ids);
    
    // Business Operations
    List<UUID> findDoctorsBySpeciality(String speciality);
    List<UUID> findDoctorsBySubspecialization(String subspecialization);
    List<UUID> findDoctorsWithMultipleSpecializations();
    List<UUID> findDoctorsWithoutSpecializations();
    boolean isDoctorSpecializedIn(UUID doctorId, String speciality);
}