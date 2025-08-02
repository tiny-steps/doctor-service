package com.tinysteps.doctorsevice.model;

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
