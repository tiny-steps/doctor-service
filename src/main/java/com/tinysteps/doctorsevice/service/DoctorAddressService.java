package com.tinysteps.doctorsevice.service;

import com.tinysteps.doctorsevice.entity.DoctorAddress;
import com.tinysteps.doctorsevice.dto.DoctorAddressDto;

import java.util.List;
import java.util.UUID;

public interface DoctorAddressService {

    /**
     * Add a doctor to an address with a specific practice role
     */
    DoctorAddressDto addDoctorToAddress(UUID doctorId, UUID addressId, DoctorAddress.PracticeRole practiceRole);

    /**
     * Remove a doctor from an address with a specific practice role
     */
    void removeDoctorFromAddress(UUID doctorId, UUID addressId, DoctorAddress.PracticeRole practiceRole);

    /**
     * Get all addresses for a doctor
     */
    List<DoctorAddressDto> getDoctorAddresses(UUID doctorId);

    /**
     * Get all doctors at an address
     */
    List<DoctorAddressDto> getDoctorsAtAddress(UUID addressId);

    /**
     * Get doctors at an address with a specific role
     */
    List<DoctorAddressDto> getDoctorsAtAddressWithRole(UUID addressId, DoctorAddress.PracticeRole practiceRole);

    /**
     * Get doctor addresses with a specific role
     */
    List<DoctorAddressDto> getDoctorAddressesWithRole(UUID doctorId, DoctorAddress.PracticeRole practiceRole);

    /**
     * Check if doctor is associated with an address
     */
    boolean isDoctorAtAddress(UUID doctorId, UUID addressId);

    /**
     * Get all unique address IDs for a doctor
     */
    List<UUID> getDoctorAddressIds(UUID doctorId);

    /**
     * Get all unique doctor IDs at an address
     */
    List<UUID> getDoctorIdsAtAddress(UUID addressId);

    /**
     * Remove all doctor-address relationships for a doctor
     */
    void removeAllDoctorAddresses(UUID doctorId);

    /**
     * Remove all doctor-address relationships for an address
     */
    void removeAllDoctorsFromAddress(UUID addressId);

    /**
     * Count total addresses for a doctor
     */
    long countDoctorAddresses(UUID doctorId);

    /**
     * Count total doctors at an address
     */
    long countDoctorsAtAddress(UUID addressId);

    /**
     * Update doctor's practice role at an address
     */
    DoctorAddressDto updateDoctorRole(UUID doctorId, UUID addressId, 
                                     DoctorAddress.PracticeRole oldRole, 
                                     DoctorAddress.PracticeRole newRole);
}