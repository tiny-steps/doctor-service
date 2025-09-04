package com.tinysteps.doctorsevice.mapper;

import com.tinysteps.doctorsevice.entity.DoctorAddress;
import com.tinysteps.doctorsevice.model.DoctorAddressRequestDto;
import com.tinysteps.doctorsevice.model.DoctorAddressResponseDto;
import org.mapstruct.*;

import java.util.List;
import java.util.UUID;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface DoctorAddressMapper {

    @Mapping(target = "doctorId", source = "id.doctorId", qualifiedByName = "uuidToString")
    @Mapping(target = "addressId", source = "id.addressId", qualifiedByName = "uuidToString")
    @Mapping(target = "practiceRole", source = "id.practiceRole")
    DoctorAddressResponseDto toResponseDto(DoctorAddress doctorAddress);

    @Mapping(target = "id.addressId", source = "addressId")
    @Mapping(target = "id.practiceRole", source = "practiceRole")
    @Mapping(target = "id.doctorId", ignore = true) // Will be set separately
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    DoctorAddress fromRequestDto(DoctorAddressRequestDto requestDto);

    List<DoctorAddressResponseDto> toResponseDtos(List<DoctorAddress> doctorAddresses);

    List<DoctorAddress> fromRequestDtos(List<DoctorAddressRequestDto> requestDtos);

    // Update entity from DTO
    @Mapping(target = "id.addressId", source = "addressId")
    @Mapping(target = "id.practiceRole", source = "practiceRole")
    @Mapping(target = "id.doctorId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
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
}