package com.tinysteps.doctorsevice.mapper;

import com.tinysteps.doctorsevice.model.DoctorRequestDto;
import com.tinysteps.doctorsevice.model.DoctorResponseDto;
import com.tinysteps.doctorsevice.entity.Doctor;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        uses = {
            AwardMapper.class,
            QualificationMapper.class,
            MembershipMapper.class,
            OrganizationMapper.class,
            RegistrationMapper.class,
            PricingMapper.class,
            SpecializationMapper.class,
            PhotoMapper.class,

            RecommendationMapper.class
        })
public interface DoctorMapper {

    @Mapping(target = "id", source = "id", qualifiedByName = "doctorUuidToString")
    @Mapping(target = "userId", source = "userId", qualifiedByName = "doctorUuidToString")
    @Mapping(target = "primaryBranchId", source = "primaryBranchId", qualifiedByName = "doctorUuidToString")
    @Mapping(target = "createdAt", source = "createdAt", qualifiedByName = "doctorTimestampToString")
    @Mapping(target = "updatedAt", source = "updatedAt", qualifiedByName = "doctorTimestampToString")
    DoctorResponseDto toResponseDto(Doctor doctor);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "name", source = "name")
    @Mapping(target = "userId", source = "userId", qualifiedByName = "doctorStringToUuid")
    @Mapping(target = "primaryBranchId", source = "primaryBranchId", qualifiedByName = "doctorStringToUuid")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "awards", ignore = true)
    @Mapping(target = "qualifications", ignore = true)
    @Mapping(target = "memberships", ignore = true)
    @Mapping(target = "organizations", ignore = true)
    @Mapping(target = "registrations", ignore = true)
    @Mapping(target = "sessionPricings", ignore = true)
    @Mapping(target = "specializations", ignore = true)
    @Mapping(target = "photos", ignore = true)
    @Mapping(target = "doctorAddresses", ignore = true)
    @Mapping(target = "recommendations", ignore = true)
    Doctor fromRequestDto(DoctorRequestDto requestDto);

    List<DoctorResponseDto> toResponseDtos(List<Doctor> doctors);

    List<Doctor> fromRequestDtos(List<DoctorRequestDto> requestDtos);

    // Update entity from DTO
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "awards", ignore = true)
    @Mapping(target = "qualifications", ignore = true)
    @Mapping(target = "memberships", ignore = true)
    @Mapping(target = "organizations", ignore = true)
    @Mapping(target = "registrations", ignore = true)
    @Mapping(target = "sessionPricings", ignore = true)
    @Mapping(target = "specializations", ignore = true)
    @Mapping(target = "photos", ignore = true)
    @Mapping(target = "doctorAddresses", ignore = true)
    @Mapping(target = "recommendations", ignore = true)
    void updateEntityFromDto(DoctorRequestDto requestDto, @MappingTarget Doctor doctor);

    // Helper methods for type conversion
    @Named("doctorUuidToString")
    default String doctorUuidToString(UUID uuid) {
        return uuid != null ? uuid.toString() : null;
    }

    @Named("doctorStringToUuid")
    default UUID doctorStringToUuid(String id) {
        return id != null && !id.isEmpty() ? UUID.fromString(id) : null;
    }

    @Named("doctorTimestampToString")
    default String doctorTimestampToString(Timestamp timestamp) {
        return timestamp != null ? timestamp.toString() : null;
    }

    @Named("doctorStringToTimestamp")
    default Timestamp doctorStringToTimestamp(String timestamp) {
        return timestamp != null && !timestamp.isEmpty() ? Timestamp.valueOf(timestamp) : null;
    }
}
