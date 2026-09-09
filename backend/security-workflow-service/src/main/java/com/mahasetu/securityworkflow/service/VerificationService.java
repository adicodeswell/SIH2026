package com.mahasetu.securityworkflow.service;

import com.mahasetu.securityworkflow.dto.CanonicalCitizenData;
import com.mahasetu.securityworkflow.dto.ResolvedConsentPolicy;
import com.mahasetu.securityworkflow.dto.SourceDataResult;
import com.mahasetu.securityworkflow.dto.verification.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class VerificationService {

    private static final Map<String, Set<String>> AUTHORITATIVE_DOMAINS = new HashMap<>();

    static {
        // Core Identity fields are authoritative across all valid systems
        Set<String> allSystems = Set.of("EDUCATION_SYSTEM", "EMPLOYMENT_SYSTEM", "HEALTH_SYSTEM", "SKILLS_SYSTEM");
        AUTHORITATIVE_DOMAINS.put("citizenId", allSystems);
        AUTHORITATIVE_DOMAINS.put("fullName", allSystems);
        AUTHORITATIVE_DOMAINS.put("dateOfBirth", allSystems);
        AUTHORITATIVE_DOMAINS.put("aadhaarNumber", allSystems);

        AUTHORITATIVE_DOMAINS.put("highestDegree", Set.of("EDUCATION_SYSTEM"));
        AUTHORITATIVE_DOMAINS.put("graduationYear", Set.of("EDUCATION_SYSTEM"));
        AUTHORITATIVE_DOMAINS.put("universityName", Set.of("EDUCATION_SYSTEM"));

        AUTHORITATIVE_DOMAINS.put("employmentStatus", Set.of("EMPLOYMENT_SYSTEM"));
        AUTHORITATIVE_DOMAINS.put("annualFamilyIncome", Set.of("EMPLOYMENT_SYSTEM"));
        AUTHORITATIVE_DOMAINS.put("bankAccountNumber", Set.of("EMPLOYMENT_SYSTEM"));
        AUTHORITATIVE_DOMAINS.put("category", Set.of("EMPLOYMENT_SYSTEM"));

        AUTHORITATIVE_DOMAINS.put("bloodGroup", Set.of("HEALTH_SYSTEM"));
        AUTHORITATIVE_DOMAINS.put("disabilityStatus", Set.of("HEALTH_SYSTEM"));

        AUTHORITATIVE_DOMAINS.put("skillStatus", Set.of("SKILLS_SYSTEM"));
    }

    public VerificationResult verify(List<SourceDataResult> results, ResolvedConsentPolicy policy) {
        if (results == null || results.isEmpty()) {
            return new VerificationResult(VerificationStatus.UNABLE_TO_VERIFY, Collections.emptyList(), Collections.emptyList());
        }

        Map<String, SourceDataResult> sourceMap = new TreeMap<>();
        for (SourceDataResult res : results) {
            String source = res.getSource();
            if (source == null || source.trim().isEmpty() || !AUTHORITATIVE_DOMAINS.get("citizenId").contains(source.toUpperCase())) {
                SourceDataResult invalid = new SourceDataResult();
                invalid.setSource(source != null ? source : "UNKNOWN");
                invalid.setStatus("FAILED");
                invalid.setError("Unknown or unauthorized source system");
                sourceMap.put(invalid.getSource(), invalid);
                continue;
            }

            if (sourceMap.containsKey(source)) {
                SourceDataResult invalid = new SourceDataResult();
                invalid.setSource(source);
                invalid.setStatus("FAILED");
                invalid.setError("Duplicate source results detected");
                sourceMap.put(source, invalid);
            } else {
                sourceMap.put(source, res);
            }
        }
        
        List<SourceVerificationResult> sourceResults = new ArrayList<>();
        boolean allRequiredFailed = true;
        boolean anyRequiredFailed = false;
        
        for (SourceDataResult res : sourceMap.values()) {
            boolean isSuccess = "SUCCESS".equalsIgnoreCase(res.getStatus());
            sourceResults.add(new SourceVerificationResult(res.getSource(), res.getStatus(), res.getError()));
            
            if (!isSuccess) {
                anyRequiredFailed = true; 
            } else {
                allRequiredFailed = false;
            }
        }
        
        if (allRequiredFailed) {
            return new VerificationResult(VerificationStatus.UNABLE_TO_VERIFY, sourceResults, Collections.emptyList());
        }
        
        List<FieldVerificationResult> fieldResults = verifyFields(sourceMap.values(), policy);
        
        VerificationStatus overallStatus = VerificationStatus.VERIFIED;
        
        boolean hasConflict = fieldResults.stream().anyMatch(f -> f.getStatus() == FieldVerificationStatus.CONFLICT);
        boolean hasMissingRequired = fieldResults.stream().anyMatch(f -> 
                f.getStatus() == FieldVerificationStatus.MISSING && policy.getRequiredFields().contains(f.getFieldName()));
                
        if (hasConflict) {
            overallStatus = VerificationStatus.NOT_VERIFIED;
        } else if (hasMissingRequired || anyRequiredFailed) {
            overallStatus = VerificationStatus.PARTIALLY_VERIFIED;
        }
        
        return new VerificationResult(overallStatus, sourceResults, fieldResults);
    }
    
    private List<FieldVerificationResult> verifyFields(Collection<SourceDataResult> results, ResolvedConsentPolicy policy) {
        List<FieldVerificationResult> fieldResults = new ArrayList<>();
        
        Set<String> allFieldsToVerify = new HashSet<>(policy.getRequiredFields());
        allFieldsToVerify.addAll(policy.getOptionalFields());
        
        for (String fieldName : allFieldsToVerify) {
            FieldVerificationResult fRes = verifyField(fieldName, results);
            if (fRes != null) {
                fieldResults.add(fRes);
            }
        }
        
        fieldResults.sort(Comparator.comparing(FieldVerificationResult::getFieldName));
        return fieldResults;
    }
    
    private FieldVerificationResult verifyField(String fieldName, Collection<SourceDataResult> results) {
        Map<String, String> rawValues = new TreeMap<>();
        Set<String> authoritativeSources = AUTHORITATIVE_DOMAINS.getOrDefault(fieldName, Collections.emptySet());
        
        for (SourceDataResult res : results) {
            if (!"SUCCESS".equalsIgnoreCase(res.getStatus()) || res.getData() == null) {
                continue;
            }
            if (!authoritativeSources.contains(res.getSource())) {
                continue;
            }
            String val = extractField(res.getData(), fieldName);
            if (val != null && !val.trim().isEmpty()) {
                rawValues.put(res.getSource(), val.trim());
            }
        }
        
        if (rawValues.isEmpty()) {
            return new FieldVerificationResult(fieldName, FieldVerificationStatus.MISSING, Collections.emptyMap());
        }
        
        String firstNormalized = null;
        boolean conflict = false;
        Map<String, String> maskedValues = new TreeMap<>();
        
        for (Map.Entry<String, String> entry : rawValues.entrySet()) {
            String source = entry.getKey();
            String rawVal = entry.getValue();
            String normalized = normalize(fieldName, rawVal);
            
            if (firstNormalized == null) {
                firstNormalized = normalized;
            } else if (!firstNormalized.equals(normalized)) {
                conflict = true;
            }
            
            maskedValues.put(source, mask(fieldName, rawVal));
        }
        
        FieldVerificationStatus status = conflict ? FieldVerificationStatus.CONFLICT : FieldVerificationStatus.MATCH;
        return new FieldVerificationResult(fieldName, status, maskedValues);
    }
    
    private String extractField(CanonicalCitizenData data, String fieldName) {
        return switch (fieldName) {
            case "citizenId" -> data.getCitizenId();
            case "fullName" -> data.getFullName();
            case "dateOfBirth" -> data.getDateOfBirth();
            case "aadhaarNumber" -> data.getAadhaarNumber();
            case "category" -> data.getCategory();
            case "annualFamilyIncome" -> data.getAnnualFamilyIncome() != null ? String.valueOf(data.getAnnualFamilyIncome()) : null;
            case "disabilityStatus" -> data.getDisabilityStatus();
            case "bankAccountNumber" -> data.getBankAccountNumber();
            case "bloodGroup" -> data.getBloodGroup();
            case "employmentStatus" -> data.getEmploymentStatus();
            case "highestDegree" -> data.getHighestDegree();
            case "graduationYear" -> data.getGraduationYear() != null ? String.valueOf(data.getGraduationYear()) : null;
            case "universityName" -> data.getUniversityName();
            case "skillStatus" -> data.getSkillStatus();
            default -> null;
        };
    }
    
    private String normalize(String fieldName, String value) {
        if (value == null) return null;
        value = value.trim();
        
        if ("fullName".equals(fieldName) || "universityName".equals(fieldName)) {
            return value.toLowerCase().replaceAll("\\s+", " ");
        }
        
        if ("dateOfBirth".equals(fieldName)) {
            if (value.matches("\\d{2}-\\d{2}-\\d{4}")) {
                String[] parts = value.split("-");
                return parts[2] + "-" + parts[1] + "-" + parts[0];
            }
            return value;
        }
        
        if ("annualFamilyIncome".equals(fieldName) || "graduationYear".equals(fieldName)) {
            try {
                double d = Double.parseDouble(value);
                long l = (long) d;
                if (d == l) {
                    return String.valueOf(l);
                }
            } catch (NumberFormatException ignored) {}
        }
        
        return value.toLowerCase();
    }
    
    private String mask(String fieldName, String value) {
        if (value == null) return null;
        if ("aadhaarNumber".equals(fieldName)) {
            return value.length() >= 4 ? "********" + value.substring(value.length() - 4) : "****";
        }
        if ("bankAccountNumber".equals(fieldName)) {
            return value.length() >= 4 ? "********" + value.substring(value.length() - 4) : "****";
        }
        return value;
    }
}
