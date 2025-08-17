package com.tinysteps.doctorsevice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    // 1. Inject the internal API secret from your application properties
    @Value("${internal.api.secret}")
    private String internalApiSecret;

    @Bean
    @LoadBalanced
    public WebClient.Builder loadBalancedWebClientBuilder() {
        // 2. Add the X-Internal-Secret header to the base builder.
        // This ensures ALL WebClient instances created from this builder
        // will have the secret header for secure inter-service communication.
        return WebClient.builder()
                .defaultHeader("X-Internal-Secret", internalApiSecret);
    }

    /**
     * A WebClient for calling public, unsecured endpoints.
     * It uses the load-balanced builder, which now automatically includes the internal secret header.
     */
    @Bean
    public WebClient publicWebClient(WebClient.Builder loadBalancedWebClientBuilder) {
        return loadBalancedWebClientBuilder.build();
    }

    /**
     * A WebClient for calling secure, token-protected endpoints.
     * It adds an ExchangeFilterFunction to propagate the user's JWT from the
     * current security context, in addition to the internal secret header provided by the builder.
     */
    @Bean
    public WebClient secureWebClient(WebClient.Builder loadBalancedWebClientBuilder) {
        return loadBalancedWebClientBuilder
                .filter(jwtPropagationFilter())
                .build();
    }

    /**
     * Creates a filter that intercepts requests to add the Authorization header with the user's JWT.
     * It retrieves the JWT from the reactive security context.
     * @return An ExchangeFilterFunction that adds a Bearer token.
     */
    private ExchangeFilterFunction jwtPropagationFilter() {
        return (request, next) -> ReactiveSecurityContextHolder.getContext()
                .flatMap(context -> {
                    Authentication authentication = context.getAuthentication();
                    if (authentication != null && authentication.getCredentials() instanceof String jwt) {
                        ClientRequest authorizedRequest = ClientRequest.from(request)
                                .headers(headers -> headers.setBearerAuth(jwt))
                                .build();
                        return next.exchange(authorizedRequest);
                    }
                    // If no user authentication is found, proceed without the Authorization header.
                    // The request will still contain the X-Internal-Secret header from the builder.
                    return next.exchange(request);
                })
                .switchIfEmpty(next.exchange(request));
    }
}
