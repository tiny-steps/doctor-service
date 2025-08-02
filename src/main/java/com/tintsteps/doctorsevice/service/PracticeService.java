package com.tintsteps.doctorsevice.service;

import com.tintsteps.doctorsevice.model.PracticeRequestDto;
import com.tintsteps.doctorsevice.model.PracticeResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

/**
 * Service interface for Practice operations
 */
public interface PracticeService {
    
    // CRUD Operations
    PracticeResponseDto create(UUID doctorId, PracticeRequestDto requestDto);
    PracticeResponseDto findById(UUID id);
    Page<PracticeResponseDto> findAll(Pageable pageable);
    PracticeResponseDto update(UUID id, PracticeRequestDto requestDto);
    PracticeResponseDto partialUpdate(UUID id, PracticeRequestDto requestDto);
    void delete(UUID id);
    
    // Doctor-specific Operations
    List<PracticeResponseDto> findByDoctorId(UUID doctorId);
    List<PracticeResponseDto> findByDoctorIdOrderByPosition(UUID doctorId);
    List<PracticeResponseDto> findByDoctorIdOrderByCreatedAt(UUID doctorId);
    Page<PracticeResponseDto> findByDoctorId(UUID doctorId, Pageable pageable);
    
    // Search Operations
    Page<PracticeResponseDto> findByPracticeName(String practiceName, Pageable pageable);
    Page<PracticeResponseDto> findByPracticeType(String practiceType, Pageable pageable);
    Page<PracticeResponseDto> findByAddressId(UUID addressId, Pageable pageable);
    PracticeResponseDto findBySlug(String slug);
    List<PracticeResponseDto> findByDoctorIdAndPracticeType(UUID doctorId, String practiceType);
    List<PracticeResponseDto> findByDoctorIdAndAddressId(UUID doctorId, UUID addressId);
    
    // Temporal Operations
    Page<PracticeResponseDto> findByCreatedAtBetween(Timestamp startDate, Timestamp endDate, Pageable pageable);
    Page<PracticeResponseDto> findRecentPractices(Timestamp startDate, Pageable pageable);
    
    // Multi-criteria Search
    Page<PracticeResponseDto> findByCriteria(UUID doctorId, String practiceType, UUID addressId, Pageable pageable);
    
    // Validation Operations
    boolean existsById(UUID id);
    boolean existsByDoctorId(UUID doctorId);
    boolean existsBySlug(String slug);
    boolean hasDoctorPracticeAtAddress(UUID doctorId, UUID addressId);
    boolean isSlugAvailable(String slug);
    
    // Statistics Operations
    long countByDoctorId(UUID doctorId);
    long countByPracticeType(String practiceType);
    long countByAddressId(UUID addressId);
    long countAll();
    
    // Bulk Operations
    List<PracticeResponseDto> createBatch(UUID doctorId, List<PracticeRequestDto> requestDtos);
    void deleteByDoctorId(UUID doctorId);
    void deleteBatch(List<UUID> ids);
    
    // Business Operations
    List<String> getUniquePracticeTypes();
    List<UUID> getUniqueAddressIds();
    List<UUID> findDoctorsWithMultiplePractices();
    List<Object[]> getMostCommonPracticeTypes();
    List<PracticeResponseDto> findHighestPositionPracticesByDoctorId(UUID doctorId);
    PracticeResponseDto updatePracticePosition(UUID id, Integer newPosition);
    void reorderPractices(UUID doctorId, List<UUID> practiceIds);
}
