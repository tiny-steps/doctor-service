package com.tintsteps.doctorsevice.service.impl;

import com.tintsteps.doctorsevice.exception.DoctorNotFoundException;
import com.tintsteps.doctorsevice.exception.EntityNotFoundException;
import com.tintsteps.doctorsevice.mapper.RecommendationMapper;
import com.tintsteps.doctorsevice.model.RecommendationRequestDto;
import com.tintsteps.doctorsevice.model.RecommendationResponseDto;
import com.tintsteps.doctorsevice.repository.DoctorRepository;
import com.tintsteps.doctorsevice.repository.RecommendationRepository;
import com.tintsteps.doctorsevice.service.RecommendationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RecommendationServiceImpl implements RecommendationService {

    private final RecommendationRepository recommendationRepository;
    private final DoctorRepository doctorRepository;
    private final RecommendationMapper recommendationMapper;

    public RecommendationServiceImpl(RecommendationRepository recommendationRepository, DoctorRepository doctorRepository, RecommendationMapper recommendationMapper) {
        this.recommendationRepository = recommendationRepository;
        this.doctorRepository = doctorRepository;
        this.recommendationMapper = recommendationMapper;
    }

    @Override
    public RecommendationResponseDto create(UUID doctorId, RecommendationRequestDto requestDto) {
        var doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new DoctorNotFoundException("Doctor not found with ID: " + doctorId));
        var recommendation = recommendationMapper.fromRequestDto(requestDto);
        recommendation.setDoctor(doctor);
        var savedRecommendation = recommendationRepository.save(recommendation);
        updateDoctorRatingAndReviewCount(doctorId);
        return recommendationMapper.toResponseDto(savedRecommendation);
    }

    @Override
    public RecommendationResponseDto findById(UUID id) {
        return recommendationRepository.findById(id)
                .map(recommendationMapper::toResponseDto)
                .orElseThrow(() -> new EntityNotFoundException("Recommendation not found with ID: " + id));
    }

    @Override
    public Page<RecommendationResponseDto> findAll(Pageable pageable) {
        return recommendationRepository.findAll(pageable).map(recommendationMapper::toResponseDto);
    }

    @Override
    public RecommendationResponseDto update(UUID id, RecommendationRequestDto requestDto) {
        var existingRecommendation = recommendationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Recommendation not found with ID: " + id));
        recommendationMapper.updateEntityFromDto(requestDto, existingRecommendation);
        var updatedRecommendation = recommendationRepository.save(existingRecommendation);
        updateDoctorRatingAndReviewCount(existingRecommendation.getDoctor().getId());
        return recommendationMapper.toResponseDto(updatedRecommendation);
    }

    @Override
    public RecommendationResponseDto partialUpdate(UUID id, RecommendationRequestDto requestDto) {
        var existingRecommendation = recommendationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Recommendation not found with ID: " + id));
        recommendationMapper.updateEntityFromDto(requestDto, existingRecommendation);
        var updatedRecommendation = recommendationRepository.save(existingRecommendation);
        updateDoctorRatingAndReviewCount(existingRecommendation.getDoctor().getId());
        return recommendationMapper.toResponseDto(updatedRecommendation);
    }

    @Override
    public void delete(UUID id) {
        var recommendation = recommendationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Recommendation not found with ID: " + id));
        var doctorId = recommendation.getDoctor().getId();
        recommendationRepository.deleteById(id);
        updateDoctorRatingAndReviewCount(doctorId);
    }

    @Override
    public List<RecommendationResponseDto> findByDoctorId(UUID doctorId) {
        if (!doctorRepository.existsById(doctorId)) {
            throw new DoctorNotFoundException("Doctor not found with ID: " + doctorId);
        }
        return recommendationRepository.findByDoctorId(doctorId).stream()
                .map(recommendationMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<RecommendationResponseDto> findByDoctorIdOrderByRating(UUID doctorId) {
        if (!doctorRepository.existsById(doctorId)) {
            throw new DoctorNotFoundException("Doctor not found with ID: " + doctorId);
        }
        return recommendationRepository.findByDoctorIdOrderByRatingDesc(doctorId).stream()
                .map(recommendationMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<RecommendationResponseDto> findByDoctorIdOrderByRecommendationCount(UUID doctorId) {
         if (!doctorRepository.existsById(doctorId)) {
            throw new DoctorNotFoundException("Doctor not found with ID: " + doctorId);
        }
        return recommendationRepository.findByDoctorIdOrderByRecommendationCountDesc(doctorId).stream()
                .map(recommendationMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public Page<RecommendationResponseDto> findByDoctorId(UUID doctorId, Pageable pageable) {
        if (!doctorRepository.existsById(doctorId)) {
            throw new DoctorNotFoundException("Doctor not found with ID: " + doctorId);
        }
        return recommendationRepository.findByDoctorId(doctorId, pageable).map(recommendationMapper::toResponseDto);
    }

    @Override
    public Page<RecommendationResponseDto> findByRating(BigDecimal rating, Pageable pageable) {
        return recommendationRepository.findByRating(rating, pageable).map(recommendationMapper::toResponseDto);
    }

    @Override
    public Page<RecommendationResponseDto> findByMinRating(BigDecimal minRating, Pageable pageable) {
        return recommendationRepository.findByRatingGreaterThanEqual(minRating, pageable).map(recommendationMapper::toResponseDto);
    }

    @Override
    public Page<RecommendationResponseDto> findByMaxRating(BigDecimal maxRating, Pageable pageable) {
        return recommendationRepository.findByRatingLessThanEqual(maxRating, pageable).map(recommendationMapper::toResponseDto);
    }

    @Override
    public Page<RecommendationResponseDto> findByRatingRange(BigDecimal minRating, BigDecimal maxRating, Pageable pageable) {
        return recommendationRepository.findByRatingBetween(minRating, maxRating, pageable).map(recommendationMapper::toResponseDto);
    }

    @Override
    public List<RecommendationResponseDto> findByDoctorIdAndMinRating(UUID doctorId, BigDecimal minRating) {
        return recommendationRepository.findByDoctorIdAndRatingGreaterThanEqual(doctorId, minRating).stream()
                .map(recommendationMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public Page<RecommendationResponseDto> findByMinRecommendationCount(Integer minCount, Pageable pageable) {
        return recommendationRepository.findByRecommendationCountGreaterThanEqual(minCount, pageable).map(recommendationMapper::toResponseDto);
    }

    @Override
    public Page<RecommendationResponseDto> findByReviewText(String reviewText, Pageable pageable) {
        return recommendationRepository.findByReviewContainingIgnoreCase(reviewText, pageable).map(recommendationMapper::toResponseDto);
    }

    @Override
    public Page<RecommendationResponseDto> findRecommendationsWithReviews(Pageable pageable) {
        return recommendationRepository.findByReviewIsNotNullAndReviewNot("", pageable).map(recommendationMapper::toResponseDto);
    }

    @Override
    public Page<RecommendationResponseDto> findRecommendationsWithoutReviews(Pageable pageable) {
        return recommendationRepository.findByReviewIsNull(pageable).map(recommendationMapper::toResponseDto);
    }

    @Override
    public Page<RecommendationResponseDto> findHighestRatedRecommendations(Pageable pageable) {
        return recommendationRepository.findAllByOrderByRatingDesc(pageable).map(recommendationMapper::toResponseDto);
    }

    @Override
    public Page<RecommendationResponseDto> findMostRecommended(Pageable pageable) {
        return recommendationRepository.findAllByOrderByRecommendationCountDesc(pageable).map(recommendationMapper::toResponseDto);
    }

    @Override
    public boolean existsById(UUID id) {
        return recommendationRepository.existsById(id);
    }

    @Override
    public boolean existsByDoctorId(UUID doctorId) {
        return recommendationRepository.existsByDoctorId(doctorId);
    }

    @Override
    public long countByDoctorId(UUID doctorId) {
        return recommendationRepository.countByDoctorId(doctorId);
    }

    @Override
    public long countByRating(BigDecimal rating) {
        return recommendationRepository.countByRating(rating);
    }

    @Override
    public long countByMinRating(BigDecimal minRating) {
        return recommendationRepository.countByRatingGreaterThanEqual(minRating);
    }

    @Override
    public BigDecimal findAverageRatingByDoctorId(UUID doctorId) {
        return recommendationRepository.findAverageRatingByDoctorId(doctorId);
    }

    @Override
    public Long findTotalRecommendationCountByDoctorId(UUID doctorId) {
        return recommendationRepository.findTotalRecommendationCountByDoctorId(doctorId);
    }

    @Override
    public Object[] findRatingStatsByDoctorId(UUID doctorId) {
        return recommendationRepository.findRatingStatsByDoctorId(doctorId);
    }

    @Override
    public Object[] findRecommendationCountStatsByDoctorId(UUID doctorId) {
        return recommendationRepository.findRecommendationCountStatsByDoctorId(doctorId);
    }

    @Override
    public long countAll() {
        return recommendationRepository.count();
    }

    @Override
    public List<RecommendationResponseDto> createBatch(UUID doctorId, List<RecommendationRequestDto> requestDtos) {
        var doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new DoctorNotFoundException("Doctor not found with ID: " + doctorId));
        var recommendations = requestDtos.stream()
                .map(recommendationMapper::fromRequestDto)
                .peek(rec -> rec.setDoctor(doctor))
                .collect(Collectors.toList());
        var savedRecommendations = recommendationRepository.saveAll(recommendations);
        updateDoctorRatingAndReviewCount(doctorId);
        return savedRecommendations.stream()
                .map(recommendationMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteByDoctorId(UUID doctorId) {
        recommendationRepository.deleteByDoctorId(doctorId);
        updateDoctorRatingAndReviewCount(doctorId);
    }

    @Override
    public void deleteBatch(List<UUID> ids) {
        // This is more complex as we need to update multiple doctors
        var recommendations = recommendationRepository.findAllById(ids);
        var doctorIds = recommendations.stream().map(r -> r.getDoctor().getId()).collect(Collectors.toSet());
        recommendationRepository.deleteAllById(ids);
        doctorIds.forEach(this::updateDoctorRatingAndReviewCount);
    }

    @Override
    public List<Object[]> findDoctorsWithHighestAverageRatings() {
        return recommendationRepository.findDoctorsWithHighestAverageRatings();
    }

    @Override
    public List<Object[]> findDoctorsWithMostRecommendations() {
        return recommendationRepository.findDoctorsWithMostRecommendations();
    }

    @Override
    public List<Object[]> getRatingDistribution() {
        return recommendationRepository.getRatingDistribution();
    }

    @Override
    public void updateDoctorRatingAndReviewCount(UUID doctorId) {
        var doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new DoctorNotFoundException("Doctor not found with ID: " + doctorId));
        var avgRating = calculateDoctorAverageRating(doctorId);
        var reviewCount = (int) countByDoctorId(doctorId);
        doctor.setRatingAverage(avgRating);
        doctor.setReviewCount(reviewCount);
        doctorRepository.save(doctor);
    }

    @Override
    public BigDecimal calculateDoctorAverageRating(UUID doctorId) {
        return recommendationRepository.findAverageRatingByDoctorId(doctorId);
    }

    @Override
    public Integer calculateDoctorTotalRecommendations(UUID doctorId) {
        Long total = recommendationRepository.findTotalRecommendationCountByDoctorId(doctorId);
        return total != null ? total.intValue() : 0;
    }
}
