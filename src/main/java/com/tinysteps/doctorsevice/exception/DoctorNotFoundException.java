package com.tinysteps.doctorsevice.exception;

import java.util.UUID;

/**
 * Exception thrown when a doctor is not found
 */
public class DoctorNotFoundException extends EntityNotFoundException {

    public DoctorNotFoundException(String message) {
        super(message);
    }

    public DoctorNotFoundException(UUID id) {
        super("Doctor", "id", id.toString());
    }

    public DoctorNotFoundException(String field, String value) {
        super("Doctor", field, value);
    }

    public DoctorNotFoundException(UUID id, Throwable cause) {
        super("Doctor", "id", id.toString(), cause);
    }
}
