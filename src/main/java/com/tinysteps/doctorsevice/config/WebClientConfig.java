package com.tinysteps.doctorsevice.config; // Or your equivalent package

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    private static final Logger logger = LoggerFactory.getLogger(WebClientConfig.class);

    @Value("${internal.api.secret}")
    private String internalApiSecret;

    @Bean
    @LoadBalanced
    public WebClient.Builder loadBalancedWebClientBuilder() {
        return WebClient.builder()
                // The key fix: Add the secret to the base builder
                .defaultHeader("X-Internal-Secret", internalApiSecret)
                // Add a filter to log outgoing request headers for debugging
                .filter(logRequestHeaders());
    }

    @Bean
    public WebClient internalWebClient(WebClient.Builder loadBalancedWebClientBuilder) {
        return loadBalancedWebClientBuilder.build();
    }

    /**
     * This logging filter will print the headers of every outgoing request
     * made by any WebClient created from the builder.
     */
    private ExchangeFilterFunction logRequestHeaders() {
        return (clientRequest, next) -> {
            logger.info("================ Outgoing Request from Doctor-Service ================");
            logger.info("Request: {} {}", clientRequest.method(), clientRequest.url());
            clientRequest.headers().forEach((name, values) ->
                    values.forEach(value -> logger.info("Header: {}={}", name, value))
            );
            logger.info("======================================================================");
            return next.exchange(clientRequest);
        };
    }
}
