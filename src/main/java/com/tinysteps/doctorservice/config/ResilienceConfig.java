package com.tinysteps.doctorservice.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;

/**
 * Configuration for Resilience4j components used in external service
 * integrations
 */
@Configuration
public class ResilienceConfig {

    // User Service Resilience Components
    @Bean
    public CircuitBreaker userServiceCircuitBreaker(CircuitBreakerRegistry registry) {
        return registry.circuitBreaker("ts-user-service");
    }

    @Bean
    public Retry userServiceRetry(RetryRegistry registry) {
        return registry.retry("ts-user-service");
    }

    @Bean
    public TimeLimiter userServiceTimeLimiter(TimeLimiterRegistry registry) {
        return registry.timeLimiter("ts-user-service");
    }

    // Address Service Resilience Components
    @Bean
    public CircuitBreaker addressServiceCircuitBreaker(CircuitBreakerRegistry registry) {
        return registry.circuitBreaker("ts-address-service");
    }

    @Bean
    public Retry addressServiceRetry(RetryRegistry registry) {
        return registry.retry("ts-address-service");
    }

    @Bean
    public TimeLimiter addressServiceTimeLimiter(TimeLimiterRegistry registry) {
        return registry.timeLimiter("ts-address-service");
    }

    // Session Service Resilience Components
    @Bean
    public CircuitBreaker sessionServiceCircuitBreaker(CircuitBreakerRegistry registry) {
        return registry.circuitBreaker("ts-session-service");
    }

    @Bean
    public Retry sessionServiceRetry(RetryRegistry registry) {
        return registry.retry("ts-session-service");
    }

    @Bean
    public TimeLimiter sessionServiceTimeLimiter(TimeLimiterRegistry registry) {
        return registry.timeLimiter("ts-session-service");
    }

    // Auth Service Resilience Components
    @Bean
    public CircuitBreaker authServiceCircuitBreaker(CircuitBreakerRegistry registry) {
        // Create custom circuit breaker config that ignores 4xx client errors
        CircuitBreakerConfig customConfig = CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .slidingWindowSize(10)
                .minimumNumberOfCalls(5)
                .waitDurationInOpenState(Duration.ofSeconds(10))
                .permittedNumberOfCallsInHalfOpenState(2)
                .ignoreException(throwable -> {
                    // Don't count 4xx client errors as failures
                    if (throwable instanceof WebClientResponseException) {
                        WebClientResponseException webEx = (WebClientResponseException) throwable;
                        return webEx.getStatusCode().is4xxClientError();
                    }
                    return false;
                })
                .build();

        return registry.circuitBreaker("auth-service", customConfig);
    }

    @Bean
    public Retry authServiceRetry(RetryRegistry registry) {
        return registry.retry("auth-service");
    }

    @Bean
    public TimeLimiter authServiceTimeLimiter(TimeLimiterRegistry registry) {
        return registry.timeLimiter("auth-service");
    }
}
