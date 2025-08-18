package com.tinysteps.doctorsevice.integration.service;

import com.tinysteps.doctorsevice.dto.UserRegistrationRequest;
import com.tinysteps.doctorsevice.dto.UserRegistrationResponse;
import com.tinysteps.doctorsevice.integration.model.UserModel;
import com.tinysteps.doctorsevice.model.ResponseModel;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import io.github.resilience4j.reactor.retry.RetryOperator;
import io.github.resilience4j.reactor.timelimiter.TimeLimiterOperator;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceIntegration {

    private final WebClient publicWebClient;
    private final io.github.resilience4j.retry.Retry authServiceRetry;
    private final io.github.resilience4j.circuitbreaker.CircuitBreaker authServiceCircuitBreaker;
    private final io.github.resilience4j.timelimiter.TimeLimiter authServiceTimeLimiter;

    @Value("${services.auth-service.base-url:http://ts-auth-service}")
    private String authServiceBaseUrl;

    @CircuitBreaker(name = "ts-auth-service", fallbackMethod = "registerUserFallback")
    @Retry(name = "ts-auth-service")
    @TimeLimiter(name = "ts-auth-service")
    public Mono<UserModel> registerUser(UserRegistrationRequest registrationRequest) {
        log.info("Registering user via auth-service with email: {}", registrationRequest.getEmail());

        return publicWebClient.post()
                .uri(authServiceBaseUrl+"/api/auth/register")
                .bodyValue(registrationRequest)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ResponseModel<UserModel>>() {})
                .map(ResponseModel::getData)
                .transformDeferred(RetryOperator.of(authServiceRetry))
                .transformDeferred(CircuitBreakerOperator.of(authServiceCircuitBreaker))
                .transformDeferred(TimeLimiterOperator.of(authServiceTimeLimiter))
                .onErrorMap(throwable -> new AuthenticationServiceException("User service is unavailable", throwable));
    }

    // Fallback method
    public CompletableFuture<UserRegistrationResponse> registerUserFallback(UserRegistrationRequest registrationRequest, Exception ex) {
        log.error("Auth service fallback triggered for registerUser: {}, error: {}", registrationRequest.getEmail(), ex.getMessage());
        throw new RuntimeException("User registration failed - auth service unavailable", ex);
    }
}
