package com.tinysteps.doctorsevice.service.impl;

import com.tinysteps.doctorsevice.entity.DoctorAddress;
import com.tinysteps.doctorsevice.entity.DoctorAddressId;
import com.tinysteps.doctorsevice.mapper.DoctorAddressMapper;
import com.tinysteps.doctorsevice.model.DoctorAddressRequestDto;
import com.tinysteps.doctorsevice.model.DoctorAddressResponseDto;
import com.tinysteps.doctorsevice.repository.DoctorAddressRepository;
import com.tinysteps.doctorsevice.service.DoctorAddressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DoctorAddressServiceImpl implements DoctorAddressService {

    private final DoctorAddressRepository doctorAddressRepository;
    private final DoctorAddressMapper doctorAddressMapper;

    @Override
    @Transactional
    public DoctorAddressResponseDto addDoctorAddress(UUID doctorId, DoctorAddressRequestDto requestDto) {
        log.debug("Adding address {} to doctor {} with role {}", requestDto.addressId(), doctorId, requestDto.practiceRole());
        
        DoctorAddressId id = new DoctorAddressId(doctorId, requestDto.addressId(), DoctorAddress.PracticeRole.valueOf(requestDto.practiceRole()));
        
        if (doctorAddressRepository.existsById(id)) {
            throw new IllegalArgumentException("Doctor address relationship already exists");
        }
        
        DoctorAddress doctorAddress = new DoctorAddress();
        doctorAddress.setDoctorId(doctorId);
        doctorAddress.setAddressId(requestDto.addressId());
        doctorAddress.setPracticeRole(DoctorAddress.PracticeRole.valueOf(requestDto.practiceRole()));
        
        DoctorAddress saved = doctorAddressRepository.save(doctorAddress);
        return doctorAddressMapper.toResponseDto(saved);
    }

    @Override
    @Transactional
    public void removeDoctorAddress(UUID doctorId, UUID addressId, String practiceRole) {
        log.debug("Removing address {} from doctor {} with role {}", addressId, doctorId, practiceRole);
        
        DoctorAddressId id = new DoctorAddressId(doctorId, addressId, DoctorAddress.PracticeRole.valueOf(practiceRole));
        
        if (!doctorAddressRepository.existsById(id)) {
            throw new IllegalArgumentException("Doctor address relationship does not exist");
        }
        
        doctorAddressRepository.deleteById(id);
    }

    @Override
    public List<DoctorAddressResponseDto> findByDoctorId(UUID doctorId) {
        log.debug("Finding addresses for doctor {}", doctorId);
        List<DoctorAddress> addresses = doctorAddressRepository.findByDoctorId(doctorId);
        return doctorAddressMapper.toResponseDtos(addresses);
    }

    @Override
    public Page<DoctorAddressResponseDto> findByDoctorId(UUID doctorId, Pageable pageable) {
        log.debug("Finding addresses for doctor {} with pagination", doctorId);
        Page<DoctorAddress> addresses = doctorAddressRepository.findByDoctorId(doctorId, pageable);
        return addresses.map(doctorAddressMapper::toResponseDto);
    }

    @Override
    public List<DoctorAddressResponseDto> findByAddressId(UUID addressId) {
        log.debug("Finding doctors for address {}", addressId);
        List<DoctorAddress> doctors = doctorAddressRepository.findByAddressId(addressId);
        return doctorAddressMapper.toResponseDtos(doctors);
    }

    @Override
    public Page<DoctorAddressResponseDto> findByAddressId(UUID addressId, Pageable pageable) {
        log.debug("Finding doctors for address {} with pagination", addressId);
        Page<DoctorAddress> doctors = doctorAddressRepository.findByAddressId(addressId, pageable);
        return doctors.map(doctorAddressMapper::toResponseDto);
    }

    @Override
    public List<DoctorAddressResponseDto> findByPracticeRole(String practiceRole) {
        log.debug("Finding doctor-address relationships by practice role {}", practiceRole);
        List<DoctorAddress> relationships = doctorAddressRepository.findByPracticeRole(DoctorAddress.PracticeRole.valueOf(practiceRole));
        return doctorAddressMapper.toResponseDtos(relationships);
    }

    @Override
    public Page<DoctorAddressResponseDto> findByPracticeRole(String practiceRole, Pageable pageable) {
        log.debug("Finding doctor-address relationships by practice role {} with pagination", practiceRole);
        Page<DoctorAddress> relationships = doctorAddressRepository.findByPracticeRole(DoctorAddress.PracticeRole.valueOf(practiceRole), pageable);
        return relationships.map(doctorAddressMapper::toResponseDto);
    }

    @Override
    public List<DoctorAddressResponseDto> findByDoctorIdAndPracticeRole(UUID doctorId, String practiceRole) {
        log.debug("Finding addresses for doctor {} with practice role {}", doctorId, practiceRole);
        List<DoctorAddress> addresses = doctorAddressRepository.findByDoctorIdAndPracticeRole(doctorId, DoctorAddress.PracticeRole.valueOf(practiceRole));
        return doctorAddressMapper.toResponseDtos(addresses);
    }

    @Override
    public boolean existsDoctorAddress(UUID doctorId, UUID addressId, String practiceRole) {
        log.debug("Checking if doctor {} has address {} with role {}", doctorId, addressId, practiceRole);
        DoctorAddressId id = new DoctorAddressId(doctorId, addressId, DoctorAddress.PracticeRole.valueOf(practiceRole));
        return doctorAddressRepository.existsById(id);
    }

    @Override
    @Transactional
    public void removeAllDoctorAddresses(UUID doctorId) {
        log.debug("Removing all addresses for doctor {}", doctorId);
        doctorAddressRepository.deleteByDoctorId(doctorId);
    }

    @Override
    @Transactional
    public void removeAllAddressDoctors(UUID addressId) {
        log.debug("Removing all doctors for address {}", addressId);
        doctorAddressRepository.deleteByAddressId(addressId);
    }

    @Override
    public long countByDoctorId(UUID doctorId) {
        log.debug("Counting addresses for doctor {}", doctorId);
        return doctorAddressRepository.countByDoctorId(doctorId);
    }

    @Override
    public long countByAddressId(UUID addressId) {
        log.debug("Counting doctors for address {}", addressId);
        return doctorAddressRepository.countByAddressId(addressId);
    }

    @Override
    @Transactional
    public List<DoctorAddressResponseDto> addMultipleDoctorAddresses(UUID doctorId, List<DoctorAddressRequestDto> requestDtos) {
        log.debug("Adding {} addresses to doctor {}", requestDtos.size(), doctorId);
        
        List<DoctorAddress> doctorAddresses = requestDtos.stream()
                .map(dto -> {
                    DoctorAddressId id = new DoctorAddressId(doctorId, dto.addressId(), DoctorAddress.PracticeRole.valueOf(dto.practiceRole()));
                    
                    if (doctorAddressRepository.existsById(id)) {
                        throw new IllegalArgumentException("Doctor address relationship already exists for address " + dto.addressId() + " with role " + dto.practiceRole());
                    }
                    
                    DoctorAddress doctorAddress = new DoctorAddress();
                    doctorAddress.setDoctorId(doctorId);
                    doctorAddress.setAddressId(dto.addressId());
                    doctorAddress.setPracticeRole(DoctorAddress.PracticeRole.valueOf(dto.practiceRole()));
                    return doctorAddress;
                })
                .toList();
        
        List<DoctorAddress> saved = doctorAddressRepository.saveAll(doctorAddresses);
        return doctorAddressMapper.toResponseDtos(saved);
    }
}