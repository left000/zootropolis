package com.zootropolis.boundary;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestBoundary {

    @GetMapping("/test")
    public String testServer() {
        return "Zootropolis Server è online e funzionante!";
    }
}