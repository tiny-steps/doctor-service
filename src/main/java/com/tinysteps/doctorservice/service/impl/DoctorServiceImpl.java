package com.tinysteps.doctorservice.service.impl;

import com.tinysteps.doctorservice.entity.Doctor;
import com.tinysteps.doctorservice.entity.DoctorAddress;
import com.tinysteps.doctorservice.entity.Status;
import com.tinysteps.doctorservice.repository.DoctorAddressRepository;
import com.tinysteps.doctorservice.exception.DoctorNotFoundException;
import com.tinysteps.doctorservice.exception.DoctorSoftDeleteException;
import com.tinysteps.doctorservice.integration.model.UserModel;
import com.tinysteps.doctorservice.mapper.*;
import com.tinysteps.doctorservice.model.*;
import com.tinysteps.doctorservice.model.DoctorAddressRequestDto;
import com.tinysteps.doctorservice.repository.*;
import com.tinysteps.doctorservice.service.DoctorService;
import com.tinysteps.doctorservice.service.DoctorAddressService;
import com.tinysteps.doctorservice.dto.UserRegistrationRequest;
import com.tinysteps.doctorservice.integration.service.AuthServiceIntegration;
import com.tinysteps.doctorservice.integration.service.UserIntegrationService;
import com.tinysteps.doctorservice.integration.model.UserUpdateRequest;
import com.tinysteps.doctorservice.service.SecurityService;
import com.tinysteps.common.entity.EntityStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private final DoctorAddressMapper doctorAddressMapper;
    private final AuthServiceIntegration authServiceIntegration;
    private final UserIntegrationService userIntegrationService;
    private final DoctorAddressService doctorAddressService;
    private final SecurityService securityService;
    private final DoctorAddressRepository doctorAddressRepository;

    @PersistenceContext
    private EntityManager entityManager;

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
            DoctorAddressMapper doctorAddressMapper, AuthServiceIntegration authServiceIntegration,
            UserIntegrationService userIntegrationService,
            DoctorAddressService doctorAddressService, SecurityService securityService,
            DoctorAddressRepository doctorAddressRepository) {
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
        this.doctorAddressMapper = doctorAddressMapper;
        this.authServiceIntegration = authServiceIntegration;
        this.userIntegrationService = userIntegrationService;
        this.doctorAddressService = doctorAddressService;
        this.securityService = securityService;
        this.doctorAddressRepository = doctorAddressRepository;
    }

    /**
     * Manually creates DoctorResponseDto from Doctor entity, handling null values
     * appropriately
     * and properly populating related entities to avoid empty lists
     */
    private DoctorResponseDto createDoctorResponseDto(Doctor doctor) {
        return createDoctorResponseDto(doctor, null);
    }

    /**
     * Creates DoctorResponseDto with branch-specific context
     */
    private DoctorResponseDto createDoctorResponseDto(Doctor doctor, UUID branchContext) {
        // Fetch user data for email, phone, and avatar
        String email = "";
        String phone = "";
        String avatar = "";
        if (doctor.getUserId() != null) {
            try {
                log.info("Fetching user data for doctor {} with userId: {}", doctor.getId(), doctor.getUserId());
                var user = userIntegrationService.getUserById(doctor.getUserId()).block();
                if (user != null) {
                    email = user.email() != null ? user.email() : "";
                    phone = user.phone() != null ? user.phone() : "";
                    avatar = user.avatar() != null ? user.avatar() : "";
                    log.info("Successfully fetched user data for doctor {}: email={}, phone={}, avatar={}",
                            doctor.getId(), email,
                            phone, avatar);
                } else {
                    log.warn("User service returned null for userId: {} (doctor: {})", doctor.getUserId(),
                            doctor.getId());
                }
            } catch (Exception e) {
                log.error("Failed to fetch user information for doctor {} with userId {}: {}", doctor.getId(),
                        doctor.getUserId(), e.getMessage(), e);
            }
        } else {
            log.warn("Doctor {} has no userId associated", doctor.getId());
        }

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

        // Get address IDs from doctor_addresses junction table using repository
        List<String> addressIds = doctorAddressRepository.findAddressIdsByDoctorId(doctor.getId())
                .stream()
                .map(UUID::toString)
                .collect(Collectors.toList());

        // Get doctor addresses with status information
        List<DoctorAddressResponseDto> doctorAddresses = doctorAddressRepository.findByDoctorId(doctor.getId())
                .stream()
                .map(doctorAddressMapper::toResponseDto)
                .collect(Collectors.toList());

        List<RecommendationResponseDto> recommendations = recommendationRepository.findByDoctorId(doctor.getId())
                .stream()
                .map(recommendationMapper::toResponseDto)
                .collect(Collectors.toList());

        // Determine doctor status based on context
        String doctorStatus = doctor.getStatus() != null ? doctor.getStatus().name() : "ACTIVE";

        // If we have a branch context, check if doctor is active in that specific
        // branch
        if (branchContext != null) {
            List<DoctorAddress> branchAssociations = doctorAddressRepository.findByDoctorIdAndAddressId(doctor.getId(),
                    branchContext);
            if (!branchAssociations.isEmpty()) {
                // If doctor has association with this branch, use the branch-specific status
                Status branchStatus = branchAssociations.get(0).getStatus();
                if (branchStatus == Status.INACTIVE) {
                    doctorStatus = "INACTIVE";
                }
            } else if (!doctor.getPrimaryBranchId().equals(branchContext)) {
                // Doctor is not associated with this branch and it's not their primary branch
                doctorStatus = "INACTIVE";
            }
        }

        return DoctorResponseDto.builder()
                .id(doctor.getId() != null ? doctor.getId().toString() : null)
                .userId(doctor.getUserId() != null ? doctor.getUserId().toString() : null)
                .name(doctor.getName() != null ? doctor.getName() : "")
                .email(email)
                .phone(phone)
                .slug(doctor.getSlug() != null ? doctor.getSlug() : "")
                .gender(doctor.getGender() != null ? doctor.getGender() : "")
                .summary(doctor.getSummary() != null ? doctor.getSummary() : "")
                .about(doctor.getAbout() != null ? doctor.getAbout() : "")
                .imageUrl(avatar) // Use avatar from user service instead of doctor.getImageUrl()
                .experienceYears(doctor.getExperienceYears() != null ? doctor.getExperienceYears() : 0)
                .isVerified(doctor.getIsVerified() != null ? doctor.getIsVerified() : false)
                .ratingAverage(doctor.getRatingAverage() != null ? doctor.getRatingAverage() : BigDecimal.ZERO)
                .reviewCount(doctor.getReviewCount() != null ? doctor.getReviewCount() : 0)
                .status(doctorStatus) // Use computed status instead of raw doctor status
                .primaryBranchId(doctor.getPrimaryBranchId() != null ? doctor.getPrimaryBranchId().toString() : null)
                .isMultiBranch(doctor.getIsMultiBranch() != null ? doctor.getIsMultiBranch() : false)
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
                .doctorAddresses(doctorAddresses) // Properly populated doctor addresses with status
                .recommendations(recommendations) // Properly populated recommendations
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public DoctorResponseDto create(DoctorRequestDto requestDto) {
        var doctor = doctorMapper.fromRequestDto(requestDto);
        var savedDoctor = doctorRepository.save(doctor);
        return createDoctorResponseDto(savedDoctor);
    }

    @Override
    @Transactional(readOnly = true)
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
        String originalStatus = existingDoctor.getStatus() != null ? existingDoctor.getStatus().name() : null;

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

        // If imageData is present, clear imageUrl for now; user-service will return new
        // avatar
        // Note: imageData is handled by user-service via auth/user flows. We keep
        // current URL until propagated.

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
                    String avatarToSend = updatedDoctor.getImageUrl();
                    try {
                        var imageDataMethod = requestDto.getClass().getMethod("imageData");
                        Object img = imageDataMethod.invoke(requestDto);
                        if (img instanceof String s && !s.isEmpty()) {
                            avatarToSend = s; // pass data URL
                        }
                    } catch (Exception ignore) {
                    }

                    UserUpdateRequest userUpdateRequest = UserUpdateRequest.builder()
                            .name(updatedDoctor.getName())
                            .email(originalEmail) // Keep current email
                            .phone(originalPhone) // Keep current phone
                            .avatar(avatarToSend)
                            .status(updatedDoctor.getStatus() != null ? updatedDoctor.getStatus().name()
                                    : EntityStatus.INACTIVE.name())
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
        String originalStatus = existingDoctor.getStatus() != null ? existingDoctor.getStatus().name() : null;

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
                    String avatarToSend = updatedDoctor.getImageUrl();
                    // If client sent base64 image via requestDto.imageData embedded in avatar flow
                    try {
                        var imageDataField = requestDto.getClass().getMethod("imageData");
                        Object img = imageDataField.invoke(requestDto);
                        if (img instanceof String s && !s.isEmpty()) {
                            avatarToSend = s; // pass data URL to user-service which will persist and return URL
                        }
                    } catch (Exception ignore) {
                    }

                    UserUpdateRequest userUpdateRequest = UserUpdateRequest.builder()
                            .name(updatedDoctor.getName())
                            .email(originalEmail) // Keep current email
                            .phone(originalPhone) // Keep current phone
                            .avatar(avatarToSend)
                            .status(updatedDoctor.getStatus() != null ? updatedDoctor.getStatus().name()
                                    : EntityStatus.INACTIVE.name())
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
        EntityStatus entityStatus = EntityStatus.valueOf(status.toUpperCase());
        return doctorRepository.findByStatus(entityStatus, pageable).map(this::createDoctorResponseDto);
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
        return doctorRepository.findByAddressLocation(addressId, pageable)
                .map(this::createDoctorResponseDto);
    }

    @Override
    public Page<DoctorResponseDto> findByLocationAndPracticeRole(UUID addressId, String practiceRole,
            Pageable pageable) {
        return doctorRepository.findByAddressLocationAndPracticeRole(addressId, practiceRole, pageable)
                .map(this::createDoctorResponseDto);
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
        doctor.setStatus(EntityStatus.ACTIVE);
        var updatedDoctor = doctorRepository.save(doctor);
        return createDoctorResponseDto(updatedDoctor);
    }

    @Override
    public DoctorResponseDto deactivateDoctor(UUID id) {
        var doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new DoctorNotFoundException("Doctor not found with ID: " + id));
        doctor.setStatus(EntityStatus.INACTIVE);
        var updatedDoctor = doctorRepository.save(doctor);
        return createDoctorResponseDto(updatedDoctor);
    }

    @Override
    public DoctorResponseDto suspendDoctor(UUID id) {
        var doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new DoctorNotFoundException("Doctor not found with ID: " + id));
        doctor.setStatus(EntityStatus.INACTIVE);
        var updatedDoctor = doctorRepository.save(doctor);
        return createDoctorResponseDto(updatedDoctor);
    }

    // Enhanced Soft Delete Operations
    @Override
    @Transactional
    public DoctorSoftDeleteResponseDto deactivateDoctorFromBranches(UUID doctorId,
            DoctorBranchDeactivationRequestDto request) {
        log.info("Deactivating doctor {} from branches: {}", doctorId, request.getBranchIds());

        // Input validation
        if (request.getBranchIds() == null || request.getBranchIds().isEmpty()) {
            throw new DoctorSoftDeleteException("Branch IDs list cannot be null or empty");
        }

        if (request.getBranchIds().size() > 50) {
            throw new DoctorSoftDeleteException("Cannot deactivate from more than 50 branches at once");
        }

        // Validate doctor exists
        var doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new DoctorNotFoundException("Doctor not found with ID: " + doctorId));

        // Validate that doctor is associated with all specified branches
        List<DoctorAddress> doctorAddresses = doctorAddressRepository.findByDoctorIdAndAddressIdIn(doctorId,
                request.getBranchIds());
        List<UUID> foundBranchIds = doctorAddresses.stream().map(DoctorAddress::getAddressId).toList();

        if (foundBranchIds.size() != request.getBranchIds().size()) {
            List<UUID> missingBranches = request.getBranchIds().stream()
                    .filter(branchId -> !foundBranchIds.contains(branchId))
                    .toList();
            throw new DoctorSoftDeleteException("Doctor is not associated with branches: " + missingBranches);
        }

        // Validate that we're not trying to deactivate from all branches without
        // explicit global deactivation
        long totalBranches = doctorAddressRepository.countByDoctorId(doctorId);
        if (request.getBranchIds().size() == totalBranches && totalBranches > 1) {
            log.warn(
                    "Attempting to deactivate doctor {} from all {} branches. Consider using global deactivation instead.",
                    doctorId, totalBranches);
        }

        try {
            // Deactivate doctor from specified branches
            doctorAddressRepository.updateStatusByDoctorIdAndAddressIdIn(doctorId, request.getBranchIds(),
                    Status.INACTIVE);

            // Check if doctor should be globally deactivated
            boolean globalStatusChanged = false;
            String newGlobalStatus = doctor.getStatus().name();

            if (request.getUpdateGlobalStatus()) {
                long activeBranchCount = doctorAddressRepository.countActiveBranchesByDoctorId(doctorId);
                if (activeBranchCount == 0 && doctor.getStatus() == EntityStatus.ACTIVE) {
                    doctor.setStatus(EntityStatus.INACTIVE);
                    doctorRepository.save(doctor);
                    globalStatusChanged = true;
                    newGlobalStatus = EntityStatus.INACTIVE.name();
                    log.info("Doctor {} global status changed to INACTIVE as they are no longer active in any branch",
                            doctorId);
                }
            }

            // Gather response information
            long remainingActiveBranches = doctorAddressRepository.countActiveBranchesByDoctorId(doctorId);

            log.info("Successfully deactivated doctor {} from {} branches. Remaining active branches: {}",
                    doctorId, request.getBranchIds().size(), remainingActiveBranches);

            return DoctorSoftDeleteResponseDto.builder()
                    .doctorId(doctorId)
                    .success(true)
                    .message(String.format("Doctor successfully deactivated from %d branch(es). %s",
                            request.getBranchIds().size(),
                            globalStatusChanged ? "Global status changed to INACTIVE." : ""))
                    .affectedBranches(request.getBranchIds())
                    .globalStatusChanged(globalStatusChanged)
                    .newGlobalStatus(newGlobalStatus)
                    .remainingActiveBranches((int) remainingActiveBranches)
                    .totalBranches((int) totalBranches)
                    .operationType("BRANCH_SPECIFIC_DEACTIVATION")
                    .build();

        } catch (DoctorSoftDeleteException e) {
            throw e; // Re-throw validation exceptions
        } catch (Exception e) {
            log.error("Failed to deactivate doctor {} from branches {}: {}", doctorId, request.getBranchIds(),
                    e.getMessage(), e);
            throw new DoctorSoftDeleteException("Failed to deactivate doctor from branches: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public DoctorSoftDeleteResponseDto deactivateDoctorGlobally(UUID doctorId) {
        log.info("Deactivating doctor {} globally", doctorId);

        // Validate doctor exists
        var doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new DoctorNotFoundException("Doctor not found with ID: " + doctorId));

        try {
            // Get all branch IDs for this doctor
            List<UUID> allBranchIds = doctorAddressRepository.findAddressIdsByDoctorId(doctorId);

            // Deactivate doctor from all branches
            if (!allBranchIds.isEmpty()) {
                doctorAddressRepository.updateStatusByDoctorId(doctorId, Status.INACTIVE);
            }

            // Set global status to inactive
            boolean globalStatusChanged = doctor.getStatus() == EntityStatus.ACTIVE;
            doctor.setStatus(EntityStatus.INACTIVE);
            doctorRepository.save(doctor);

            log.info("Successfully deactivated doctor {} globally from {} branches", doctorId, allBranchIds.size());

            return DoctorSoftDeleteResponseDto.builder()
                    .doctorId(doctorId)
                    .success(true)
                    .message(String.format("Doctor successfully deactivated globally from %d branch(es)",
                            allBranchIds.size()))
                    .affectedBranches(allBranchIds)
                    .globalStatusChanged(globalStatusChanged)
                    .newGlobalStatus(EntityStatus.INACTIVE.name())
                    .remainingActiveBranches(0)
                    .totalBranches(allBranchIds.size())
                    .operationType("GLOBAL_DEACTIVATION")
                    .build();

        } catch (Exception e) {
            log.error("Failed to deactivate doctor {} globally: {}", doctorId, e.getMessage(), e);
            throw new RuntimeException("Failed to deactivate doctor globally: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public DoctorSoftDeleteResponseDto activateDoctorInBranch(UUID doctorId, UUID branchId) {
        log.info("Activating doctor {} in branch {}", doctorId, branchId);

        // Validate doctor exists
        var doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new DoctorNotFoundException("Doctor not found with ID: " + doctorId));

        // Validate that doctor is associated with the branch
        List<DoctorAddress> doctorAddresses = doctorAddressRepository.findByDoctorIdAndAddressId(doctorId, branchId);
        if (doctorAddresses.isEmpty()) {
            throw new IllegalArgumentException("Doctor is not associated with branch: " + branchId);
        }

        try {
            // Activate doctor in the specific branch
            doctorAddressRepository.updateStatusByDoctorIdAndAddressIdAndPracticeRole(
                    doctorId, branchId, doctorAddresses.get(0).getPracticeRole(), Status.ACTIVE);

            // If doctor's global status is INACTIVE, activate it
            boolean globalStatusChanged = false;
            String newGlobalStatus = doctor.getStatus().name();

            if (doctor.getStatus() == EntityStatus.INACTIVE) {
                doctor.setStatus(EntityStatus.ACTIVE);
                doctorRepository.save(doctor);
                globalStatusChanged = true;
                newGlobalStatus = EntityStatus.ACTIVE.name();
                log.info("Doctor {} global status changed to ACTIVE", doctorId);
            }

            // Gather response information
            long activeBranchCount = doctorAddressRepository.countActiveBranchesByDoctorId(doctorId);
            long totalBranches = doctorAddressRepository.countByDoctorId(doctorId);

            log.info("Successfully activated doctor {} in branch {}. Total active branches: {}",
                    doctorId, branchId, activeBranchCount);

            return DoctorSoftDeleteResponseDto.builder()
                    .doctorId(doctorId)
                    .success(true)
                    .message(String.format("Doctor successfully activated in branch. %s",
                            globalStatusChanged ? "Global status changed to ACTIVE." : ""))
                    .affectedBranches(List.of(branchId))
                    .globalStatusChanged(globalStatusChanged)
                    .newGlobalStatus(newGlobalStatus)
                    .remainingActiveBranches((int) activeBranchCount)
                    .totalBranches((int) totalBranches)
                    .operationType("BRANCH_ACTIVATION")
                    .build();

        } catch (Exception e) {
            log.error("Failed to activate doctor {} in branch {}: {}", doctorId, branchId, e.getMessage(), e);
            throw new RuntimeException("Failed to activate doctor in branch: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<UUID, Boolean> getDoctorBranchStatus(UUID doctorId) {
        log.debug("Getting branch status for doctor {}", doctorId);

        // Validate doctor exists
        if (!doctorRepository.existsById(doctorId)) {
            throw new DoctorNotFoundException("Doctor not found with ID: " + doctorId);
        }

        List<Object[]> branchStatusList = doctorAddressRepository.findBranchStatusByDoctorId(doctorId);

        Map<UUID, Boolean> branchStatusMap = new HashMap<>();
        for (Object[] row : branchStatusList) {
            UUID branchId = (UUID) row[0];
            Status status = (Status) row[1];
            branchStatusMap.put(branchId, status == Status.ACTIVE);
        }

        return branchStatusMap;
    }

    // Soft Delete Utility Methods
    @Override
    public boolean isDoctorActiveInAnyBranch(UUID doctorId) {
        log.debug("Checking if doctor {} is active in any branch", doctorId);

        if (!doctorRepository.existsById(doctorId)) {
            throw new DoctorNotFoundException("Doctor not found with ID: " + doctorId);
        }

        return doctorAddressRepository.hasActiveBranches(doctorId);
    }

    @Override
    public long getActiveBranchCount(UUID doctorId) {
        log.debug("Getting active branch count for doctor {}", doctorId);

        if (!doctorRepository.existsById(doctorId)) {
            throw new DoctorNotFoundException("Doctor not found with ID: " + doctorId);
        }

        return doctorAddressRepository.countActiveBranchesByDoctorId(doctorId);
    }

    @Override
    public List<UUID> getActiveBranches(UUID doctorId) {
        log.debug("Getting active branches for doctor {}", doctorId);

        if (!doctorRepository.existsById(doctorId)) {
            throw new DoctorNotFoundException("Doctor not found with ID: " + doctorId);
        }

        return doctorAddressRepository.findActiveBranchIdsByDoctorId(doctorId);
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
                .map(doctor -> doctor.getStatus() == EntityStatus.ACTIVE)
                .orElse(false);
    }

    @Override
    public long countAll() {
        return doctorRepository.count();
    }

    @Override
    public long countByStatus(String status) {
        EntityStatus entityStatus = EntityStatus.valueOf(status.toUpperCase());
        return doctorRepository.countByStatus(entityStatus);
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
                    .imageData(requestDto.imageData()) // Pass image data for avatar
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
                    requestDto.imageData(), // Now support imageData during registration
                    requestDto.experienceYears(),
                    requestDto.isVerified(),
                    requestDto.ratingAverage(),
                    requestDto.reviewCount(),
                    StringUtils.hasText(requestDto.status()) ? requestDto.status() : "ACTIVE",
                    requestDto.branchId(), // Use branchId from request
                    false // isMultiBranch - default to false
            );
            log.info("Creating doctor with request: {}", doctorRequestDto);
            var doctor = doctorMapper.fromRequestDto(doctorRequestDto);
            log.info("Doctor from mapper :{}", doctor);
            // doctor.setUserId(UUID.fromString(userResponse.id()));
            var savedDoctor = doctorRepository.save(doctor);

            // Handle imageData if provided - update user avatar in user service
            if (StringUtils.hasText(requestDto.imageData())) {
                try {
                    log.info("Updating user avatar with imageData for doctor ID: {} with userId: {}",
                            savedDoctor.getId(), userResponse.id());

                    UserUpdateRequest userUpdateRequest = UserUpdateRequest.builder()
                            .name(savedDoctor.getName())
                            .avatar(requestDto.imageData()) // Pass the base64 data URL
                            .imageData(requestDto.imageData()) // Also pass as imageData
                            .build();

                    userIntegrationService.updateUser(UUID.fromString(userResponse.id()), userUpdateRequest).block();
                    log.info("Successfully updated user avatar for doctor ID: {}", savedDoctor.getId());
                } catch (Exception e) {
                    log.warn("Failed to update user avatar for doctor ID: {}: {}",
                            savedDoctor.getId(), e.getMessage());
                }
            }

            // Create doctor-address relationship if branchId is provided
            if (requestDto.branchId() != null) {
                try {
                    UUID branchUUID = UUID.fromString(requestDto.branchId());
                    DoctorAddressRequestDto addressRequest = new DoctorAddressRequestDto(
                            branchUUID,
                            "CONSULTANT", // Default practice role
                            "ACTIVE");
                    doctorAddressService.addDoctorAddress(savedDoctor.getId(), addressRequest);
                    log.info("Created doctor-address relationship for doctor {} and branch {}",
                            savedDoctor.getId(), requestDto.branchId());
                } catch (Exception e) {
                    log.warn("Failed to create doctor-address relationship for doctor {} and branch {}: {}",
                            savedDoctor.getId(), requestDto.branchId(), e.getMessage());
                }
            }

            return createDoctorResponseDto(savedDoctor);
        } catch (Exception e) {

            throw new RuntimeException("Failed to register doctor", e);
        }
    }

    // Branch-based methods
    @Override
    public long countByBranch(UUID branchId) {
        return doctorRepository.countByPrimaryBranchId(branchId);
    }

    @Override
    public long countByBranchAndStatus(UUID branchId, String status) {
        EntityStatus entityStatus = EntityStatus.valueOf(status.toUpperCase());
        return doctorRepository.countByPrimaryBranchIdAndStatus(branchId, entityStatus);
    }

    @Override
    public Page<DoctorResponseDto> findByBranch(UUID branchId, Pageable pageable) {
        // For specific branch queries, only include doctors associated with that branch
        // Do not include doctors with NULL primaryBranchId (legacy doctors) in specific
        // branch queries
        Page<Doctor> doctors = doctorRepository.findByPrimaryOrAssociatedBranch(branchId, pageable);
        return doctors.map(this::createDoctorResponseDto);
    }

    @Override
    public List<DoctorResponseDto> findByBranch(UUID branchId) {
        // For specific branch queries, only include doctors associated with that branch
        // Do not include doctors with NULL primaryBranchId (legacy doctors) in specific
        // branch queries
        List<Doctor> doctors = doctorRepository.findByPrimaryOrAssociatedBranch(branchId);
        return doctors.stream()
                .map(this::createDoctorResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public Page<DoctorResponseDto> findByBranchAndStatus(UUID branchId, String status, Pageable pageable) {
        EntityStatus entityStatus = EntityStatus.valueOf(status.toUpperCase());
        Page<Doctor> doctors = doctorRepository.findByPrimaryBranchIdAndStatus(branchId, entityStatus, pageable);
        return doctors.map(this::createDoctorResponseDto);
    }

    @Override
    public List<DoctorResponseDto> findByBranchAndStatus(UUID branchId, String status) {
        EntityStatus entityStatus = EntityStatus.valueOf(status.toUpperCase());
        List<Doctor> doctors = doctorRepository.findByPrimaryBranchIdAndStatus(branchId, entityStatus);
        return doctors.stream()
                .map(this::createDoctorResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<DoctorResponseDto> findByBranchAndVerificationStatus(UUID branchId, Boolean isVerified) {
        Page<Doctor> doctors = doctorRepository.findByPrimaryBranchIdAndIsVerified(branchId, isVerified,
                Pageable.unpaged());
        return doctors.stream()
                .map(this::createDoctorResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public Page<DoctorResponseDto> findByBranchAndVerificationStatus(UUID branchId, Boolean isVerified,
            Pageable pageable) {
        Page<Doctor> doctors = doctorRepository.findByPrimaryBranchIdAndIsVerified(branchId, isVerified, pageable);
        return doctors.map(this::createDoctorResponseDto);
    }

    @Override
    public Page<DoctorResponseDto> findMultiBranchDoctors(Pageable pageable) {
        Page<Doctor> doctors = doctorRepository.findByIsMultiBranch(true, pageable);
        return doctors.map(this::createDoctorResponseDto);
    }

    @Override
    public List<DoctorResponseDto> findMultiBranchDoctors() {
        List<Doctor> doctors = doctorRepository.findByIsMultiBranch(true);
        return doctors.stream()
                .map(this::createDoctorResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public Page<DoctorResponseDto> findDoctorsByCurrentUserBranch(Pageable pageable) {
        UUID primaryBranchId = securityService.getPrimaryBranchId();
        return findByBranch(primaryBranchId, pageable);
    }

    @Override
    public List<DoctorResponseDto> findDoctorsByCurrentUserBranch() {
        UUID primaryBranchId = securityService.getPrimaryBranchId();
        return findByBranch(primaryBranchId);
    }

    @Override
    public Page<DoctorResponseDto> searchDoctorsInBranch(UUID branchId, String name, String speciality,
            Boolean isVerified,
            BigDecimal minRating, Pageable pageable) {
        // Build specification based on provided parameters
        StringBuilder queryBuilder = new StringBuilder("SELECT d FROM Doctor d WHERE d.primaryBranchId = :branchId");
        Map<String, Object> params = new HashMap<>();
        params.put("branchId", branchId);

        if (name != null && !name.isEmpty()) {
            queryBuilder.append(" AND (d.name ILIKE :name OR d.firstName ILIKE :name OR d.lastName ILIKE :name)");
            params.put("name", "%" + name + "%");
        }

        if (speciality != null && !speciality.isEmpty()) {
            queryBuilder.append(" AND d.speciality ILIKE :speciality");
            params.put("speciality", "%" + speciality + "%");
        }

        if (isVerified != null) {
            queryBuilder.append(" AND d.isVerified = :isVerified");
            params.put("isVerified", isVerified);
        }

        if (minRating != null) {
            queryBuilder.append(" AND d.ratingAverage >= :minRating");
            params.put("minRating", minRating);
        }

        // Execute query with pagination
        TypedQuery<Doctor> query = entityManager.createQuery(queryBuilder.toString(), Doctor.class);
        params.forEach(query::setParameter);

        // Apply pagination
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        List<Doctor> doctors = query.getResultList();

        // Get total count for pagination
        String countQueryStr = queryBuilder.toString().replace("SELECT d FROM Doctor d",
                "SELECT COUNT(d) FROM Doctor d");
        TypedQuery<Long> countQuery = entityManager.createQuery(countQueryStr, Long.class);
        params.forEach(countQuery::setParameter);
        long total = countQuery.getSingleResult();

        // Convert to DTOs
        List<DoctorResponseDto> doctorDtos = doctors.stream()
                .map(this::createDoctorResponseDto)
                .collect(Collectors.toList());

        return new PageImpl<>(doctorDtos, pageable, total);
    }

    @Override
    public Page<DoctorResponseDto> findByBranchWithStatusFilter(UUID branchId, boolean includeInactive,
            Pageable pageable) {
        log.debug("Finding doctors for branch {} with includeInactive={}", branchId, includeInactive);

        Page<Doctor> doctors;
        if (includeInactive) {
            // Include both active and inactive doctors associated with the branch
            doctors = doctorRepository.findByPrimaryOrAssociatedBranchAllStatuses(branchId, pageable);
        } else {
            // Only include doctors with active status in the branch
            doctors = doctorRepository.findByPrimaryOrAssociatedBranch(branchId, pageable);
        }

        return doctors.map(doctor -> createDoctorResponseDto(doctor, branchId));
    }

    @Override
    public Map<String, Object> getBranchStatistics(UUID branchId) {
        Map<String, Object> statistics = new HashMap<>();

        // Total doctors in branch
        long totalDoctors = doctorRepository.countByPrimaryBranchId(branchId);
        statistics.put("totalDoctors", totalDoctors);

        // Verified doctors
        long verifiedDoctors = doctorRepository.countByPrimaryBranchIdAndIsVerified(branchId, true);
        statistics.put("verifiedDoctors", verifiedDoctors);

        // Active doctors
        long activeDoctors = doctorRepository.countByPrimaryBranchIdAndStatus(branchId, EntityStatus.ACTIVE);
        statistics.put("activeDoctors", activeDoctors);

        // Doctors by gender
        long maleDoctors = doctorRepository.countByPrimaryBranchIdAndGender(branchId, "MALE");
        long femaleDoctors = doctorRepository.countByPrimaryBranchIdAndGender(branchId, "FEMALE");
        statistics.put("maleDoctors", maleDoctors);
        statistics.put("femaleDoctors", femaleDoctors);

        // Average rating
        Double avgRating = doctorRepository.findAverageRatingByPrimaryBranchId(branchId);
        statistics.put("averageRating", avgRating != null ? avgRating : 0.0);

        // Doctors by experience range
        long experiencedDoctors = doctorRepository.countByPrimaryBranchIdAndExperienceYearsGreaterThanEqual(branchId,
                10);
        long midCareerDoctors = doctorRepository.countByPrimaryBranchIdAndExperienceYearsBetween(branchId, 5, 9);
        long newDoctors = doctorRepository.countByPrimaryBranchIdAndExperienceYearsLessThan(branchId, 5);
        statistics.put("experiencedDoctors", experiencedDoctors);
        statistics.put("midCareerDoctors", midCareerDoctors);
        statistics.put("newDoctors", newDoctors);

        return statistics;
    }

    @Override
    public Map<String, Object> getCurrentUserBranchStatistics() {
        UUID primaryBranchId = securityService.getPrimaryBranchId();
        return getBranchStatistics(primaryBranchId);
    }

    @Override
    public Map<String, Object> getAllBranchesStatistics() {
        Map<String, Object> statistics = new HashMap<>();

        // Get all branches for current user
        List<UUID> branchIds = securityService.getBranchIds();

        // Total doctors across all accessible branches
        long totalDoctors = doctorRepository.countByPrimaryBranchIdIn(branchIds);
        statistics.put("totalDoctors", totalDoctors);

        // Verified doctors across all branches
        long verifiedDoctors = doctorRepository.countByPrimaryBranchIdInAndIsVerified(branchIds, true);
        statistics.put("verifiedDoctors", verifiedDoctors);

        // Active doctors across all branches
        long activeDoctors = doctorRepository.countByPrimaryBranchIdInAndStatus(branchIds, EntityStatus.ACTIVE);
        statistics.put("activeDoctors", activeDoctors);

        // Doctors by gender across all branches
        long maleDoctors = doctorRepository.countByPrimaryBranchIdInAndGender(branchIds, "MALE");
        long femaleDoctors = doctorRepository.countByPrimaryBranchIdInAndGender(branchIds, "FEMALE");
        statistics.put("maleDoctors", maleDoctors);
        statistics.put("femaleDoctors", femaleDoctors);

        // Average rating across all branches
        Double avgRating = doctorRepository.findAverageRatingByPrimaryBranchIdIn(branchIds);
        statistics.put("averageRating", avgRating != null ? avgRating : 0.0);

        return statistics;
    }
}
