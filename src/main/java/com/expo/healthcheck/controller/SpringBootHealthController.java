package com.expo.healthcheck.controller;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:4200/")
public class SpringBootHealthController implements HealthIndicator {

    @Override
    public Health health() {
        boolean isHealthy = true;
        if (isHealthy) {
            return Health.up().build();
        } else {
            return Health.down().build();
        }
    }

    @GetMapping("/actuator/health")
    public ResponseEntity<String> getHealthStatus() {
        Health health = health();
        if (health.getStatus().equals(Status.UP)) {
            return ResponseEntity.ok("Healthy");
        } else {
            return ResponseEntity.status(503).body("Unhealthy");
        }
    }
}
