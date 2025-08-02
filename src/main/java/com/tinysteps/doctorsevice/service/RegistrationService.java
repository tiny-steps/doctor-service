package com.tinysteps.doctorsevice.service;

import com.tinysteps.doctorsevice.model.RegistrationRequestDto;
import com.tinysteps.doctorsevice.model.RegistrationResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for Registration operations
 */
public interface RegistrationService {

    // CRUD Operations
    RegistrationResponseDto create(UUID doctorId, RegistrationRequestDto requestDto);
    RegistrationResponseDto findById(UUID id);
    Page<RegistrationResponseDto> findAll(Pageable pageable);
    RegistrationResponseDto update(UUID id, RegistrationRequestDto requestDto);
    RegistrationResponseDto partialUpdate(UUID id, RegistrationRequestDto requestDto);
    void delete(UUID id);

    // Doctor-specific Operations
    List<RegistrationResponseDto> findByDoctorId(UUID doctorId);
    List<RegistrationResponseDto> findByDoctorIdOrderByYear(UUID doctorId);
    Page<RegistrationResponseDto> findByDoctorId(UUID doctorId, Pageable pageable);

    // Search Operations
    Page<RegistrationResponseDto> findByCouncilName(String councilName, Pageable pageable);
    RegistrationResponseDto findByRegistrationNumber(String registrationNumber);
    Page<RegistrationResponseDto> findByRegistrationYear(Integer year, Pageable pageable);
    Page<RegistrationResponseDto> findByYearRange(Integer startYear, Integer endYear, Pageable pageable);
    List<RegistrationResponseDto> findByDoctorIdAndCouncilName(UUID doctorId, String councilName);
    RegistrationResponseDto findByDoctorIdAndRegistrationNumber(UUID doctorId, String registrationNumber);
    Page<RegistrationResponseDto> findRecentRegistrations(Integer startYear, Pageable pageable);

    // Validation Operations
    boolean existsById(UUID id);
    boolean existsByDoctorId(UUID doctorId);
    boolean existsByRegistrationNumber(String registrationNumber);
    boolean hasDoctorRegistrationWithCouncil(UUID doctorId, String councilName);
    boolean isRegistrationNumberUnique(String registrationNumber);

    // Statistics Operations
    long countByDoctorId(UUID doctorId);
    long countByCouncilName(String councilName);
    long countByRegistrationYear(Integer year);
    long countAll();

    // Bulk Operations
    List<RegistrationResponseDto> createBatch(UUID doctorId, List<RegistrationRequestDto> requestDtos);
    void deleteByDoctorId(UUID doctorId);
    void deleteBatch(List<UUID> ids);

    // Business Operations
    List<String> getUniqueCouncilNames();
    List<RegistrationResponseDto> findDoctorRegistrationsInYearRange(UUID doctorId, Integer startYear, Integer endYear);
    boolean isDoctorRegisteredWithCouncil(UUID doctorId, String councilName);
    boolean validateRegistrationNumber(String registrationNumber);
}
