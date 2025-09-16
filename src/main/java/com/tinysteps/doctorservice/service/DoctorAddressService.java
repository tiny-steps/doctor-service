package com.tinysteps.doctorservice.service;

import com.tinysteps.doctorservice.entity.DoctorAddress;
import com.tinysteps.doctorservice.model.DoctorAddressRequestDto;
import com.tinysteps.doctorservice.model.DoctorAddressResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface DoctorAddressService {

    /**
     * Add a doctor address relationship
     */
    DoctorAddressResponseDto addDoctorAddress(UUID doctorId, DoctorAddressRequestDto requestDto);

    /**
     * Remove a doctor from an address with a specific practice role (sets status to INACTIVE)
     */
    void removeDoctorAddress(UUID doctorId, UUID addressId, String practiceRole);

    /**
     * Activate a doctor address relationship (sets status to ACTIVE)
     */
    void activateDoctorAddress(UUID doctorId, UUID addressId, String practiceRole);

    /**
     * Get all addresses for a doctor
     */
    List<DoctorAddressResponseDto> findByDoctorId(UUID doctorId);

    /**
     * Get only active addresses for a doctor
     */
    List<DoctorAddressResponseDto> findActiveDoctorAddresses(UUID doctorId);

    /**
     * Get all addresses for a doctor with pagination
     */
    Page<DoctorAddressResponseDto> findByDoctorId(UUID doctorId, Pageable pageable);

    /**
     * Get all doctors at an address
     */
    List<DoctorAddressResponseDto> findByAddressId(UUID addressId);

    /**
     * Get all doctors at an address with pagination
     */
    Page<DoctorAddressResponseDto> findByAddressId(UUID addressId, Pageable pageable);

    /**
     * Get relationships by practice role
     */
    List<DoctorAddressResponseDto> findByPracticeRole(String practiceRole);

    /**
     * Get relationships by practice role with pagination
     */
    Page<DoctorAddressResponseDto> findByPracticeRole(String practiceRole, Pageable pageable);

    /**
     * Get doctor addresses with a specific role
     */
    List<DoctorAddressResponseDto> findByDoctorIdAndPracticeRole(UUID doctorId, String practiceRole);

    /**
     * Check if doctor is associated with an address
     */
    boolean existsDoctorAddress(UUID doctorId, UUID addressId, String practiceRole);

    /**
     * Remove all doctor-address relationships for a doctor (sets status to INACTIVE)
     */
    void removeAllDoctorAddresses(UUID doctorId);

    /**
     * Remove all doctor-address relationships for an address (sets status to INACTIVE)
     */
    void removeAllAddressDoctors(UUID addressId);

    /**
     * Count total addresses for a doctor
     */
    long countByDoctorId(UUID doctorId);

    /**
     * Count active addresses for a doctor
     */
    long countActiveDoctorAddresses(UUID doctorId);

    /**
     * Count total doctors at an address
     */
    long countByAddressId(UUID addressId);

    /**
     * Add multiple doctor addresses
     */
    List<DoctorAddressResponseDto> addMultipleDoctorAddresses(UUID doctorId, List<DoctorAddressRequestDto> requestDtos);
}
