package com.mahasetu.securityworkflow.exception;

/**
 * Thrown when a service code has no configured consent policy.
 * This enforces default-deny: unknown service codes cannot silently
 * receive broad consent.
 */
public class UnsupportedServiceCodeException extends RuntimeException {

    private final String serviceCode;

    public UnsupportedServiceCodeException(String serviceCode) {
        super("No consent policy configured for service code: " + serviceCode);
        this.serviceCode = serviceCode;
    }

    public String getServiceCode() {
        return serviceCode;
    }
}
