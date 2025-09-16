package com.tinysteps.doctorservice.exception;

/**
 * Exception thrown when attempting to perform soft delete operations
 * on doctors with invalid states or configurations.
 */
public class DoctorSoftDeleteException extends RuntimeException {

    public DoctorSoftDeleteException(String message) {
        super(message);
    }

    public DoctorSoftDeleteException(String message, Throwable cause) {
        super(message, cause);
    }
}