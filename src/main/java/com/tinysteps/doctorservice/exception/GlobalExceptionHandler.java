package com.tinysteps.doctorservice.exception;

import com.tinysteps.doctorservice.model.ErrorModel;
import com.tinysteps.doctorservice.model.ResponseModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.ArrayList;
import java.util.List;

/**
 * Global exception handler for the doctor service
 * Handles all exceptions and returns consistent error responses
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(DoctorNotFoundException.class)
    public ResponseEntity<ResponseModel<?>> handleDoctorNotFoundException(DoctorNotFoundException ex) {
        log.error("Doctor not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ResponseModel.builder()
                        .status(HttpStatus.NOT_FOUND)
                        .code(HttpStatus.NOT_FOUND.value())
                        .message(ex.getMessage())
                        .errors(List.of(new ErrorModel("NOT_FOUND", ex.getDetails())))
                        .build());
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ResponseModel<?>> handleEntityNotFoundException(EntityNotFoundException ex) {
        log.error("Entity not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ResponseModel.builder()
                        .status(HttpStatus.NOT_FOUND)
                        .code(HttpStatus.NOT_FOUND.value())
                        .message(ex.getMessage())
                        .errors(List.of(new ErrorModel("NOT_FOUND", ex.getDetails())))
                        .build());
    }

    @ExceptionHandler(DuplicateEntityException.class)
    public ResponseEntity<ResponseModel<?>> handleDuplicateEntityException(DuplicateEntityException ex) {
        log.error("Duplicate entity: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ResponseModel.builder()
                        .status(HttpStatus.CONFLICT)
                        .code(HttpStatus.CONFLICT.value())
                        .message(ex.getMessage())
                        .errors(List.of(new ErrorModel("DUPLICATE_ENTITY", ex.getDetails())))
                        .build());
    }

    @ExceptionHandler(InvalidDataException.class)
    public ResponseEntity<ResponseModel<?>> handleInvalidDataException(InvalidDataException ex) {
        log.error("Invalid data: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ResponseModel.builder()
                        .status(HttpStatus.BAD_REQUEST)
                        .code(HttpStatus.BAD_REQUEST.value())
                        .message(ex.getMessage())
                        .errors(List.of(new ErrorModel("INVALID_DATA", ex.getDetails())))
                        .build());
    }

    @ExceptionHandler(BusinessLogicException.class)
    public ResponseEntity<ResponseModel<?>> handleBusinessLogicException(BusinessLogicException ex) {
        log.error("Business logic error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ResponseModel.builder()
                        .status(HttpStatus.UNPROCESSABLE_ENTITY)
                        .code(HttpStatus.UNPROCESSABLE_ENTITY.value())
                        .message(ex.getMessage())
                        .errors(List.of(new ErrorModel("BUSINESS_LOGIC_ERROR", ex.getDetails())))
                        .build());
    }

    @ExceptionHandler(IntegrationException.class)
    public ResponseEntity<ResponseModel<?>> handleIntegrationException(IntegrationException ex) {
        log.error("Integration error: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(ResponseModel.builder()
                        .status(HttpStatus.BAD_GATEWAY)
                        .code(HttpStatus.BAD_GATEWAY.value())
                        .message(ex.getMessage())
                        .errors(List.of(new ErrorModel("INTEGRATION_ERROR", ex.getDetails())))
                        .build());
    }

    @ExceptionHandler(WebClientResponseException.class)
    public ResponseEntity<ResponseModel<?>> handleWebClientResponseException(WebClientResponseException ex) {
        log.error("WebClient error: {} - {}", ex.getStatusCode(), ex.getResponseBodyAsString());

        // Try to extract meaningful error message from the response
        String errorMessage = "An error occurred while communicating with external service";
        String details = ex.getResponseBodyAsString();

        // Parse common error scenarios
        if (details != null) {
            if (details.contains("email address already exists") || details.contains("users_email_key")) {
                errorMessage = "A doctor with this email address already exists. Please use a different email.";
            } else if (details.contains("phone number already exists") || details.contains("users_phone_key")) {
                errorMessage = "A doctor with this phone number already exists. Please use a different phone number.";
            } else if (details.contains("already exists")) {
                errorMessage = "A record with this information already exists. Please check your input.";
            }
        }

        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        return ResponseEntity.status(status)
                .body(ResponseModel.builder()
                        .status(status)
                        .code(status.value())
                        .message(errorMessage)
                        .errors(List.of(new ErrorModel("EXTERNAL_SERVICE_ERROR", details)))
                        .build());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ResponseModel<?>> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        log.error("Data integrity violation: {}", ex.getMessage());

        String message = "A record with this information already exists";
        String details = ex.getMessage();

        // Parse constraint violations for user-friendly messages
        if (details != null) {
            if (details.contains("email") || details.contains("unique_email")) {
                message = "A doctor with this email address already exists";
            } else if (details.contains("phone") || details.contains("unique_phone")) {
                message = "A doctor with this phone number already exists";
            } else if (details.contains("slug") || details.contains("unique_slug")) {
                message = "A doctor with this slug already exists";
            }
        }

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ResponseModel.builder()
                        .status(HttpStatus.CONFLICT)
                        .code(HttpStatus.CONFLICT.value())
                        .message(message)
                        .errors(List.of(new ErrorModel("DUPLICATE_ENTRY", details)))
                        .build());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseModel<?>> handleValidationException(MethodArgumentNotValidException ex) {
        log.error("Validation error: {}", ex.getMessage());

        List<ErrorModel> errors = new ArrayList<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.add(new ErrorModel(error.getField(), error.getDefaultMessage()));
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ResponseModel.builder()
                        .status(HttpStatus.BAD_REQUEST)
                        .code(HttpStatus.BAD_REQUEST.value())
                        .message("Validation failed")
                        .errors(errors)
                        .build());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ResponseModel<?>> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.error("Illegal argument: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ResponseModel.builder()
                        .status(HttpStatus.BAD_REQUEST)
                        .code(HttpStatus.BAD_REQUEST.value())
                        .message(ex.getMessage())
                        .errors(List.of(new ErrorModel("INVALID_ARGUMENT", ex.getMessage())))
                        .build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseModel<?>> handleGeneralException(Exception ex) {
        log.error("Unexpected error occurred", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseModel.builder()
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .message("An unexpected error occurred. Please try again later.")
                        .errors(List.of(new ErrorModel("INTERNAL_ERROR", ex.getMessage())))
                        .build());
    }
}
