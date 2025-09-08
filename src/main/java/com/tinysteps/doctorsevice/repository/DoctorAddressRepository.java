package com.tinysteps.doctorsevice.repository;

import com.tinysteps.doctorsevice.entity.DoctorAddress;
import com.tinysteps.doctorsevice.entity.DoctorAddressId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
     * Find all addresses for a specific doctor with pagination
     */
    Page<DoctorAddress> findByDoctorId(UUID doctorId, Pageable pageable);

    /**
     * Find all doctors at a specific address
     */
    List<DoctorAddress> findByAddressId(UUID addressId);

    /**
     * Find all doctors at a specific address with pagination
     */
    Page<DoctorAddress> findByAddressId(UUID addressId, Pageable pageable);

    /**
     * Find doctors by address and practice role
     */
    List<DoctorAddress> findByAddressIdAndPracticeRole(UUID addressId, DoctorAddress.PracticeRole practiceRole);

    /**
     * Find doctor addresses by practice role
     */
    List<DoctorAddress> findByDoctorIdAndPracticeRole(UUID doctorId, DoctorAddress.PracticeRole practiceRole);

    /**
     * Find relationships by practice role
     */
    List<DoctorAddress> findByPracticeRole(DoctorAddress.PracticeRole practiceRole);

    /**
     * Find relationships by practice role with pagination
     */
    Page<DoctorAddress> findByPracticeRole(DoctorAddress.PracticeRole practiceRole, Pageable pageable);

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

    /**
     * Find doctor-address relationships by doctor and address
     */
    List<DoctorAddress> findByDoctorIdAndAddressId(UUID doctorId, UUID addressId);

    /**
     * Check if doctor-address relationship exists with specific role
     */
    boolean existsByDoctorIdAndAddressIdAndPracticeRole(UUID doctorId, UUID addressId,
            DoctorAddress.PracticeRole practiceRole);

    /**
     * Delete doctor-address relationships by doctor and address
     */
    void deleteByDoctorIdAndAddressId(UUID doctorId, UUID addressId);
}