package com.tintsteps.doctorsevice.service.impl;

import com.tintsteps.doctorsevice.exception.DoctorNotFoundException;
import com.tintsteps.doctorsevice.exception.EntityNotFoundException;
import com.tintsteps.doctorsevice.mapper.MembershipMapper;
import com.tintsteps.doctorsevice.model.MembershipRequestDto;
import com.tintsteps.doctorsevice.model.MembershipResponseDto;
import com.tintsteps.doctorsevice.repository.DoctorRepository;
import com.tintsteps.doctorsevice.repository.MembershipRepository;
import com.tintsteps.doctorsevice.service.MembershipService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MembershipServiceImpl implements MembershipService {

    private final MembershipRepository membershipRepository;
    private final DoctorRepository doctorRepository;
    private final MembershipMapper membershipMapper;

    public MembershipServiceImpl(MembershipRepository membershipRepository, DoctorRepository doctorRepository, MembershipMapper membershipMapper) {
        this.membershipRepository = membershipRepository;
        this.doctorRepository = doctorRepository;
        this.membershipMapper = membershipMapper;
    }

    @Override
    public MembershipResponseDto create(UUID doctorId, MembershipRequestDto requestDto) {
        var doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new DoctorNotFoundException("Doctor not found with ID: " + doctorId));
        var membership = membershipMapper.fromRequestDto(requestDto);
        membership.setDoctor(doctor);
        var savedMembership = membershipRepository.save(membership);
        return membershipMapper.toResponseDto(savedMembership);
    }

    @Override
    public MembershipResponseDto findById(UUID id) {
        return membershipRepository.findById(id)
                .map(membershipMapper::toResponseDto)
                .orElseThrow(() -> new EntityNotFoundException("Membership not found with ID: " + id));
    }

    @Override
    public Page<MembershipResponseDto> findAll(Pageable pageable) {
        return membershipRepository.findAll(pageable).map(membershipMapper::toResponseDto);
    }

    @Override
    public MembershipResponseDto update(UUID id, MembershipRequestDto requestDto) {
        var existingMembership = membershipRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Membership not found with ID: " + id));
        membershipMapper.updateEntityFromDto(requestDto, existingMembership);
        var updatedMembership = membershipRepository.save(existingMembership);
        return membershipMapper.toResponseDto(updatedMembership);
    }

    @Override
    public MembershipResponseDto partialUpdate(UUID id, MembershipRequestDto requestDto) {
        var existingMembership = membershipRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Membership not found with ID: " + id));
        membershipMapper.updateEntityFromDto(requestDto, existingMembership);
        var updatedMembership = membershipRepository.save(existingMembership);
        return membershipMapper.toResponseDto(updatedMembership);
    }

    @Override
    public void delete(UUID id) {
        if (!membershipRepository.existsById(id)) {
            throw new EntityNotFoundException("Membership not found with ID: " + id);
        }
        membershipRepository.deleteById(id);
    }

    @Override
    public List<MembershipResponseDto> findByDoctorId(UUID doctorId) {
        if (!doctorRepository.existsById(doctorId)) {
            throw new DoctorNotFoundException("Doctor not found with ID: " + doctorId);
        }
        return membershipRepository.findByDoctorId(doctorId).stream()
                .map(membershipMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public Page<MembershipResponseDto> findByDoctorId(UUID doctorId, Pageable pageable) {
        if (!doctorRepository.existsById(doctorId)) {
            throw new DoctorNotFoundException("Doctor not found with ID: " + doctorId);
        }
        return membershipRepository.findByDoctorId(doctorId, pageable).map(membershipMapper::toResponseDto);
    }

    @Override
    public Page<MembershipResponseDto> findByCouncilName(String councilName, Pageable pageable) {
        return membershipRepository.findByMembershipCouncilNameContainingIgnoreCase(councilName, pageable).map(membershipMapper::toResponseDto);
    }

    @Override
    public List<MembershipResponseDto> findByDoctorIdAndCouncilName(UUID doctorId, String councilName) {
        return membershipRepository.findByDoctorIdAndMembershipCouncilNameContainingIgnoreCase(doctorId, councilName).stream()
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
        return membershipRepository.existsByDoctorIdAndMembershipCouncilNameContainingIgnoreCase(doctorId, councilName);
    }

    @Override
    public long countByDoctorId(UUID doctorId) {
        return membershipRepository.countByDoctorId(doctorId);
    }

    @Override
    public long countByCouncilName(String councilName) {
        return membershipRepository.countByMembershipCouncilNameContainingIgnoreCase(councilName);
    }

    @Override
    public long countAll() {
        return membershipRepository.count();
    }

    @Override
    public List<MembershipResponseDto> createBatch(UUID doctorId, List<MembershipRequestDto> requestDtos) {
        var doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new DoctorNotFoundException("Doctor not found with ID: " + doctorId));
        var memberships = requestDtos.stream()
                .map(membershipMapper::fromRequestDto)
                .peek(membership -> membership.setDoctor(doctor))
                .collect(Collectors.toList());
        var savedMemberships = membershipRepository.saveAll(memberships);
        return savedMemberships.stream()
                .map(membershipMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteByDoctorId(UUID doctorId) {
        membershipRepository.deleteByDoctorId(doctorId);
    }

    @Override
    public void deleteBatch(List<UUID> ids) {
        membershipRepository.deleteAllById(ids);
    }

    @Override
    public List<String> getUniqueCouncilNames() {
        return membershipRepository.findDistinctMembershipCouncilNames();
    }

    @Override
    public boolean isDoctorMemberOfCouncil(UUID doctorId, String councilName) {
        return hasDoctorMembershipInCouncil(doctorId, councilName);
    }
}
