package com.vehicle.authentication.inventory.controller;

import com.vehicle.authentication.inventory.service.AsyncService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AsyncController {

    @Autowired
    private AsyncService asyncService;

    @GetMapping("/start-async-task")
    public String startAsyncTask() {
        asyncService.performAsyncTask("SampleTask");
        return "Async task started!";
    }
}