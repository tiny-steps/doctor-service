package com.tintsteps.doctorsevice.service.impl;

import com.tintsteps.doctorsevice.exception.DoctorNotFoundException;
import com.tintsteps.doctorsevice.exception.EntityNotFoundException;
import com.tintsteps.doctorsevice.mapper.RegistrationMapper;
import com.tintsteps.doctorsevice.model.RegistrationRequestDto;
import com.tintsteps.doctorsevice.model.RegistrationResponseDto;
import com.tintsteps.doctorsevice.repository.DoctorRepository;
import com.tintsteps.doctorsevice.repository.RegistrationRepository;
import com.tintsteps.doctorsevice.service.RegistrationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RegistrationServiceImpl implements RegistrationService {

    private final RegistrationRepository registrationRepository;
    private final DoctorRepository doctorRepository;
    private final RegistrationMapper registrationMapper;

    public RegistrationServiceImpl(RegistrationRepository registrationRepository, DoctorRepository doctorRepository, RegistrationMapper registrationMapper) {
        this.registrationRepository = registrationRepository;
        this.doctorRepository = doctorRepository;
        this.registrationMapper = registrationMapper;
    }

    @Override
    public RegistrationResponseDto create(UUID doctorId, RegistrationRequestDto requestDto) {
        var doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new DoctorNotFoundException("Doctor not found with ID: " + doctorId));
        var registration = registrationMapper.fromRequestDto(requestDto);
        registration.setDoctor(doctor);
        var savedRegistration = registrationRepository.save(registration);
        return registrationMapper.toResponseDto(savedRegistration);
    }

    @Override
    public RegistrationResponseDto findById(UUID id) {
        return registrationRepository.findById(id)
                .map(registrationMapper::toResponseDto)
                .orElseThrow(() -> new EntityNotFoundException("Registration not found with ID: " + id));
    }

    @Override
    public Page<RegistrationResponseDto> findAll(Pageable pageable) {
        return registrationRepository.findAll(pageable).map(registrationMapper::toResponseDto);
    }

    @Override
    public RegistrationResponseDto update(UUID id, RegistrationRequestDto requestDto) {
        var existingRegistration = registrationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Registration not found with ID: " + id));
        registrationMapper.updateEntityFromDto(requestDto, existingRegistration);
        var updatedRegistration = registrationRepository.save(existingRegistration);
        return registrationMapper.toResponseDto(updatedRegistration);
    }

    @Override
    public RegistrationResponseDto partialUpdate(UUID id, RegistrationRequestDto requestDto) {
        var existingRegistration = registrationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Registration not found with ID: " + id));
        registrationMapper.updateEntityFromDto(requestDto, existingRegistration);
        var updatedRegistration = registrationRepository.save(existingRegistration);
        return registrationMapper.toResponseDto(updatedRegistration);
    }

    @Override
    public void delete(UUID id) {
        if (!registrationRepository.existsById(id)) {
            throw new EntityNotFoundException("Registration not found with ID: " + id);
        }
        registrationRepository.deleteById(id);
    }

    @Override
    public List<RegistrationResponseDto> findByDoctorId(UUID doctorId) {
        if (!doctorRepository.existsById(doctorId)) {
            throw new DoctorNotFoundException("Doctor not found with ID: " + doctorId);
        }
        return registrationRepository.findByDoctorId(doctorId).stream()
                .map(registrationMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<RegistrationResponseDto> findByDoctorIdOrderByYear(UUID doctorId) {
        if (!doctorRepository.existsById(doctorId)) {
            throw new DoctorNotFoundException("Doctor not found with ID: " + doctorId);
        }
        return registrationRepository.findByDoctorIdOrderByRegistrationYearDesc(doctorId).stream()
                .map(registrationMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public Page<RegistrationResponseDto> findByDoctorId(UUID doctorId, Pageable pageable) {
        if (!doctorRepository.existsById(doctorId)) {
            throw new DoctorNotFoundException("Doctor not found with ID: " + doctorId);
        }
        return registrationRepository.findByDoctorId(doctorId, pageable).map(registrationMapper::toResponseDto);
    }

    @Override
    public Page<RegistrationResponseDto> findByCouncilName(String councilName, Pageable pageable) {
        return registrationRepository.findByRegistrationCouncilNameContainingIgnoreCase(councilName, pageable).map(registrationMapper::toResponseDto);
    }

    @Override
    public RegistrationResponseDto findByRegistrationNumber(String registrationNumber) {
        return registrationRepository.findByRegistrationNumber(registrationNumber)
                .map(registrationMapper::toResponseDto)
                .orElseThrow(() -> new EntityNotFoundException("Registration not found with number: " + registrationNumber));
    }

    @Override
    public Page<RegistrationResponseDto> findByRegistrationYear(Integer year, Pageable pageable) {
        return registrationRepository.findByRegistrationYear(year, pageable).map(registrationMapper::toResponseDto);
    }

    @Override
    public Page<RegistrationResponseDto> findByYearRange(Integer startYear, Integer endYear, Pageable pageable) {
        return registrationRepository.findByRegistrationYearBetween(startYear, endYear, pageable).map(registrationMapper::toResponseDto);
    }

    @Override
    public List<RegistrationResponseDto> findByDoctorIdAndCouncilName(UUID doctorId, String councilName) {
        return registrationRepository.findByDoctorIdAndRegistrationCouncilNameContainingIgnoreCase(doctorId, councilName).stream()
                .map(registrationMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public RegistrationResponseDto findByDoctorIdAndRegistrationNumber(UUID doctorId, String registrationNumber) {
        return registrationRepository.findByDoctorIdAndRegistrationNumber(doctorId, registrationNumber)
                .map(registrationMapper::toResponseDto)
                .orElseThrow(() -> new EntityNotFoundException("Registration not found for doctor " + doctorId + " with number " + registrationNumber));
    }

    @Override
    public Page<RegistrationResponseDto> findRecentRegistrations(Integer startYear, Pageable pageable) {
        return registrationRepository.findByRegistrationYearGreaterThanEqual(startYear, pageable).map(registrationMapper::toResponseDto);
    }

    @Override
    public boolean existsById(UUID id) {
        return registrationRepository.existsById(id);
    }

    @Override
    public boolean existsByDoctorId(UUID doctorId) {
        return registrationRepository.existsByDoctorId(doctorId);
    }

    @Override
    public boolean existsByRegistrationNumber(String registrationNumber) {
        return registrationRepository.existsByRegistrationNumber(registrationNumber);
    }

    @Override
    public boolean hasDoctorRegistrationWithCouncil(UUID doctorId, String councilName) {
        return registrationRepository.existsByDoctorIdAndRegistrationCouncilNameContainingIgnoreCase(doctorId, councilName);
    }

    @Override
    public boolean isRegistrationNumberUnique(String registrationNumber) {
        return !registrationRepository.existsByRegistrationNumber(registrationNumber);
    }

    @Override
    public long countByDoctorId(UUID doctorId) {
        return registrationRepository.countByDoctorId(doctorId);
    }

    @Override
    public long countByCouncilName(String councilName) {
        return registrationRepository.countByRegistrationCouncilNameContainingIgnoreCase(councilName);
    }

    @Override
    public long countByRegistrationYear(Integer year) {
        return registrationRepository.countByRegistrationYear(year);
    }

    @Override
    public long countAll() {
        return registrationRepository.count();
    }

    @Override
    public List<RegistrationResponseDto> createBatch(UUID doctorId, List<RegistrationRequestDto> requestDtos) {
        var doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new DoctorNotFoundException("Doctor not found with ID: " + doctorId));
        var registrations = requestDtos.stream()
                .map(registrationMapper::fromRequestDto)
                .peek(reg -> reg.setDoctor(doctor))
                .collect(Collectors.toList());
        var savedRegistrations = registrationRepository.saveAll(registrations);
        return savedRegistrations.stream()
                .map(registrationMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteByDoctorId(UUID doctorId) {
        registrationRepository.deleteByDoctorId(doctorId);
    }

    @Override
    public void deleteBatch(List<UUID> ids) {
        registrationRepository.deleteAllById(ids);
    }

    @Override
    public List<String> getUniqueCouncilNames() {
        return registrationRepository.findDistinctCouncilNames();
    }

    @Override
    public List<RegistrationResponseDto> findDoctorRegistrationsInYearRange(UUID doctorId, Integer startYear, Integer endYear) {
        return registrationRepository.findByDoctorIdAndRegistrationYearBetween(doctorId, startYear, endYear).stream()
                .map(registrationMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isDoctorRegisteredWithCouncil(UUID doctorId, String councilName) {
        return hasDoctorRegistrationWithCouncil(doctorId, councilName);
    }

    @Override
    public boolean validateRegistrationNumber(String registrationNumber) {
        // Basic validation logic, can be expanded
        return registrationNumber != null && !registrationNumber.trim().isEmpty();
    }
}
