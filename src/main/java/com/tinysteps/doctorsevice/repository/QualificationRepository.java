package com.tinysteps.doctorsevice.repository;

import com.tinysteps.doctorsevice.entity.Qualification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface QualificationRepository extends JpaRepository<Qualification, UUID> {

    // Find qualifications by doctor ID
    List<Qualification> findByDoctorId(UUID doctorId);

    // Find qualifications by doctor ID ordered by completion year (most recent first)
    List<Qualification> findByDoctorIdOrderByCompletionYearDesc(UUID doctorId);

    // Find qualifications by qualification name (case-insensitive)
    List<Qualification> findByQualificationNameContainingIgnoreCase(String qualificationName);

    // Find qualifications by college name (case-insensitive)
    List<Qualification> findByCollegeNameContainingIgnoreCase(String collegeName);

    // Find qualifications by completion year
    List<Qualification> findByCompletionYear(Integer year);

    // Find qualifications by year range
    List<Qualification> findByCompletionYearBetween(Integer startYear, Integer endYear);

    // Find qualifications by doctor and qualification name
    List<Qualification> findByDoctorIdAndQualificationNameContainingIgnoreCase(UUID doctorId, String qualificationName);

    // Count qualifications by doctor
    long countByDoctorId(UUID doctorId);

    // Find doctors with specific qualification
    @Query("SELECT q FROM Qualification q WHERE q.qualificationName = :qualificationName ORDER BY q.completionYear DESC")
    List<Qualification> findByExactQualificationName(@Param("qualificationName") String qualificationName);

    // Find qualifications from specific college
    @Query("SELECT q FROM Qualification q WHERE LOWER(q.collegeName) = LOWER(:collegeName) ORDER BY q.completionYear DESC")
    List<Qualification> findByExactCollegeName(@Param("collegeName") String collegeName);

    // Find recent qualifications (last 10 years)
    @Query("SELECT q FROM Qualification q WHERE q.completionYear >= :startYear ORDER BY q.completionYear DESC")
    List<Qualification> findRecentQualifications(@Param("startYear") Integer startYear);

    // Find qualifications by doctor with year range
    @Query("SELECT q FROM Qualification q WHERE q.doctor.id = :doctorId AND q.completionYear BETWEEN :startYear AND :endYear ORDER BY q.completionYear DESC")
    List<Qualification> findByDoctorIdAndYearRange(@Param("doctorId") UUID doctorId,
                                                  @Param("startYear") Integer startYear,
                                                  @Param("endYear") Integer endYear);

    // Additional missing methods
    Page<Qualification> findByDoctorId(UUID doctorId, Pageable pageable);
    Page<Qualification> findByQualificationNameContainingIgnoreCase(String qualificationName, Pageable pageable);
    Page<Qualification> findByCollegeNameContainingIgnoreCase(String collegeName, Pageable pageable);
    Page<Qualification> findByCompletionYear(Integer year, Pageable pageable);
    Page<Qualification> findByCompletionYearBetween(Integer startYear, Integer endYear, Pageable pageable);
    Page<Qualification> findByCompletionYearGreaterThanEqual(Integer startYear, Pageable pageable);
    boolean existsByDoctorId(UUID doctorId);
    boolean existsByDoctorIdAndQualificationNameContainingIgnoreCase(UUID doctorId, String qualificationName);
    long countByQualificationNameContainingIgnoreCase(String qualificationName);
    long countByCollegeNameContainingIgnoreCase(String collegeName);
    void deleteByDoctorId(UUID doctorId);
    List<Qualification> findByDoctorIdAndCompletionYearBetween(UUID doctorId, Integer startYear, Integer endYear);

    @Query("SELECT DISTINCT q.qualificationName FROM Qualification q WHERE q.qualificationName IS NOT NULL ORDER BY q.qualificationName")
    List<String> findDistinctQualificationNames();

    @Query("SELECT DISTINCT q.collegeName FROM Qualification q WHERE q.collegeName IS NOT NULL ORDER BY q.collegeName")
    List<String> findDistinctCollegeNames();
}
