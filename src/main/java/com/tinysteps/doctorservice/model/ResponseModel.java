package com.tinysteps.doctorservice.model;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.List;

@Data
@Builder
@Getter
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

    // Success response with data
    public static <T> ResponseModel<T> success(String message, T data) {
        return ResponseModel.<T>builder()
                .status(HttpStatus.OK)
                .code(HttpStatus.OK.value())
                .message(message)
                .data(data)
                .build();
    }

    // Success response without data
    public static <T> ResponseModel<T> success(String message) {
        return ResponseModel.<T>builder()
                .status(HttpStatus.OK)
                .code(HttpStatus.OK.value())
                .message(message)
                .build();
    }

    // Created response
    public static <T> ResponseModel<T> created(String message, T data) {
        return ResponseModel.<T>builder()
                .status(HttpStatus.CREATED)
                .code(HttpStatus.CREATED.value())
                .message(message)
                .data(data)
                .build();
    }

    // Error response
    public static <T> ResponseModel<T> error(HttpStatus status, String message, List<ErrorModel> errors) {
        return ResponseModel.<T>builder()
                .status(status)
                .code(status.value())
                .message(message)
                .errors(errors)
                .build();
    }
}
