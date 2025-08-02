package com.tintsteps.doctorsevice.mapper;

import com.tintsteps.doctorsevice.entity.Practice;
import com.tintsteps.doctorsevice.model.PracticeRequestDto;
import com.tintsteps.doctorsevice.model.PracticeResponseDto;
import com.tintsteps.doctorsevice.entity.Practice;
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
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PracticeMapper {

    @Mapping(target = "id", source = "id", qualifiedByName = "uuidToString")
    @Mapping(target = "doctorId", source = "doctor.id", qualifiedByName = "uuidToString")
    @Mapping(target = "addressId", source = "addressId", qualifiedByName = "uuidToString")
    @Mapping(target = "createdAt", source = "createdAt", qualifiedByName = "timestampToString")
    PracticeResponseDto toResponseDto(Practice practice);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "doctor", ignore = true)
    @Mapping(target = "addressId", source = "addressId", qualifiedByName = "stringToUuid")
    @Mapping(target = "createdAt", ignore = true)
    Practice fromRequestDto(PracticeRequestDto requestDto);

    List<PracticeResponseDto> toResponseDtos(List<Practice> practices);

    List<Practice> fromRequestDtos(List<PracticeRequestDto> requestDtos);

    // Update entity from DTO
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "doctor", ignore = true)
    @Mapping(target = "addressId", source = "addressId", qualifiedByName = "stringToUuid")
    @Mapping(target = "createdAt", ignore = true)
    void updateEntityFromDto(PracticeRequestDto requestDto, @MappingTarget Practice practice);

    // Helper methods for type conversion
    @Named("uuidToString")
    default String uuidToString(UUID uuid) {
        return uuid != null ? uuid.toString() : null;
    }

    @Named("stringToUuid")
    default UUID stringToUuid(String id) {
        return id != null && !id.isEmpty() ? UUID.fromString(id) : null;
    }

    @Named("timestampToString")
    default String timestampToString(Timestamp timestamp) {
        return timestamp != null ? timestamp.toString() : null;
    }

    @Named("stringToTimestamp")
    default Timestamp stringToTimestamp(String timestamp) {
        return timestamp != null && !timestamp.isEmpty() ? Timestamp.valueOf(timestamp) : null;
    }
}
