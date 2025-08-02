package com.tintsteps.doctorsevice.model;

import lombok.Builder;
import org.springframework.http.HttpStatus;

import java.util.List;

@Builder
public record ResponseModel<T>(
        HttpStatus status,
        int code,
        String message,
        T data,
        List<ErrorModel> errors
) {
}
