package com.tinysteps.doctorsevice.mapper;

import com.tinysteps.doctorsevice.entity.Specialization;
import com.tinysteps.doctorsevice.model.SpecializationRequestDto;
import com.tinysteps.doctorsevice.model.SpecializationResponseDto;
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
public interface SpecializationMapper {

    @Mapping(target = "id", source = "id", qualifiedByName = "uuidToString")
    @Mapping(target = "doctorId", source = "doctor.id", qualifiedByName = "uuidToString")
    SpecializationResponseDto toResponseDto(Specialization specialization);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "doctor", ignore = true)
    Specialization fromRequestDto(SpecializationRequestDto requestDto);

    List<SpecializationResponseDto> toResponseDtos(List<Specialization> specializations);

    List<Specialization> fromRequestDtos(List<SpecializationRequestDto> requestDtos);

    // Update entity from DTO
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "doctor", ignore = true)
    void updateEntityFromDto(SpecializationRequestDto requestDto, @MappingTarget Specialization specialization);

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
