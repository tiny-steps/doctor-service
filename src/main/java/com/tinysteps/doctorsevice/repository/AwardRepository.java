package com.tinysteps.doctorsevice.repository;

import com.tinysteps.doctorsevice.entity.Award;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AwardRepository extends JpaRepository<Award, UUID> {

    // Find awards by doctor ID
    List<Award> findByDoctorId(UUID doctorId);

    // Find awards by doctor ID ordered by year (most recent first)
    List<Award> findByDoctorIdOrderByAwardedYearDesc(UUID doctorId);

    // Find awards by title (case-insensitive)
    List<Award> findByTitleContainingIgnoreCase(String title);

    // Find awards by year
    List<Award> findByAwardedYear(Integer year);

    // Find awards by year range
    List<Award> findByAwardedYearBetween(Integer startYear, Integer endYear);

    // Find awards by doctor and year
    List<Award> findByDoctorIdAndAwardedYear(UUID doctorId, Integer year);

    // Count awards by doctor
    long countByDoctorId(UUID doctorId);

    // Find recent awards (last 5 years)
    @Query("SELECT a FROM Award a WHERE a.awardedYear >= :startYear ORDER BY a.awardedYear DESC")
    List<Award> findRecentAwards(@Param("startYear") Integer startYear);

    // Find awards by doctor with year range
    @Query("SELECT a FROM Award a WHERE a.doctor.id = :doctorId AND a.awardedYear BETWEEN :startYear AND :endYear ORDER BY a.awardedYear DESC")
    List<Award> findByDoctorIdAndYearRange(@Param("doctorId") UUID doctorId,
                                          @Param("startYear") Integer startYear,
                                          @Param("endYear") Integer endYear);

    @Query("SELECT a FROM Award a WHERE a.doctor.id = :doctorId AND a.awardedYear = :year")
    List<Award> findByDoctorIdAndYear(@Param("doctorId") UUID doctorId, @Param("year") Integer year);

    boolean existsByDoctorIdAndAwardedYear(UUID doctorId, Integer year);

    void deleteByDoctorId(UUID doctorId);

    long countByAwardedYear(Integer year);

    boolean existsByDoctorId(UUID doctorId);

    // Pageable versions
    Page<Award> findByDoctorId(UUID doctorId, Pageable pageable);
    Page<Award> findByTitleContainingIgnoreCase(String title, Pageable pageable);
    Page<Award> findByAwardedYear(Integer year, Pageable pageable);
    Page<Award> findByAwardedYearBetween(Integer startYear, Integer endYear, Pageable pageable);
    Page<Award> findByAwardedYearGreaterThanEqual(Integer startYear, Pageable pageable);
}
