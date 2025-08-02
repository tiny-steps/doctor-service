package com.tinysteps.doctorsevice.service.impl;

import com.tinysteps.doctorsevice.entity.Doctor;
import com.tinysteps.doctorsevice.exception.DoctorNotFoundException;
import com.tinysteps.doctorsevice.mapper.DoctorMapper;
import com.tinysteps.doctorsevice.model.DoctorRequestDto;
import com.tinysteps.doctorsevice.model.DoctorResponseDto;
import com.tinysteps.doctorsevice.repository.DoctorRepository;
import com.tinysteps.doctorsevice.service.DoctorService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DoctorServiceImpl implements DoctorService {

    private final DoctorRepository doctorRepository;
    private final DoctorMapper doctorMapper;

    public DoctorServiceImpl(DoctorRepository doctorRepository, DoctorMapper doctorMapper) {
        this.doctorRepository = doctorRepository;
        this.doctorMapper = doctorMapper;
    }

    @Override
    public DoctorResponseDto create(DoctorRequestDto requestDto) {
        var doctor = doctorMapper.fromRequestDto(requestDto);
        var savedDoctor = doctorRepository.save(doctor);
        return doctorMapper.toResponseDto(savedDoctor);
    }

    @Override
    public DoctorResponseDto findById(UUID id) {
        return doctorRepository.findById(id)
                .map(doctorMapper::toResponseDto)
                .orElseThrow(() -> new DoctorNotFoundException("Doctor not found with ID: " + id));
    }

    @Override
    public Page<DoctorResponseDto> findAll(Pageable pageable) {
        return doctorRepository.findAll(pageable).map(doctorMapper::toResponseDto);
    }

    @Override
    public DoctorResponseDto update(UUID id, DoctorRequestDto requestDto) {
        var existingDoctor = doctorRepository.findById(id)
                .orElseThrow(() -> new DoctorNotFoundException("Doctor not found with ID: " + id));
        doctorMapper.updateEntityFromDto(requestDto, existingDoctor);
        var updatedDoctor = doctorRepository.save(existingDoctor);
        return doctorMapper.toResponseDto(updatedDoctor);
    }

    @Override
    public DoctorResponseDto partialUpdate(UUID id, DoctorRequestDto requestDto) {
        var existingDoctor = doctorRepository.findById(id)
                .orElseThrow(() -> new DoctorNotFoundException("Doctor not found with ID: " + id));
        doctorMapper.updateEntityFromDto(requestDto, existingDoctor);
        var updatedDoctor = doctorRepository.save(existingDoctor);
        return doctorMapper.toResponseDto(updatedDoctor);
    }

    @Override
    public void delete(UUID id) {
        if (!doctorRepository.existsById(id)) {
            throw new DoctorNotFoundException("Doctor not found with ID: " + id);
        }
        doctorRepository.deleteById(id);
    }

    @Override
    public DoctorResponseDto findBySlug(String slug) {
        return doctorRepository.findBySlug(slug)
                .map(doctorMapper::toResponseDto)
                .orElseThrow(() -> new DoctorNotFoundException("Doctor not found with slug: " + slug));
    }

    @Override
    public DoctorResponseDto findByUserId(UUID userId) {
        return doctorRepository.findByUserId(userId)
                .map(doctorMapper::toResponseDto)
                .orElseThrow(() -> new DoctorNotFoundException("Doctor not found with user ID: " + userId));
    }

    @Override
    public List<DoctorResponseDto> findByName(String name) {
        return doctorRepository.findByNameContainingIgnoreCase(name).stream()
                .map(doctorMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public Page<DoctorResponseDto> findByStatus(String status, Pageable pageable) {
        return doctorRepository.findByStatus(status, pageable).map(doctorMapper::toResponseDto);
    }

    @Override
    public Page<DoctorResponseDto> findByVerificationStatus(Boolean isVerified, Pageable pageable) {
        return doctorRepository.findByIsVerified(isVerified, pageable).map(doctorMapper::toResponseDto);
    }

    @Override
    public Page<DoctorResponseDto> findByGender(String gender, Pageable pageable) {
        return doctorRepository.findByGender(gender, pageable).map(doctorMapper::toResponseDto);
    }

    @Override
    public Page<DoctorResponseDto> findByExperienceRange(Integer minYears, Integer maxYears, Pageable pageable) {
        return doctorRepository.findByExperienceYearsBetween(minYears, maxYears, pageable).map(doctorMapper::toResponseDto);
    }

    @Override
    public Page<DoctorResponseDto> findByMinRating(BigDecimal minRating, Pageable pageable) {
        return doctorRepository.findByRatingAverageGreaterThanEqual(minRating, pageable).map(doctorMapper::toResponseDto);
    }

    @Override
    public Page<DoctorResponseDto> findBySpeciality(String speciality, Pageable pageable) {
        // This assumes a relationship between Doctor and Specialization that is not directly in the Doctor entity.
        // This would require a more complex query or a different data model.
        // For now, returning an empty page.
        return Page.empty(pageable);
    }

    @Override
    public Page<DoctorResponseDto> findByLocation(UUID addressId, Pageable pageable) {
        // This assumes a relationship between Doctor and Address that is not directly in the Doctor entity.
        // This would require a more complex query or a different data model.
        // For now, returning an empty page.
        return Page.empty(pageable);
    }

    @Override
    public Page<DoctorResponseDto> searchDoctors(String name, String speciality, Boolean isVerified, BigDecimal minRating, Pageable pageable) {
        // This would require a specification-based search.
        // For now, returning an empty page.
        return Page.empty(pageable);
    }

    @Override
    public Page<DoctorResponseDto> findTopRatedDoctors(Pageable pageable) {
        return doctorRepository.findAllByOrderByRatingAverageDesc(pageable).map(doctorMapper::toResponseDto);
    }

    @Override
    public Page<DoctorResponseDto> findVerifiedDoctorsWithMinRating(BigDecimal minRating, Pageable pageable) {
        return doctorRepository.findByIsVerifiedAndRatingAverageGreaterThanEqual(true, minRating, pageable).map(doctorMapper::toResponseDto);
    }

    @Override
    public DoctorResponseDto verifyDoctor(UUID id) {
        var doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new DoctorNotFoundException("Doctor not found with ID: " + id));
        doctor.setIsVerified(true);
        var updatedDoctor = doctorRepository.save(doctor);
        return doctorMapper.toResponseDto(updatedDoctor);
    }

    @Override
    public DoctorResponseDto unverifyDoctor(UUID id) {
        var doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new DoctorNotFoundException("Doctor not found with ID: " + id));
        doctor.setIsVerified(false);
        var updatedDoctor = doctorRepository.save(doctor);
        return doctorMapper.toResponseDto(updatedDoctor);
    }

    @Override
    public DoctorResponseDto activateDoctor(UUID id) {
        var doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new DoctorNotFoundException("Doctor not found with ID: " + id));
        doctor.setStatus("ACTIVE");
        var updatedDoctor = doctorRepository.save(doctor);
        return doctorMapper.toResponseDto(updatedDoctor);
    }

    @Override
    public DoctorResponseDto deactivateDoctor(UUID id) {
        var doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new DoctorNotFoundException("Doctor not found with ID: " + id));
        doctor.setStatus("INACTIVE");
        var updatedDoctor = doctorRepository.save(doctor);
        return doctorMapper.toResponseDto(updatedDoctor);
    }

    @Override
    public DoctorResponseDto suspendDoctor(UUID id) {
        var doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new DoctorNotFoundException("Doctor not found with ID: " + id));
        doctor.setStatus("SUSPENDED");
        var updatedDoctor = doctorRepository.save(doctor);
        return doctorMapper.toResponseDto(updatedDoctor);
    }

    @Override
    public void updateRatingAndReviewCount(UUID id, BigDecimal newRating, Integer reviewCount) {
        var doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new DoctorNotFoundException("Doctor not found with ID: " + id));
        doctor.setRatingAverage(newRating);
        doctor.setReviewCount(reviewCount);
        doctorRepository.save(doctor);
    }

    @Override
    public boolean existsById(UUID id) {
        return doctorRepository.existsById(id);
    }

    @Override
    public boolean existsBySlug(String slug) {
        return doctorRepository.existsBySlug(slug);
    }

    @Override
    public boolean existsByUserId(UUID userId) {
        return doctorRepository.existsByUserId(userId);
    }

    @Override
    public boolean isSlugAvailable(String slug) {
        return !doctorRepository.existsBySlug(slug);
    }

    @Override
    public boolean isDoctorVerified(UUID id) {
        return doctorRepository.findById(id)
                .map(Doctor::getIsVerified)
                .orElse(false);
    }

    @Override
    public boolean isDoctorActive(UUID id) {
        return doctorRepository.findById(id)
                .map(doctor -> "ACTIVE".equalsIgnoreCase(doctor.getStatus()))
                .orElse(false);
    }

    @Override
    public long countAll() {
        return doctorRepository.count();
    }

    @Override
    public long countByStatus(String status) {
        return doctorRepository.countByStatus(status);
    }

    @Override
    public long countByVerificationStatus(Boolean isVerified) {
        return doctorRepository.countByIsVerified(isVerified);
    }

    @Override
    public long countBySpeciality(String speciality) {
        // This would require a more complex query or a different data model.
        return 0;
    }

    @Override
    public List<DoctorResponseDto> createBatch(List<DoctorRequestDto> requestDtos) {
        var doctors = requestDtos.stream()
                .map(doctorMapper::fromRequestDto)
                .collect(Collectors.toList());
        var savedDoctors = doctorRepository.saveAll(doctors);
        return savedDoctors.stream()
                .map(doctorMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteBatch(List<UUID> ids) {
        doctorRepository.deleteAllById(ids);
    }

    @Override
    public int calculateProfileCompleteness(UUID id) {
        var doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new DoctorNotFoundException("Doctor not found with ID: " + id));

        int completeness = 0;
        if (doctor.getName() != null && !doctor.getName().isEmpty()) completeness += 10;
        if (doctor.getAbout() != null && !doctor.getAbout().isEmpty()) completeness += 10;
        if (doctor.getGender() != null && !doctor.getGender().isEmpty()) completeness += 5;
        if (doctor.getExperienceYears() != null) completeness += 10;
        // Add checks for other fields

        return Math.min(100, completeness);
    }

    @Override
    public boolean isProfileComplete(UUID id) {
        return calculateProfileCompleteness(id) == 100;
    }

    @Override
    public List<String> getMissingProfileFields(UUID id) {
        var doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new DoctorNotFoundException("Doctor not found with ID: " + id));
        List<String> missingFields = new ArrayList<>();

        if (doctor.getName() == null || doctor.getName().isEmpty()) missingFields.add("name");
        if (doctor.getAbout() == null || doctor.getAbout().isEmpty()) missingFields.add("bio");
        if (doctor.getGender() == null || doctor.getGender().isEmpty()) missingFields.add("gender");
        if (doctor.getExperienceYears() == null) missingFields.add("yearsOfExperience");
        // Add checks for other fields

        return missingFields;
    }
}
