package com.stocksense.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {

    // Define the RestTemplate bean so it can be injected into services
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
