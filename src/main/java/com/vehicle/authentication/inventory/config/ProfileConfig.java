package com.vehicle.authentication.inventory.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class ProfileConfig {

    @Bean
    @Profile("local")
    public String localBean() {
        return "Running in Local Environment";
    }

    @Bean
    @Profile("prd")
    public String prodBean() {
        return "Running in Production Environment";
    }
}