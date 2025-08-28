package com.tinysteps.doctorsevice.repository;

import com.tinysteps.doctorsevice.entity.Photo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PhotoRepository extends JpaRepository<Photo, UUID> {

    // Find photos by doctor ID
    List<Photo> findByDoctorId(UUID doctorId);

    // Find default photo by doctor ID
    Optional<Photo> findByDoctorIdAndIsDefault(UUID doctorId, Boolean isDefault);

    // Find default photo by doctor ID (convenience method)
    Optional<Photo> findByDoctorIdAndIsDefaultTrue(UUID doctorId);

    // Find non-default photos by doctor ID
    List<Photo> findByDoctorIdAndIsDefaultFalse(UUID doctorId);

    // Find photos by photo URL
    Optional<Photo> findByPhotoUrl(String photoUrl);



    // Count photos by doctor
    long countByDoctorId(UUID doctorId);

    // Count default photos by doctor
    long countByDoctorIdAndIsDefault(UUID doctorId, Boolean isDefault);

    // Check if doctor has default photo
    @Query("SELECT COUNT(p) > 0 FROM Photo p WHERE p.doctor.id = :doctorId AND p.isDefault = true")
    boolean hasDefaultPhoto(@Param("doctorId") UUID doctorId);

    // Check if photo URL exists
    boolean existsByPhotoUrl(String photoUrl);

    // Check if doctor has photos
    boolean existsByDoctorId(UUID doctorId);

    // Find all default photos
    List<Photo> findByIsDefaultTrue();

    // Find all non-default photos
    List<Photo> findByIsDefaultFalse();

    // Find photos by URL pattern (case-insensitive)
    @Query("SELECT p FROM Photo p WHERE LOWER(p.photoUrl) LIKE LOWER(CONCAT('%', :urlPattern, '%'))")
    List<Photo> findByPhotoUrlContaining(@Param("urlPattern") String urlPattern);

    // Find doctors with multiple photos
    @Query("SELECT p.doctor.id FROM Photo p GROUP BY p.doctor.id HAVING COUNT(p) > 1")
    List<UUID> findDoctorsWithMultiplePhotos();

    // Find doctors without photos
    @Query("SELECT d.id FROM Doctor d WHERE d.id NOT IN (SELECT DISTINCT p.doctor.id FROM Photo p)")
    List<UUID> findDoctorsWithoutPhotos();

    // Find doctors without default photos
    @Query("SELECT d.id FROM Doctor d WHERE d.id NOT IN (SELECT DISTINCT p.doctor.id FROM Photo p WHERE p.isDefault = true)")
    List<UUID> findDoctorsWithoutDefaultPhotos();

    // Get photo count statistics
    @Query("SELECT MIN(photoCount), MAX(photoCount), AVG(photoCount) FROM (SELECT COUNT(p) as photoCount FROM Photo p GROUP BY p.doctor.id) as counts")
    Object[] getPhotoCountStatistics();

    // Pageable versions
    Page<Photo> findByDoctorId(UUID doctorId, Pageable pageable);
    Page<Photo> findByPhotoUrlContainingIgnoreCase(String photoUrl, Pageable pageable);
    Page<Photo> findByIsDefault(Boolean isDefault, Pageable pageable);

    // Additional missing methods
    boolean existsByDoctorIdAndIsDefault(UUID doctorId, Boolean isDefault);
    void deleteByDoctorId(UUID doctorId);
    long countByIsDefault(Boolean isDefault);

    @Query("SELECT d.id FROM Doctor d WHERE d.id NOT IN (SELECT DISTINCT p.doctor.id FROM Photo p WHERE p.isDefault = true)")
    List<UUID> findDoctorsWithoutDefaultPhoto();

    Optional<Photo> findFirstByDoctorId(UUID doctorId);

    boolean existsByDoctorIdAndPhotoUrlContainingIgnoreCase(UUID doctorId, String photoUrl);

    // Find photo by ID with doctor eagerly loaded
    @Query("SELECT p FROM Photo p JOIN FETCH p.doctor WHERE p.id = :id")
    java.util.Optional<Photo> findByIdWithDoctor(@Param("id") UUID id);
}
