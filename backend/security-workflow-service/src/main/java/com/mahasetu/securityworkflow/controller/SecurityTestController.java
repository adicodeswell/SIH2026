package com.mahasetu.securityworkflow.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/security")
public class SecurityTestController {

    @GetMapping("/test")
    public ResponseEntity<Map<String, Boolean>> testSecurity() {
        return ResponseEntity.ok(Map.of("authenticated", true));
    }
}
