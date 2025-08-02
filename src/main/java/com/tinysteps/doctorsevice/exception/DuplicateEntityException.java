package com.tinysteps.doctorsevice.exception;

/**
 * Exception thrown when attempting to create a duplicate entity
 */
public class DuplicateEntityException extends BaseException {

    public DuplicateEntityException(String entityName, String field, String value) {
        super(
            String.format("%s already exists with %s: %s", entityName, field, value),
            "DUPLICATE_ENTITY",
            String.format("Entity: %s, Field: %s, Value: %s", entityName, field, value)
        );
    }

    public DuplicateEntityException(String entityName, String field, String value, Throwable cause) {
        super(
            String.format("%s already exists with %s: %s", entityName, field, value),
            "DUPLICATE_ENTITY",
            String.format("Entity: %s, Field: %s, Value: %s", entityName, field, value),
            cause
        );
    }
}
