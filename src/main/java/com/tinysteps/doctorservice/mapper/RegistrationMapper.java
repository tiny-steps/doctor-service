package com.tinysteps.doctorservice.mapper;

import com.tinysteps.doctorservice.model.RegistrationRequestDto;
import com.tinysteps.doctorservice.model.RegistrationResponseDto;
import com.tinysteps.doctorservice.entity.Registration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface RegistrationMapper {

    @Mapping(target = "id", source = "id", qualifiedByName = "uuidToString")
    @Mapping(target = "doctorId", source = "doctor.id", qualifiedByName = "uuidToString")
    RegistrationResponseDto toResponseDto(Registration registration);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "doctor", ignore = true)
    Registration fromRequestDto(RegistrationRequestDto requestDto);

    List<RegistrationResponseDto> toResponseDtos(List<Registration> registrations);

    List<Registration> fromRequestDtos(List<RegistrationRequestDto> requestDtos);

    // Update entity from DTO
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "doctor", ignore = true)
    void updateEntityFromDto(RegistrationRequestDto requestDto, @MappingTarget Registration registration);

    // Helper methods for type conversion
    @Named("uuidToString")
    default String uuidToString(UUID uuid) {
        return uuid != null ? uuid.toString() : null;
    }

    @Named("stringToUuid")
    default UUID stringToUuid(String id) {
        return id != null && !id.isEmpty() ? UUID.fromString(id) : null;
    }
}
