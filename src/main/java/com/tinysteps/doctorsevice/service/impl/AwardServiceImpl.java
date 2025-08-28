package com.tinysteps.doctorsevice.service.impl;

import com.tinysteps.doctorsevice.exception.DoctorNotFoundException;
import com.tinysteps.doctorsevice.exception.EntityNotFoundException;
import com.tinysteps.doctorsevice.mapper.AwardMapper;
import com.tinysteps.doctorsevice.model.AwardRequestDto;
import com.tinysteps.doctorsevice.model.AwardResponseDto;
import com.tinysteps.doctorsevice.repository.AwardRepository;
import com.tinysteps.doctorsevice.repository.DoctorRepository;
import com.tinysteps.doctorsevice.service.AwardService;
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
public class AwardServiceImpl implements AwardService {

    private final AwardRepository awardRepository;
    private final DoctorRepository doctorRepository;
    private final AwardMapper awardMapper;

    public AwardServiceImpl(AwardRepository awardRepository, DoctorRepository doctorRepository,
            AwardMapper awardMapper) {
        this.awardRepository = awardRepository;
        this.doctorRepository = doctorRepository;
        this.awardMapper = awardMapper;
    }

    @Override
    @CacheEvict(value = { "doctors", "doctor", "awards" }, allEntries = true)
    public AwardResponseDto create(UUID doctorId, AwardRequestDto requestDto) {
        var doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new DoctorNotFoundException(doctorId));
        var award = awardMapper.fromRequestDto(requestDto);
        award.setDoctor(doctor);
        var savedAward = awardRepository.save(award);
        log.info("Created award {} for doctor {}", savedAward.getId(), doctorId);
        return awardMapper.toResponseDto(savedAward);
    }

    @Override
    @Cacheable(value = "award", key = "#id")
    public AwardResponseDto findById(UUID id) {
        return awardRepository.findById(id)
                .map(awardMapper::toResponseDto)
                .orElseThrow(() -> new EntityNotFoundException("Award not found with", "id", String.valueOf(id)));
    }

    @Override
    @Cacheable(value = "awards", key = "'all'")
    public Page<AwardResponseDto> findAll(Pageable pageable) {
        return awardRepository.findAll(pageable).map(awardMapper::toResponseDto);
    }

    @Override
    @CacheEvict(value = { "doctors", "doctor", "awards" }, allEntries = true)
    public AwardResponseDto update(UUID id, AwardRequestDto requestDto) {
        var existingAward = awardRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Award not found with ID: " + id));
        awardMapper.updateEntityFromDto(requestDto, existingAward);
        var updatedAward = awardRepository.save(existingAward);
        log.info("Updated award {} for doctor {}", id, existingAward.getDoctor().getId());
        return awardMapper.toResponseDto(updatedAward);
    }

    @Override
    @CacheEvict(value = { "doctors", "doctor", "awards" }, allEntries = true)
    public AwardResponseDto partialUpdate(UUID id, AwardRequestDto requestDto) {
        var existingAward = awardRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Award not found with ID: " + id));
        awardMapper.updateEntityFromDto(requestDto, existingAward);
        var updatedAward = awardRepository.save(existingAward);
        log.info("Partially updated award {} for doctor {}", id, existingAward.getDoctor().getId());
        return awardMapper.toResponseDto(updatedAward);
    }

    @Override
    @CacheEvict(value = { "doctors", "doctor", "awards" }, allEntries = true)
    public void delete(UUID id) {
        var award = awardRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Award not found with ID: " + id));
        UUID doctorId = award.getDoctor().getId();
        awardRepository.deleteById(id);
        log.info("Deleted award {} for doctor {}", id, doctorId);
    }

    @Override
    @Cacheable(value = "awards", key = "'doctor-' + #doctorId")
    public List<AwardResponseDto> findByDoctorId(UUID doctorId) {
        if (!doctorRepository.existsById(doctorId)) {
            throw new DoctorNotFoundException("Doctor not found with ID: " + doctorId);
        }
        return awardRepository.findByDoctorId(doctorId).stream()
                .map(awardMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "awards", key = "'doctor-' + #doctorId + '-ordered'")
    public List<AwardResponseDto> findByDoctorIdOrderByYear(UUID doctorId) {
        if (!doctorRepository.existsById(doctorId)) {
            throw new DoctorNotFoundException("Doctor not found with ID: " + doctorId);
        }
        return awardRepository.findByDoctorIdOrderByAwardedYearDesc(doctorId).stream()
                .map(awardMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "awards", key = "'doctor-' + #doctorId + '-page-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<AwardResponseDto> findByDoctorId(UUID doctorId, Pageable pageable) {
        if (!doctorRepository.existsById(doctorId)) {
            throw new DoctorNotFoundException("Doctor not found with ID: " + doctorId);
        }
        return awardRepository.findByDoctorId(doctorId, pageable).map(awardMapper::toResponseDto);
    }

    @Override
    @Cacheable(value = "awards", key = "'title-' + #title")
    public Page<AwardResponseDto> findByTitle(String title, Pageable pageable) {
        return awardRepository.findByTitleContainingIgnoreCase(title, pageable).map(awardMapper::toResponseDto);
    }

    @Override
    @Cacheable(value = "awards", key = "'year-' + #year")
    public Page<AwardResponseDto> findByAwardedYear(Integer year, Pageable pageable) {
        return awardRepository.findByAwardedYear(year, pageable).map(awardMapper::toResponseDto);
    }

    @Override
    @Cacheable(value = "awards", key = "'year-range-' + #startYear + '-' + #endYear")
    public Page<AwardResponseDto> findByAwardedYearRange(Integer startYear, Integer endYear, Pageable pageable) {
        return awardRepository.findByAwardedYearBetween(startYear, endYear, pageable).map(awardMapper::toResponseDto);
    }

    @Override
    @Cacheable(value = "awards", key = "'doctor-' + #doctorId + '-year-' + #year")
    public List<AwardResponseDto> findByDoctorIdAndAwardedYear(UUID doctorId, Integer year) {
        if (!doctorRepository.existsById(doctorId)) {
            throw new DoctorNotFoundException("Doctor not found with ID: " + doctorId);
        }
        return awardRepository.findByDoctorIdAndAwardedYear(doctorId, year).stream()
                .map(awardMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "awards", key = "'recent-' + #startYear")
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
    @Cacheable(value = "awards", key = "'count-doctor-' + #doctorId")
    public long countByDoctorId(UUID doctorId) {
        if (!doctorRepository.existsById(doctorId)) {
            throw new DoctorNotFoundException(doctorId);
        }
        return awardRepository.countByDoctorId(doctorId);
    }

    @Override
    @Cacheable(value = "awards", key = "'count-year-' + #year")
    public long countByAwardedYear(Integer year) {
        return awardRepository.countByAwardedYear(year);
    }

    @Override
    @Cacheable(value = "awards", key = "'count-all'")
    public long countAll() {
        return awardRepository.count();
    }

    @Override
    @CacheEvict(value = { "doctors", "doctor", "awards" }, allEntries = true)
    public List<AwardResponseDto> createBatch(UUID doctorId, List<AwardRequestDto> requestDtos) {
        var doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new DoctorNotFoundException(doctorId));
        var awards = requestDtos.stream()
                .map(awardMapper::fromRequestDto)
                .peek(award -> award.setDoctor(doctor))
                .collect(Collectors.toList());
        var savedAwards = awardRepository.saveAll(awards);
        log.info("Created {} awards for doctor {}", savedAwards.size(), doctorId);
        return savedAwards.stream()
                .map(awardMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @CacheEvict(value = { "doctors", "doctor", "awards" }, allEntries = true)
    public void deleteByDoctorId(UUID doctorId) {
        if (!doctorRepository.existsById(doctorId)) {
            throw new DoctorNotFoundException(doctorId);
        }
        long count = awardRepository.countByDoctorId(doctorId);
        awardRepository.deleteByDoctorId(doctorId);
        log.info("Deleted {} awards for doctor {}", count, doctorId);
    }

    @Override
    @CacheEvict(value = { "doctors", "doctor", "awards" }, allEntries = true)
    public void deleteBatch(List<UUID> ids) {
        long count = ids.size();
        awardRepository.deleteAllById(ids);
        log.info("Deleted {} awards in batch", count);
    }

    @Override
    @Cacheable(value = "awards", key = "'doctor-' + #doctorId + '-year-range-' + #startYear + '-' + #endYear")
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
