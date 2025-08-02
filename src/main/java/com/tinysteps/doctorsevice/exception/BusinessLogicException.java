package com.tinysteps.doctorsevice.exception;

/**
 * Exception thrown when business logic rules are violated
 */
public class BusinessLogicException extends BaseException {

    public BusinessLogicException(String message) {
        super(message, "BUSINESS_LOGIC_VIOLATION", message);
    }

    public BusinessLogicException(String message, String details) {
        super(message, "BUSINESS_LOGIC_VIOLATION", details);
    }

    public BusinessLogicException(String message, String details, Throwable cause) {
        super(message, "BUSINESS_LOGIC_VIOLATION", details, cause);
    }
}
