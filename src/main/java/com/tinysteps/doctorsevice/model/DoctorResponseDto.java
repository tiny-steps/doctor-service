package com.tinysteps.doctorsevice.model;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Builder
public record DoctorResponseDto(
        String id,
        String userId,
        String name,
        String email,
        String phone,
        String slug,
        String gender,
        String summary,
        String about,
        String imageUrl,
        Integer experienceYears,
        Boolean isVerified,
        BigDecimal ratingAverage,
        Integer reviewCount,
        String status,
        String createdAt,
        String updatedAt,
        List<AwardResponseDto> awards,
        List<QualificationResponseDto> qualifications,
        List<MembershipResponseDto> memberships,
        List<OrganizationResponseDto> organizations,
        List<RegistrationResponseDto> registrations,
        List<PricingResponseDto> sessionPricings,
        List<SpecializationResponseDto> specializations,
        List<PhotoResponseDto> photos,
        List<PracticeResponseDto> practices,
        List<RecommendationResponseDto> recommendations
) {
}
