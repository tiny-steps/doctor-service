package com.tintsteps.doctorsevice.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpStatus;

import java.util.List;

@Data
@Builder
public class ResponseModel<T> {
    private HttpStatus status;
    private int code;
    private String message;
    private T data;
    private List<ErrorModel> errors;

    // Static factory method for generic builder
    @SuppressWarnings("unchecked")
    public static <T> ResponseModelBuilder<T> builder() {
        return (ResponseModelBuilder<T>) new ResponseModelBuilder<Object>();
    }
}
