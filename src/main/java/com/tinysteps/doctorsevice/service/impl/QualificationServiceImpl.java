package com.tinysteps.doctorsevice.service.impl;

import com.tinysteps.doctorsevice.exception.DoctorNotFoundException;
import com.tinysteps.doctorsevice.exception.EntityNotFoundException;
import com.tinysteps.doctorsevice.mapper.QualificationMapper;
import com.tinysteps.doctorsevice.model.QualificationRequestDto;
import com.tinysteps.doctorsevice.model.QualificationResponseDto;
import com.tinysteps.doctorsevice.repository.DoctorRepository;
import com.tinysteps.doctorsevice.repository.QualificationRepository;
import com.tinysteps.doctorsevice.service.QualificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class QualificationServiceImpl implements QualificationService {

    private final QualificationRepository qualificationRepository;
    private final DoctorRepository doctorRepository;
    private final QualificationMapper qualificationMapper;

    public QualificationServiceImpl(QualificationRepository qualificationRepository, DoctorRepository doctorRepository, QualificationMapper qualificationMapper) {
        this.qualificationRepository = qualificationRepository;
        this.doctorRepository = doctorRepository;
        this.qualificationMapper = qualificationMapper;
    }

    @Override
    public QualificationResponseDto create(UUID doctorId, QualificationRequestDto requestDto) {
        var doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new DoctorNotFoundException("Doctor not found with ID: " + doctorId));
        var qualification = qualificationMapper.fromRequestDto(requestDto);
        qualification.setDoctor(doctor);
        var savedQualification = qualificationRepository.save(qualification);
        return qualificationMapper.toResponseDto(savedQualification);
    }

    @Override
    public QualificationResponseDto findById(UUID id) {
        return qualificationRepository.findById(id)
                .map(qualificationMapper::toResponseDto)
                .orElseThrow(() -> new EntityNotFoundException("Qualification not found with ID: " + id));
    }

    @Override
    public Page<QualificationResponseDto> findAll(Pageable pageable) {
        return qualificationRepository.findAll(pageable).map(qualificationMapper::toResponseDto);
    }

    @Override
    public QualificationResponseDto update(UUID id, QualificationRequestDto requestDto) {
        var existingQualification = qualificationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Qualification not found with ID: " + id));
        qualificationMapper.updateEntityFromDto(requestDto, existingQualification);
        var updatedQualification = qualificationRepository.save(existingQualification);
        return qualificationMapper.toResponseDto(updatedQualification);
    }

    @Override
    public QualificationResponseDto partialUpdate(UUID id, QualificationRequestDto requestDto) {
        var existingQualification = qualificationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Qualification not found with ID: " + id));
        qualificationMapper.updateEntityFromDto(requestDto, existingQualification);
        var updatedQualification = qualificationRepository.save(existingQualification);
        return qualificationMapper.toResponseDto(updatedQualification);
    }

    @Override
    public void delete(UUID id) {
        if (!qualificationRepository.existsById(id)) {
            throw new EntityNotFoundException("Qualification not found with ID: " + id);
        }
        qualificationRepository.deleteById(id);
    }

    @Override
    public List<QualificationResponseDto> findByDoctorId(UUID doctorId) {
        if (!doctorRepository.existsById(doctorId)) {
            throw new DoctorNotFoundException("Doctor not found with ID: " + doctorId);
        }
        return qualificationRepository.findByDoctorId(doctorId).stream()
                .map(qualificationMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<QualificationResponseDto> findByDoctorIdOrderByYear(UUID doctorId) {
        if (!doctorRepository.existsById(doctorId)) {
            throw new DoctorNotFoundException("Doctor not found with ID: " + doctorId);
        }
        return qualificationRepository.findByDoctorIdOrderByCompletionYearDesc(doctorId).stream()
                .map(qualificationMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public Page<QualificationResponseDto> findByDoctorId(UUID doctorId, Pageable pageable) {
        if (!doctorRepository.existsById(doctorId)) {
            throw new DoctorNotFoundException("Doctor not found with ID: " + doctorId);
        }
        return qualificationRepository.findByDoctorId(doctorId, pageable).map(qualificationMapper::toResponseDto);
    }

    @Override
    public Page<QualificationResponseDto> findByQualificationName(String qualificationName, Pageable pageable) {
        return qualificationRepository.findByQualificationNameContainingIgnoreCase(qualificationName, pageable).map(qualificationMapper::toResponseDto);
    }

    @Override
    public Page<QualificationResponseDto> findByCollegeName(String collegeName, Pageable pageable) {
        return qualificationRepository.findByCollegeNameContainingIgnoreCase(collegeName, pageable).map(qualificationMapper::toResponseDto);
    }

    @Override
    public Page<QualificationResponseDto> findByCompletionYear(Integer year, Pageable pageable) {
        return qualificationRepository.findByCompletionYear(year, pageable).map(qualificationMapper::toResponseDto);
    }

    @Override
    public Page<QualificationResponseDto> findByYearRange(Integer startYear, Integer endYear, Pageable pageable) {
        return qualificationRepository.findByCompletionYearBetween(startYear, endYear, pageable).map(qualificationMapper::toResponseDto);
    }

    @Override
    public List<QualificationResponseDto> findByDoctorIdAndQualificationName(UUID doctorId, String qualificationName) {
        return qualificationRepository.findByDoctorIdAndQualificationNameContainingIgnoreCase(doctorId, qualificationName).stream()
                .map(qualificationMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public Page<QualificationResponseDto> findRecentQualifications(Integer startYear, Pageable pageable) {
        return qualificationRepository.findByCompletionYearGreaterThanEqual(startYear, pageable).map(qualificationMapper::toResponseDto);
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
        return qualificationRepository.existsByDoctorIdAndQualificationNameContainingIgnoreCase(doctorId, qualificationName);
    }

    @Override
    public long countByDoctorId(UUID doctorId) {
        return qualificationRepository.countByDoctorId(doctorId);
    }

    @Override
    public long countByQualificationName(String qualificationName) {
        return qualificationRepository.countByQualificationNameContainingIgnoreCase(qualificationName);
    }

    @Override
    public long countByCollegeName(String collegeName) {
        return qualificationRepository.countByCollegeNameContainingIgnoreCase(collegeName);
    }

    @Override
    public long countAll() {
        return qualificationRepository.count();
    }

    @Override
    public List<QualificationResponseDto> createBatch(UUID doctorId, List<QualificationRequestDto> requestDtos) {
        var doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new DoctorNotFoundException("Doctor not found with ID: " + doctorId));
        var qualifications = requestDtos.stream()
                .map(qualificationMapper::fromRequestDto)
                .peek(q -> q.setDoctor(doctor))
                .collect(Collectors.toList());
        var savedQualifications = qualificationRepository.saveAll(qualifications);
        return savedQualifications.stream()
                .map(qualificationMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteByDoctorId(UUID doctorId) {
        qualificationRepository.deleteByDoctorId(doctorId);
    }

    @Override
    public void deleteBatch(List<UUID> ids) {
        qualificationRepository.deleteAllById(ids);
    }

    @Override
    public List<QualificationResponseDto> findDoctorQualificationsInYearRange(UUID doctorId, Integer startYear, Integer endYear) {
        return qualificationRepository.findByDoctorIdAndCompletionYearBetween(doctorId, startYear, endYear).stream()
                .map(qualificationMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getUniqueQualificationNames() {
        return qualificationRepository.findDistinctQualificationNames();
    }

    @Override
    public List<String> getUniqueCollegeNames() {
        return qualificationRepository.findDistinctCollegeNames();
    }

    @Override
    public boolean isDoctorQualifiedInField(UUID doctorId, String field) {
        return hasDoctorQualification(doctorId, field);
    }
}
