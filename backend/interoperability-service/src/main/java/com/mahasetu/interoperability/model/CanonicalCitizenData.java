package com.mahasetu.interoperability.model;

/**
 * The standard internal data representation for MahaSetu.
 * All external data formats (JSON, XML, CSV) from different government 
 * departments are transformed into this single standard model.
 */
public class CanonicalCitizenData {
    
    // Core Identity
    private String citizenId;
    private String fullName;
    private String dateOfBirth;
    
    // Department Specific Data
    private String employmentStatus;
    private String highestDegree;
    private Integer graduationYear;
    private String skillStatus;

    // Constructors
    public CanonicalCitizenData() {
    }

    // Getters and Setters
    public String getCitizenId() {
        return citizenId;
    }

    public void setCitizenId(String citizenId) {
        this.citizenId = citizenId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getEmploymentStatus() {
        return employmentStatus;
    }

    public void setEmploymentStatus(String employmentStatus) {
        this.employmentStatus = employmentStatus;
    }

    public String getHighestDegree() {
        return highestDegree;
    }

    public void setHighestDegree(String highestDegree) {
        this.highestDegree = highestDegree;
    }

    public Integer getGraduationYear() {
        return graduationYear;
    }

    public void setGraduationYear(Integer graduationYear) {
        this.graduationYear = graduationYear;
    }

    public String getSkillStatus() {
        return skillStatus;
    }

    public void setSkillStatus(String skillStatus) {
        this.skillStatus = skillStatus;
    }
}
