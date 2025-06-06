package com.vehicle.authentication.inventory.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
public class AsyncConfig {

    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5); // Minimum number of threads
        executor.setMaxPoolSize(10); // Maximum number of threads
        executor.setQueueCapacity(25); // Queue size
        executor.setThreadNamePrefix("AsyncThread-");
        executor.initialize();
        return executor;
    }
}