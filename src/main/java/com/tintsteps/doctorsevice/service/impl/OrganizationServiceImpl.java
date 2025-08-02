package com.tintsteps.doctorsevice.service.impl;

import com.tintsteps.doctorsevice.exception.DoctorNotFoundException;
import com.tintsteps.doctorsevice.exception.EntityNotFoundException;
import com.tintsteps.doctorsevice.mapper.OrganizationMapper;
import com.tintsteps.doctorsevice.model.OrganizationRequestDto;
import com.tintsteps.doctorsevice.model.OrganizationResponseDto;
import com.tintsteps.doctorsevice.repository.DoctorRepository;
import com.tintsteps.doctorsevice.repository.OrganizationRepository;
import com.tintsteps.doctorsevice.service.OrganizationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrganizationServiceImpl implements OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final DoctorRepository doctorRepository;
    private final OrganizationMapper organizationMapper;

    public OrganizationServiceImpl(OrganizationRepository organizationRepository, DoctorRepository doctorRepository, OrganizationMapper organizationMapper) {
        this.organizationRepository = organizationRepository;
        this.doctorRepository = doctorRepository;
        this.organizationMapper = organizationMapper;
    }

    @Override
    public OrganizationResponseDto create(UUID doctorId, OrganizationRequestDto requestDto) {
        var doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new DoctorNotFoundException("Doctor not found with ID: " + doctorId));
        var organization = organizationMapper.fromRequestDto(requestDto);
        organization.setDoctor(doctor);
        var savedOrganization = organizationRepository.save(organization);
        return organizationMapper.toResponseDto(savedOrganization);
    }

    @Override
    public OrganizationResponseDto findById(UUID id) {
        return organizationRepository.findById(id)
                .map(organizationMapper::toResponseDto)
                .orElseThrow(() -> new EntityNotFoundException("Organization not found with ID: " + id));
    }

    @Override
    public Page<OrganizationResponseDto> findAll(Pageable pageable) {
        return organizationRepository.findAll(pageable).map(organizationMapper::toResponseDto);
    }

    @Override
    public OrganizationResponseDto update(UUID id, OrganizationRequestDto requestDto) {
        var existingOrganization = organizationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Organization not found with ID: " + id));
        organizationMapper.updateEntityFromDto(requestDto, existingOrganization);
        var updatedOrganization = organizationRepository.save(existingOrganization);
        return organizationMapper.toResponseDto(updatedOrganization);
    }

    @Override
    public OrganizationResponseDto partialUpdate(UUID id, OrganizationRequestDto requestDto) {
        var existingOrganization = organizationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Organization not found with ID: " + id));
        organizationMapper.updateEntityFromDto(requestDto, existingOrganization);
        var updatedOrganization = organizationRepository.save(existingOrganization);
        return organizationMapper.toResponseDto(updatedOrganization);
    }

    @Override
    public void delete(UUID id) {
        if (!organizationRepository.existsById(id)) {
            throw new EntityNotFoundException("Organization not found with ID: " + id);
        }
        organizationRepository.deleteById(id);
    }

    @Override
    public List<OrganizationResponseDto> findByDoctorId(UUID doctorId) {
        if (!doctorRepository.existsById(doctorId)) {
            throw new DoctorNotFoundException("Doctor not found with ID: " + doctorId);
        }
        return organizationRepository.findByDoctorId(doctorId).stream()
                .map(organizationMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrganizationResponseDto> findByDoctorIdOrderByTenureStart(UUID doctorId) {
        if (!doctorRepository.existsById(doctorId)) {
            throw new DoctorNotFoundException("Doctor not found with ID: " + doctorId);
        }
        return organizationRepository.findByDoctorIdOrderByTenureStartDesc(doctorId).stream()
                .map(organizationMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public Page<OrganizationResponseDto> findByDoctorId(UUID doctorId, Pageable pageable) {
        if (!doctorRepository.existsById(doctorId)) {
            throw new DoctorNotFoundException("Doctor not found with ID: " + doctorId);
        }
        return organizationRepository.findByDoctorId(doctorId, pageable).map(organizationMapper::toResponseDto);
    }

    @Override
    public Page<OrganizationResponseDto> findByOrganizationName(String organizationName, Pageable pageable) {
        return organizationRepository.findByOrganizationNameContainingIgnoreCase(organizationName, pageable).map(organizationMapper::toResponseDto);
    }

    @Override
    public Page<OrganizationResponseDto> findByRole(String role, Pageable pageable) {
        return organizationRepository.findByRoleContainingIgnoreCase(role, pageable).map(organizationMapper::toResponseDto);
    }

    @Override
    public Page<OrganizationResponseDto> findByCity(String city, Pageable pageable) {
        return organizationRepository.findByCityContainingIgnoreCase(city, pageable).map(organizationMapper::toResponseDto);
    }

    @Override
    public Page<OrganizationResponseDto> findByState(String state, Pageable pageable) {
        return organizationRepository.findByStateContainingIgnoreCase(state, pageable).map(organizationMapper::toResponseDto);
    }

    @Override
    public Page<OrganizationResponseDto> findByCountry(String country, Pageable pageable) {
        return organizationRepository.findByCountryContainingIgnoreCase(country, pageable).map(organizationMapper::toResponseDto);
    }

    @Override
    public Page<OrganizationResponseDto> findByLocation(String city, String state, String country, Pageable pageable) {
        return organizationRepository.findByCityAndStateAndCountry(city, state, country, pageable).map(organizationMapper::toResponseDto);
    }

    @Override
    public List<OrganizationResponseDto> findCurrentOrganizations() {
        return organizationRepository.findByTenureEndIsNull().stream()
                .map(organizationMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrganizationResponseDto> findCurrentOrganizationsByDoctorId(UUID doctorId) {
        return organizationRepository.findByDoctorIdAndTenureEndIsNull(doctorId).stream()
                .map(organizationMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrganizationResponseDto> findPastOrganizationsByDoctorId(UUID doctorId) {
        return organizationRepository.findByDoctorIdAndTenureEndIsNotNull(doctorId).stream()
                .map(organizationMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public Page<OrganizationResponseDto> findByTenurePeriod(Date startDate, Date endDate, Pageable pageable) {
        return organizationRepository.findByTenureStartBetween(startDate, endDate, pageable).map(organizationMapper::toResponseDto);
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
    public boolean isDoctorCurrentlyEmployed(UUID doctorId) {
        return organizationRepository.existsByDoctorIdAndTenureEndIsNull(doctorId);
    }

    @Override
    public long countByDoctorId(UUID doctorId) {
        return organizationRepository.countByDoctorId(doctorId);
    }

    @Override
    public long countCurrentOrganizationsByDoctorId(UUID doctorId) {
        return organizationRepository.countByDoctorIdAndTenureEndIsNull(doctorId);
    }

    @Override
    public long countByOrganizationName(String organizationName) {
        return organizationRepository.countByOrganizationNameContainingIgnoreCase(organizationName);
    }

    @Override
    public long countAll() {
        return organizationRepository.count();
    }

    @Override
    public List<OrganizationResponseDto> createBatch(UUID doctorId, List<OrganizationRequestDto> requestDtos) {
        var doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new DoctorNotFoundException("Doctor not found with ID: " + doctorId));
        var organizations = requestDtos.stream()
                .map(organizationMapper::fromRequestDto)
                .peek(org -> org.setDoctor(doctor))
                .collect(Collectors.toList());
        var savedOrganizations = organizationRepository.saveAll(organizations);
        return savedOrganizations.stream()
                .map(organizationMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteByDoctorId(UUID doctorId) {
        organizationRepository.deleteByDoctorId(doctorId);
    }

    @Override
    public void deleteBatch(List<UUID> ids) {
        organizationRepository.deleteAllById(ids);
    }

    @Override
    public List<String> getUniqueOrganizationNames() {
        return organizationRepository.findDistinctOrganizationNames();
    }

    @Override
    public List<String> getUniqueRoles() {
        return organizationRepository.findDistinctRoles();
    }

    @Override
    public boolean isDoctorEmployedAtOrganization(UUID doctorId, String organizationName) {
        return organizationRepository.existsByDoctorIdAndOrganizationNameContainingIgnoreCase(doctorId, organizationName);
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
