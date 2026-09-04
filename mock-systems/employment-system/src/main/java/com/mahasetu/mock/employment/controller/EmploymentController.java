package com.mahasetu.mock.employment.controller;

import com.mahasetu.mock.employment.model.EmploymentRecord;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/employment")
public class EmploymentController {

    // Simple in-memory mock database for the MVP
    private static final Map<String, EmploymentRecord> mockDatabase = new HashMap<>();

    static {
        // Pre-populate with the exact test case from the SRS documentation
        mockDatabase.put("MH1001", new EmploymentRecord("Rahul Sharma", "2004-01-10", "UNEMPLOYED"));
        mockDatabase.put("MH1002", new EmploymentRecord("Priya Patel", "1998-05-22", "EMPLOYED"));
    }

    @GetMapping("/{citizenId}")
    public ResponseEntity<EmploymentRecord> getEmploymentStatus(@PathVariable String citizenId) {
        EmploymentRecord record = mockDatabase.get(citizenId);
        
        if (record == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(record);
    }
}
