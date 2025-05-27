package com.vehicle.authentication.inventory.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class AsyncService {
    private static final Logger logger = LoggerFactory.getLogger(AsyncService.class);

    @Async
    public void performAsyncTask(String taskName) {
        logger.info("Starting async task: {}", taskName);
        try {
            // Simulate a long-running task
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Task interrupted", e);
        }
        logger.info("Completed async task: {}", taskName);
    }
}