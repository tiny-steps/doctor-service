package com.tinysteps.doctorservice.service;

import com.tinysteps.doctorservice.model.OrganizationRequestDto;
import com.tinysteps.doctorservice.model.OrganizationResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.sql.Date;
import java.util.List;
import java.util.UUID;

/**
 * Service interface for Organization operations
 */
public interface OrganizationService {

    // CRUD Operations
    OrganizationResponseDto create(UUID doctorId, OrganizationRequestDto requestDto);
    OrganizationResponseDto findById(UUID id);
    Page<OrganizationResponseDto> findAll(Pageable pageable);
    OrganizationResponseDto update(UUID id, OrganizationRequestDto requestDto);
    OrganizationResponseDto partialUpdate(UUID id, OrganizationRequestDto requestDto);
    void delete(UUID id);

    // Doctor-specific Operations
    List<OrganizationResponseDto> findByDoctorId(UUID doctorId);
    List<OrganizationResponseDto> findByDoctorIdOrderByTenureStart(UUID doctorId);
    Page<OrganizationResponseDto> findByDoctorId(UUID doctorId, Pageable pageable);

    // Search Operations
    Page<OrganizationResponseDto> findByOrganizationName(String organizationName, Pageable pageable);
    Page<OrganizationResponseDto> findByRole(String role, Pageable pageable);
    Page<OrganizationResponseDto> findByCity(String city, Pageable pageable);
    Page<OrganizationResponseDto> findByState(String state, Pageable pageable);
    Page<OrganizationResponseDto> findByCountry(String country, Pageable pageable);
    Page<OrganizationResponseDto> findByLocation(String city, String state, String country, Pageable pageable);

    // Temporal Operations
    List<OrganizationResponseDto> findCurrentOrganizations();
    List<OrganizationResponseDto> findCurrentOrganizationsByDoctorId(UUID doctorId);
    List<OrganizationResponseDto> findPastOrganizationsByDoctorId(UUID doctorId);
    Page<OrganizationResponseDto> findByTenurePeriod(Date startDate, Date endDate, Pageable pageable);

    // Validation Operations
    boolean existsById(UUID id);
    boolean existsByDoctorId(UUID doctorId);
    boolean isDoctorCurrentlyEmployed(UUID doctorId);

    // Statistics Operations
    long countByDoctorId(UUID doctorId);
    long countCurrentOrganizationsByDoctorId(UUID doctorId);
    long countByOrganizationName(String organizationName);
    long countAll();

    // Bulk Operations
    List<OrganizationResponseDto> createBatch(UUID doctorId, List<OrganizationRequestDto> requestDtos);
    void deleteByDoctorId(UUID doctorId);
    void deleteBatch(List<UUID> ids);

    // Business Operations
    List<String> getUniqueOrganizationNames();
    List<String> getUniqueRoles();
    boolean isDoctorEmployedAtOrganization(UUID doctorId, String organizationName);
    int calculateTotalExperienceYears(UUID doctorId);
}
