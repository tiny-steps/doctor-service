package com.tinysteps.doctorservice.service.impl;

import com.tinysteps.doctorservice.entity.SpecializationMaster;
import com.tinysteps.doctorservice.repository.SpecializationMasterRepository;
import com.tinysteps.doctorservice.service.SpecializationMasterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SpecializationMasterServiceImpl implements SpecializationMasterService {

    private final SpecializationMasterRepository specializationMasterRepository;

    @Override
    @Transactional
    public SpecializationMaster getOrCreate(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Specialization name cannot be empty");
        }

        String trimmedName = name.trim();

        // Try to find existing specialization (case-insensitive)
        Optional<SpecializationMaster> existing = specializationMasterRepository.findByNameIgnoreCase(trimmedName);

        if (existing.isPresent()) {
            log.debug("Found existing specialization: {}", trimmedName);
            return existing.get();
        }

        // Create new specialization
        SpecializationMaster newSpec = new SpecializationMaster();
        newSpec.setName(trimmedName);
        newSpec.setIsActive(true);

        SpecializationMaster saved = specializationMasterRepository.save(newSpec);
        log.info("Created new specialization: {} with ID: {}", trimmedName, saved.getId());

        return saved;
    }

    @Override
    public Optional<SpecializationMaster> findByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return Optional.empty();
        }
        return specializationMasterRepository.findByNameIgnoreCase(name.trim());
    }

    @Override
    public List<SpecializationMaster> getAllActive() {
        return specializationMasterRepository.findByIsActiveTrue();
    }

    @Override
    public List<String> getAllDistinctNames() {
        return specializationMasterRepository.findAllDistinctNames();
    }

    @Override
    @Transactional
    public SpecializationMaster create(String name, String description) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Specialization name cannot be empty");
        }

        String trimmedName = name.trim();

        // Check if already exists
        if (specializationMasterRepository.existsByNameIgnoreCase(trimmedName)) {
            throw new IllegalArgumentException("Specialization with name '" + trimmedName + "' already exists");
        }

        SpecializationMaster spec = new SpecializationMaster();
        spec.setName(trimmedName);
        spec.setDescription(description);
        spec.setIsActive(true);

        return specializationMasterRepository.save(spec);
    }

    @Override
    @Transactional
    public SpecializationMaster update(UUID id, String name, String description) {
        SpecializationMaster spec = specializationMasterRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Specialization not found with ID: " + id));

        if (name != null && !name.trim().isEmpty()) {
            String trimmedName = name.trim();
            // Check if another specialization with this name exists
            Optional<SpecializationMaster> existing = specializationMasterRepository.findByNameIgnoreCase(trimmedName);
            if (existing.isPresent() && !existing.get().getId().equals(id)) {
                throw new IllegalArgumentException(
                        "Another specialization with name '" + trimmedName + "' already exists");
            }
            spec.setName(trimmedName);
        }

        if (description != null) {
            spec.setDescription(description);
        }

        return specializationMasterRepository.save(spec);
    }

    @Override
    @Transactional
    public void deactivate(UUID id) {
        SpecializationMaster spec = specializationMasterRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Specialization not found with ID: " + id));
        spec.setIsActive(false);
        specializationMasterRepository.save(spec);
        log.info("Deactivated specialization: {}", spec.getName());
    }

    @Override
    @Transactional
    public void activate(UUID id) {
        SpecializationMaster spec = specializationMasterRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Specialization not found with ID: " + id));
        spec.setIsActive(true);
        specializationMasterRepository.save(spec);
        log.info("Activated specialization: {}", spec.getName());
    }

    @Override
    public SpecializationMaster findById(UUID id) {
        return specializationMasterRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Specialization not found with ID: " + id));
    }

    @Override
    public SpecializationMaster validateAndGet(UUID id) {
        SpecializationMaster spec = findById(id);
        
        if (!spec.getIsActive()) {
            throw new IllegalArgumentException(
                    "Specialization '" + spec.getName() + "' is inactive and cannot be assigned to doctors");
        }
        
        log.debug("Validated specialization: {}", spec.getName());
        return spec;
    }
}


