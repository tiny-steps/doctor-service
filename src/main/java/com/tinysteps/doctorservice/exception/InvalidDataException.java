package com.tinysteps.doctorservice.exception;

/**
 * Exception thrown when invalid data is provided
 */
public class InvalidDataException extends BaseException {

    public InvalidDataException(String message) {
        super(message, "INVALID_DATA", message);
    }

    public InvalidDataException(String message, String details) {
        super(message, "INVALID_DATA", details);
    }

    public InvalidDataException(String message, String details, Throwable cause) {
        super(message, "INVALID_DATA", details, cause);
    }
}
