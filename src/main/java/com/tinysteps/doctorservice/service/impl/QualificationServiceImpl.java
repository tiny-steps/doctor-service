package com.tinysteps.doctorservice.service.impl;

import com.tinysteps.doctorservice.exception.DoctorNotFoundException;
import com.tinysteps.doctorservice.exception.EntityNotFoundException;
import com.tinysteps.doctorservice.mapper.QualificationMapper;
import com.tinysteps.doctorservice.model.QualificationRequestDto;
import com.tinysteps.doctorservice.model.QualificationResponseDto;
import com.tinysteps.doctorservice.repository.DoctorRepository;
import com.tinysteps.doctorservice.repository.QualificationRepository;
import com.tinysteps.doctorservice.service.QualificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class QualificationServiceImpl implements QualificationService {

    private final QualificationRepository qualificationRepository;
    private final DoctorRepository doctorRepository;
    private final QualificationMapper qualificationMapper;

    public QualificationServiceImpl(QualificationRepository qualificationRepository, DoctorRepository doctorRepository,
            QualificationMapper qualificationMapper) {
        this.qualificationRepository = qualificationRepository;
        this.doctorRepository = doctorRepository;
        this.qualificationMapper = qualificationMapper;
    }

    @Override
    @CacheEvict(value = { "doctors", "doctor", "qualifications" }, allEntries = true)
    public QualificationResponseDto create(UUID doctorId, QualificationRequestDto requestDto) {
        var doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new DoctorNotFoundException("Doctor not found with ID: " + doctorId));
        var qualification = qualificationMapper.fromRequestDto(requestDto);
        qualification.setDoctor(doctor);
        var savedQualification = qualificationRepository.save(qualification);
        log.info("Created qualification {} for doctor {}", savedQualification.getId(), doctorId);
        return qualificationMapper.toResponseDto(savedQualification);
    }

    @Override
    @Cacheable(value = "qualification", key = "#id")
    public QualificationResponseDto findById(UUID id) {
        return qualificationRepository.findById(id)
                .map(qualificationMapper::toResponseDto)
                .orElseThrow(() -> new EntityNotFoundException("Qualification not found with ID: " + id));
    }

    @Override
    @Cacheable(value = "qualifications", key = "'all'")
    public Page<QualificationResponseDto> findAll(Pageable pageable) {
        return qualificationRepository.findAll(pageable).map(qualificationMapper::toResponseDto);
    }

    @Override
    @CacheEvict(value = { "doctors", "doctor", "qualifications" }, allEntries = true)
    public QualificationResponseDto update(UUID id, QualificationRequestDto requestDto) {
        var existingQualification = qualificationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Qualification not found with ID: " + id));
        qualificationMapper.updateEntityFromDto(requestDto, existingQualification);
        var updatedQualification = qualificationRepository.save(existingQualification);
        log.info("Updated qualification {} for doctor {}", id, existingQualification.getDoctor().getId());
        return qualificationMapper.toResponseDto(updatedQualification);
    }

    @Override
    @CacheEvict(value = { "doctors", "doctor", "qualifications" }, allEntries = true)
    public QualificationResponseDto partialUpdate(UUID id, QualificationRequestDto requestDto) {
        var existingQualification = qualificationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Qualification not found with ID: " + id));
        qualificationMapper.updateEntityFromDto(requestDto, existingQualification);
        var updatedQualification = qualificationRepository.save(existingQualification);
        log.info("Partially updated qualification {} for doctor {}", id, existingQualification.getDoctor().getId());
        return qualificationMapper.toResponseDto(updatedQualification);
    }

    @Override
    @CacheEvict(value = { "doctors", "doctor", "qualifications" }, allEntries = true)
    public void delete(UUID id) {
        var qualification = qualificationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Qualification not found with ID: " + id));
        UUID doctorId = qualification.getDoctor().getId();
        qualificationRepository.deleteById(id);
        log.info("Deleted qualification {} for doctor {}", id, doctorId);
    }

    @Override
    @Cacheable(value = "qualifications", key = "'doctor-' + #doctorId")
    public List<QualificationResponseDto> findByDoctorId(UUID doctorId) {
        if (!doctorRepository.existsById(doctorId)) {
            throw new DoctorNotFoundException("Doctor not found with ID: " + doctorId);
        }
        return qualificationRepository.findByDoctorId(doctorId).stream()
                .map(qualificationMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "qualifications", key = "'doctor-' + #doctorId + '-ordered'")
    public List<QualificationResponseDto> findByDoctorIdOrderByYear(UUID doctorId) {
        if (!doctorRepository.existsById(doctorId)) {
            throw new DoctorNotFoundException("Doctor not found with ID: " + doctorId);
        }
        return qualificationRepository.findByDoctorIdOrderByCompletionYearDesc(doctorId).stream()
                .map(qualificationMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "qualifications", key = "'doctor-' + #doctorId + '-page-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<QualificationResponseDto> findByDoctorId(UUID doctorId, Pageable pageable) {
        if (!doctorRepository.existsById(doctorId)) {
            throw new DoctorNotFoundException("Doctor not found with ID: " + doctorId);
        }
        return qualificationRepository.findByDoctorId(doctorId, pageable).map(qualificationMapper::toResponseDto);
    }

    @Override
    @Cacheable(value = "qualifications", key = "'name-' + #qualificationName")
    public Page<QualificationResponseDto> findByQualificationName(String qualificationName, Pageable pageable) {
        return qualificationRepository.findByQualificationNameContainingIgnoreCase(qualificationName, pageable)
                .map(qualificationMapper::toResponseDto);
    }

    @Override
    @Cacheable(value = "qualifications", key = "'college-' + #collegeName")
    public Page<QualificationResponseDto> findByCollegeName(String collegeName, Pageable pageable) {
        return qualificationRepository.findByCollegeNameContainingIgnoreCase(collegeName, pageable)
                .map(qualificationMapper::toResponseDto);
    }

    @Override
    @Cacheable(value = "qualifications", key = "'year-' + #year")
    public Page<QualificationResponseDto> findByCompletionYear(Integer year, Pageable pageable) {
        return qualificationRepository.findByCompletionYear(year, pageable).map(qualificationMapper::toResponseDto);
    }

    @Override
    @Cacheable(value = "qualifications", key = "'year-range-' + #startYear + '-' + #endYear")
    public Page<QualificationResponseDto> findByYearRange(Integer startYear, Integer endYear, Pageable pageable) {
        return qualificationRepository.findByCompletionYearBetween(startYear, endYear, pageable)
                .map(qualificationMapper::toResponseDto);
    }

    @Override
    @Cacheable(value = "qualifications", key = "'doctor-' + #doctorId + '-name-' + #qualificationName")
    public List<QualificationResponseDto> findByDoctorIdAndQualificationName(UUID doctorId, String qualificationName) {
        return qualificationRepository
                .findByDoctorIdAndQualificationNameContainingIgnoreCase(doctorId, qualificationName).stream()
                .map(qualificationMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "qualifications", key = "'recent-' + #startYear")
    public Page<QualificationResponseDto> findRecentQualifications(Integer startYear, Pageable pageable) {
        return qualificationRepository.findByCompletionYearGreaterThanEqual(startYear, pageable)
                .map(qualificationMapper::toResponseDto);
    }

    @Override
    public boolean existsById(UUID id) {
        return qualificationRepository.existsById(id);
    }

    @Override
    public boolean existsByDoctorId(UUID doctorId) {
        return qualificationRepository.existsByDoctorId(doctorId);
    }

    @Override
    public boolean hasDoctorQualification(UUID doctorId, String qualificationName) {
        return qualificationRepository.existsByDoctorIdAndQualificationNameContainingIgnoreCase(doctorId,
                qualificationName);
    }

    @Override
    @Cacheable(value = "qualifications", key = "'count-doctor-' + #doctorId")
    public long countByDoctorId(UUID doctorId) {
        return qualificationRepository.countByDoctorId(doctorId);
    }

    @Override
    @Cacheable(value = "qualifications", key = "'count-name-' + #qualificationName")
    public long countByQualificationName(String qualificationName) {
        return qualificationRepository.countByQualificationNameContainingIgnoreCase(qualificationName);
    }

    @Override
    @Cacheable(value = "qualifications", key = "'count-college-' + #collegeName")
    public long countByCollegeName(String collegeName) {
        return qualificationRepository.countByCollegeNameContainingIgnoreCase(collegeName);
    }

    @Override
    @Cacheable(value = "qualifications", key = "'count-all'")
    public long countAll() {
        return qualificationRepository.count();
    }

    @Override
    @CacheEvict(value = { "doctors", "doctor", "qualifications" }, allEntries = true)
    public List<QualificationResponseDto> createBatch(UUID doctorId, List<QualificationRequestDto> requestDtos) {
        var doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new DoctorNotFoundException("Doctor not found with ID: " + doctorId));
        var qualifications = requestDtos.stream()
                .map(qualificationMapper::fromRequestDto)
                .peek(q -> q.setDoctor(doctor))
                .collect(Collectors.toList());
        var savedQualifications = qualificationRepository.saveAll(qualifications);
        log.info("Created {} qualifications for doctor {}", savedQualifications.size(), doctorId);
        return savedQualifications.stream()
                .map(qualificationMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @CacheEvict(value = { "doctors", "doctor", "qualifications" }, allEntries = true)
    public void deleteByDoctorId(UUID doctorId) {
        long count = qualificationRepository.countByDoctorId(doctorId);
        qualificationRepository.deleteByDoctorId(doctorId);
        log.info("Deleted {} qualifications for doctor {}", count, doctorId);
    }

    @Override
    @CacheEvict(value = { "doctors", "doctor", "qualifications" }, allEntries = true)
    public void deleteBatch(List<UUID> ids) {
        long count = ids.size();
        qualificationRepository.deleteAllById(ids);
        log.info("Deleted {} qualifications in batch", count);
    }

    @Override
    @Cacheable(value = "qualifications", key = "'doctor-' + #doctorId + '-year-range-' + #startYear + '-' + #endYear")
    public List<QualificationResponseDto> findDoctorQualificationsInYearRange(UUID doctorId, Integer startYear,
            Integer endYear) {
        return qualificationRepository.findByDoctorIdAndCompletionYearBetween(doctorId, startYear, endYear).stream()
                .map(qualificationMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "qualifications", key = "'unique-names'")
    public List<String> getUniqueQualificationNames() {
        return qualificationRepository.findDistinctQualificationNames();
    }

    @Override
    @Cacheable(value = "qualifications", key = "'unique-colleges'")
    public List<String> getUniqueCollegeNames() {
        return qualificationRepository.findDistinctCollegeNames();
    }

    @Override
    public boolean isDoctorQualifiedInField(UUID doctorId, String field) {
        return hasDoctorQualification(doctorId, field);
    }
}
