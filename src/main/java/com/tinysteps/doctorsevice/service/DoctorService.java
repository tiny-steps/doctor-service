package com.tinysteps.doctorsevice.service;

import com.tinysteps.doctorsevice.model.DoctorDto;
import com.tinysteps.doctorsevice.model.DoctorRequestDto;
import com.tinysteps.doctorsevice.model.DoctorResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Service interface for Doctor operations
 */
public interface DoctorService {

    // CRUD Operations
    DoctorResponseDto create(DoctorRequestDto requestDto);

    // Add registration method
    DoctorResponseDto registerDoctor(DoctorDto requestDto);
    DoctorResponseDto findById(UUID id);
    Page<DoctorResponseDto> findAll(Pageable pageable);
    DoctorResponseDto update(UUID id, DoctorRequestDto requestDto);
    DoctorResponseDto partialUpdate(UUID id, DoctorRequestDto requestDto);
    void delete(UUID id);

    // Search Operations
    DoctorResponseDto findBySlug(String slug);
    DoctorResponseDto findByUserId(UUID userId);
    List<DoctorResponseDto> findByName(String name);
    Page<DoctorResponseDto> findByStatus(String status, Pageable pageable);
    Page<DoctorResponseDto> findByVerificationStatus(Boolean isVerified, Pageable pageable);
    Page<DoctorResponseDto> findByGender(String gender, Pageable pageable);
    Page<DoctorResponseDto> findByExperienceRange(Integer minYears, Integer maxYears, Pageable pageable);
    Page<DoctorResponseDto> findByMinRating(BigDecimal minRating, Pageable pageable);
    Page<DoctorResponseDto> findBySpeciality(String speciality, Pageable pageable);
    Page<DoctorResponseDto> findByLocation(UUID addressId, Pageable pageable);
    Page<DoctorResponseDto> findByLocationAndPracticeRole(UUID addressId, String practiceRole, Pageable pageable);
    
    // Branch-based Operations
    Page<DoctorResponseDto> findByBranch(UUID primaryBranchId, Pageable pageable);
    List<DoctorResponseDto> findByBranch(UUID primaryBranchId);
    Page<DoctorResponseDto> findByBranchAndStatus(UUID primaryBranchId, String status, Pageable pageable);
    List<DoctorResponseDto> findByBranchAndStatus(UUID primaryBranchId, String status);
    Page<DoctorResponseDto> findByBranchAndVerificationStatus(UUID primaryBranchId, Boolean isVerified, Pageable pageable);
    List<DoctorResponseDto> findByBranchAndVerificationStatus(UUID primaryBranchId, Boolean isVerified);
    Page<DoctorResponseDto> findMultiBranchDoctors(Pageable pageable);
    List<DoctorResponseDto> findMultiBranchDoctors();
    List<DoctorResponseDto> findDoctorsByCurrentUserBranch();
    Page<DoctorResponseDto> findDoctorsByCurrentUserBranch(Pageable pageable);

    // Advanced Search
    Page<DoctorResponseDto> searchDoctors(String name, String speciality, Boolean isVerified,
                                         BigDecimal minRating, Pageable pageable);
    Page<DoctorResponseDto> findTopRatedDoctors(Pageable pageable);
    Page<DoctorResponseDto> findVerifiedDoctorsWithMinRating(BigDecimal minRating, Pageable pageable);

    // Business Operations
    DoctorResponseDto verifyDoctor(UUID id);
    DoctorResponseDto unverifyDoctor(UUID id);
    DoctorResponseDto activateDoctor(UUID id);
    DoctorResponseDto deactivateDoctor(UUID id);
    DoctorResponseDto suspendDoctor(UUID id);
    void updateRatingAndReviewCount(UUID id, BigDecimal newRating, Integer reviewCount);

    // Validation Operations
    boolean existsById(UUID id);
    boolean existsBySlug(String slug);
    boolean existsByUserId(UUID userId);
    boolean isSlugAvailable(String slug);
    boolean isDoctorVerified(UUID id);
    boolean isDoctorActive(UUID id);

    // Statistics Operations
    long countAll();
    long countByStatus(String status);
    long countByVerificationStatus(Boolean isVerified);
    long countBySpeciality(String speciality);
    long countByBranch(UUID primaryBranchId);
    long countByBranchAndStatus(UUID primaryBranchId, String status);

    // Bulk Operations
    List<DoctorResponseDto> createBatch(List<DoctorRequestDto> requestDtos);
    void deleteBatch(List<UUID> ids);

    // Profile Completeness
    int calculateProfileCompleteness(UUID id);
    boolean isProfileComplete(UUID id);
    List<String> getMissingProfileFields(UUID id);
}
