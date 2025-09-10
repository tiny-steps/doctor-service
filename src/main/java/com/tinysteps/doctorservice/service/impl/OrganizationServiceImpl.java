package com.tinysteps.doctorservice.service.impl;

import com.tinysteps.doctorservice.exception.DoctorNotFoundException;
import com.tinysteps.doctorservice.exception.EntityNotFoundException;
import com.tinysteps.doctorservice.mapper.OrganizationMapper;
import com.tinysteps.doctorservice.model.OrganizationRequestDto;
import com.tinysteps.doctorservice.model.OrganizationResponseDto;
import com.tinysteps.doctorservice.repository.DoctorRepository;
import com.tinysteps.doctorservice.repository.OrganizationRepository;
import com.tinysteps.doctorservice.service.OrganizationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OrganizationServiceImpl implements OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final DoctorRepository doctorRepository;
    private final OrganizationMapper organizationMapper;

    public OrganizationServiceImpl(OrganizationRepository organizationRepository, DoctorRepository doctorRepository,
            OrganizationMapper organizationMapper) {
        this.organizationRepository = organizationRepository;
        this.doctorRepository = doctorRepository;
        this.organizationMapper = organizationMapper;
    }

    @Override
    @CacheEvict(value = { "doctors", "doctor", "organizations" }, allEntries = true)
    public OrganizationResponseDto create(UUID doctorId, OrganizationRequestDto requestDto) {
        var doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new DoctorNotFoundException("Doctor not found with ID: " + doctorId));
        var organization = organizationMapper.fromRequestDto(requestDto);
        organization.setDoctor(doctor);
        var savedOrganization = organizationRepository.save(organization);
        log.info("Created organization {} for doctor {}", savedOrganization.getId(), doctorId);
        return organizationMapper.toResponseDto(savedOrganization);
    }

    @Override
    @Cacheable(value = "organization", key = "#id")
    public OrganizationResponseDto findById(UUID id) {
        return organizationRepository.findById(id)
                .map(organizationMapper::toResponseDto)
                .orElseThrow(() -> new EntityNotFoundException("Organization not found with ID: " + id));
    }

    @Override
    @Cacheable(value = "organizations", key = "'all'")
    public Page<OrganizationResponseDto> findAll(Pageable pageable) {
        return organizationRepository.findAll(pageable).map(organizationMapper::toResponseDto);
    }

    @Override
    @CacheEvict(value = { "doctors", "doctor", "organizations" }, allEntries = true)
    public OrganizationResponseDto update(UUID id, OrganizationRequestDto requestDto) {
        var existingOrganization = organizationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Organization not found with ID: " + id));
        organizationMapper.updateEntityFromDto(requestDto, existingOrganization);
        var updatedOrganization = organizationRepository.save(existingOrganization);
        log.info("Updated organization {} for doctor {}", id, existingOrganization.getDoctor().getId());
        return organizationMapper.toResponseDto(updatedOrganization);
    }

    @Override
    @CacheEvict(value = { "doctors", "doctor", "organizations" }, allEntries = true)
    public OrganizationResponseDto partialUpdate(UUID id, OrganizationRequestDto requestDto) {
        var existingOrganization = organizationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Organization not found with ID: " + id));
        organizationMapper.updateEntityFromDto(requestDto, existingOrganization);
        var updatedOrganization = organizationRepository.save(existingOrganization);
        log.info("Partially updated organization {} for doctor {}", id, existingOrganization.getDoctor().getId());
        return organizationMapper.toResponseDto(updatedOrganization);
    }

    @Override
    @CacheEvict(value = { "doctors", "doctor", "organizations" }, allEntries = true)
    public void delete(UUID id) {
        var organization = organizationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Organization not found with ID: " + id));
        UUID doctorId = organization.getDoctor().getId();
        organizationRepository.deleteById(id);
        log.info("Deleted organization {} for doctor {}", id, doctorId);
    }

    @Override
    @Cacheable(value = "organizations", key = "'doctor-' + #doctorId")
    public List<OrganizationResponseDto> findByDoctorId(UUID doctorId) {
        if (!doctorRepository.existsById(doctorId)) {
            throw new DoctorNotFoundException("Doctor not found with ID: " + doctorId);
        }
        return organizationRepository.findByDoctorId(doctorId).stream()
                .map(organizationMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "organizations", key = "'doctor-' + #doctorId + '-ordered'")
    public List<OrganizationResponseDto> findByDoctorIdOrderByTenureStart(UUID doctorId) {
        if (!doctorRepository.existsById(doctorId)) {
            throw new DoctorNotFoundException("Doctor not found with ID: " + doctorId);
        }
        return organizationRepository.findByDoctorIdOrderByTenureStartDesc(doctorId).stream()
                .map(organizationMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "organizations", key = "'doctor-' + #doctorId + '-page-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<OrganizationResponseDto> findByDoctorId(UUID doctorId, Pageable pageable) {
        if (!doctorRepository.existsById(doctorId)) {
            throw new DoctorNotFoundException("Doctor not found with ID: " + doctorId);
        }
        return organizationRepository.findByDoctorId(doctorId, pageable).map(organizationMapper::toResponseDto);
    }

    @Override
    @Cacheable(value = "organizations", key = "'name-' + #organizationName")
    public Page<OrganizationResponseDto> findByOrganizationName(String organizationName, Pageable pageable) {
        return organizationRepository.findByOrganizationNameContainingIgnoreCase(organizationName, pageable)
                .map(organizationMapper::toResponseDto);
    }

    @Override
    @Cacheable(value = "organizations", key = "'role-' + #role")
    public Page<OrganizationResponseDto> findByRole(String role, Pageable pageable) {
        return organizationRepository.findByRoleContainingIgnoreCase(role, pageable)
                .map(organizationMapper::toResponseDto);
    }

    @Override
    @Cacheable(value = "organizations", key = "'city-' + #city")
    public Page<OrganizationResponseDto> findByCity(String city, Pageable pageable) {
        return organizationRepository.findByCityContainingIgnoreCase(city, pageable)
                .map(organizationMapper::toResponseDto);
    }

    @Override
    @Cacheable(value = "organizations", key = "'state-' + #state")
    public Page<OrganizationResponseDto> findByState(String state, Pageable pageable) {
        return organizationRepository.findByStateContainingIgnoreCase(state, pageable)
                .map(organizationMapper::toResponseDto);
    }

    @Override
    @Cacheable(value = "organizations", key = "'country-' + #country")
    public Page<OrganizationResponseDto> findByCountry(String country, Pageable pageable) {
        return organizationRepository.findByCountryContainingIgnoreCase(country, pageable)
                .map(organizationMapper::toResponseDto);
    }

    @Override
    @Cacheable(value = "organizations", key = "'current'")
    public List<OrganizationResponseDto> findCurrentOrganizations() {
        return organizationRepository.findCurrentOrganizations().stream()
                .map(organizationMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "organizations", key = "'doctor-' + #doctorId + '-current'")
    public List<OrganizationResponseDto> findCurrentOrganizationsByDoctorId(UUID doctorId) {
        return organizationRepository.findCurrentOrganizationsByDoctorId(doctorId).stream()
                .map(organizationMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "organizations", key = "'doctor-' + #doctorId + '-past'")
    public List<OrganizationResponseDto> findPastOrganizationsByDoctorId(UUID doctorId) {
        return organizationRepository.findPastOrganizationsByDoctorId(doctorId).stream()
                .map(organizationMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsById(UUID id) {
        return organizationRepository.existsById(id);
    }

    @Override
    public boolean existsByDoctorId(UUID doctorId) {
        return organizationRepository.existsByDoctorId(doctorId);
    }

    @Override
    @Cacheable(value = "organizations", key = "'count-doctor-' + #doctorId")
    public long countByDoctorId(UUID doctorId) {
        return organizationRepository.countByDoctorId(doctorId);
    }

    @Override
    @Cacheable(value = "organizations", key = "'count-name-' + #organizationName")
    public long countByOrganizationName(String organizationName) {
        return organizationRepository.countByOrganizationNameContainingIgnoreCase(organizationName);
    }

    @Override
    @Cacheable(value = "organizations", key = "'count-all'")
    public long countAll() {
        return organizationRepository.count();
    }

    @Override
    @CacheEvict(value = { "doctors", "doctor", "organizations" }, allEntries = true)
    public List<OrganizationResponseDto> createBatch(UUID doctorId, List<OrganizationRequestDto> requestDtos) {
        var doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new DoctorNotFoundException("Doctor not found with ID: " + doctorId));
        var organizations = requestDtos.stream()
                .map(organizationMapper::fromRequestDto)
                .peek(o -> o.setDoctor(doctor))
                .collect(Collectors.toList());
        var savedOrganizations = organizationRepository.saveAll(organizations);
        log.info("Created {} organizations for doctor {}", savedOrganizations.size(), doctorId);
        return savedOrganizations.stream()
                .map(organizationMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @CacheEvict(value = { "doctors", "doctor", "organizations" }, allEntries = true)
    public void deleteByDoctorId(UUID doctorId) {
        long count = organizationRepository.countByDoctorId(doctorId);
        organizationRepository.deleteByDoctorId(doctorId);
        log.info("Deleted {} organizations for doctor {}", count, doctorId);
    }

    @Override
    @CacheEvict(value = { "doctors", "doctor", "organizations" }, allEntries = true)
    public void deleteBatch(List<UUID> ids) {
        long count = ids.size();
        organizationRepository.deleteAllById(ids);
        log.info("Deleted {} organizations in batch", count);
    }

    @Override
    @Cacheable(value = "organizations", key = "'unique-names'")
    public List<String> getUniqueOrganizationNames() {
        return organizationRepository.findDistinctOrganizationNames();
    }

    @Override
    @Cacheable(value = "organizations", key = "'unique-roles'")
    public List<String> getUniqueRoles() {
        return organizationRepository.findDistinctRoles();
    }

    @Override
    @Cacheable(value = "organizations", key = "'location-' + #city + '-' + #state + '-' + #country")
    public Page<OrganizationResponseDto> findByLocation(String city, String state, String country, Pageable pageable) {
        return organizationRepository.findByCityAndStateAndCountry(city, state, country, pageable)
                .map(organizationMapper::toResponseDto);
    }

    @Override
    @Cacheable(value = "organizations", key = "'tenure-period-' + #startDate + '-' + #endDate")
    public Page<OrganizationResponseDto> findByTenurePeriod(Date startDate, Date endDate, Pageable pageable) {
        return organizationRepository.findByTenureStartBetween(startDate, endDate, pageable)
                .map(organizationMapper::toResponseDto);
    }

    @Override
    public boolean isDoctorCurrentlyEmployed(UUID doctorId) {
        return organizationRepository.existsByDoctorIdAndTenureEndIsNull(doctorId);
    }

    @Override
    @Cacheable(value = "organizations", key = "'count-current-doctor-' + #doctorId")
    public long countCurrentOrganizationsByDoctorId(UUID doctorId) {
        return organizationRepository.countCurrentOrganizationsByDoctorId(doctorId);
    }

    @Override
    public boolean isDoctorEmployedAtOrganization(UUID doctorId, String organizationName) {
        return organizationRepository.existsByDoctorIdAndOrganizationNameContainingIgnoreCase(doctorId,
                organizationName);
    }

    @Override
    public int calculateTotalExperienceYears(UUID doctorId) {
        var organizations = organizationRepository.findByDoctorId(doctorId);
        long totalMonths = 0;
        for (var org : organizations) {
            LocalDate start = org.getTenureStart().toLocalDate();
            LocalDate end = (org.getTenureEnd() == null) ? LocalDate.now() : org.getTenureEnd().toLocalDate();
            totalMonths += ChronoUnit.MONTHS.between(start, end);
        }
        return (int) (totalMonths / 12);
    }
}
