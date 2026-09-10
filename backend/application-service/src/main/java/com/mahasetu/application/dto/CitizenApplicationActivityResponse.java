package com.mahasetu.application.dto;

import java.time.LocalDateTime;

public class CitizenApplicationActivityResponse {
    private String id;
    private String type;
    private String category;
    private String title;
    private String description;
    private String status;
    private LocalDateTime occurredAt;
    private String actorType;

    public CitizenApplicationActivityResponse() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getOccurredAt() { return occurredAt; }
    public void setOccurredAt(LocalDateTime occurredAt) { this.occurredAt = occurredAt; }

    public String getActorType() { return actorType; }
    public void setActorType(String actorType) { this.actorType = actorType; }
}
