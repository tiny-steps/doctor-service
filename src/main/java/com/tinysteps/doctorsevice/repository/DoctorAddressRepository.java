package com.tinysteps.doctorsevice.repository;

import com.tinysteps.doctorsevice.entity.DoctorAddress;
import com.tinysteps.doctorsevice.entity.DoctorAddressId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DoctorAddressRepository extends JpaRepository<DoctorAddress, DoctorAddressId> {

    /**
     * Find all addresses for a specific doctor
     */
    List<DoctorAddress> findByDoctorId(UUID doctorId);

    /**
     * Find all doctors at a specific address
     */
    List<DoctorAddress> findByAddressId(UUID addressId);

    /**
     * Find doctors by address and practice role
     */
    List<DoctorAddress> findByAddressIdAndPracticeRole(UUID addressId, DoctorAddress.PracticeRole practiceRole);

    /**
     * Find doctor addresses by practice role
     */
    List<DoctorAddress> findByDoctorIdAndPracticeRole(UUID doctorId, DoctorAddress.PracticeRole practiceRole);

    /**
     * Check if doctor is associated with a specific address
     */
    boolean existsByDoctorIdAndAddressId(UUID doctorId, UUID addressId);

    /**
     * Get all unique address IDs for a doctor
     */
    @Query("SELECT DISTINCT da.addressId FROM DoctorAddress da WHERE da.doctorId = :doctorId")
    List<UUID> findAddressIdsByDoctorId(@Param("doctorId") UUID doctorId);

    /**
     * Get all unique doctor IDs for an address
     */
    @Query("SELECT DISTINCT da.doctorId FROM DoctorAddress da WHERE da.addressId = :addressId")
    List<UUID> findDoctorIdsByAddressId(@Param("addressId") UUID addressId);

    /**
     * Delete all doctor-address relationships for a doctor
     */
    void deleteByDoctorId(UUID doctorId);

    /**
     * Delete all doctor-address relationships for an address
     */
    void deleteByAddressId(UUID addressId);

    /**
     * Count total addresses for a doctor
     */
    long countByDoctorId(UUID doctorId);

    /**
     * Count total doctors at an address
     */
    long countByAddressId(UUID addressId);
}