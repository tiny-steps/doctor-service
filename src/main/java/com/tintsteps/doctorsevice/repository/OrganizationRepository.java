package com.tintsteps.doctorsevice.repository;

import com.tintsteps.doctorsevice.entity.Organization;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.List;
import java.util.UUID;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, UUID> {
    
    // Find organizations by doctor ID
    List<Organization> findByDoctorId(UUID doctorId);
    
    // Find organizations by doctor ID ordered by tenure start (most recent first)
    List<Organization> findByDoctorIdOrderByTenureStartDesc(UUID doctorId);
    
    // Find organizations by organization name (case-insensitive)
    List<Organization> findByOrganizationNameContainingIgnoreCase(String organizationName);
    
    // Find organizations by role (case-insensitive)
    List<Organization> findByRoleContainingIgnoreCase(String role);
    
    // Find organizations by city
    List<Organization> findByCityIgnoreCase(String city);
    
    // Find organizations by state
    List<Organization> findByStateIgnoreCase(String state);
    
    // Find organizations by country
    List<Organization> findByCountryIgnoreCase(String country);
    
    // Find current organizations (tenure end is null or in future)
    @Query("SELECT o FROM Organization o WHERE o.tenureEnd IS NULL OR o.tenureEnd >= CURRENT_DATE")
    List<Organization> findCurrentOrganizations();
    
    // Find current organizations by doctor
    @Query("SELECT o FROM Organization o WHERE o.doctor.id = :doctorId AND (o.tenureEnd IS NULL OR o.tenureEnd >= CURRENT_DATE)")
    List<Organization> findCurrentOrganizationsByDoctorId(@Param("doctorId") UUID doctorId);
    
    // Find past organizations by doctor
    @Query("SELECT o FROM Organization o WHERE o.doctor.id = :doctorId AND o.tenureEnd < CURRENT_DATE ORDER BY o.tenureEnd DESC")
    List<Organization> findPastOrganizationsByDoctorId(@Param("doctorId") UUID doctorId);
    
    // Find organizations by tenure period
    @Query("SELECT o FROM Organization o WHERE o.tenureStart <= :endDate AND (o.tenureEnd IS NULL OR o.tenureEnd >= :startDate)")
    List<Organization> findByTenurePeriod(@Param("startDate") Date startDate, @Param("endDate") Date endDate);
    
    // Find organizations by location (city, state, country)
    @Query("SELECT o FROM Organization o WHERE " +
           "(:city IS NULL OR LOWER(o.city) = LOWER(:city)) AND " +
           "(:state IS NULL OR LOWER(o.state) = LOWER(:state)) AND " +
           "(:country IS NULL OR LOWER(o.country) = LOWER(:country))")
    List<Organization> findByLocation(@Param("city") String city, 
                                     @Param("state") String state, 
                                     @Param("country") String country);
    
    // Count organizations by doctor
    long countByDoctorId(UUID doctorId);
    
    // Count current organizations by doctor
    @Query("SELECT COUNT(o) FROM Organization o WHERE o.doctor.id = :doctorId AND (o.tenureEnd IS NULL OR o.tenureEnd >= CURRENT_DATE)")
    long countCurrentOrganizationsByDoctorId(@Param("doctorId") UUID doctorId);
    
    // Find all unique organization names
    @Query("SELECT DISTINCT o.organizationName FROM Organization o ORDER BY o.organizationName")
    List<String> findAllUniqueOrganizationNames();
    
    // Find all unique roles
    @Query("SELECT DISTINCT o.role FROM Organization o WHERE o.role IS NOT NULL ORDER BY o.role")
    List<String> findAllUniqueRoles();
    
    // Additional missing methods
    Page<Organization> findByDoctorId(UUID doctorId, Pageable pageable);
    Page<Organization> findByOrganizationNameContainingIgnoreCase(String organizationName, Pageable pageable);
    Page<Organization> findByRoleContainingIgnoreCase(String role, Pageable pageable);
    Page<Organization> findByCityContainingIgnoreCase(String city, Pageable pageable);
    Page<Organization> findByStateContainingIgnoreCase(String state, Pageable pageable);
    Page<Organization> findByCountryContainingIgnoreCase(String country, Pageable pageable);
    Page<Organization> findByCityAndStateAndCountry(String city, String state, String country, Pageable pageable);
    List<Organization> findByTenureEndIsNull();
    List<Organization> findByDoctorIdAndTenureEndIsNull(UUID doctorId);
    List<Organization> findByDoctorIdAndTenureEndIsNotNull(UUID doctorId);
    Page<Organization> findByTenureStartBetween(Date startDate, Date endDate, Pageable pageable);
    boolean existsByDoctorId(UUID doctorId);
    boolean existsByDoctorIdAndTenureEndIsNull(UUID doctorId);
    long countByDoctorIdAndTenureEndIsNull(UUID doctorId);
    long countByOrganizationNameContainingIgnoreCase(String organizationName);
    void deleteByDoctorId(UUID doctorId);

    @Query("SELECT DISTINCT o.organizationName FROM Organization o WHERE o.organizationName IS NOT NULL ORDER BY o.organizationName")
    List<String> findDistinctOrganizationNames();

    @Query("SELECT DISTINCT o.role FROM Organization o WHERE o.role IS NOT NULL ORDER BY o.role")
    List<String> findDistinctRoles();

    boolean existsByDoctorIdAndOrganizationNameContainingIgnoreCase(UUID doctorId, String organizationName);
}
