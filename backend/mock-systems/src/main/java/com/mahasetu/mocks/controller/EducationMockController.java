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
        String name = "Unknown";
        String degree = "None";
        String year = "0";
        if (citizenId.equals("MH1001")) {
            name = "Rahul Patil";
            degree = "B.Tech";
            year = "2022";
        } else if (citizenId.equals("MH1002")) {
            name = "Aditya Jha";
            degree = "B.Tech";
            year = "2028";
        } else if (citizenId.equals("MH1003")) {
            name = "Ankit Kumar";
            degree = "B.Tech";
            year = "2028";
        }
        return "ID,STUDENT_NAME,DEGREE,UNIVERSITY,YEAR\n" +
               citizenId + "," + name + "," + degree + ",Mumbai University," + year + "\n";
    }
}
