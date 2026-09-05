package com.mahasetu.interoperability.transformer;

import com.mahasetu.interoperability.model.CanonicalCitizenData;
import com.mahasetu.interoperability.model.ExternalSystem;
import com.mahasetu.interoperability.model.RawExternalResponse;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class HealthTransformerTest {

    @Test
    public void testTransformXml() {
        HealthTransformer transformer = new HealthTransformer();
        String mockXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
               "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
               "   <soapenv:Body>\n" +
               "      <HealthRecord>\n" +
               "         <CitizenID>MH2002</CitizenID>\n" +
               "         <Name>Rahul Patil</Name>\n" +
               "         <BloodGroup>O+</BloodGroup>\n" +
               "         <LastCheckup>2023-11-15</LastCheckup>\n" +
               "      </HealthRecord>\n" +
               "   </soapenv:Body>\n" +
               "</soapenv:Envelope>";
               
        RawExternalResponse response = new RawExternalResponse(mockXml, ExternalSystem.HEALTH_SYSTEM);
        CanonicalCitizenData result = transformer.transform(response);
        
        assertEquals("MH2002", result.getCitizenId());
        assertEquals("Rahul Patil", result.getFullName());
    }
}
