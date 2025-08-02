package com.tintsteps.doctorsevice.integration.service;

import com.tintsteps.doctorsevice.exception.IntegrationException;
import com.tintsteps.doctorsevice.integration.model.IntegrationResponseModel;
import com.tintsteps.doctorsevice.integration.model.SessionTypeIntegrationModel;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import io.github.resilience4j.reactor.retry.RetryOperator;
import io.github.resilience4j.reactor.timelimiter.TimeLimiterOperator;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.timelimiter.TimeLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Service for integrating with Session Service
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SessionIntegrationService {

    private final WebClient publicWebClient;
    private final Retry sessionServiceRetry;
    private final CircuitBreaker sessionServiceCircuitBreaker;
    private final TimeLimiter sessionServiceTimeLimiter;

    @Value("${integration.session-service.base-url:http://ts-session-service/api/v1/session-types}")
    private String sessionServiceBaseUrl;

    /**
     * Validates if a session type exists and is active
     *
     * @param sessionTypeId the session type ID to validate
     * @return true if session type exists and is active
     * @throws IntegrationException if integration fails
     */
    public boolean validateSessionType(UUID sessionTypeId) {
        try {
            log.debug("Validating session type with ID: {}", sessionTypeId);

            SessionTypeIntegrationModel sessionType = getSessionTypeById(sessionTypeId).block();
            boolean isValid = sessionType != null && sessionType.isActive();

            log.debug("Session type validation result for ID {}: exists={}, active={}",
                    sessionTypeId, sessionType != null, isValid);

            return isValid;

        } catch (Exception e) {
            log.error("Failed to validate session type with ID: {}", sessionTypeId, e);
            throw new IntegrationException("Session Service",
                    "Failed to validate session type: " + e.getMessage(), e);
        }
    }

    /**
     * Retrieves session type information by ID
     *
     * @param sessionTypeId the session type ID
     * @return session type information
     * @throws IntegrationException if session type not found or integration fails
     */
    public Mono<SessionTypeIntegrationModel> getSessionTypeById(UUID sessionTypeId) {
        log.debug("Fetching session type information for ID: {}", sessionTypeId);

        return publicWebClient.get()
                .uri(sessionServiceBaseUrl + "/{id}", sessionTypeId)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<IntegrationResponseModel<SessionTypeIntegrationModel>>() {})
                .map(IntegrationResponseModel::data)
                .transformDeferred(RetryOperator.of(sessionServiceRetry))
                .transformDeferred(CircuitBreakerOperator.of(sessionServiceCircuitBreaker))
                .transformDeferred(TimeLimiterOperator.of(sessionServiceTimeLimiter))
                .doOnSuccess(sessionType -> log.debug("Successfully fetched session type information for ID: {}", sessionTypeId))
                .onErrorMap(throwable -> {
                    log.error("Failed to fetch session type with ID: {}", sessionTypeId, throwable);
                    return new IntegrationException("Session Service",
                            "Failed to fetch session type information: " + throwable.getMessage(), throwable);
                });
    }

    /**
     * Checks if a session type exists
     *
     * @param sessionTypeId the session type ID to check
     * @return true if session type exists
     */
    public boolean sessionTypeExists(UUID sessionTypeId) {
        try {
            SessionTypeIntegrationModel sessionType = getSessionTypeById(sessionTypeId).block();
            return sessionType != null;
        } catch (IntegrationException e) {
            log.debug("Session type with ID {} does not exist", sessionTypeId);
            return false;
        }
    }

    /**
     * Validates if a session type is active
     *
     * @param sessionTypeId the session type ID to check
     * @return true if session type is active
     */
    public boolean isSessionTypeActive(UUID sessionTypeId) {
        try {
            SessionTypeIntegrationModel sessionType = getSessionTypeById(sessionTypeId).block();
            return sessionType != null && sessionType.isActive();
        } catch (IntegrationException e) {
            log.debug("Session type with ID {} is not active or does not exist", sessionTypeId);
            return false;
        }
    }
}
