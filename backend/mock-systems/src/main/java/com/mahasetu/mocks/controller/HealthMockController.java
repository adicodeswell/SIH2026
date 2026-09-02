package com.mahasetu.mocks.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Simulates an outdated SOAP/XML Web Service from the Health Department.
 */
@RestController
@RequestMapping("/health")
public class HealthMockController {

    @GetMapping(value = "/{citizenId}", produces = MediaType.APPLICATION_XML_VALUE)
    public String getHealthData(@PathVariable String citizenId) {
        String fullName = citizenId.equals("MH1001") ? "Aditya Sharma" : "Unknown";
        // Simulating a messy XML response
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
               "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
               "   <soapenv:Body>\n" +
               "      <HealthRecord>\n" +
               "         <CitizenID>" + citizenId + "</CitizenID>\n" +
               "         <Name>" + fullName + "</Name>\n" +
               "         <BloodGroup>O+</BloodGroup>\n" +
               "         <LastCheckup>2023-11-15</LastCheckup>\n" +
               "      </HealthRecord>\n" +
               "   </soapenv:Body>\n" +
               "</soapenv:Envelope>";
    }
}
