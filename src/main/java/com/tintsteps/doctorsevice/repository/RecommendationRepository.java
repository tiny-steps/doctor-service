package com.tintsteps.doctorsevice.repository;

import com.tintsteps.doctorsevice.entity.Recommendation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Repository
public interface RecommendationRepository extends JpaRepository<Recommendation, UUID> {
    
    // Find recommendations by doctor ID
    List<Recommendation> findByDoctorId(UUID doctorId);
    
    // Find recommendations by doctor ID ordered by rating (highest first)
    List<Recommendation> findByDoctorIdOrderByRatingDesc(UUID doctorId);
    
    // Find recommendations by doctor ID ordered by recommendation count (highest first)
    List<Recommendation> findByDoctorIdOrderByRecommendationCountDesc(UUID doctorId);
    
    // Find recommendations by rating
    List<Recommendation> findByRating(BigDecimal rating);
    
    // Find recommendations by minimum rating
    List<Recommendation> findByRatingGreaterThanEqual(BigDecimal minRating);
    
    // Find recommendations by maximum rating
    List<Recommendation> findByRatingLessThanEqual(BigDecimal maxRating);
    
    // Find recommendations by rating range
    List<Recommendation> findByRatingBetween(BigDecimal minRating, BigDecimal maxRating);
    
    // Find recommendations by minimum recommendation count
    List<Recommendation> findByRecommendationCountGreaterThanEqual(Integer minCount);
    
    // Find recommendations with review text (case-insensitive)
    List<Recommendation> findByReviewContainingIgnoreCase(String reviewText);
    
    // Find recommendations by doctor and minimum rating
    List<Recommendation> findByDoctorIdAndRatingGreaterThanEqual(UUID doctorId, BigDecimal minRating);
    
    // Count recommendations by doctor
    long countByDoctorId(UUID doctorId);
    
    // Count recommendations by rating
    long countByRating(BigDecimal rating);
    
    // Count recommendations with minimum rating
    long countByRatingGreaterThanEqual(BigDecimal minRating);
    
    // Find average rating for doctor
    @Query("SELECT AVG(r.rating) FROM Recommendation r WHERE r.doctor.id = :doctorId AND r.rating IS NOT NULL")
    BigDecimal findAverageRatingByDoctorId(@Param("doctorId") UUID doctorId);
    
    // Find total recommendation count for doctor
    @Query("SELECT SUM(r.recommendationCount) FROM Recommendation r WHERE r.doctor.id = :doctorId AND r.recommendationCount IS NOT NULL")
    Long findTotalRecommendationCountByDoctorId(@Param("doctorId") UUID doctorId);
    
    // Find highest rated recommendations
    @Query("SELECT r FROM Recommendation r WHERE r.rating IS NOT NULL ORDER BY r.rating DESC")
    List<Recommendation> findHighestRatedRecommendations();
    
    // Find most recommended (by count)
    @Query("SELECT r FROM Recommendation r WHERE r.recommendationCount IS NOT NULL ORDER BY r.recommendationCount DESC")
    List<Recommendation> findMostRecommended();
    
    // Find recommendations with reviews
    @Query("SELECT r FROM Recommendation r WHERE r.review IS NOT NULL AND r.review != ''")
    List<Recommendation> findRecommendationsWithReviews();
    
    // Find recommendations without reviews
    @Query("SELECT r FROM Recommendation r WHERE r.review IS NULL OR r.review = ''")
    List<Recommendation> findRecommendationsWithoutReviews();
    
    // Find rating statistics for doctor
    @Query("SELECT MIN(r.rating), MAX(r.rating), AVG(r.rating), COUNT(r) FROM Recommendation r WHERE r.doctor.id = :doctorId AND r.rating IS NOT NULL")
    Object[] findRatingStatsByDoctorId(@Param("doctorId") UUID doctorId);
    
    // Find recommendation count statistics for doctor
    @Query("SELECT MIN(r.recommendationCount), MAX(r.recommendationCount), AVG(r.recommendationCount), SUM(r.recommendationCount) FROM Recommendation r WHERE r.doctor.id = :doctorId AND r.recommendationCount IS NOT NULL")
    Object[] findRecommendationCountStatsByDoctorId(@Param("doctorId") UUID doctorId);
    
    // Find doctors with highest average ratings
    @Query("SELECT r.doctor.id, AVG(r.rating) as avgRating FROM Recommendation r WHERE r.rating IS NOT NULL GROUP BY r.doctor.id ORDER BY avgRating DESC")
    List<Object[]> findDoctorsWithHighestAverageRatings();
    
    // Find doctors with most recommendations
    @Query("SELECT r.doctor.id, SUM(r.recommendationCount) as totalCount FROM Recommendation r WHERE r.recommendationCount IS NOT NULL GROUP BY r.doctor.id ORDER BY totalCount DESC")
    List<Object[]> findDoctorsWithMostRecommendations();
    
    // Find overall rating distribution
    @Query("SELECT r.rating, COUNT(r) FROM Recommendation r WHERE r.rating IS NOT NULL GROUP BY r.rating ORDER BY r.rating")
    List<Object[]> findRatingDistribution();

    // Additional missing methods for compilation errors

    // Pageable versions of existing methods
    Page<Recommendation> findByDoctorId(UUID doctorId, Pageable pageable);
    Page<Recommendation> findByRating(BigDecimal rating, Pageable pageable);
    Page<Recommendation> findByRatingGreaterThanEqual(BigDecimal minRating, Pageable pageable);
    Page<Recommendation> findByRatingLessThanEqual(BigDecimal maxRating, Pageable pageable);
    Page<Recommendation> findByRatingBetween(BigDecimal minRating, BigDecimal maxRating, Pageable pageable);
    Page<Recommendation> findByRecommendationCountGreaterThanEqual(Integer minCount, Pageable pageable);

    // Missing methods with Pageable
    Page<Recommendation> findByReviewTextContainingIgnoreCase(String reviewText, Pageable pageable);
    Page<Recommendation> findByReviewTextIsNotNullAndReviewTextNot(String excludeText, Pageable pageable);
    Page<Recommendation> findByReviewTextIsNull(Pageable pageable);
    Page<Recommendation> findAllByOrderByRatingDesc(Pageable pageable);
    Page<Recommendation> findAllByOrderByRecommendationCountDesc(Pageable pageable);

    // Missing existence and deletion methods
    boolean existsByDoctorId(UUID doctorId);
    void deleteByDoctorId(UUID doctorId);

    // Alias for getRatingDistribution (used in service)
    @Query("SELECT r.rating, COUNT(r) FROM Recommendation r WHERE r.rating IS NOT NULL GROUP BY r.rating ORDER BY r.rating")
    List<Object[]> getRatingDistribution();
}
