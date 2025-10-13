package com.tinysteps.doctorservice.service.impl;

import com.tinysteps.doctorservice.entity.DoctorSpecialization;
import com.tinysteps.doctorservice.entity.SpecializationMaster;
import com.tinysteps.doctorservice.exception.DoctorNotFoundException;
import com.tinysteps.doctorservice.exception.EntityNotFoundException;
import com.tinysteps.doctorservice.mapper.SpecializationMapper;
import com.tinysteps.doctorservice.model.SpecializationRequestDto;
import com.tinysteps.doctorservice.model.SpecializationResponseDto;
import com.tinysteps.doctorservice.repository.DoctorRepository;
import com.tinysteps.doctorservice.repository.SpecializationRepository;
import com.tinysteps.doctorservice.service.SpecializationService;
import com.tinysteps.doctorservice.service.SpecializationMasterService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class SpecializationServiceImpl implements SpecializationService {

    private static final Logger log = LoggerFactory.getLogger(SpecializationServiceImpl.class);

    private final SpecializationRepository specializationRepository;
    private final DoctorRepository doctorRepository;
    private final SpecializationMapper specializationMapper;
    private final SpecializationMasterService specializationMasterService;

    public SpecializationServiceImpl(SpecializationRepository specializationRepository,
            DoctorRepository doctorRepository, SpecializationMapper specializationMapper,
            SpecializationMasterService specializationMasterService) {
        this.specializationRepository = specializationRepository;
        this.doctorRepository = doctorRepository;
        this.specializationMapper = specializationMapper;
        this.specializationMasterService = specializationMasterService;
    }

    @Override
    @CacheEvict(value = { "doctors", "doctor", "specializations" }, allEntries = true)
    public SpecializationResponseDto create(UUID doctorId, SpecializationRequestDto requestDto) {
        var doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new DoctorNotFoundException("Doctor not found with ID: " + doctorId));

        // Get or create the specialization master record
        SpecializationMaster master = specializationMasterService.getOrCreate(requestDto.speciality());

        // Create the junction record
        DoctorSpecialization specialization = new DoctorSpecialization();
        specialization.setDoctor(doctor);
        specialization.setSpecializationMaster(master);
        specialization.setSubspecialization(requestDto.subspecialization());
        // Populate deprecated speciality field for backward compatibility
        specialization.setSpeciality(master.getName());

        var savedSpecialization = specializationRepository.save(specialization);
        log.info("Created specialization {} for doctor {}", savedSpecialization.getId(), doctorId);
        return specializationMapper.toResponseDto(savedSpecialization);
    }

    @Override
    @Cacheable(value = "specialization", key = "#id")
    public SpecializationResponseDto findById(UUID id) {
        return specializationRepository.findById(id)
                .map(specializationMapper::toResponseDto)
                .orElseThrow(() -> new EntityNotFoundException("Specialization not found with ID: " + id));
    }

    @Override
    @Cacheable(value = "specializations", key = "'all'")
    public Page<SpecializationResponseDto> findAll(Pageable pageable) {
        return specializationRepository.findAll(pageable).map(specializationMapper::toResponseDto);
    }

    @Override
    @CacheEvict(value = { "doctors", "doctor", "specializations" }, allEntries = true)
    public SpecializationResponseDto update(UUID id, SpecializationRequestDto requestDto) {
        var existingSpecialization = specializationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Specialization not found with ID: " + id));

        // Update specialization master if changed
        if (requestDto.speciality() != null && !requestDto.speciality().isEmpty()) {
            SpecializationMaster master = specializationMasterService.getOrCreate(requestDto.speciality());
            existingSpecialization.setSpecializationMaster(master);
            // Update deprecated speciality field for backward compatibility
            existingSpecialization.setSpeciality(master.getName());
        }

        // Update subspecialization
        if (requestDto.subspecialization() != null) {
            existingSpecialization.setSubspecialization(requestDto.subspecialization());
        }

        var updatedSpecialization = specializationRepository.save(existingSpecialization);
        log.info("Updated specialization {} for doctor {}", id, existingSpecialization.getDoctor().getId());
        return specializationMapper.toResponseDto(updatedSpecialization);
    }

    @Override
    @CacheEvict(value = { "doctors", "doctor", "specializations" }, allEntries = true)
    public SpecializationResponseDto partialUpdate(UUID id, SpecializationRequestDto requestDto) {
        var existingSpecialization = specializationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Specialization not found with ID: " + id));

        // Update specialization master if provided
        if (requestDto.speciality() != null && !requestDto.speciality().isEmpty()) {
            SpecializationMaster master = specializationMasterService.getOrCreate(requestDto.speciality());
            existingSpecialization.setSpecializationMaster(master);
            // Update deprecated speciality field for backward compatibility
            existingSpecialization.setSpeciality(master.getName());
        }

        // Update subspecialization if provided
        if (requestDto.subspecialization() != null) {
            existingSpecialization.setSubspecialization(requestDto.subspecialization());
        }

        var updatedSpecialization = specializationRepository.save(existingSpecialization);
        log.info("Partially updated specialization {} for doctor {}", id, existingSpecialization.getDoctor().getId());
        return specializationMapper.toResponseDto(updatedSpecialization);
    }

    @Override
    @CacheEvict(value = { "doctors", "doctor", "specializations" }, allEntries = true)
    public void delete(UUID id) {
        var specialization = specializationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Specialization not found with ID: " + id));
        UUID doctorId = specialization.getDoctor().getId();
        specializationRepository.deleteById(id);
        log.info("Deleted specialization {} for doctor {}", id, doctorId);
    }

    @Override
    @Cacheable(value = "specializations", key = "'doctor-' + #doctorId")
    public List<SpecializationResponseDto> findByDoctorId(UUID doctorId) {
        if (!doctorRepository.existsById(doctorId)) {
            throw new DoctorNotFoundException("Doctor not found with ID: " + doctorId);
        }
        return specializationRepository.findByDoctorId(doctorId).stream()
                .map(specializationMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "specializations", key = "'doctor-' + #doctorId + '-page-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<SpecializationResponseDto> findByDoctorId(UUID doctorId, Pageable pageable) {
        if (!doctorRepository.existsById(doctorId)) {
            throw new DoctorNotFoundException("Doctor not found with ID: " + doctorId);
        }
        return specializationRepository.findByDoctorId(doctorId, pageable).map(specializationMapper::toResponseDto);
    }

    @Override
    @Cacheable(value = "specializations", key = "'speciality-' + #speciality")
    public Page<SpecializationResponseDto> findBySpeciality(String speciality, Pageable pageable) {
        return specializationRepository.findBySpecialityContainingIgnoreCase(speciality, pageable)
                .map(specializationMapper::toResponseDto);
    }

    @Override
    @Cacheable(value = "specializations", key = "'subspecialization-' + #subspecialization")
    public Page<SpecializationResponseDto> findBySubspecialization(String subspecialization, Pageable pageable) {
        return specializationRepository.findBySubspecializationContainingIgnoreCase(subspecialization, pageable)
                .map(specializationMapper::toResponseDto);
    }

    @Override
    @Cacheable(value = "specializations", key = "'doctor-' + #doctorId + '-speciality-' + #speciality")
    public List<SpecializationResponseDto> findByDoctorIdAndSpeciality(UUID doctorId, String speciality) {
        return specializationRepository.findByDoctorIdAndSpecialityContainingIgnoreCase(doctorId, speciality).stream()
                .map(specializationMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "specializations", key = "'pattern-' + #pattern")
    public Page<SpecializationResponseDto> findBySpecialityPattern(String pattern, Pageable pageable) {
        return specializationRepository.findBySpecialityContainingIgnoreCase(pattern, pageable)
                .map(specializationMapper::toResponseDto);
    }

    @Override
    @Cacheable(value = "specializations", key = "'distinct-specialities'")
    public List<String> findDistinctSpecialities() {
        return specializationRepository.findDistinctSpecialities();
    }

    @Override
    @Cacheable(value = "specializations", key = "'distinct-subspecializations'")
    public List<String> findDistinctSubspecializations() {
        return specializationRepository.findDistinctSubSpecializations();
    }

    @Override
    @Cacheable(value = "specializations", key = "'subspecializations-by-speciality-' + #speciality")
    public List<String> findSubspecializationsBySpeciality(String speciality) {
        return specializationRepository.findSubspecializationsBySpeciality(speciality);
    }

    @Override
    public boolean existsById(UUID id) {
        return specializationRepository.existsById(id);
    }

    @Override
    public boolean existsByDoctorId(UUID doctorId) {
        return specializationRepository.existsByDoctorId(doctorId);
    }

    @Override
    public boolean existsByDoctorIdAndSpeciality(UUID doctorId, String speciality) {
        return specializationRepository.existsByDoctorIdAndSpecialityContainingIgnoreCase(doctorId, speciality);
    }

    @Override
    public boolean hasSpecialization(UUID doctorId, String speciality) {
        return existsByDoctorIdAndSpeciality(doctorId, speciality);
    }

    @Override
    @Cacheable(value = "specializations", key = "'count-doctor-' + #doctorId")
    public long countByDoctorId(UUID doctorId) {
        return specializationRepository.countByDoctorId(doctorId);
    }

    @Override
    @Cacheable(value = "specializations", key = "'count-speciality-' + #speciality")
    public long countBySpeciality(String speciality) {
        return specializationRepository.countBySpecialityContainingIgnoreCase(speciality);
    }

    @Override
    @Cacheable(value = "specializations", key = "'count-subspecialization-' + #subspecialization")
    public long countBySubspecialization(String subspecialization) {
        return specializationRepository.countBySubspecializationContainingIgnoreCase(subspecialization);
    }

    @Override
    @Cacheable(value = "specializations", key = "'count-all'")
    public long countAll() {
        return specializationRepository.count();
    }

    @Override
    @Cacheable(value = "specializations", key = "'statistics'")
    public Object[] getSpecializationStatistics() {
        return new Object[] { specializationRepository.count(), specializationRepository.countDistinctSpecialities() };
    }

    @Override
    @CacheEvict(value = { "doctors", "doctor", "specializations" }, allEntries = true)
    public List<SpecializationResponseDto> createBatch(UUID doctorId, List<SpecializationRequestDto> requestDtos) {
        var doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new DoctorNotFoundException("Doctor not found with ID: " + doctorId));

        var specializations = requestDtos.stream()
                .map(requestDto -> {
                    // Get or create the specialization master record
                    SpecializationMaster master = specializationMasterService.getOrCreate(requestDto.speciality());

                    // Create the junction record
                    DoctorSpecialization specialization = new DoctorSpecialization();
                    specialization.setDoctor(doctor);
                    specialization.setSpecializationMaster(master);
                    specialization.setSubspecialization(requestDto.subspecialization());
                    // Populate deprecated speciality field for backward compatibility
                    specialization.setSpeciality(master.getName());

                    return specialization;
                })
                .collect(Collectors.toList());

        var savedSpecializations = specializationRepository.saveAll(specializations);
        log.info("Created {} specializations for doctor {}", savedSpecializations.size(), doctorId);
        return savedSpecializations.stream()
                .map(specializationMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @CacheEvict(value = { "doctors", "doctor", "specializations" }, allEntries = true)
    public void deleteByDoctorId(UUID doctorId) {
        long count = specializationRepository.countByDoctorId(doctorId);
        specializationRepository.deleteByDoctorId(doctorId);
        log.info("Deleted {} specializations for doctor {}", count, doctorId);
    }

    @Override
    @CacheEvict(value = { "doctors", "doctor", "specializations" }, allEntries = true)
    public void deleteBatch(List<UUID> ids) {
        long count = ids.size();
        specializationRepository.deleteAllById(ids);
        log.info("Deleted {} specializations in batch", count);
    }

    @Override
    @Cacheable(value = "specializations", key = "'doctors-by-speciality-' + #speciality")
    public List<UUID> findDoctorsBySpeciality(String speciality) {
        return specializationRepository.findDoctorIdsBySpeciality(speciality);
    }

    @Override
    @Cacheable(value = "specializations", key = "'doctors-by-subspecialization-' + #subspecialization")
    public List<UUID> findDoctorsBySubspecialization(String subspecialization) {
        return specializationRepository.findDoctorIdsBySubspecialization(subspecialization);
    }

    @Override
    @Cacheable(value = "specializations", key = "'doctors-multiple-specializations'")
    public List<UUID> findDoctorsWithMultipleSpecializations() {
        return specializationRepository.findDoctorsWithMultipleSpecializations();
    }

    @Override
    @Cacheable(value = "specializations", key = "'doctors-without-specializations'")
    public List<UUID> findDoctorsWithoutSpecializations() {
        return specializationRepository.findDoctorsWithoutSpecializations();
    }

    @Override
    public boolean isDoctorSpecializedIn(UUID doctorId, String speciality) {
        return hasSpecialization(doctorId, speciality);
    }
}
