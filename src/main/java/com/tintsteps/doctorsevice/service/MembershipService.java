package com.tintsteps.doctorsevice.service;

import com.tintsteps.doctorsevice.model.MembershipRequestDto;
import com.tintsteps.doctorsevice.model.MembershipResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for Membership operations
 */
public interface MembershipService {
    
    // CRUD Operations
    MembershipResponseDto create(UUID doctorId, MembershipRequestDto requestDto);
    MembershipResponseDto findById(UUID id);
    Page<MembershipResponseDto> findAll(Pageable pageable);
    MembershipResponseDto update(UUID id, MembershipRequestDto requestDto);
    MembershipResponseDto partialUpdate(UUID id, MembershipRequestDto requestDto);
    void delete(UUID id);
    
    // Doctor-specific Operations
    List<MembershipResponseDto> findByDoctorId(UUID doctorId);
    Page<MembershipResponseDto> findByDoctorId(UUID doctorId, Pageable pageable);
    
    // Search Operations
    Page<MembershipResponseDto> findByCouncilName(String councilName, Pageable pageable);
    List<MembershipResponseDto> findByDoctorIdAndCouncilName(UUID doctorId, String councilName);
    
    // Validation Operations
    boolean existsById(UUID id);
    boolean existsByDoctorId(UUID doctorId);
    boolean hasDoctorMembershipInCouncil(UUID doctorId, String councilName);
    
    // Statistics Operations
    long countByDoctorId(UUID doctorId);
    long countByCouncilName(String councilName);
    long countAll();
    
    // Bulk Operations
    List<MembershipResponseDto> createBatch(UUID doctorId, List<MembershipRequestDto> requestDtos);
    void deleteByDoctorId(UUID doctorId);
    void deleteBatch(List<UUID> ids);
    
    // Business Operations
    List<String> getUniqueCouncilNames();
    boolean isDoctorMemberOfCouncil(UUID doctorId, String councilName);
}
