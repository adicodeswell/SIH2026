package com.mahasetu.mocks.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Simulates a modern(ish) JSON REST API from the Employment Department.
 */
@RestController
@RequestMapping("/employment")
public class EmploymentMockController {

    @GetMapping("/{citizenId}")
    public Map<String, Object> getEmploymentData(@PathVariable String citizenId) {
        Map<String, Object> response = new HashMap<>();
        response.put("cit_id", citizenId);
        
        // Mocking data based on citizen ID
        if (citizenId.equals("MH1001")) {
            response.put("full_name", "Rahul Patil"); // Match the SQL seed name
            response.put("dob", "2000-01-01");
            response.put("emp_status", "UNEMPLOYED"); // Change to UNEMPLOYED so they qualify for Tech Scholarship
            response.put("highest_degree", "B.Tech");
            response.put("grad_year", 2022);
            response.put("annual_family_income", 250000.00); // 2.5 Lakhs
            response.put("category", "OBC");
            response.put("bank_account", "XXXX-XXXX-9876");
        } else if (citizenId.equals("MH1002")) {
            response.put("full_name", "Aditya Jha");
            response.put("dob", "2006-07-30");
            response.put("emp_status", "STUDENT");
            response.put("highest_degree", "High School");
            response.put("grad_year", 2024);
            response.put("annual_family_income", 150000.00); 
            response.put("category", "General");
            response.put("bank_account", "XXXX-XXXX-1111");
        } else if (citizenId.equals("MH1003")) {
            response.put("full_name", "Ankit Kumar");
            response.put("dob", "2006-01-16");
            response.put("emp_status", "STUDENT");
            response.put("highest_degree", "High School");
            response.put("grad_year", 2024);
            response.put("annual_family_income", 100000.00); 
            response.put("category", "OBC");
            response.put("bank_account", "XXXX-XXXX-2222");
        } else {
            response.put("full_name", "Unknown Citizen");
            response.put("dob", "1990-01-01");
            response.put("emp_status", "EMPLOYED");
            response.put("highest_degree", "None");
            response.put("grad_year", 0);
            response.put("annual_family_income", 800000.00);
            response.put("category", "General");
            response.put("bank_account", "XXXX-XXXX-0000");
        }
        return response;
    }
}
