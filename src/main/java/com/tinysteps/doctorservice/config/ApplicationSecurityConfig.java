package com.tinysteps.doctorservice.config;

import com.tinysteps.doctorservice.entity.*;
import com.tinysteps.doctorservice.exception.EntityNotFoundException;
import com.tinysteps.doctorservice.repository.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("doctorSecurity")
public class ApplicationSecurityConfig {

    private final DoctorRepository doctorRepository;
    private final AwardRepository awardRepository;
    private final MembershipRepository membershipRepository;
    private final OrganizationRepository organizationRepository;
    private final PhotoRepository photoRepository;

    private final PricingRepository pricingRepository;
    private final QualificationRepository qualificationRepository;
    private final RecommendationRepository recommendationRepository;
    private final RegistrationRepository registrationRepository;
    private final SpecializationRepository specializationRepository;

    public ApplicationSecurityConfig(DoctorRepository doctorRepository, AwardRepository awardRepository,
            MembershipRepository membershipRepository, OrganizationRepository organizationRepository,
            PhotoRepository photoRepository, PricingRepository pricingRepository,
            QualificationRepository qualificationRepository, RecommendationRepository recommendationRepository,
            RegistrationRepository registrationRepository, SpecializationRepository specializationRepository) {
        this.doctorRepository = doctorRepository;
        this.awardRepository = awardRepository;
        this.membershipRepository = membershipRepository;
        this.organizationRepository = organizationRepository;
        this.photoRepository = photoRepository;

        this.pricingRepository = pricingRepository;
        this.qualificationRepository = qualificationRepository;
        this.recommendationRepository = recommendationRepository;
        this.registrationRepository = registrationRepository;
        this.specializationRepository = specializationRepository;
    }

    private boolean isOwner(String userId, Doctor doctor) {
        return doctor.getUserId().toString().equals(userId);
    }

    public boolean isDoctorOwner(Authentication authentication, UUID doctorId) {
        String userId = authentication.getName();
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new EntityNotFoundException("Doctor", "id", doctorId.toString()));
        return isOwner(userId, doctor);
    }

    public boolean isAwardOwner(Authentication authentication, UUID awardId) {
        String userId = authentication.getName();
        Award award = awardRepository.findByIdWithDoctor(awardId)
                .orElseThrow(() -> new EntityNotFoundException("Award", "id", awardId.toString()));
        return isOwner(userId, award.getDoctor());
    }

    public boolean isMembershipOwner(Authentication authentication, UUID membershipId) {
        String userId = authentication.getName();
        Membership membership = membershipRepository.findByIdWithDoctor(membershipId)
                .orElseThrow(() -> new EntityNotFoundException("Membership", "id", membershipId.toString()));
        return isOwner(userId, membership.getDoctor());
    }

    public boolean isOrganizationOwner(Authentication authentication, UUID organizationId) {
        String userId = authentication.getName();
        Organization organization = organizationRepository.findByIdWithDoctor(organizationId)
                .orElseThrow(() -> new EntityNotFoundException("Organization", "id", organizationId.toString()));
        return isOwner(userId, organization.getDoctor());
    }

    public boolean isPhotoOwner(Authentication authentication, UUID photoId) {
        String userId = authentication.getName();
        Photo photo = photoRepository.findByIdWithDoctor(photoId)
                .orElseThrow(() -> new EntityNotFoundException("Photo", "id", photoId.toString()));
        return isOwner(userId, photo.getDoctor());
    }



    public boolean isPricingOwner(Authentication authentication, UUID doctorId) {
        // Pricing is directly linked to doctor, so we can reuse isDoctorOwner
        return isDoctorOwner(authentication, doctorId);
    }

    public boolean isQualificationOwner(Authentication authentication, UUID qualificationId) {
        String userId = authentication.getName();
        Qualification qualification = qualificationRepository.findByIdWithDoctor(qualificationId)
                .orElseThrow(() -> new EntityNotFoundException("Qualification", "id", qualificationId.toString()));
        return isOwner(userId, qualification.getDoctor());
    }

    public boolean isRecommendationOwner(Authentication authentication, UUID recommendationId) {
        String userId = authentication.getName();
        Recommendation recommendation = recommendationRepository.findByIdWithDoctor(recommendationId)
                .orElseThrow(() -> new EntityNotFoundException("Recommendation", "id", recommendationId.toString()));
        return isOwner(userId, recommendation.getDoctor());
    }

    public boolean isRegistrationOwner(Authentication authentication, UUID registrationId) {
        String userId = authentication.getName();
        Registration registration = registrationRepository.findByIdWithDoctor(registrationId)
                .orElseThrow(() -> new EntityNotFoundException("Registration", "id", registrationId.toString()));
        return isOwner(userId, registration.getDoctor());
    }

    public boolean isSpecializationOwner(Authentication authentication, UUID specializationId) {
        String userId = authentication.getName();
        Specialization specialization = specializationRepository.findByIdWithDoctor(specializationId)
                .orElseThrow(() -> new EntityNotFoundException("Specialization", "id", specializationId.toString()));
        return isOwner(userId, specialization.getDoctor());
    }
}
