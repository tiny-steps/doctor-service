package com.tinysteps.doctorsevice.service.impl;

import com.tinysteps.doctorsevice.exception.DoctorNotFoundException;
import com.tinysteps.doctorsevice.exception.EntityNotFoundException;
import com.tinysteps.doctorsevice.mapper.PhotoMapper;
import com.tinysteps.doctorsevice.model.PhotoRequestDto;
import com.tinysteps.doctorsevice.model.PhotoResponseDto;
import com.tinysteps.doctorsevice.repository.DoctorRepository;
import com.tinysteps.doctorsevice.repository.PhotoRepository;
import com.tinysteps.doctorsevice.service.PhotoService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PhotoServiceImpl implements PhotoService {

    private final PhotoRepository photoRepository;
    private final DoctorRepository doctorRepository;
    private final PhotoMapper photoMapper;

    public PhotoServiceImpl(PhotoRepository photoRepository, DoctorRepository doctorRepository, PhotoMapper photoMapper) {
        this.photoRepository = photoRepository;
        this.doctorRepository = doctorRepository;
        this.photoMapper = photoMapper;
    }

    @Override
    @Transactional
    public PhotoResponseDto create(UUID doctorId, PhotoRequestDto requestDto) {
        var doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new DoctorNotFoundException("Doctor not found with ID: " + doctorId));
        var photo = photoMapper.fromRequestDto(requestDto);
        photo.setDoctor(doctor);

        if (Boolean.TRUE.equals(requestDto.isDefault())) {
            photoRepository.findByDoctorIdAndIsDefault(doctorId, true).stream().findFirst().ifPresent(p -> {
                p.setIsDefault(false);
                photoRepository.save(p);
            });
        }

        var savedPhoto = photoRepository.save(photo);
        return photoMapper.toResponseDto(savedPhoto);
    }

    @Override
    public PhotoResponseDto findById(UUID id) {
        return photoRepository.findById(id)
                .map(photoMapper::toResponseDto)
                .orElseThrow(() -> new EntityNotFoundException("Photo not found with ID: " + id));
    }

    @Override
    public Page<PhotoResponseDto> findAll(Pageable pageable) {
        return photoRepository.findAll(pageable).map(photoMapper::toResponseDto);
    }

    @Override
    @Transactional
    public PhotoResponseDto update(UUID id, PhotoRequestDto requestDto) {
        var existingPhoto = photoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Photo not found with ID: " + id));

        if (Boolean.TRUE.equals(requestDto.isDefault()) && !Boolean.TRUE.equals(existingPhoto.getIsDefault())) {
            photoRepository.findByDoctorIdAndIsDefault(existingPhoto.getDoctor().getId(), true).stream().findFirst().ifPresent(p -> {
                p.setIsDefault(false);
                photoRepository.save(p);
            });
        }

        photoMapper.updateEntityFromDto(requestDto, existingPhoto);
        var updatedPhoto = photoRepository.save(existingPhoto);
        return photoMapper.toResponseDto(updatedPhoto);
    }

    @Override
    public PhotoResponseDto partialUpdate(UUID id, PhotoRequestDto requestDto) {
        return update(id, requestDto);
    }

    @Override
    public void delete(UUID id) {
        if (!photoRepository.existsById(id)) {
            throw new EntityNotFoundException("Photo not found with ID: " + id);
        }
        photoRepository.deleteById(id);
    }

    @Override
    public List<PhotoResponseDto> findByDoctorId(UUID doctorId) {
        if (!doctorRepository.existsById(doctorId)) {
            throw new DoctorNotFoundException("Doctor not found with ID: " + doctorId);
        }
        return photoRepository.findByDoctorId(doctorId).stream()
                .map(photoMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public Page<PhotoResponseDto> findByDoctorId(UUID doctorId, Pageable pageable) {
        if (!doctorRepository.existsById(doctorId)) {
            throw new DoctorNotFoundException("Doctor not found with ID: " + doctorId);
        }
        return photoRepository.findByDoctorId(doctorId, pageable).map(photoMapper::toResponseDto);
    }

    @Override
    public PhotoResponseDto findDefaultPhotoByDoctorId(UUID doctorId) {
        if (!doctorRepository.existsById(doctorId)) {
            throw new DoctorNotFoundException("Doctor not found with ID: " + doctorId);
        }
        return photoRepository.findByDoctorIdAndIsDefault(doctorId, true).stream().findFirst()
                .map(photoMapper::toResponseDto)
                .orElseThrow(() -> new EntityNotFoundException("Default photo not found for doctor with ID: " + doctorId));
    }

    @Override
    public List<PhotoResponseDto> findNonDefaultPhotosByDoctorId(UUID doctorId) {
        if (!doctorRepository.existsById(doctorId)) {
            throw new DoctorNotFoundException("Doctor not found with ID: " + doctorId);
        }
        return photoRepository.findByDoctorIdAndIsDefault(doctorId, false).stream()
                .map(photoMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PhotoResponseDto setAsDefaultPhoto(UUID id) {
        var photoToSetAsDefault = photoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Photo not found with ID: " + id));

        UUID doctorId = photoToSetAsDefault.getDoctor().getId();

        photoRepository.findByDoctorIdAndIsDefault(doctorId, true).stream().findFirst().ifPresent(currentDefault -> {
            if (!currentDefault.getId().equals(id)) {
                currentDefault.setIsDefault(false);
                photoRepository.save(currentDefault);
            }
        });

        photoToSetAsDefault.setIsDefault(true);
        var updatedPhoto = photoRepository.save(photoToSetAsDefault);
        return photoMapper.toResponseDto(updatedPhoto);
    }

    @Override
    public PhotoResponseDto removeDefaultStatus(UUID id) {
        var photo = photoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Photo not found with ID: " + id));
        photo.setIsDefault(false);
        var updatedPhoto = photoRepository.save(photo);
        return photoMapper.toResponseDto(updatedPhoto);
    }

    @Override
    public PhotoResponseDto findByPhotoUrl(String photoUrl) {
        return photoRepository.findByPhotoUrl(photoUrl)
                .map(photoMapper::toResponseDto)
                .orElseThrow(() -> new EntityNotFoundException("Photo not found with URL: " + photoUrl));
    }

    @Override
    public Page<PhotoResponseDto> findByUrlPattern(String urlPattern, Pageable pageable) {
        return photoRepository.findByPhotoUrlContainingIgnoreCase(urlPattern, pageable).map(photoMapper::toResponseDto);
    }

    @Override
    public Page<PhotoResponseDto> findDefaultPhotos(Pageable pageable) {
        return photoRepository.findByIsDefault(true, pageable).map(photoMapper::toResponseDto);
    }

    @Override
    public Page<PhotoResponseDto> findNonDefaultPhotos(Pageable pageable) {
        return photoRepository.findByIsDefault(false, pageable).map(photoMapper::toResponseDto);
    }

    @Override
    public boolean existsById(UUID id) {
        return photoRepository.existsById(id);
    }

    @Override
    public boolean existsByDoctorId(UUID doctorId) {
        return photoRepository.existsByDoctorId(doctorId);
    }

    @Override
    public boolean existsByPhotoUrl(String photoUrl) {
        return photoRepository.existsByPhotoUrl(photoUrl);
    }

    @Override
    public boolean hasDefaultPhoto(UUID doctorId) {
        return photoRepository.existsByDoctorIdAndIsDefault(doctorId, true);
    }

    @Override
    public boolean isPhotoUrlUnique(String photoUrl) {
        return !photoRepository.existsByPhotoUrl(photoUrl);
    }

    @Override
    public long countByDoctorId(UUID doctorId) {
        if (!doctorRepository.existsById(doctorId)) {
            throw new DoctorNotFoundException("Doctor not found with ID: " + doctorId);
        }
        return photoRepository.countByDoctorId(doctorId);
    }

    @Override
    public long countDefaultPhotosByDoctorId(UUID doctorId) {
         if (!doctorRepository.existsById(doctorId)) {
            throw new DoctorNotFoundException("Doctor not found with ID: " + doctorId);
        }
        return photoRepository.countByDoctorIdAndIsDefault(doctorId, true);
    }

    @Override
    public long countAll() {
        return photoRepository.count();
    }

    @Override
    public Object[] getPhotoCountStatistics() {
        return new Object[]{photoRepository.count(), photoRepository.countByIsDefault(true)};
    }

    @Override
    public List<PhotoResponseDto> createBatch(UUID doctorId, List<PhotoRequestDto> requestDtos) {
        var doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new DoctorNotFoundException("Doctor not found with ID: " + doctorId));
        var photos = requestDtos.stream()
                .map(photoMapper::fromRequestDto)
                .peek(photo -> photo.setDoctor(doctor))
                .collect(Collectors.toList());
        var savedPhotos = photoRepository.saveAll(photos);
        return savedPhotos.stream()
                .map(photoMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteByDoctorId(UUID doctorId) {
        if (!doctorRepository.existsById(doctorId)) {
            throw new DoctorNotFoundException("Doctor not found with ID: " + doctorId);
        }
        photoRepository.deleteByDoctorId(doctorId);
    }

    @Override
    public void deleteBatch(List<UUID> ids) {
        photoRepository.deleteAllById(ids);
    }

    @Override
    public List<UUID> findDoctorsWithMultiplePhotos() {
        return photoRepository.findDoctorsWithMultiplePhotos();
    }

    @Override
    public List<UUID> findDoctorsWithoutPhotos() {
        return photoRepository.findDoctorsWithoutPhotos();
    }

    @Override
    public List<UUID> findDoctorsWithoutDefaultPhotos() {
        return photoRepository.findDoctorsWithoutDefaultPhoto();
    }

    @Override
    @Transactional
    public PhotoResponseDto replaceDefaultPhoto(UUID doctorId, PhotoRequestDto newPhotoRequest) {
        photoRepository.findByDoctorIdAndIsDefault(doctorId, true).stream().findFirst().ifPresent(photoRepository::delete);
        return create(doctorId, newPhotoRequest);
    }

    @Override
    public void ensureDefaultPhoto(UUID doctorId) {
        if (!hasDefaultPhoto(doctorId)) {
            photoRepository.findFirstByDoctorId(doctorId).ifPresent(photo -> {
                photo.setIsDefault(true);
                photoRepository.save(photo);
            });
        }
    }
}
