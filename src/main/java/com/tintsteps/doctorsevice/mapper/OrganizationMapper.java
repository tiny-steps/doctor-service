package com.tintsteps.doctorsevice.mapper;

import com.tintsteps.doctorsevice.model.OrganizationRequestDto;
import com.tintsteps.doctorsevice.model.OrganizationResponseDto;
import com.tintsteps.doctorsevice.entity.Organization;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.sql.Date;
import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring", 
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface OrganizationMapper {
    
    @Mapping(target = "id", source = "id", qualifiedByName = "uuidToString")
    @Mapping(target = "doctorId", source = "doctor.id", qualifiedByName = "uuidToString")
    @Mapping(target = "tenureStart", source = "tenureStart", qualifiedByName = "dateToString")
    @Mapping(target = "tenureEnd", source = "tenureEnd", qualifiedByName = "dateToString")
    OrganizationResponseDto toResponseDto(Organization organization);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "doctor", ignore = true)
    @Mapping(target = "tenureStart", source = "tenureStart", qualifiedByName = "stringToDate")
    @Mapping(target = "tenureEnd", source = "tenureEnd", qualifiedByName = "stringToDate")
    Organization fromRequestDto(OrganizationRequestDto requestDto);
    
    List<OrganizationResponseDto> toResponseDtos(List<Organization> organizations);
    
    List<Organization> fromRequestDtos(List<OrganizationRequestDto> requestDtos);
    
    // Update entity from DTO
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "doctor", ignore = true)
    @Mapping(target = "tenureStart", source = "tenureStart", qualifiedByName = "stringToDate")
    @Mapping(target = "tenureEnd", source = "tenureEnd", qualifiedByName = "stringToDate")
    void updateEntityFromDto(OrganizationRequestDto requestDto, @MappingTarget Organization organization);
    
    // Helper methods for type conversion
    @Named("uuidToString")
    default String uuidToString(UUID uuid) {
        return uuid != null ? uuid.toString() : null;
    }
    
    @Named("stringToUuid")
    default UUID stringToUuid(String id) {
        return id != null && !id.isEmpty() ? UUID.fromString(id) : null;
    }
    
    @Named("dateToString")
    default String dateToString(Date date) {
        return date != null ? date.toString() : null;
    }
    
    @Named("stringToDate")
    default Date stringToDate(String date) {
        return date != null && !date.isEmpty() ? Date.valueOf(date) : null;
    }
}
