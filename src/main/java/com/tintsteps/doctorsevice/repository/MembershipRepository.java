package com.tintsteps.doctorsevice.repository;

import com.tintsteps.doctorsevice.entity.Membership;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MembershipRepository extends JpaRepository<Membership, UUID> {
    
    // Find memberships by doctor ID
    List<Membership> findByDoctorId(UUID doctorId);
    
    // Find memberships by council name (case-insensitive)
    List<Membership> findByMembershipCouncilNameContainingIgnoreCase(String councilName);
    
    // Find memberships by exact council name
    List<Membership> findByMembershipCouncilName(String councilName);
    
    // Find memberships by doctor and council name
    List<Membership> findByDoctorIdAndMembershipCouncilNameContainingIgnoreCase(UUID doctorId, String councilName);
    
    // Count memberships by doctor
    long countByDoctorId(UUID doctorId);
    
    // Count memberships by council
    long countByMembershipCouncilName(String councilName);
    
    // Find doctors with membership in specific council
    @Query("SELECT m FROM Membership m WHERE LOWER(m.membershipCouncilName) = LOWER(:councilName)")
    List<Membership> findByExactCouncilName(@Param("councilName") String councilName);
    
    // Find all unique council names
    @Query("SELECT DISTINCT m.membershipCouncilName FROM Membership m ORDER BY m.membershipCouncilName")
    List<String> findAllUniqueCouncilNames();
    
    // Check if doctor has membership in specific council
    @Query("SELECT COUNT(m) > 0 FROM Membership m WHERE m.doctor.id = :doctorId AND LOWER(m.membershipCouncilName) = LOWER(:councilName)")
    boolean existsByDoctorIdAndCouncilName(@Param("doctorId") UUID doctorId, @Param("councilName") String councilName);
    
    // Additional missing methods
    Page<Membership> findByDoctorId(UUID doctorId, Pageable pageable);
    Page<Membership> findByCouncilNameContainingIgnoreCase(String councilName, Pageable pageable);
    List<Membership> findByDoctorIdAndCouncilNameContainingIgnoreCase(UUID doctorId, String councilName);
    boolean existsByDoctorId(UUID doctorId);
    boolean existsByDoctorIdAndCouncilNameContainingIgnoreCase(UUID doctorId, String councilName);
    long countByCouncilNameContainingIgnoreCase(String councilName);
    void deleteByDoctorId(UUID doctorId);
    List<String> findDistinctCouncilNames();
}
