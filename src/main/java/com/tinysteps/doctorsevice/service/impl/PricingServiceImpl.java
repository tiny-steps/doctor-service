package com.tinysteps.doctorsevice.service.impl;

import com.tinysteps.doctorsevice.exception.DoctorNotFoundException;
import com.tinysteps.doctorsevice.exception.EntityNotFoundException;
import com.tinysteps.doctorsevice.mapper.PricingMapper;
import com.tinysteps.doctorsevice.model.PricingRequestDto;
import com.tinysteps.doctorsevice.model.PricingResponseDto;
import com.tinysteps.doctorsevice.repository.DoctorRepository;
import com.tinysteps.doctorsevice.repository.PricingRepository;
import com.tinysteps.doctorsevice.service.PricingService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PricingServiceImpl implements PricingService {

    private final PricingRepository pricingRepository;
    private final DoctorRepository doctorRepository;
    private final PricingMapper pricingMapper;

    public PricingServiceImpl(PricingRepository pricingRepository, DoctorRepository doctorRepository, PricingMapper pricingMapper) {
        this.pricingRepository = pricingRepository;
        this.doctorRepository = doctorRepository;
        this.pricingMapper = pricingMapper;
    }

    @Override
    public PricingResponseDto create(UUID doctorId, PricingRequestDto requestDto) {
        var doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new DoctorNotFoundException("Doctor not found with ID: " + doctorId));
        var pricing = pricingMapper.fromRequestDto(requestDto);
        pricing.setDoctor(doctor);
        var savedPricing = pricingRepository.save(pricing);
        return pricingMapper.toResponseDto(savedPricing);
    }

    @Override
    public PricingResponseDto findById(UUID id) {
        return pricingRepository.findById(id)
                .map(pricingMapper::toResponseDto)
                .orElseThrow(() -> new EntityNotFoundException("Pricing not found with ID: " + id));
    }

    @Override
    public Page<PricingResponseDto> findAll(Pageable pageable) {
        return pricingRepository.findAll(pageable).map(pricingMapper::toResponseDto);
    }

    @Override
    public PricingResponseDto update(UUID id, PricingRequestDto requestDto) {
        var existingPricing = pricingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pricing not found with ID: " + id));
        pricingMapper.updateEntityFromDto(requestDto, existingPricing);
        var updatedPricing = pricingRepository.save(existingPricing);
        return pricingMapper.toResponseDto(updatedPricing);
    }

    @Override
    public PricingResponseDto partialUpdate(UUID id, PricingRequestDto requestDto) {
        var existingPricing = pricingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pricing not found with ID: " + id));
        pricingMapper.updateEntityFromDto(requestDto, existingPricing);
        var updatedPricing = pricingRepository.save(existingPricing);
        return pricingMapper.toResponseDto(updatedPricing);
    }

    @Override
    public void delete(UUID id) {
        if (!pricingRepository.existsById(id)) {
            throw new EntityNotFoundException("Pricing not found with ID: " + id);
        }
        pricingRepository.deleteById(id);
    }

    @Override
    public List<PricingResponseDto> findByDoctorId(UUID doctorId) {
        if (!doctorRepository.existsById(doctorId)) {
            throw new DoctorNotFoundException("Doctor not found with ID: " + doctorId);
        }
        return pricingRepository.findByDoctorId(doctorId).stream()
                .map(pricingMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<PricingResponseDto> findActivePricingByDoctorId(UUID doctorId) {
        if (!doctorRepository.existsById(doctorId)) {
            throw new DoctorNotFoundException("Doctor not found with ID: " + doctorId);
        }
        return pricingRepository.findByDoctorIdAndIsActive(doctorId, true).stream()
                .map(pricingMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public Page<PricingResponseDto> findByDoctorId(UUID doctorId, Pageable pageable) {
        if (!doctorRepository.existsById(doctorId)) {
            throw new DoctorNotFoundException("Doctor not found with ID: " + doctorId);
        }
        return pricingRepository.findByDoctorId(doctorId, pageable).map(pricingMapper::toResponseDto);
    }

    @Override
    public List<PricingResponseDto> findBySessionTypeId(UUID sessionTypeId) {
        return pricingRepository.findBySessionTypeId(sessionTypeId).stream()
                .map(pricingMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<PricingResponseDto> findActiveBySessionTypeId(UUID sessionTypeId) {
        return pricingRepository.findBySessionTypeIdAndIsActive(sessionTypeId, true).stream()
                .map(pricingMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public PricingResponseDto findByDoctorIdAndSessionTypeId(UUID doctorId, UUID sessionTypeId) {
        return pricingRepository.findByDoctorIdAndSessionTypeId(doctorId, sessionTypeId)
                .map(pricingMapper::toResponseDto)
                .orElseThrow(() -> new EntityNotFoundException("Pricing not found for doctor " + doctorId + " and session type " + sessionTypeId));
    }

    @Override
    public PricingResponseDto findActiveByDoctorIdAndSessionTypeId(UUID doctorId, UUID sessionTypeId) {
        return pricingRepository.findByDoctorIdAndSessionTypeIdAndIsActive(doctorId, sessionTypeId, true)
                .map(pricingMapper::toResponseDto)
                .orElseThrow(() -> new EntityNotFoundException("Active pricing not found for doctor " + doctorId + " and session type " + sessionTypeId));
    }

    @Override
    public Page<PricingResponseDto> findByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        return pricingRepository.findByCustomPriceBetween(minPrice, maxPrice, pageable).map(pricingMapper::toResponseDto);
    }

    @Override
    public Page<PricingResponseDto> findActivePricingByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        return pricingRepository.findByCustomPriceBetweenAndIsActive(minPrice, maxPrice, true, pageable).map(pricingMapper::toResponseDto);
    }

    @Override
    public Page<PricingResponseDto> findByMinPrice(BigDecimal minPrice, Pageable pageable) {
        return pricingRepository.findByCustomPriceGreaterThanEqual(minPrice, pageable).map(pricingMapper::toResponseDto);
    }

    @Override
    public Page<PricingResponseDto> findByMaxPrice(BigDecimal maxPrice, Pageable pageable) {
        return pricingRepository.findByCustomPriceLessThanEqual(maxPrice, pageable).map(pricingMapper::toResponseDto);
    }

    @Override
    public Page<PricingResponseDto> findActivePricing(Pageable pageable) {
        return pricingRepository.findByIsActive(true, pageable).map(pricingMapper::toResponseDto);
    }

    @Override
    public Page<PricingResponseDto> findInactivePricing(Pageable pageable) {
        return pricingRepository.findByIsActive(false, pageable).map(pricingMapper::toResponseDto);
    }

    @Override
    public boolean existsById(UUID id) {
        return pricingRepository.existsById(id);
    }

    @Override
    public boolean existsByDoctorId(UUID doctorId) {
        return pricingRepository.existsByDoctorId(doctorId);
    }

    @Override
    public boolean hasDoctorPricingForSessionType(UUID doctorId, UUID sessionTypeId) {
        return pricingRepository.existsByDoctorIdAndSessionTypeId(doctorId, sessionTypeId);
    }

    @Override
    public boolean hasActivePricingForSessionType(UUID doctorId, UUID sessionTypeId) {
        return pricingRepository.existsByDoctorIdAndSessionTypeIdAndIsActive(doctorId, sessionTypeId, true);
    }

    @Override
    public long countByDoctorId(UUID doctorId) {
        return pricingRepository.countByDoctorId(doctorId);
    }

    @Override
    public long countActivePricingByDoctorId(UUID doctorId) {
        return pricingRepository.countByDoctorIdAndIsActive(doctorId, true);
    }

    @Override
    public long countBySessionTypeId(UUID sessionTypeId) {
        return pricingRepository.countBySessionTypeId(sessionTypeId);
    }

    @Override
    public BigDecimal findAveragePriceForSessionType(UUID sessionTypeId) {
        return pricingRepository.findAveragePriceForSessionType(sessionTypeId);
    }

    @Override
    public BigDecimal findMinPriceForSessionType(UUID sessionTypeId) {
        return pricingRepository.findMinPriceForSessionType(sessionTypeId);
    }

    @Override
    public BigDecimal findMaxPriceForSessionType(UUID sessionTypeId) {
        return pricingRepository.findMaxPriceForSessionType(sessionTypeId);
    }

    @Override
    public long countAll() {
        return pricingRepository.count();
    }

    @Override
    public List<PricingResponseDto> createBatch(UUID doctorId, List<PricingRequestDto> requestDtos) {
        var doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new DoctorNotFoundException("Doctor not found with ID: " + doctorId));
        var pricings = requestDtos.stream()
                .map(pricingMapper::fromRequestDto)
                .peek(pricing -> pricing.setDoctor(doctor))
                .collect(Collectors.toList());
        var savedPricings = pricingRepository.saveAll(pricings);
        return savedPricings.stream()
                .map(pricingMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteByDoctorId(UUID doctorId) {
        pricingRepository.deleteByDoctorId(doctorId);
    }

    @Override
    public void deleteBatch(List<UUID> ids) {
        pricingRepository.deleteAllById(ids);
    }

    @Override
    public List<UUID> getActiveSessionTypeIds() {
        return pricingRepository.findDistinctSessionTypeIdsByIsActive(true);
    }

    @Override
    public List<PricingResponseDto> findCheapestPricingForSessionType(UUID sessionTypeId) {
        // This would require a more complex query.
        return List.of();
    }

    @Override
    public List<PricingResponseDto> findMostExpensivePricingForSessionType(UUID sessionTypeId) {
        // This would require a more complex query.
        return List.of();
    }

    @Override
    public PricingResponseDto activatePricing(UUID id) {
        var pricing = pricingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pricing not found with ID: " + id));
        pricing.setIsActive(true);
        var savedPricing = pricingRepository.save(pricing);
        return pricingMapper.toResponseDto(savedPricing);
    }

    @Override
    public PricingResponseDto deactivatePricing(UUID id) {
        var pricing = pricingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pricing not found with ID: " + id));
        pricing.setIsActive(false);
        var savedPricing = pricingRepository.save(pricing);
        return pricingMapper.toResponseDto(savedPricing);
    }

    @Override
    public Object[] getPricingStatsByDoctorId(UUID doctorId) {
        return new Object[]{
                pricingRepository.countByDoctorId(doctorId),
                pricingRepository.countByDoctorIdAndIsActive(doctorId, true)
        };
    }
}
