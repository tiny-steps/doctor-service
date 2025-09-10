package com.tinysteps.doctorservice.mapper;

import com.tinysteps.doctorservice.model.PricingRequestDto;
import com.tinysteps.doctorservice.model.PricingResponseDto;
import com.tinysteps.doctorservice.entity.Pricing;
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
public interface PricingMapper {

    @Mapping(target = "id", source = "id", qualifiedByName = "uuidToString")
    @Mapping(target = "doctorId", source = "doctor.id", qualifiedByName = "uuidToString")
    @Mapping(target = "sessionTypeId", source = "sessionTypeId", qualifiedByName = "uuidToString")
    PricingResponseDto toResponseDto(Pricing pricing);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "doctor", ignore = true)
    @Mapping(target = "sessionTypeId", source = "sessionTypeId", qualifiedByName = "stringToUuid")
    Pricing fromRequestDto(PricingRequestDto requestDto);

    List<PricingResponseDto> toResponseDtos(List<Pricing> pricings);

    List<Pricing> fromRequestDtos(List<PricingRequestDto> requestDtos);

    // Update entity from DTO
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "doctor", ignore = true)
    @Mapping(target = "sessionTypeId", source = "sessionTypeId", qualifiedByName = "stringToUuid")
    void updateEntityFromDto(PricingRequestDto requestDto, @MappingTarget Pricing pricing);

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
