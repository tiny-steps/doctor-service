package com.tintsteps.doctorsevice.service;

import com.tintsteps.doctorsevice.model.QualificationRequestDto;
import com.tintsteps.doctorsevice.model.QualificationResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for Qualification operations
 */
public interface QualificationService {
    
    // CRUD Operations
    QualificationResponseDto create(UUID doctorId, QualificationRequestDto requestDto);
    QualificationResponseDto findById(UUID id);
    Page<QualificationResponseDto> findAll(Pageable pageable);
    QualificationResponseDto update(UUID id, QualificationRequestDto requestDto);
    QualificationResponseDto partialUpdate(UUID id, QualificationRequestDto requestDto);
    void delete(UUID id);
    
    // Doctor-specific Operations
    List<QualificationResponseDto> findByDoctorId(UUID doctorId);
    List<QualificationResponseDto> findByDoctorIdOrderByYear(UUID doctorId);
    Page<QualificationResponseDto> findByDoctorId(UUID doctorId, Pageable pageable);
    
    // Search Operations
    Page<QualificationResponseDto> findByQualificationName(String qualificationName, Pageable pageable);
    Page<QualificationResponseDto> findByCollegeName(String collegeName, Pageable pageable);
    Page<QualificationResponseDto> findByCompletionYear(Integer year, Pageable pageable);
    Page<QualificationResponseDto> findByYearRange(Integer startYear, Integer endYear, Pageable pageable);
    List<QualificationResponseDto> findByDoctorIdAndQualificationName(UUID doctorId, String qualificationName);
    Page<QualificationResponseDto> findRecentQualifications(Integer startYear, Pageable pageable);
    
    // Validation Operations
    boolean existsById(UUID id);
    boolean existsByDoctorId(UUID doctorId);
    boolean hasDoctorQualification(UUID doctorId, String qualificationName);
    
    // Statistics Operations
    long countByDoctorId(UUID doctorId);
    long countByQualificationName(String qualificationName);
    long countByCollegeName(String collegeName);
    long countAll();
    
    // Bulk Operations
    List<QualificationResponseDto> createBatch(UUID doctorId, List<QualificationRequestDto> requestDtos);
    void deleteByDoctorId(UUID doctorId);
    void deleteBatch(List<UUID> ids);
    
    // Business Operations
    List<QualificationResponseDto> findDoctorQualificationsInYearRange(UUID doctorId, Integer startYear, Integer endYear);
    List<String> getUniqueQualificationNames();
    List<String> getUniqueCollegeNames();
    boolean isDoctorQualifiedInField(UUID doctorId, String field);
}
