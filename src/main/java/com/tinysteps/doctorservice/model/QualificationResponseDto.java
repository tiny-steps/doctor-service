package com.tinysteps.doctorservice.model;

import lombok.Builder;

@Builder
public record QualificationResponseDto(
        String id,
        String doctorId,
        String qualificationName,
        String collegeName,
        Integer completionYear
) {
}
