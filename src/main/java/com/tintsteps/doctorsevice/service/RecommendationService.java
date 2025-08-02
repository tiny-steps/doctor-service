package com.tintsteps.doctorsevice.service;

import com.tintsteps.doctorsevice.model.RecommendationRequestDto;
import com.tintsteps.doctorsevice.model.RecommendationResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Service interface for Recommendation operations
 */
public interface RecommendationService {
    
    // CRUD Operations
    RecommendationResponseDto create(UUID doctorId, RecommendationRequestDto requestDto);
    RecommendationResponseDto findById(UUID id);
    Page<RecommendationResponseDto> findAll(Pageable pageable);
    RecommendationResponseDto update(UUID id, RecommendationRequestDto requestDto);
    RecommendationResponseDto partialUpdate(UUID id, RecommendationRequestDto requestDto);
    void delete(UUID id);
    
    // Doctor-specific Operations
    List<RecommendationResponseDto> findByDoctorId(UUID doctorId);
    List<RecommendationResponseDto> findByDoctorIdOrderByRating(UUID doctorId);
    List<RecommendationResponseDto> findByDoctorIdOrderByRecommendationCount(UUID doctorId);
    Page<RecommendationResponseDto> findByDoctorId(UUID doctorId, Pageable pageable);
    
    // Rating Operations
    Page<RecommendationResponseDto> findByRating(BigDecimal rating, Pageable pageable);
    Page<RecommendationResponseDto> findByMinRating(BigDecimal minRating, Pageable pageable);
    Page<RecommendationResponseDto> findByMaxRating(BigDecimal maxRating, Pageable pageable);
    Page<RecommendationResponseDto> findByRatingRange(BigDecimal minRating, BigDecimal maxRating, Pageable pageable);
    List<RecommendationResponseDto> findByDoctorIdAndMinRating(UUID doctorId, BigDecimal minRating);
    
    // Recommendation Count Operations
    Page<RecommendationResponseDto> findByMinRecommendationCount(Integer minCount, Pageable pageable);
    
    // Review Operations
    Page<RecommendationResponseDto> findByReviewText(String reviewText, Pageable pageable);
    Page<RecommendationResponseDto> findRecommendationsWithReviews(Pageable pageable);
    Page<RecommendationResponseDto> findRecommendationsWithoutReviews(Pageable pageable);
    
    // Top Recommendations
    Page<RecommendationResponseDto> findHighestRatedRecommendations(Pageable pageable);
    Page<RecommendationResponseDto> findMostRecommended(Pageable pageable);
    
    // Validation Operations
    boolean existsById(UUID id);
    boolean existsByDoctorId(UUID doctorId);
    
    // Statistics Operations
    long countByDoctorId(UUID doctorId);
    long countByRating(BigDecimal rating);
    long countByMinRating(BigDecimal minRating);
    BigDecimal findAverageRatingByDoctorId(UUID doctorId);
    Long findTotalRecommendationCountByDoctorId(UUID doctorId);
    Object[] findRatingStatsByDoctorId(UUID doctorId);
    Object[] findRecommendationCountStatsByDoctorId(UUID doctorId);
    long countAll();
    
    // Bulk Operations
    List<RecommendationResponseDto> createBatch(UUID doctorId, List<RecommendationRequestDto> requestDtos);
    void deleteByDoctorId(UUID doctorId);
    void deleteBatch(List<UUID> ids);
    
    // Business Operations
    List<Object[]> findDoctorsWithHighestAverageRatings();
    List<Object[]> findDoctorsWithMostRecommendations();
    List<Object[]> getRatingDistribution();
    void updateDoctorRatingAndReviewCount(UUID doctorId);
    BigDecimal calculateDoctorAverageRating(UUID doctorId);
    Integer calculateDoctorTotalRecommendations(UUID doctorId);
}
