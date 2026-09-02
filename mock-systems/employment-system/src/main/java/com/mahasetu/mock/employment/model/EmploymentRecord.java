package com.mahasetu.mock.employment.model;

public class EmploymentRecord {
    private String candidateName;
    private String dateOfBirth;
    private String employmentStatus;

    public EmploymentRecord() {
    }

    public EmploymentRecord(String candidateName, String dateOfBirth, String employmentStatus) {
        this.candidateName = candidateName;
        this.dateOfBirth = dateOfBirth;
        this.employmentStatus = employmentStatus;
    }

    public String getCandidateName() {
        return candidateName;
    }

    public void setCandidateName(String candidateName) {
        this.candidateName = candidateName;
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
}
