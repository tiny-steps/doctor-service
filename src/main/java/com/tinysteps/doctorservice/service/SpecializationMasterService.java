package com.tinysteps.doctorservice.service;

import com.tinysteps.doctorservice.entity.SpecializationMaster;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpecializationMasterService {

    /**
     * Get or create a specialization master by name
     * If specialization exists, return it. Otherwise create new.
     */
    SpecializationMaster getOrCreate(String name);

    /**
     * Find specialization by name (case-insensitive)
     */
    Optional<SpecializationMaster> findByName(String name);

    /**
     * Get all active specializations
     */
    List<SpecializationMaster> getAllActive();

    /**
     * Get all distinct specialization names
     */
    List<String> getAllDistinctNames();

    /**
     * Create a new specialization
     */
    SpecializationMaster create(String name, String description);

    /**
     * Update a specialization
     */
    SpecializationMaster update(UUID id, String name, String description);

    /**
     * Deactivate a specialization
     */
    void deactivate(UUID id);

    /**
     * Activate a specialization
     */
    void activate(UUID id);

    /**
     * Find specialization by ID
     * @throws IllegalArgumentException if not found
     */
    SpecializationMaster findById(UUID id);

    /**
     * Validate that specialization exists and is active
     * @throws IllegalArgumentException if not found or inactive
     */
    SpecializationMaster validateAndGet(UUID id);
}


