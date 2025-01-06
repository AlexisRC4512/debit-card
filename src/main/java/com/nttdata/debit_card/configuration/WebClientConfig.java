package com.nttdata.debit_card.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Configuration class for setting up a WebClient bean.
 */
@Configuration
public class WebClientConfig {

    @Value("${server.url.account}")
    private String acountUrl;
    /**
     * Creates and configures a WebClient bean.
     *
     * @return a configured WebClient instance
     */
    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .baseUrl(acountUrl)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

}
