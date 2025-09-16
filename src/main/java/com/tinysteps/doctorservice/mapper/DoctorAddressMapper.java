package com.tinysteps.doctorservice.mapper;

import com.tinysteps.doctorservice.entity.DoctorAddress;
import com.tinysteps.doctorservice.entity.PracticeRole;
import com.tinysteps.doctorservice.entity.Status;
import com.tinysteps.doctorservice.model.DoctorAddressRequestDto;
import com.tinysteps.doctorservice.model.DoctorAddressResponseDto;
import org.mapstruct.*;

import java.util.List;
import java.util.UUID;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface DoctorAddressMapper {

    @Mapping(target = "doctorId", source = "doctorId", qualifiedByName = "uuidToString")
    @Mapping(target = "addressId", source = "addressId", qualifiedByName = "uuidToString")
    @Mapping(target = "practiceRole", source = "practiceRole")
    @Mapping(target = "status", source = "status", qualifiedByName = "statusToString")
    @Mapping(target = "createdAt", ignore = true) // Not available in entity
    @Mapping(target = "updatedAt", ignore = true) // Not available in entity
    DoctorAddressResponseDto toResponseDto(DoctorAddress doctorAddress);

    @Mapping(target = "addressId", source = "addressId")
    @Mapping(target = "practiceRole", source = "practiceRole", qualifiedByName = "stringToPracticeRole")
    @Mapping(target = "status", source = "status", qualifiedByName = "stringToStatus")
    @Mapping(target = "doctorId", ignore = true) // Will be set separately
    @Mapping(target = "doctor", ignore = true)
    DoctorAddress fromRequestDto(DoctorAddressRequestDto requestDto);

    List<DoctorAddressResponseDto> toResponseDtos(List<DoctorAddress> doctorAddresses);

    List<DoctorAddress> fromRequestDtos(List<DoctorAddressRequestDto> requestDtos);

    // Update entity from DTO
    @Mapping(target = "addressId", source = "addressId")
    @Mapping(target = "practiceRole", source = "practiceRole", qualifiedByName = "stringToPracticeRole")
    @Mapping(target = "status", source = "status", qualifiedByName = "stringToStatus")
    @Mapping(target = "doctorId", ignore = true)
    @Mapping(target = "doctor", ignore = true)
    void updateEntityFromDto(DoctorAddressRequestDto requestDto, @MappingTarget DoctorAddress doctorAddress);

    // Helper method to convert UUID to String
    @Named("uuidToString")
    default String uuidToString(UUID uuid) {
        return uuid != null ? uuid.toString() : null;
    }

    // Helper method to convert String to UUID
    @Named("stringToUuid")
    default UUID stringToUuid(String str) {
        return str != null ? UUID.fromString(str) : null;
    }

    // Helper method to convert String to PracticeRole
    @Named("stringToPracticeRole")
    default PracticeRole stringToPracticeRole(String str) {
        return str != null ? PracticeRole.valueOf(str.toUpperCase()) : null;
    }

    // Helper method to convert Status to String
    @Named("statusToString")
    default String statusToString(Status status) {
        return status != null ? status.name() : null;
    }

    // Helper method to convert String to Status
    @Named("stringToStatus")
    default Status stringToStatus(String str) {
        return str != null && !str.trim().isEmpty() ? Status.valueOf(str.toUpperCase()) : Status.ACTIVE;
    }
}
