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
        String fullName = "Unknown";
        String aadhaar = "XXXX-XXXX-0000";
        String disability = "None";
        String bloodGroup = "O+";
        if (citizenId.equals("MH1001")) {
            fullName = "Rahul Patil";
            aadhaar = "XXXX-XXXX-1234";
        } else if (citizenId.equals("MH1002")) {
            fullName = "Aditya Jha";
            aadhaar = "XXXX-XXXX-2222";
            bloodGroup = "B+";
        } else if (citizenId.equals("MH1003")) {
            fullName = "Ankit Kumar";
            aadhaar = "XXXX-XXXX-3333";
            bloodGroup = "A+";
        }
        
        // Simulating a messy XML response
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
               "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
               "   <soapenv:Body>\n" +
               "      <HealthRecord>\n" +
               "         <CitizenID>" + citizenId + "</CitizenID>\n" +
               "         <Name>" + fullName + "</Name>\n" +
               "         <AadhaarNumber>" + aadhaar + "</AadhaarNumber>\n" +
               "         <BloodGroup>" + bloodGroup + "</BloodGroup>\n" +
               "         <DisabilityStatus>" + disability + "</DisabilityStatus>\n" +
               "         <LastCheckup>2023-11-15</LastCheckup>\n" +
               "      </HealthRecord>\n" +
               "   </soapenv:Body>\n" +
               "</soapenv:Envelope>";
    }
}
