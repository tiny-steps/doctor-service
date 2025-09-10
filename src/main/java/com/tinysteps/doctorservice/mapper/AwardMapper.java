package com.tinysteps.doctorservice.mapper;

import com.tinysteps.doctorservice.model.AwardRequestDto;
import com.tinysteps.doctorservice.model.AwardResponseDto;
import com.tinysteps.doctorservice.entity.Award;
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
public interface AwardMapper {

    @Mapping(target = "id", source = "id", qualifiedByName = "uuidToString")
    @Mapping(target = "doctorId", source = "doctor.id", qualifiedByName = "uuidToString")
    AwardResponseDto toResponseDto(Award award);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "doctor", ignore = true)
    Award fromRequestDto(AwardRequestDto requestDto);

    List<AwardResponseDto> toResponseDtos(List<Award> awards);

    List<Award> fromRequestDtos(List<AwardRequestDto> requestDtos);

    // Update entity from DTO
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "doctor", ignore = true)
    void updateEntityFromDto(AwardRequestDto requestDto, @MappingTarget Award award);

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
