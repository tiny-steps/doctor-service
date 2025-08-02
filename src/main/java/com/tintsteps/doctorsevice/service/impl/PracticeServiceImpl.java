package com.tintsteps.doctorsevice.service.impl;

import com.tintsteps.doctorsevice.exception.DoctorNotFoundException;
import com.tintsteps.doctorsevice.exception.EntityNotFoundException;
import com.tintsteps.doctorsevice.mapper.PracticeMapper;
import com.tintsteps.doctorsevice.model.PracticeRequestDto;
import com.tintsteps.doctorsevice.model.PracticeResponseDto;
import com.tintsteps.doctorsevice.repository.DoctorRepository;
import com.tintsteps.doctorsevice.repository.PracticeRepository;
import com.tintsteps.doctorsevice.service.PracticeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PracticeServiceImpl implements PracticeService {

    private final PracticeRepository practiceRepository;
    private final DoctorRepository doctorRepository;
    private final PracticeMapper practiceMapper;

    public PracticeServiceImpl(PracticeRepository practiceRepository, DoctorRepository doctorRepository, PracticeMapper practiceMapper) {
        this.practiceRepository = practiceRepository;
        this.doctorRepository = doctorRepository;
        this.practiceMapper = practiceMapper;
    }

    @Override
    public PracticeResponseDto create(UUID doctorId, PracticeRequestDto requestDto) {
        var doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new DoctorNotFoundException("Doctor not found with ID: " + doctorId));
        var practice = practiceMapper.fromRequestDto(requestDto);
        practice.setDoctor(doctor);
        var savedPractice = practiceRepository.save(practice);
        return practiceMapper.toResponseDto(savedPractice);
    }

    @Override
    public PracticeResponseDto findById(UUID id) {
        return practiceRepository.findById(id)
                .map(practiceMapper::toResponseDto)
                .orElseThrow(() -> new EntityNotFoundException("Practice not found with ID: " + id));
    }

    @Override
    public Page<PracticeResponseDto> findAll(Pageable pageable) {
        return practiceRepository.findAll(pageable).map(practiceMapper::toResponseDto);
    }

    @Override
    public PracticeResponseDto update(UUID id, PracticeRequestDto requestDto) {
        var existingPractice = practiceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Practice not found with ID: " + id));
        practiceMapper.updateEntityFromDto(requestDto, existingPractice);
        var updatedPractice = practiceRepository.save(existingPractice);
        return practiceMapper.toResponseDto(updatedPractice);
    }

    @Override
    public PracticeResponseDto partialUpdate(UUID id, PracticeRequestDto requestDto) {
        var existingPractice = practiceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Practice not found with ID: " + id));
        practiceMapper.updateEntityFromDto(requestDto, existingPractice);
        var updatedPractice = practiceRepository.save(existingPractice);
        return practiceMapper.toResponseDto(updatedPractice);
    }

    @Override
    public void delete(UUID id) {
        if (!practiceRepository.existsById(id)) {
            throw new EntityNotFoundException("Practice not found with ID: " + id);
        }
        practiceRepository.deleteById(id);
    }

    @Override
    public List<PracticeResponseDto> findByDoctorId(UUID doctorId) {
        if (!doctorRepository.existsById(doctorId)) {
            throw new DoctorNotFoundException("Doctor not found with ID: " + doctorId);
        }
        return practiceRepository.findByDoctorId(doctorId).stream()
                .map(practiceMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<PracticeResponseDto> findByDoctorIdOrderByPosition(UUID doctorId) {
        if (!doctorRepository.existsById(doctorId)) {
            throw new DoctorNotFoundException("Doctor not found with ID: " + doctorId);
        }
        return practiceRepository.findByDoctorIdOrderByPracticePosition(doctorId).stream()
                .map(practiceMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<PracticeResponseDto> findByDoctorIdOrderByCreatedAt(UUID doctorId) {
        if (!doctorRepository.existsById(doctorId)) {
            throw new DoctorNotFoundException("Doctor not found with ID: " + doctorId);
        }
        return practiceRepository.findByDoctorIdOrderByCreatedAtDesc(doctorId).stream()
                .map(practiceMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public Page<PracticeResponseDto> findByDoctorId(UUID doctorId, Pageable pageable) {
        if (!doctorRepository.existsById(doctorId)) {
            throw new DoctorNotFoundException("Doctor not found with ID: " + doctorId);
        }
        return practiceRepository.findByDoctorId(doctorId, pageable).map(practiceMapper::toResponseDto);
    }

    @Override
    public Page<PracticeResponseDto> findByPracticeName(String practiceName, Pageable pageable) {
        return practiceRepository.findByPracticeNameContainingIgnoreCase(practiceName, pageable).map(practiceMapper::toResponseDto);
    }

    @Override
    public Page<PracticeResponseDto> findByPracticeType(String practiceType, Pageable pageable) {
        return practiceRepository.findByPracticeType(practiceType, pageable).map(practiceMapper::toResponseDto);
    }

    @Override
    public Page<PracticeResponseDto> findByAddressId(UUID addressId, Pageable pageable) {
        return practiceRepository.findByAddressId(addressId, pageable).map(practiceMapper::toResponseDto);
    }

    @Override
    public PracticeResponseDto findBySlug(String slug) {
        return practiceRepository.findBySlug(slug)
                .map(practiceMapper::toResponseDto)
                .orElseThrow(() -> new EntityNotFoundException("Practice not found with slug: " + slug));
    }

    @Override
    public List<PracticeResponseDto> findByDoctorIdAndPracticeType(UUID doctorId, String practiceType) {
        return practiceRepository.findByDoctorIdAndPracticeType(doctorId, practiceType).stream()
                .map(practiceMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<PracticeResponseDto> findByDoctorIdAndAddressId(UUID doctorId, UUID addressId) {
        return practiceRepository.findByDoctorIdAndAddressId(doctorId, addressId).stream()
                .map(practiceMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public Page<PracticeResponseDto> findByCreatedAtBetween(Timestamp startDate, Timestamp endDate, Pageable pageable) {
        return practiceRepository.findByCreatedAtBetween(startDate, endDate, pageable).map(practiceMapper::toResponseDto);
    }

    @Override
    public Page<PracticeResponseDto> findRecentPractices(Timestamp startDate, Pageable pageable) {
        return practiceRepository.findByCreatedAtGreaterThanEqual(startDate, pageable).map(practiceMapper::toResponseDto);
    }

    @Override
    public Page<PracticeResponseDto> findByCriteria(UUID doctorId, String practiceType, UUID addressId, Pageable pageable) {
        // This would require a specification-based search.
        return Page.empty(pageable);
    }

    @Override
    public boolean existsById(UUID id) {
        return practiceRepository.existsById(id);
    }

    @Override
    public boolean existsByDoctorId(UUID doctorId) {
        return practiceRepository.existsByDoctorId(doctorId);
    }

    @Override
    public boolean existsBySlug(String slug) {
        return practiceRepository.existsBySlug(slug);
    }

    @Override
    public boolean hasDoctorPracticeAtAddress(UUID doctorId, UUID addressId) {
        return practiceRepository.existsByDoctorIdAndAddressId(doctorId, addressId);
    }

    @Override
    public boolean isSlugAvailable(String slug) {
        return !practiceRepository.existsBySlug(slug);
    }

    @Override
    public long countByDoctorId(UUID doctorId) {
        return practiceRepository.countByDoctorId(doctorId);
    }

    @Override
    public long countByPracticeType(String practiceType) {
        return practiceRepository.countByPracticeType(practiceType);
    }

    @Override
    public long countByAddressId(UUID addressId) {
        return practiceRepository.countByAddressId(addressId);
    }

    @Override
    public long countAll() {
        return practiceRepository.count();
    }

    @Override
    public List<PracticeResponseDto> createBatch(UUID doctorId, List<PracticeRequestDto> requestDtos) {
        var doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new DoctorNotFoundException("Doctor not found with ID: " + doctorId));
        var practices = requestDtos.stream()
                .map(practiceMapper::fromRequestDto)
                .peek(practice -> practice.setDoctor(doctor))
                .collect(Collectors.toList());
        var savedPractices = practiceRepository.saveAll(practices);
        return savedPractices.stream()
                .map(practiceMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteByDoctorId(UUID doctorId) {
        practiceRepository.deleteByDoctorId(doctorId);
    }

    @Override
    public void deleteBatch(List<UUID> ids) {
        practiceRepository.deleteAllById(ids);
    }

    @Override
    public List<String> getUniquePracticeTypes() {
        return practiceRepository.findDistinctPracticeTypes();
    }

    @Override
    public List<UUID> getUniqueAddressIds() {
        return practiceRepository.findDistinctAddressIds();
    }

    @Override
    public List<UUID> findDoctorsWithMultiplePractices() {
        return practiceRepository.findDoctorsWithMultiplePractices();
    }

    @Override
    public List<Object[]> getMostCommonPracticeTypes() {
        return practiceRepository.findMostCommonPracticeTypes();
    }

    @Override
    public List<PracticeResponseDto> findHighestPositionPracticesByDoctorId(UUID doctorId) {
        // This would require a more complex query.
        return List.of();
    }

    @Override
    public PracticeResponseDto updatePracticePosition(UUID id, Integer newPosition) {
        var practice = practiceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Practice not found with ID: " + id));
        practice.setPracticePosition(newPosition);
        var savedPractice = practiceRepository.save(practice);
        return practiceMapper.toResponseDto(savedPractice);
    }

    @Override
    public void reorderPractices(UUID doctorId, List<UUID> practiceIds) {
        // This requires careful implementation to avoid race conditions and ensure data integrity.
    }
}
