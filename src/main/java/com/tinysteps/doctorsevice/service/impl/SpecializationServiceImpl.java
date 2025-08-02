package com.tinysteps.doctorsevice.service.impl;

import com.tinysteps.doctorsevice.exception.DoctorNotFoundException;
import com.tinysteps.doctorsevice.exception.EntityNotFoundException;
import com.tinysteps.doctorsevice.mapper.SpecializationMapper;
import com.tinysteps.doctorsevice.model.SpecializationRequestDto;
import com.tinysteps.doctorsevice.model.SpecializationResponseDto;
import com.tinysteps.doctorsevice.repository.DoctorRepository;
import com.tinysteps.doctorsevice.repository.SpecializationRepository;
import com.tinysteps.doctorsevice.service.SpecializationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SpecializationServiceImpl implements SpecializationService {

    private final SpecializationRepository specializationRepository;
    private final DoctorRepository doctorRepository;
    private final SpecializationMapper specializationMapper;

    public SpecializationServiceImpl(SpecializationRepository specializationRepository, DoctorRepository doctorRepository, SpecializationMapper specializationMapper) {
        this.specializationRepository = specializationRepository;
        this.doctorRepository = doctorRepository;
        this.specializationMapper = specializationMapper;
    }

    @Override
    public SpecializationResponseDto create(UUID doctorId, SpecializationRequestDto requestDto) {
        var doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new DoctorNotFoundException("Doctor not found with ID: " + doctorId));
        var specialization = specializationMapper.fromRequestDto(requestDto);
        specialization.setDoctor(doctor);
        var savedSpecialization = specializationRepository.save(specialization);
        return specializationMapper.toResponseDto(savedSpecialization);
    }

    @Override
    public SpecializationResponseDto findById(UUID id) {
        return specializationRepository.findById(id)
                .map(specializationMapper::toResponseDto)
                .orElseThrow(() -> new EntityNotFoundException("Specialization not found with ID: " + id));
    }

    @Override
    public Page<SpecializationResponseDto> findAll(Pageable pageable) {
        return specializationRepository.findAll(pageable).map(specializationMapper::toResponseDto);
    }

    @Override
    public SpecializationResponseDto update(UUID id, SpecializationRequestDto requestDto) {
        var existingSpecialization = specializationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Specialization not found with ID: " + id));
        specializationMapper.updateEntityFromDto(requestDto, existingSpecialization);
        var updatedSpecialization = specializationRepository.save(existingSpecialization);
        return specializationMapper.toResponseDto(updatedSpecialization);
    }

    @Override
    public SpecializationResponseDto partialUpdate(UUID id, SpecializationRequestDto requestDto) {
        var existingSpecialization = specializationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Specialization not found with ID: " + id));
        specializationMapper.updateEntityFromDto(requestDto, existingSpecialization);
        var updatedSpecialization = specializationRepository.save(existingSpecialization);
        return specializationMapper.toResponseDto(updatedSpecialization);
    }

    @Override
    public void delete(UUID id) {
        if (!specializationRepository.existsById(id)) {
            throw new EntityNotFoundException("Specialization not found with ID: " + id);
        }
        specializationRepository.deleteById(id);
    }

    @Override
    public List<SpecializationResponseDto> findByDoctorId(UUID doctorId) {
        if (!doctorRepository.existsById(doctorId)) {
            throw new DoctorNotFoundException("Doctor not found with ID: " + doctorId);
        }
        return specializationRepository.findByDoctorId(doctorId).stream()
                .map(specializationMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public Page<SpecializationResponseDto> findByDoctorId(UUID doctorId, Pageable pageable) {
        if (!doctorRepository.existsById(doctorId)) {
            throw new DoctorNotFoundException("Doctor not found with ID: " + doctorId);
        }
        return specializationRepository.findByDoctorId(doctorId, pageable).map(specializationMapper::toResponseDto);
    }

    @Override
    public Page<SpecializationResponseDto> findBySpeciality(String speciality, Pageable pageable) {
        return specializationRepository.findBySpecialityContainingIgnoreCase(speciality, pageable).map(specializationMapper::toResponseDto);
    }

    @Override
    public Page<SpecializationResponseDto> findBySubspecialization(String subspecialization, Pageable pageable) {
        return specializationRepository.findBySubspecializationContainingIgnoreCase(subspecialization, pageable).map(specializationMapper::toResponseDto);
    }

    @Override
    public List<SpecializationResponseDto> findByDoctorIdAndSpeciality(UUID doctorId, String speciality) {
        return specializationRepository.findByDoctorIdAndSpecialityContainingIgnoreCase(doctorId, speciality).stream()
                .map(specializationMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public Page<SpecializationResponseDto> findBySpecialityPattern(String pattern, Pageable pageable) {
        return specializationRepository.findBySpecialityContainingIgnoreCase(pattern, pageable).map(specializationMapper::toResponseDto);
    }

    @Override
    public List<String> findDistinctSpecialities() {
        return specializationRepository.findDistinctSpecialities();
    }

    @Override
    public List<String> findDistinctSubspecializations() {
        return specializationRepository.findDistinctSubSpecializations();
    }

    @Override
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
    public long countByDoctorId(UUID doctorId) {
        return specializationRepository.countByDoctorId(doctorId);
    }

    @Override
    public long countBySpeciality(String speciality) {
        return specializationRepository.countBySpecialityContainingIgnoreCase(speciality);
    }

    @Override
    public long countBySubspecialization(String subspecialization) {
        return specializationRepository.countBySubspecializationContainingIgnoreCase(subspecialization);
    }

    @Override
    public long countAll() {
        return specializationRepository.count();
    }

    @Override
    public Object[] getSpecializationStatistics() {
        return new Object[]{specializationRepository.count(), specializationRepository.countDistinctSpecialities()};
    }

    @Override
    public List<SpecializationResponseDto> createBatch(UUID doctorId, List<SpecializationRequestDto> requestDtos) {
        var doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new DoctorNotFoundException("Doctor not found with ID: " + doctorId));
        var specializations = requestDtos.stream()
                .map(specializationMapper::fromRequestDto)
                .peek(spec -> spec.setDoctor(doctor))
                .collect(Collectors.toList());
        var savedSpecializations = specializationRepository.saveAll(specializations);
        return savedSpecializations.stream()
                .map(specializationMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteByDoctorId(UUID doctorId) {
        specializationRepository.deleteByDoctorId(doctorId);
    }

    @Override
    public void deleteBatch(List<UUID> ids) {
        specializationRepository.deleteAllById(ids);
    }

    @Override
    public List<UUID> findDoctorsBySpeciality(String speciality) {
        return specializationRepository.findDoctorIdsBySpeciality(speciality);
    }

    @Override
    public List<UUID> findDoctorsBySubspecialization(String subspecialization) {
        return specializationRepository.findDoctorIdsBySubspecialization(subspecialization);
    }

    @Override
    public List<UUID> findDoctorsWithMultipleSpecializations() {
        return specializationRepository.findDoctorsWithMultipleSpecializations();
    }

    @Override
    public List<UUID> findDoctorsWithoutSpecializations() {
        return specializationRepository.findDoctorsWithoutSpecializations();
    }

    @Override
    public boolean isDoctorSpecializedIn(UUID doctorId, String speciality) {
        return hasSpecialization(doctorId, speciality);
    }
}
