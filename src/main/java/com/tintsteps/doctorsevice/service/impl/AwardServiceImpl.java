package com.tintsteps.doctorsevice.service.impl;

import com.tintsteps.doctorsevice.exception.DoctorNotFoundException;
import com.tintsteps.doctorsevice.exception.EntityNotFoundException;
import com.tintsteps.doctorsevice.mapper.AwardMapper;
import com.tintsteps.doctorsevice.model.AwardRequestDto;
import com.tintsteps.doctorsevice.model.AwardResponseDto;
import com.tintsteps.doctorsevice.repository.AwardRepository;
import com.tintsteps.doctorsevice.repository.DoctorRepository;
import com.tintsteps.doctorsevice.service.AwardService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AwardServiceImpl implements AwardService {

    private final AwardRepository awardRepository;
    private final DoctorRepository doctorRepository;
    private final AwardMapper awardMapper;

    public AwardServiceImpl(AwardRepository awardRepository, DoctorRepository doctorRepository, AwardMapper awardMapper) {
        this.awardRepository = awardRepository;
        this.doctorRepository = doctorRepository;
        this.awardMapper = awardMapper;
    }

    @Override
    public AwardResponseDto create(UUID doctorId, AwardRequestDto requestDto) {
        var doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new DoctorNotFoundException(doctorId));
        var award = awardMapper.fromRequestDto(requestDto);
        award.setDoctor(doctor);
        var savedAward = awardRepository.save(award);
        return awardMapper.toResponseDto(savedAward);
    }

    @Override
    public AwardResponseDto findById(UUID id) {
        return awardRepository.findById(id)
                .map(awardMapper::toResponseDto)
                .orElseThrow(() -> new EntityNotFoundException("Award not found with","id",String.valueOf(id)));
    }

    @Override
    public Page<AwardResponseDto> findAll(Pageable pageable) {
        return awardRepository.findAll(pageable).map(awardMapper::toResponseDto);
    }

    @Override
    public AwardResponseDto update(UUID id, AwardRequestDto requestDto) {
        var existingAward = awardRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Award not found with ID: " + id));
        awardMapper.updateEntityFromDto(requestDto, existingAward);
        var updatedAward = awardRepository.save(existingAward);
        return awardMapper.toResponseDto(updatedAward);
    }

    @Override
    public AwardResponseDto partialUpdate(UUID id, AwardRequestDto requestDto) {
        var existingAward = awardRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Award not found with ID: " + id));
        awardMapper.updateEntityFromDto(requestDto, existingAward);
        var updatedAward = awardRepository.save(existingAward);
        return awardMapper.toResponseDto(updatedAward);
    }

    @Override
    public void delete(UUID id) {
        if (!awardRepository.existsById(id)) {
            throw new EntityNotFoundException("Award not found with ID: " + id);
        }
        awardRepository.deleteById(id);
    }

    @Override
    public List<AwardResponseDto> findByDoctorId(UUID doctorId) {
        if (!doctorRepository.existsById(doctorId)) {
            throw new DoctorNotFoundException("Doctor not found with ID: " + doctorId);
        }
        return awardRepository.findByDoctorId(doctorId).stream()
                .map(awardMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<AwardResponseDto> findByDoctorIdOrderByYear(UUID doctorId) {
        if (!doctorRepository.existsById(doctorId)) {
            throw new DoctorNotFoundException("Doctor not found with ID: " + doctorId);
        }
        return awardRepository.findByDoctorIdOrderByAwardedYearDesc(doctorId).stream()
                .map(awardMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public Page<AwardResponseDto> findByDoctorId(UUID doctorId, Pageable pageable) {
        if (!doctorRepository.existsById(doctorId)) {
            throw new DoctorNotFoundException("Doctor not found with ID: " + doctorId);
        }
        return awardRepository.findByDoctorId(doctorId, pageable).map(awardMapper::toResponseDto);
    }

    @Override
    public Page<AwardResponseDto> findByTitle(String title, Pageable pageable) {
        return awardRepository.findByTitleContainingIgnoreCase(title, pageable).map(awardMapper::toResponseDto);
    }

    @Override
    public Page<AwardResponseDto> findByAwardedYear(Integer year, Pageable pageable) {
        return awardRepository.findByAwardedYear(year, pageable).map(awardMapper::toResponseDto);
    }

    @Override
    public Page<AwardResponseDto> findByAwardedYearRange(Integer startYear, Integer endYear, Pageable pageable) {
        return awardRepository.findByAwardedYearBetween(startYear, endYear, pageable).map(awardMapper::toResponseDto);
    }

    @Override
    public List<AwardResponseDto> findByDoctorIdAndAwardedYear(UUID doctorId, Integer year) {
        if (!doctorRepository.existsById(doctorId)) {
            throw new DoctorNotFoundException("Doctor not found with ID: " + doctorId);
        }
        return awardRepository.findByDoctorIdAndAwardedYear(doctorId, year).stream()
                .map(awardMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public Page<AwardResponseDto> findRecentAwards(Integer startYear, Pageable pageable) {
        return awardRepository.findByAwardedYearGreaterThanEqual(startYear, pageable).map(awardMapper::toResponseDto);
    }

    @Override
    public boolean existsById(UUID id) {
        return awardRepository.existsById(id);
    }

    @Override
    public boolean existsByDoctorId(UUID doctorId) {
        return awardRepository.existsByDoctorId(doctorId);
    }

    @Override
    public long countByDoctorId(UUID doctorId) {
        if (!doctorRepository.existsById(doctorId)) {
            throw new DoctorNotFoundException(doctorId);
        }
        return awardRepository.countByDoctorId(doctorId);
    }

    @Override
    public long countByAwardedYear(Integer year) {
        return awardRepository.countByAwardedYear(year);
    }

    @Override
    public long countAll() {
        return awardRepository.count();
    }

    @Override
    public List<AwardResponseDto> createBatch(UUID doctorId, List<AwardRequestDto> requestDtos) {
        var doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new DoctorNotFoundException(doctorId));
        var awards = requestDtos.stream()
                .map(awardMapper::fromRequestDto)
                .peek(award -> award.setDoctor(doctor))
                .collect(Collectors.toList());
        var savedAwards = awardRepository.saveAll(awards);
        return savedAwards.stream()
                .map(awardMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteByDoctorId(UUID doctorId) {
        if (!doctorRepository.existsById(doctorId)) {
            throw new DoctorNotFoundException(doctorId);
        }
        awardRepository.deleteByDoctorId(doctorId);
    }

    @Override
    public void deleteBatch(List<UUID> ids) {
        awardRepository.deleteAllById(ids);
    }

    @Override
    public List<AwardResponseDto> findDoctorAwardsInYearRange(UUID doctorId, Integer startYear, Integer endYear) {
        if (!doctorRepository.existsById(doctorId)) {
            throw new DoctorNotFoundException(doctorId);
        }
        return awardRepository.findByDoctorIdAndYearRange(doctorId, startYear, endYear).stream()
                .map(awardMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public boolean hasDoctorReceivedAwardInYear(UUID doctorId, Integer year) {
        if (!doctorRepository.existsById(doctorId)) {
            throw new DoctorNotFoundException("Doctor not found with ID: " + doctorId);
        }
        return awardRepository.existsByDoctorIdAndAwardedYear(doctorId, year);
    }
}
