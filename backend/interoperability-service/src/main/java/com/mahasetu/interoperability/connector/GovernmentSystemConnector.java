package com.mahasetu.interoperability.connector;

import com.mahasetu.interoperability.model.ExternalSystem;
import com.mahasetu.interoperability.model.RawExternalResponse;

/**
 * The core abstraction for connecting to any external government system.
 * Whether the system uses REST, SOAP, or CSV, its adapter must implement this interface.
 */
public interface GovernmentSystemConnector {
    
    /**
     * Fetches raw data from the external department.
     * 
     * @param citizenId The unique ID of the citizen.
     * @param system The system being queried.
     * @return The raw JSON/XML/CSV response wrapped in a RawExternalResponse object.
     */
    RawExternalResponse fetch(String citizenId, ExternalSystem system);
    
}
