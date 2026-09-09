package com.mahasetu.securityworkflow.service;

import org.junit.jupiter.api.Test;
import java.lang.reflect.Method;
import static org.junit.jupiter.api.Assertions.*;

class VerificationServiceBigDecimalTest {

    @Test
    void testNumericEquivalence() throws Exception {
        VerificationService service = new VerificationService();
        Method normalizeMethod = VerificationService.class.getDeclaredMethod("normalize", String.class, String.class);
        normalizeMethod.setAccessible(true);
        
        String val1 = (String) normalizeMethod.invoke(service, "annualFamilyIncome", "500000");
        String val2 = (String) normalizeMethod.invoke(service, "annualFamilyIncome", "500000.0");
        String val3 = (String) normalizeMethod.invoke(service, "annualFamilyIncome", "500000.00");
        
        assertEquals(val1, val2);
        assertEquals(val2, val3);
        
        String val4 = (String) normalizeMethod.invoke(service, "annualFamilyIncome", "500000.01");
        assertNotEquals(val1, val4);
        
        String val5 = (String) normalizeMethod.invoke(service, "annualFamilyIncome", "invalid_money");
        assertNull(val5, "Malformed money should return null");
        
        String val6 = (String) normalizeMethod.invoke(service, "annualFamilyIncome", "   ");
        assertNull(val6, "Missing money should return null");
    }
}
