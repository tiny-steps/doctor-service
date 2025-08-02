package com.tintsteps.doctorsevice.service;

import com.tintsteps.doctorsevice.model.AwardRequestDto;
import com.tintsteps.doctorsevice.model.AwardResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for Award operations
 */
public interface AwardService {

    // CRUD Operations
    AwardResponseDto create(UUID doctorId, AwardRequestDto requestDto);
    AwardResponseDto findById(UUID id);
    Page<AwardResponseDto> findAll(Pageable pageable);
    AwardResponseDto update(UUID id, AwardRequestDto requestDto);
    AwardResponseDto partialUpdate(UUID id, AwardRequestDto requestDto);
    void delete(UUID id);

    // Doctor-specific Operations
    List<AwardResponseDto> findByDoctorId(UUID doctorId);
    List<AwardResponseDto> findByDoctorIdOrderByYear(UUID doctorId);
    Page<AwardResponseDto> findByDoctorId(UUID doctorId, Pageable pageable);

    // Search Operations
    Page<AwardResponseDto> findByTitle(String title, Pageable pageable);
    Page<AwardResponseDto> findByAwardedYear(Integer year, Pageable pageable);
    Page<AwardResponseDto> findByAwardedYearRange(Integer startYear, Integer endYear, Pageable pageable);
    List<AwardResponseDto> findByDoctorIdAndAwardedYear(UUID doctorId, Integer year);
    Page<AwardResponseDto> findRecentAwards(Integer startYear, Pageable pageable);

    // Validation Operations
    boolean existsById(UUID id);
    boolean existsByDoctorId(UUID doctorId);

    // Statistics Operations
    long countByDoctorId(UUID doctorId);
    long countByAwardedYear(Integer year);
    long countAll();

    // Bulk Operations
    List<AwardResponseDto> createBatch(UUID doctorId, List<AwardRequestDto> requestDtos);
    void deleteByDoctorId(UUID doctorId);
    void deleteBatch(List<UUID> ids);

    // Business Operations
    List<AwardResponseDto> findDoctorAwardsInYearRange(UUID doctorId, Integer startYear, Integer endYear);
    boolean hasDoctorReceivedAwardInYear(UUID doctorId, Integer year);
}
