package com.tinysteps.doctorservice.service;

import com.tinysteps.doctorservice.model.PhotoRequestDto;
import com.tinysteps.doctorservice.model.PhotoResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for Photo operations
 */
public interface PhotoService {

    // CRUD Operations
    PhotoResponseDto create(UUID doctorId, PhotoRequestDto requestDto);
    PhotoResponseDto findById(UUID id);
    Page<PhotoResponseDto> findAll(Pageable pageable);
    PhotoResponseDto update(UUID id, PhotoRequestDto requestDto);
    PhotoResponseDto partialUpdate(UUID id, PhotoRequestDto requestDto);
    void delete(UUID id);

    // Doctor-specific Operations
    List<PhotoResponseDto> findByDoctorId(UUID doctorId);
    Page<PhotoResponseDto> findByDoctorId(UUID doctorId, Pageable pageable);

    // Default Photo Operations
    PhotoResponseDto findDefaultPhotoByDoctorId(UUID doctorId);
    List<PhotoResponseDto> findNonDefaultPhotosByDoctorId(UUID doctorId);
    PhotoResponseDto setAsDefaultPhoto(UUID id);
    PhotoResponseDto removeDefaultStatus(UUID id);

    // Search Operations
    PhotoResponseDto findByPhotoUrl(String photoUrl);
    Page<PhotoResponseDto> findByUrlPattern(String urlPattern, Pageable pageable);
    Page<PhotoResponseDto> findDefaultPhotos(Pageable pageable);
    Page<PhotoResponseDto> findNonDefaultPhotos(Pageable pageable);

    // Validation Operations
    boolean existsById(UUID id);
    boolean existsByDoctorId(UUID doctorId);
    boolean existsByPhotoUrl(String photoUrl);
    boolean hasDefaultPhoto(UUID doctorId);
    boolean isPhotoUrlUnique(String photoUrl);

    // Statistics Operations
    long countByDoctorId(UUID doctorId);
    long countDefaultPhotosByDoctorId(UUID doctorId);
    long countAll();
    Object[] getPhotoCountStatistics();

    // Bulk Operations
    List<PhotoResponseDto> createBatch(UUID doctorId, List<PhotoRequestDto> requestDtos);
    void deleteByDoctorId(UUID doctorId);
    void deleteBatch(List<UUID> ids);

    // Business Operations
    List<UUID> findDoctorsWithMultiplePhotos();
    List<UUID> findDoctorsWithoutPhotos();
    List<UUID> findDoctorsWithoutDefaultPhotos();
    PhotoResponseDto replaceDefaultPhoto(UUID doctorId, PhotoRequestDto newPhotoRequest);
    void ensureDefaultPhoto(UUID doctorId);
}
