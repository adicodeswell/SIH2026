package com.mahasetu.mocks.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Simulates a system that dumps CSV records for Education.
 */
@RestController
@RequestMapping("/education")
public class EducationMockController {

    @GetMapping(value = "/{citizenId}", produces = "text/csv")
    public String getEducationData(@PathVariable String citizenId) {
        // Simulating a CSV row
        String name = citizenId.equals("MH1001") ? "Rahul Patil" : "Unknown";
        return "ID,STUDENT_NAME,DEGREE,UNIVERSITY,YEAR\n" +
               citizenId + "," + name + ",B.Tech,Mumbai University,2022\n";
    }
}
