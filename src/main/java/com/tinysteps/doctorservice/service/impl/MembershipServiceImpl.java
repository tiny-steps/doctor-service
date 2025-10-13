package com.tinysteps.doctorservice.service.impl;

import com.tinysteps.doctorservice.exception.DoctorNotFoundException;
import com.tinysteps.doctorservice.exception.EntityNotFoundException;
import com.tinysteps.doctorservice.mapper.MembershipMapper;
import com.tinysteps.doctorservice.model.MembershipRequestDto;
import com.tinysteps.doctorservice.model.MembershipResponseDto;
import com.tinysteps.doctorservice.repository.DoctorRepository;
import com.tinysteps.doctorservice.repository.MembershipRepository;
import com.tinysteps.doctorservice.service.MembershipService;
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
public class MembershipServiceImpl implements MembershipService {

    private final MembershipRepository membershipRepository;
    private final DoctorRepository doctorRepository;
    private final MembershipMapper membershipMapper;

    public MembershipServiceImpl(MembershipRepository membershipRepository, DoctorRepository doctorRepository,
            MembershipMapper membershipMapper) {
        this.membershipRepository = membershipRepository;
        this.doctorRepository = doctorRepository;
        this.membershipMapper = membershipMapper;
    }

    @Override
    @CacheEvict(value = { "doctors", "doctor", "memberships" }, allEntries = true)
    public MembershipResponseDto create(UUID doctorId, MembershipRequestDto requestDto) {
        var doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new DoctorNotFoundException("Doctor not found with ID: " + doctorId));
        var membership = membershipMapper.fromRequestDto(requestDto);
        membership.setDoctor(doctor);
        var savedMembership = membershipRepository.save(membership);
        log.info("Created membership {} for doctor {}", savedMembership.getId(), doctorId);
        return membershipMapper.toResponseDto(savedMembership);
    }

    @Override
    @Cacheable(value = "membership", key = "#id")
    public MembershipResponseDto findById(UUID id) {
        return membershipRepository.findById(id)
                .map(membershipMapper::toResponseDto)
                .orElseThrow(() -> new EntityNotFoundException("Membership not found with ID: " + id));
    }

    @Override
    @Cacheable(value = "memberships", key = "'all'")
    public Page<MembershipResponseDto> findAll(Pageable pageable) {
        return membershipRepository.findAll(pageable).map(membershipMapper::toResponseDto);
    }

    @Override
    @CacheEvict(value = { "doctors", "doctor", "memberships" }, allEntries = true)
    public MembershipResponseDto update(UUID id, MembershipRequestDto requestDto) {
        var existingMembership = membershipRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Membership not found with ID: " + id));

        // Handle doctor reassignment if doctorId is provided
        UUID originalDoctorId = existingMembership.getDoctor().getId();
        if (requestDto.doctorId() != null && !requestDto.doctorId().isEmpty()) {
            try {
                UUID newDoctorId = UUID.fromString(requestDto.doctorId());
                var newDoctor = doctorRepository.findById(newDoctorId)
                        .orElseThrow(() -> new DoctorNotFoundException(newDoctorId));
                existingMembership.setDoctor(newDoctor);
                log.info("Reassigned membership {} from doctor {} to doctor {}",
                        id, originalDoctorId, newDoctorId);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid doctor ID format: " + requestDto.doctorId());
            }
        }

        membershipMapper.updateEntityFromDto(requestDto, existingMembership);
        var updatedMembership = membershipRepository.save(existingMembership);
        log.info("Updated membership {} for doctor {}", id, existingMembership.getDoctor().getId());
        return membershipMapper.toResponseDto(updatedMembership);
    }

    @Override
    @CacheEvict(value = { "doctors", "doctor", "memberships" }, allEntries = true)
    public MembershipResponseDto partialUpdate(UUID id, MembershipRequestDto requestDto) {
        var existingMembership = membershipRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Membership not found with ID: " + id));

        // Handle doctor reassignment if doctorId is provided
        UUID originalDoctorId = existingMembership.getDoctor().getId();
        if (requestDto.doctorId() != null && !requestDto.doctorId().isEmpty()) {
            try {
                UUID newDoctorId = UUID.fromString(requestDto.doctorId());
                var newDoctor = doctorRepository.findById(newDoctorId)
                        .orElseThrow(() -> new DoctorNotFoundException(newDoctorId));
                existingMembership.setDoctor(newDoctor);
                log.info("Reassigned membership {} from doctor {} to doctor {}",
                        id, originalDoctorId, newDoctorId);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid doctor ID format: " + requestDto.doctorId());
            }
        }

        membershipMapper.updateEntityFromDto(requestDto, existingMembership);
        var updatedMembership = membershipRepository.save(existingMembership);
        log.info("Partially updated membership {} for doctor {}", id, existingMembership.getDoctor().getId());
        return membershipMapper.toResponseDto(updatedMembership);
    }

    @Override
    @CacheEvict(value = { "doctors", "doctor", "memberships" }, allEntries = true)
    public void delete(UUID id) {
        var membership = membershipRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Membership not found with ID: " + id));
        UUID doctorId = membership.getDoctor().getId();
        membershipRepository.deleteById(id);
        log.info("Deleted membership {} for doctor {}", id, doctorId);
    }

    @Override
    @Cacheable(value = "memberships", key = "'doctor-' + #doctorId")
    public List<MembershipResponseDto> findByDoctorId(UUID doctorId) {
        if (!doctorRepository.existsById(doctorId)) {
            throw new DoctorNotFoundException("Doctor not found with ID: " + doctorId);
        }
        return membershipRepository.findByDoctorId(doctorId).stream()
                .map(membershipMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "memberships", key = "'doctor-' + #doctorId + '-page-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<MembershipResponseDto> findByDoctorId(UUID doctorId, Pageable pageable) {
        if (!doctorRepository.existsById(doctorId)) {
            throw new DoctorNotFoundException("Doctor not found with ID: " + doctorId);
        }
        return membershipRepository.findByDoctorId(doctorId, pageable).map(membershipMapper::toResponseDto);
    }

    @Override
    @Cacheable(value = "memberships", key = "'council-' + #councilName")
    public Page<MembershipResponseDto> findByCouncilName(String councilName, Pageable pageable) {
        return membershipRepository.findByMembershipCouncilNameContainingIgnoreCase(councilName, pageable)
                .map(membershipMapper::toResponseDto);
    }

    @Override
    @Cacheable(value = "memberships", key = "'doctor-' + #doctorId + '-council-' + #councilName")
    public List<MembershipResponseDto> findByDoctorIdAndCouncilName(UUID doctorId, String councilName) {
        return membershipRepository.findByDoctorIdAndMembershipCouncilNameContainingIgnoreCase(doctorId, councilName)
                .stream()
                .map(membershipMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsById(UUID id) {
        return membershipRepository.existsById(id);
    }

    @Override
    public boolean existsByDoctorId(UUID doctorId) {
        return membershipRepository.existsByDoctorId(doctorId);
    }

    @Override
    public boolean hasDoctorMembershipInCouncil(UUID doctorId, String councilName) {
        return membershipRepository.existsByDoctorIdAndCouncilName(doctorId, councilName);
    }

    @Override
    @Cacheable(value = "memberships", key = "'count-doctor-' + #doctorId")
    public long countByDoctorId(UUID doctorId) {
        return membershipRepository.countByDoctorId(doctorId);
    }

    @Override
    @Cacheable(value = "memberships", key = "'count-council-' + #councilName")
    public long countByCouncilName(String councilName) {
        return membershipRepository.countByMembershipCouncilNameContainingIgnoreCase(councilName);
    }

    @Override
    @Cacheable(value = "memberships", key = "'count-all'")
    public long countAll() {
        return membershipRepository.count();
    }

    @Override
    @CacheEvict(value = { "doctors", "doctor", "memberships" }, allEntries = true)
    public List<MembershipResponseDto> createBatch(UUID doctorId, List<MembershipRequestDto> requestDtos) {
        var doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new DoctorNotFoundException("Doctor not found with ID: " + doctorId));
        var memberships = requestDtos.stream()
                .map(membershipMapper::fromRequestDto)
                .peek(membership -> membership.setDoctor(doctor))
                .collect(Collectors.toList());
        var savedMemberships = membershipRepository.saveAll(memberships);
        log.info("Created {} memberships for doctor {}", savedMemberships.size(), doctorId);
        return savedMemberships.stream()
                .map(membershipMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @CacheEvict(value = { "doctors", "doctor", "memberships" }, allEntries = true)
    public void deleteByDoctorId(UUID doctorId) {
        long count = membershipRepository.countByDoctorId(doctorId);
        membershipRepository.deleteByDoctorId(doctorId);
        log.info("Deleted {} memberships for doctor {}", count, doctorId);
    }

    @Override
    @CacheEvict(value = { "doctors", "doctor", "memberships" }, allEntries = true)
    public void deleteBatch(List<UUID> ids) {
        long count = ids.size();
        membershipRepository.deleteAllById(ids);
        log.info("Deleted {} memberships in batch", count);
    }

    @Override
    @Cacheable(value = "memberships", key = "'unique-councils'")
    public List<String> getUniqueCouncilNames() {
        return membershipRepository.findDistinctMembershipCouncilNames();
    }

    @Override
    public boolean isDoctorMemberOfCouncil(UUID doctorId, String councilName) {
        return hasDoctorMembershipInCouncil(doctorId, councilName);
    }
}
