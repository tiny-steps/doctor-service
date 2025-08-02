package com.tintsteps.doctorsevice.exception;

/**
 * Base exception for entity not found scenarios
 */
public class EntityNotFoundException extends BaseException {

    public EntityNotFoundException(String message) {
        super(message, "ENTITY_NOT_FOUND", message);
    }

    public EntityNotFoundException(String entityName, String identifier, String value) {
        super(
            String.format("%s not found with %s: %s", entityName, identifier, value),
            "ENTITY_NOT_FOUND",
            String.format("Entity: %s, Identifier: %s, Value: %s", entityName, identifier, value)
        );
    }

    protected EntityNotFoundException(String entityName, String identifier, String value, Throwable cause) {
        super(
            String.format("%s not found with %s: %s", entityName, identifier, value),
            "ENTITY_NOT_FOUND",
            String.format("Entity: %s, Identifier: %s, Value: %s", entityName, identifier, value),
            cause
        );
    }
}
