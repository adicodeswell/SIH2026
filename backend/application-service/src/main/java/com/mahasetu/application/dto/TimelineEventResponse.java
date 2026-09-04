package com.mahasetu.application.dto;

import java.time.LocalDateTime;

public class TimelineEventResponse {
    private String eventType;
    private String description;
    private LocalDateTime occurredAt;

    // Getters and Setters
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getOccurredAt() { return occurredAt; }
    public void setOccurredAt(LocalDateTime occurredAt) { this.occurredAt = occurredAt; }
}
