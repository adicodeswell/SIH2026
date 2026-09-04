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

    @GetMapping("/citizen")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('CITIZEN')")
    public ResponseEntity<Map<String, String>> citizenAccess() {
        return ResponseEntity.ok(Map.of("role", "CITIZEN"));
    }

    @GetMapping("/officer")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('OFFICER')")
    public ResponseEntity<Map<String, String>> officerAccess() {
        return ResponseEntity.ok(Map.of("role", "OFFICER"));
    }

    @GetMapping("/admin")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> adminAccess() {
        return ResponseEntity.ok(Map.of("role", "ADMIN"));
    }
}
