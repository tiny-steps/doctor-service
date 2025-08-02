package com.tintsteps.doctorsevice.exception;

import java.util.UUID;

/**
 * Collection of entity-specific not found exceptions
 */
public class EntityNotFoundExceptions {
    
    public static class AwardNotFoundException extends EntityNotFoundException {
        public AwardNotFoundException(UUID id) {
            super("Award", "id", id.toString());
        }
        
        public AwardNotFoundException(String field, String value) {
            super("Award", field, value);
        }
    }
    
    public static class QualificationNotFoundException extends EntityNotFoundException {
        public QualificationNotFoundException(UUID id) {
            super("Qualification", "id", id.toString());
        }
        
        public QualificationNotFoundException(String field, String value) {
            super("Qualification", field, value);
        }
    }
    
    public static class MembershipNotFoundException extends EntityNotFoundException {
        public MembershipNotFoundException(UUID id) {
            super("Membership", "id", id.toString());
        }
        
        public MembershipNotFoundException(String field, String value) {
            super("Membership", field, value);
        }
    }
    
    public static class OrganizationNotFoundException extends EntityNotFoundException {
        public OrganizationNotFoundException(UUID id) {
            super("Organization", "id", id.toString());
        }
        
        public OrganizationNotFoundException(String field, String value) {
            super("Organization", field, value);
        }
    }
    
    public static class RegistrationNotFoundException extends EntityNotFoundException {
        public RegistrationNotFoundException(UUID id) {
            super("Registration", "id", id.toString());
        }
        
        public RegistrationNotFoundException(String field, String value) {
            super("Registration", field, value);
        }
    }
    
    public static class PricingNotFoundException extends EntityNotFoundException {
        public PricingNotFoundException(UUID id) {
            super("Pricing", "id", id.toString());
        }
        
        public PricingNotFoundException(String field, String value) {
            super("Pricing", field, value);
        }
    }
    
    public static class SpecializationNotFoundException extends EntityNotFoundException {
        public SpecializationNotFoundException(UUID id) {
            super("Specialization", "id", id.toString());
        }
        
        public SpecializationNotFoundException(String field, String value) {
            super("Specialization", field, value);
        }
    }
    
    public static class PhotoNotFoundException extends EntityNotFoundException {
        public PhotoNotFoundException(UUID id) {
            super("Photo", "id", id.toString());
        }
        
        public PhotoNotFoundException(String field, String value) {
            super("Photo", field, value);
        }
    }
    
    public static class PracticeNotFoundException extends EntityNotFoundException {
        public PracticeNotFoundException(UUID id) {
            super("Practice", "id", id.toString());
        }
        
        public PracticeNotFoundException(String field, String value) {
            super("Practice", field, value);
        }
    }
    
    public static class RecommendationNotFoundException extends EntityNotFoundException {
        public RecommendationNotFoundException(UUID id) {
            super("Recommendation", "id", id.toString());
        }
        
        public RecommendationNotFoundException(String field, String value) {
            super("Recommendation", field, value);
        }
    }
}
