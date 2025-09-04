package com.tinysteps.doctorsevice.service.impl;

import com.tinysteps.doctorsevice.entity.Doctor;
import com.tinysteps.doctorsevice.exception.DoctorNotFoundException;
import com.tinysteps.doctorsevice.integration.model.UserModel;
import com.tinysteps.doctorsevice.mapper.*;
import com.tinysteps.doctorsevice.model.*;
import com.tinysteps.doctorsevice.repository.*;
import com.tinysteps.doctorsevice.service.DoctorService;
import com.tinysteps.doctorsevice.dto.UserRegistrationRequest;
import com.tinysteps.doctorsevice.integration.service.AuthServiceIntegration;
import com.tinysteps.doctorsevice.integration.service.UserIntegrationService;
import com.tinysteps.doctorsevice.integration.model.UserUpdateRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DoctorServiceImpl implements DoctorService {

    private final DoctorRepository doctorRepository;
    private final DoctorMapper doctorMapper;
    private final SpecializationMapper specializationMapper;
    private final SpecializationRepository specializationRepository;
    private final AwardMapper awardMapper;
    private final AwardRepository awardRepository;
    private final QualificationMapper qualificationMapper;
    private final QualificationRepository qualificationRepository;
    private final MembershipMapper membershipMapper;
    private final MembershipRepository membershipRepository;
    private final OrganizationMapper organizationMapper;
    private final OrganizationRepository organizationRepository;
    private final RegistrationMapper registrationMapper;
    private final RegistrationRepository registrationRepository;
    private final PricingMapper pricingMapper;
    private final PricingRepository pricingRepository;
    private final PhotoMapper photoMapper;
    private final PhotoRepository photoRepository;

    private final RecommendationMapper recommendationMapper;
    private final RecommendationRepository recommendationRepository;
    private final AuthServiceIntegration authServiceIntegration;
    private final UserIntegrationService userIntegrationService;

    public DoctorServiceImpl(DoctorRepository doctorRepository, DoctorMapper doctorMapper,
            SpecializationMapper specializationMapper, SpecializationRepository specializationRepository,
            AwardMapper awardMapper, AwardRepository awardRepository,
            QualificationMapper qualificationMapper, QualificationRepository qualificationRepository,
            MembershipMapper membershipMapper, MembershipRepository membershipRepository,
            OrganizationMapper organizationMapper, OrganizationRepository organizationRepository,
            RegistrationMapper registrationMapper, RegistrationRepository registrationRepository,
            PricingMapper pricingMapper, PricingRepository pricingRepository,
            PhotoMapper photoMapper, PhotoRepository photoRepository,

            RecommendationMapper recommendationMapper, RecommendationRepository recommendationRepository,
            AuthServiceIntegration authServiceIntegration, UserIntegrationService userIntegrationService) {
        this.doctorRepository = doctorRepository;
        this.doctorMapper = doctorMapper;
        this.specializationMapper = specializationMapper;
        this.specializationRepository = specializationRepository;
        this.awardMapper = awardMapper;
        this.awardRepository = awardRepository;
        this.qualificationMapper = qualificationMapper;
        this.qualificationRepository = qualificationRepository;
        this.membershipMapper = membershipMapper;
        this.membershipRepository = membershipRepository;
        this.organizationMapper = organizationMapper;
        this.organizationRepository = organizationRepository;
        this.registrationMapper = registrationMapper;
        this.registrationRepository = registrationRepository;
        this.pricingMapper = pricingMapper;
        this.pricingRepository = pricingRepository;
        this.photoMapper = photoMapper;
        this.photoRepository = photoRepository;

        this.recommendationMapper = recommendationMapper;
        this.recommendationRepository = recommendationRepository;
        this.authServiceIntegration = authServiceIntegration;
        this.userIntegrationService = userIntegrationService;
    }

    /**
     * Manually creates DoctorResponseDto from Doctor entity, handling null values
     * appropriately
     * and properly populating related entities to avoid empty lists
     */
    private DoctorResponseDto createDoctorResponseDto(Doctor doctor) {
        // Fetch all related entities for this doctor
        List<SpecializationResponseDto> specializations = specializationRepository.findByDoctorId(doctor.getId())
                .stream()
                .map(specializationMapper::toResponseDto)
                .collect(Collectors.toList());

        List<AwardResponseDto> awards = awardRepository.findByDoctorId(doctor.getId())
                .stream()
                .map(awardMapper::toResponseDto)
                .collect(Collectors.toList());

        List<QualificationResponseDto> qualifications = qualificationRepository.findByDoctorId(doctor.getId())
                .stream()
                .map(qualificationMapper::toResponseDto)
                .collect(Collectors.toList());

        List<MembershipResponseDto> memberships = membershipRepository.findByDoctorId(doctor.getId())
                .stream()
                .map(membershipMapper::toResponseDto)
                .collect(Collectors.toList());

        List<OrganizationResponseDto> organizations = organizationRepository.findByDoctorId(doctor.getId())
                .stream()
                .map(organizationMapper::toResponseDto)
                .collect(Collectors.toList());

        List<RegistrationResponseDto> registrations = registrationRepository.findByDoctorId(doctor.getId())
                .stream()
                .map(registrationMapper::toResponseDto)
                .collect(Collectors.toList());

        List<PricingResponseDto> sessionPricings = pricingRepository.findByDoctorId(doctor.getId())
                .stream()
                .map(pricingMapper::toResponseDto)
                .collect(Collectors.toList());

        List<PhotoResponseDto> photos = photoRepository.findByDoctorId(doctor.getId())
                .stream()
                .map(photoMapper::toResponseDto)
                .collect(Collectors.toList());

        // Get address IDs from doctor_addresses junction table
        List<String> addressIds = doctor.getAddressIds() != null ? doctor.getAddressIds() : new ArrayList<>();

        List<RecommendationResponseDto> recommendations = recommendationRepository.findByDoctorId(doctor.getId())
                .stream()
                .map(recommendationMapper::toResponseDto)
                .collect(Collectors.toList());

        return DoctorResponseDto.builder()
                .id(doctor.getId() != null ? doctor.getId().toString() : null)
                .userId(doctor.getUserId() != null ? doctor.getUserId().toString() : null)
                .name(doctor.getName() != null ? doctor.getName() : "")
                .slug(doctor.getSlug() != null ? doctor.getSlug() : "")
                .gender(doctor.getGender() != null ? doctor.getGender() : "")
                .summary(doctor.getSummary() != null ? doctor.getSummary() : "")
                .about(doctor.getAbout() != null ? doctor.getAbout() : "")
                .imageUrl(doctor.getImageUrl() != null ? doctor.getImageUrl() : "")
                .experienceYears(doctor.getExperienceYears() != null ? doctor.getExperienceYears() : 0)
                .isVerified(doctor.getIsVerified() != null ? doctor.getIsVerified() : false)
                .ratingAverage(doctor.getRatingAverage() != null ? doctor.getRatingAverage() : BigDecimal.ZERO)
                .reviewCount(doctor.getReviewCount() != null ? doctor.getReviewCount() : 0)
                .status(doctor.getStatus() != null ? doctor.getStatus() : "INACTIVE")
                .createdAt(doctor.getCreatedAt() != null ? doctor.getCreatedAt().toString() : "")
                .updatedAt(doctor.getUpdatedAt() != null ? doctor.getUpdatedAt().toString() : "")
                .awards(awards) // Properly populated awards
                .qualifications(qualifications) // Properly populated qualifications
                .memberships(memberships) // Properly populated memberships
                .organizations(organizations) // Properly populated organizations
                .registrations(registrations) // Properly populated registrations
                .sessionPricings(sessionPricings) // Properly populated session pricings
                .specializations(specializations) // Properly populated specializations
                .photos(photos) // Properly populated photos
                .addressIds(addressIds) // Properly populated address IDs
                .recommendations(recommendations) // Properly populated recommendations
                .build();
    }

    @Override
    public DoctorResponseDto create(DoctorRequestDto requestDto) {
        var doctor = doctorMapper.fromRequestDto(requestDto);
        var savedDoctor = doctorRepository.save(doctor);
        return createDoctorResponseDto(savedDoctor);
    }

    @Override
    public DoctorResponseDto findById(UUID id) {
        return doctorRepository.findById(id)
                .map(this::createDoctorResponseDto)
                .orElseThrow(() -> new DoctorNotFoundException("Doctor not found with ID: " + id));
    }

    @Override
    public Page<DoctorResponseDto> findAll(Pageable pageable) {
        var doctors = doctorRepository.findAll(pageable);
        log.info("Found {} doctors", doctors.getTotalElements());
        return doctors.map(this::createDoctorResponseDto);
    }

    @Override
    public DoctorResponseDto update(UUID id, DoctorRequestDto requestDto) {
        var existingDoctor = doctorRepository.findById(id)
                .orElseThrow(() -> new DoctorNotFoundException("Doctor not found with ID: " + id));

        // Store original values for comparison
        String originalName = existingDoctor.getName();
        String originalAvatar = existingDoctor.getImageUrl();
        String originalStatus = existingDoctor.getStatus();

        // Get current user information for comparison
        String originalEmail = null;
        String originalPhone = null;
        if (existingDoctor.getUserId() != null) {
            try {
                var currentUser = userIntegrationService.getUserById(existingDoctor.getUserId()).block();
                if (currentUser != null) {
                    originalEmail = currentUser.email();
                    originalPhone = currentUser.phone();
                }
            } catch (Exception e) {
                log.warn("Could not fetch current user information for comparison: {}", e.getMessage());
            }
        }

        // Update doctor entity
        doctorMapper.updateEntityFromDto(requestDto, existingDoctor);
        var updatedDoctor = doctorRepository.save(existingDoctor);

        // Update user information if doctor has a userId
        if (existingDoctor.getUserId() != null) {
            try {
                // Check if any user-related fields have changed
                boolean nameChanged = !originalName.equals(updatedDoctor.getName());
                boolean avatarChanged = !originalAvatar.equals(updatedDoctor.getImageUrl());
                boolean statusChanged = !originalStatus.equals(updatedDoctor.getStatus());

                // For email and phone, we need to get them from the request or user service
                // Since Doctor entity doesn't store email/phone, we'll update user service with
                // current values
                boolean shouldUpdateUser = nameChanged || avatarChanged || statusChanged;

                if (shouldUpdateUser) {
                    log.info("Updating user information for doctor ID: {} with userId: {}", id,
                            existingDoctor.getUserId());

                    // Update user in user service with current values
                    UserUpdateRequest userUpdateRequest = UserUpdateRequest.builder()
                            .name(updatedDoctor.getName())
                            .email(originalEmail) // Keep current email
                            .phone(originalPhone) // Keep current phone
                            .avatar(updatedDoctor.getImageUrl())
                            .status(updatedDoctor.getStatus())
                            .build();

                    userIntegrationService.updateUser(existingDoctor.getUserId(), userUpdateRequest)
                            .doOnSuccess(
                                    user -> log.info("Successfully updated user information for doctor ID: {}", id))
                            .doOnError(error -> log.error("Failed to update user information for doctor ID: {}", id,
                                    error))
                            .subscribe();
                }
            } catch (Exception e) {
                log.error("Error updating user information for doctor ID: {}", id, e);
                // Don't fail the doctor update if user update fails
            }
        }

        return createDoctorResponseDto(updatedDoctor);
    }

    @Override
    public DoctorResponseDto partialUpdate(UUID id, DoctorRequestDto requestDto) {
        var existingDoctor = doctorRepository.findById(id)
                .orElseThrow(() -> new DoctorNotFoundException("Doctor not found with ID: " + id));

        // Store original values for comparison
        String originalName = existingDoctor.getName();
        String originalAvatar = existingDoctor.getImageUrl();
        String originalStatus = existingDoctor.getStatus();

        // Get current user information for comparison
        String originalEmail = null;
        String originalPhone = null;
        if (existingDoctor.getUserId() != null) {
            try {
                var currentUser = userIntegrationService.getUserById(existingDoctor.getUserId()).block();
                if (currentUser != null) {
                    originalEmail = currentUser.email();
                    originalPhone = currentUser.phone();
                }
            } catch (Exception e) {
                log.warn("Could not fetch current user information for comparison: {}", e.getMessage());
            }
        }

        // Update doctor entity
        doctorMapper.updateEntityFromDto(requestDto, existingDoctor);
        var updatedDoctor = doctorRepository.save(existingDoctor);

        // Update user information if doctor has a userId
        if (existingDoctor.getUserId() != null) {
            try {
                // Check if any user-related fields have changed
                boolean nameChanged = !originalName.equals(updatedDoctor.getName());
                boolean avatarChanged = !originalAvatar.equals(updatedDoctor.getImageUrl());
                boolean statusChanged = !originalStatus.equals(updatedDoctor.getStatus());

                // For email and phone, we need to get them from the request or user service
                // Since Doctor entity doesn't store email/phone, we'll update user service with
                // current values
                boolean shouldUpdateUser = nameChanged || avatarChanged || statusChanged;

                if (shouldUpdateUser) {
                    log.info("Updating user information for doctor ID: {} with userId: {}", id,
                            existingDoctor.getUserId());

                    // Update user in user service with current values
                    UserUpdateRequest userUpdateRequest = UserUpdateRequest.builder()
                            .name(updatedDoctor.getName())
                            .email(originalEmail) // Keep current email
                            .phone(originalPhone) // Keep current phone
                            .avatar(updatedDoctor.getImageUrl())
                            .status(updatedDoctor.getStatus())
                            .build();

                    userIntegrationService.updateUser(existingDoctor.getUserId(), userUpdateRequest)
                            .doOnSuccess(
                                    user -> log.info("Successfully updated user information for doctor ID: {}", id))
                            .doOnError(error -> log.error("Failed to update user information for doctor ID: {}", id,
                                    error))
                            .subscribe();
                }
            } catch (Exception e) {
                log.error("Error updating user information for doctor ID: {}", id, e);
                // Don't fail the doctor update if user update fails
            }
        }

        return createDoctorResponseDto(updatedDoctor);
    }

    @Override
    public void delete(UUID id) {
        log.info("Starting deletion process for doctor with ID: {}", id);

        // Find the doctor first to get the userId
        var doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new DoctorNotFoundException("Doctor not found with ID: " + id));

        UUID userId = doctor.getUserId();
        log.info("Found doctor with ID: {} and userId: {}", id, userId);

        // Step 1: Delete the doctor record first
        log.info("Step 1: Deleting doctor record with ID: {}", id);
        doctorRepository.deleteById(id);
        log.info("Successfully deleted doctor record with ID: {}", id);

        // Step 2: Delete the user from both auth-service and user-service if userId
        // exists
        if (userId != null) {
            log.info("Step 2: Deleting user with ID: {} from auth-service and user-service", userId);

            try {
                // Delete from auth-service
                authServiceIntegration.deleteUser(userId.toString())
                        .doOnSuccess(
                                result -> log.info("Successfully deleted user from auth-service with ID: {}", userId))
                        .doOnError(error -> log.error("Failed to delete user from auth-service with ID: {}", userId,
                                error))
                        .subscribe();

                // Delete from user-service
                // userIntegrationService.deleteUser(userId)
                // .doOnSuccess(
                // result -> log.info("Successfully deleted user from user-service with ID: {}",
                // userId))
                // .doOnError(error -> log.error("Failed to delete user from user-service with
                // ID: {}", userId,
                // error))
                // .subscribe();

                log.info("User deletion requests sent to both auth-service and user-service for user ID: {}", userId);

            } catch (Exception e) {
                log.error("Error during user deletion process for user ID: {}", userId, e);
                // Don't fail the doctor deletion if user deletion fails
                // The user deletion is asynchronous and will be handled by the respective
                // services
            }
        } else {
            log.info("No userId associated with doctor ID: {}, skipping user deletion", id);
        }

        log.info("Completed deletion process for doctor with ID: {}", id);
    }

    @Override
    public DoctorResponseDto findBySlug(String slug) {
        return doctorRepository.findBySlug(slug)
                .map(this::createDoctorResponseDto)
                .orElseThrow(() -> new DoctorNotFoundException("Doctor not found with slug: " + slug));
    }

    @Override
    public DoctorResponseDto findByUserId(UUID userId) {
        return doctorRepository.findByUserId(userId)
                .map(this::createDoctorResponseDto)
                .orElseThrow(() -> new DoctorNotFoundException("Doctor not found with user ID: " + userId));
    }

    @Override
    public List<DoctorResponseDto> findByName(String name) {
        return doctorRepository.findByNameContainingIgnoreCase(name).stream()
                .map(this::createDoctorResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public Page<DoctorResponseDto> findByStatus(String status, Pageable pageable) {
        return doctorRepository.findByStatus(status, pageable).map(this::createDoctorResponseDto);
    }

    @Override
    public Page<DoctorResponseDto> findByVerificationStatus(Boolean isVerified, Pageable pageable) {
        return doctorRepository.findByIsVerified(isVerified, pageable).map(this::createDoctorResponseDto);
    }

    @Override
    public Page<DoctorResponseDto> findByGender(String gender, Pageable pageable) {
        return doctorRepository.findByGender(gender, pageable).map(this::createDoctorResponseDto);
    }

    @Override
    public Page<DoctorResponseDto> findByExperienceRange(Integer minYears, Integer maxYears, Pageable pageable) {
        return doctorRepository.findByExperienceYearsBetween(minYears, maxYears, pageable)
                .map(this::createDoctorResponseDto);
    }

    @Override
    public Page<DoctorResponseDto> findByMinRating(BigDecimal minRating, Pageable pageable) {
        return doctorRepository.findByRatingAverageGreaterThanEqual(minRating, pageable)
                .map(this::createDoctorResponseDto);
    }

    @Override
    public Page<DoctorResponseDto> findBySpeciality(String speciality, Pageable pageable) {
        // This assumes a relationship between Doctor and Specialization that is not
        // directly in the Doctor entity.
        // This would require a more complex query or a different data model.
        // For now, returning an empty page.
        return Page.empty(pageable);
    }

    @Override
    public Page<DoctorResponseDto> findByLocation(UUID addressId, Pageable pageable) {
        List<Doctor> doctors = doctorRepository.findByAddressLocation(addressId);
        List<DoctorResponseDto> doctorDtos = doctors.stream()
                .map(this::createDoctorResponseDto)
                .collect(Collectors.toList());
        
        // Convert list to page manually since repository method returns List
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), doctorDtos.size());
        List<DoctorResponseDto> pageContent = doctorDtos.subList(start, end);
        
        return new PageImpl<>(pageContent, pageable, doctorDtos.size());
    }

    @Override
    public Page<DoctorResponseDto> searchDoctors(String name, String speciality, Boolean isVerified,
            BigDecimal minRating, Pageable pageable) {
        // This would require a specification-based search.
        // For now, returning an empty page.
        return Page.empty(pageable);
    }

    @Override
    public Page<DoctorResponseDto> findTopRatedDoctors(Pageable pageable) {
        return doctorRepository.findAllByOrderByRatingAverageDesc(pageable).map(this::createDoctorResponseDto);
    }

    @Override
    public Page<DoctorResponseDto> findVerifiedDoctorsWithMinRating(BigDecimal minRating, Pageable pageable) {
        return doctorRepository.findByIsVerifiedAndRatingAverageGreaterThanEqual(true, minRating, pageable)
                .map(this::createDoctorResponseDto);
    }

    @Override
    public DoctorResponseDto verifyDoctor(UUID id) {
        var doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new DoctorNotFoundException("Doctor not found with ID: " + id));
        doctor.setIsVerified(true);
        var updatedDoctor = doctorRepository.save(doctor);
        return createDoctorResponseDto(updatedDoctor);
    }

    @Override
    public DoctorResponseDto unverifyDoctor(UUID id) {
        var doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new DoctorNotFoundException("Doctor not found with ID: " + id));
        doctor.setIsVerified(false);
        var updatedDoctor = doctorRepository.save(doctor);
        return createDoctorResponseDto(updatedDoctor);
    }

    @Override
    public DoctorResponseDto activateDoctor(UUID id) {
        var doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new DoctorNotFoundException("Doctor not found with ID: " + id));
        doctor.setStatus("ACTIVE");
        var updatedDoctor = doctorRepository.save(doctor);
        return createDoctorResponseDto(updatedDoctor);
    }

    @Override
    public DoctorResponseDto deactivateDoctor(UUID id) {
        var doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new DoctorNotFoundException("Doctor not found with ID: " + id));
        doctor.setStatus("INACTIVE");
        var updatedDoctor = doctorRepository.save(doctor);
        return createDoctorResponseDto(updatedDoctor);
    }

    @Override
    public DoctorResponseDto suspendDoctor(UUID id) {
        var doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new DoctorNotFoundException("Doctor not found with ID: " + id));
        doctor.setStatus("SUSPENDED");
        var updatedDoctor = doctorRepository.save(doctor);
        return createDoctorResponseDto(updatedDoctor);
    }

    @Override
    public void updateRatingAndReviewCount(UUID id, BigDecimal newRating, Integer reviewCount) {
        var doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new DoctorNotFoundException("Doctor not found with ID: " + id));
        doctor.setRatingAverage(newRating);
        doctor.setReviewCount(reviewCount);
        doctorRepository.save(doctor);
    }

    @Override
    public boolean existsById(UUID id) {
        return doctorRepository.existsById(id);
    }

    @Override
    public boolean existsBySlug(String slug) {
        return doctorRepository.existsBySlug(slug);
    }

    @Override
    public boolean existsByUserId(UUID userId) {
        return doctorRepository.existsByUserId(userId);
    }

    @Override
    public boolean isSlugAvailable(String slug) {
        return !doctorRepository.existsBySlug(slug);
    }

    @Override
    public boolean isDoctorVerified(UUID id) {
        return doctorRepository.findById(id)
                .map(Doctor::getIsVerified)
                .orElse(false);
    }

    @Override
    public boolean isDoctorActive(UUID id) {
        return doctorRepository.findById(id)
                .map(doctor -> "ACTIVE".equalsIgnoreCase(doctor.getStatus()))
                .orElse(false);
    }

    @Override
    public long countAll() {
        return doctorRepository.count();
    }

    @Override
    public long countByStatus(String status) {
        return doctorRepository.countByStatus(status);
    }

    @Override
    public long countByVerificationStatus(Boolean isVerified) {
        return doctorRepository.countByIsVerified(isVerified);
    }

    @Override
    public long countBySpeciality(String speciality) {
        // This would require a more complex query or a different data model.
        return 0;
    }

    @Override
    public List<DoctorResponseDto> createBatch(List<DoctorRequestDto> requestDtos) {
        var doctors = requestDtos.stream()
                .map(doctorMapper::fromRequestDto)
                .collect(Collectors.toList());
        var savedDoctors = doctorRepository.saveAll(doctors);
        return savedDoctors.stream()
                .map(this::createDoctorResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteBatch(List<UUID> ids) {
        doctorRepository.deleteAllById(ids);
    }

    @Override
    public int calculateProfileCompleteness(UUID id) {
        var doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new DoctorNotFoundException("Doctor not found with ID: " + id));

        int completeness = 0;
        if (doctor.getName() != null && !doctor.getName().isEmpty())
            completeness += 10;
        if (doctor.getAbout() != null && !doctor.getAbout().isEmpty())
            completeness += 10;
        if (doctor.getGender() != null && !doctor.getGender().isEmpty())
            completeness += 5;
        if (doctor.getExperienceYears() != null)
            completeness += 10;
        // Add checks for other fields

        return Math.min(100, completeness);
    }

    @Override
    public boolean isProfileComplete(UUID id) {
        return calculateProfileCompleteness(id) == 100;
    }

    @Override
    public List<String> getMissingProfileFields(UUID id) {
        var doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new DoctorNotFoundException("Doctor not found with ID: " + id));
        List<String> missingFields = new ArrayList<>();

        if (doctor.getName() == null || doctor.getName().isEmpty())
            missingFields.add("name");
        if (doctor.getAbout() == null || doctor.getAbout().isEmpty())
            missingFields.add("bio");
        if (doctor.getGender() == null || doctor.getGender().isEmpty())
            missingFields.add("gender");
        if (doctor.getExperienceYears() == null)
            missingFields.add("yearsOfExperience");
        // Add checks for other fields

        return missingFields;
    }

    @Override
    public DoctorResponseDto registerDoctor(DoctorDto requestDto) {
        try {
            // Step 1: Register user via auth-service
            UserRegistrationRequest userRequest = UserRegistrationRequest.builder()
                    .name(requestDto.name())
                    .email(requestDto.email())
                    .phone(requestDto.phone())
                    .password(requestDto.password())
                    .role("DOCTOR")
                    .build();

            UserModel userResponse = authServiceIntegration.registerUser(userRequest).block();

            if (userResponse == null || userResponse.id() == null) {
                throw new RuntimeException("Failed to register user - no user ID returned");
            }

            log.info("Registered user with ID: {}", userResponse);

            // Step 2: Create doctor with the returned user ID
            // Create a new DoctorRequestDto with userId set
            DoctorRequestDto doctorRequestDto = new DoctorRequestDto(
                    userResponse.id(),
                    requestDto.name(), // Use requestDto.name() as fallback
                    requestDto.slug(),
                    requestDto.gender(),
                    requestDto.summary(),
                    requestDto.about(),
                    requestDto.imageUrl(),
                    requestDto.experienceYears(),
                    requestDto.isVerified(),
                    requestDto.ratingAverage(),
                    requestDto.reviewCount(),

                    StringUtils.hasText(requestDto.status()) ? requestDto.status() : "ACTIVE");
            log.info("Creating doctor with request: {}", doctorRequestDto);
            var doctor = doctorMapper.fromRequestDto(doctorRequestDto);
            log.info("Doctor from mapper :{}", doctor);
            // doctor.setUserId(UUID.fromString(userResponse.id()));
            var savedDoctor = doctorRepository.save(doctor);
            return createDoctorResponseDto(savedDoctor);
        } catch (Exception e) {

            throw new RuntimeException("Failed to register doctor", e);
        }
    }
}
