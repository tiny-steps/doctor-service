package com.tinysteps.doctorservice.repository;

import com.tinysteps.doctorservice.entity.SpecializationMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpecializationMasterRepository extends JpaRepository<SpecializationMaster, UUID> {

    /**
     * Find specialization by exact name (case-sensitive)
     */
    Optional<SpecializationMaster> findByName(String name);

    /**
     * Find specialization by name ignoring case
     */
    Optional<SpecializationMaster> findByNameIgnoreCase(String name);

    /**
     * Check if a specialization with given name exists
     */
    boolean existsByNameIgnoreCase(String name);

    /**
     * Get all active specializations
     */
    List<SpecializationMaster> findByIsActiveTrue();

    /**
     * Get distinct specialization names for autocomplete
     */
    @Query("SELECT DISTINCT s.name FROM SpecializationMaster s WHERE s.isActive = true ORDER BY s.name")
    List<String> findAllDistinctNames();
}




