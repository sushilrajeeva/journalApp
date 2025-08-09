package com.sb.journalApp.controller;

import com.sb.journalApp.service.DbHealthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class HealthController {
    private final DbHealthService dbHealthService;

    @GetMapping("/api/health")
    public String health() {
        return "Journal App Works!";
    }

    @GetMapping("/api/health/db")
    public Map<String, Object> dbHealth() {
        return dbHealthService.health();
    }
}
