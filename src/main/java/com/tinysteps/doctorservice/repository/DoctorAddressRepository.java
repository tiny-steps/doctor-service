package com.tinysteps.doctorservice.repository;

import com.tinysteps.doctorservice.entity.DoctorAddress;
import com.tinysteps.doctorservice.entity.DoctorAddressId;
import com.tinysteps.doctorservice.entity.PracticeRole;
import com.tinysteps.doctorservice.entity.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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
     * Find addresses for a specific doctor by status
     */
    List<DoctorAddress> findByDoctorIdAndStatus(UUID doctorId, Status status);

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
    List<DoctorAddress> findByAddressIdAndPracticeRole(UUID addressId, PracticeRole practiceRole);

    /**
     * Find doctor addresses by practice role
     */
    List<DoctorAddress> findByDoctorIdAndPracticeRole(UUID doctorId, PracticeRole practiceRole);

    /**
     * Find relationships by practice role
     */
    List<DoctorAddress> findByPracticeRole(PracticeRole practiceRole);

    /**
     * Find relationships by practice role with pagination
     */
    Page<DoctorAddress> findByPracticeRole(PracticeRole practiceRole, Pageable pageable);

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
     * Update status to INACTIVE for all doctor-address relationships for a doctor
     */
    @Modifying
    @Query("UPDATE DoctorAddress da SET da.status = :status WHERE da.doctorId = :doctorId")
    void updateStatusByDoctorId(@Param("doctorId") UUID doctorId, @Param("status") Status status);

    /**
     * Update status to INACTIVE for all doctor-address relationships for an address
     */
    @Modifying
    @Query("UPDATE DoctorAddress da SET da.status = :status WHERE da.addressId = :addressId")
    void updateStatusByAddressId(@Param("addressId") UUID addressId, @Param("status") Status status);

    /**
     * Update status for a specific doctor-address-role relationship
     */
    @Modifying
    @Query("UPDATE DoctorAddress da SET da.status = :status WHERE da.doctorId = :doctorId AND da.addressId = :addressId AND da.practiceRole = :practiceRole")
    void updateStatusByDoctorIdAndAddressIdAndPracticeRole(@Param("doctorId") UUID doctorId,
            @Param("addressId") UUID addressId,
            @Param("practiceRole") PracticeRole practiceRole,
            @Param("status") Status status);

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
     * Count addresses for a doctor by status
     */
    long countByDoctorIdAndStatus(UUID doctorId, Status status);

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
    boolean existsByDoctorIdAndAddressIdAndPracticeRole(UUID doctorId, UUID addressId, PracticeRole practiceRole);

    /**
     * Delete doctor-address relationships by doctor and address
     */
    void deleteByDoctorIdAndAddressId(UUID doctorId, UUID addressId);

    // New methods for enhanced soft delete functionality

    /**
     * Find doctor-address relationships by doctor and multiple branch IDs
     */
    List<DoctorAddress> findByDoctorIdAndAddressIdIn(UUID doctorId, List<UUID> branchIds);

    /**
     * Update status for multiple doctor-address relationships by branch IDs
     */
    @Modifying
    @Query("UPDATE DoctorAddress da SET da.status = :status WHERE da.doctorId = :doctorId AND da.addressId IN :branchIds")
    void updateStatusByDoctorIdAndAddressIdIn(@Param("doctorId") UUID doctorId,
            @Param("branchIds") List<UUID> branchIds,
            @Param("status") Status status);

    /**
     * Count active branches for a doctor
     */
    @Query("SELECT COUNT(da) FROM DoctorAddress da WHERE da.doctorId = :doctorId AND da.status = com.tinysteps.doctorservice.entity.Status.ACTIVE")
    long countActiveBranchesByDoctorId(@Param("doctorId") UUID doctorId);

    /**
     * Get all active branch IDs for a doctor
     */
    @Query("SELECT DISTINCT da.addressId FROM DoctorAddress da WHERE da.doctorId = :doctorId AND da.status = com.tinysteps.doctorservice.entity.Status.ACTIVE")
    List<UUID> findActiveBranchIdsByDoctorId(@Param("doctorId") UUID doctorId);

    /**
     * Check if doctor has any active branches
     */
    @Query("SELECT COUNT(da) > 0 FROM DoctorAddress da WHERE da.doctorId = :doctorId AND da.status = com.tinysteps.doctorservice.entity.Status.ACTIVE")
    boolean hasActiveBranches(@Param("doctorId") UUID doctorId);

    /**
     * Get all branch IDs and their status for a doctor
     */
    @Query("SELECT da.addressId, da.status FROM DoctorAddress da WHERE da.doctorId = :doctorId")
    List<Object[]> findBranchStatusByDoctorId(@Param("doctorId") UUID doctorId);

    /**
     * Find doctor-address relationships by doctor and specific branch with status
     */
    List<DoctorAddress> findByDoctorIdAndAddressIdAndStatus(UUID doctorId, UUID addressId, Status status);
}
