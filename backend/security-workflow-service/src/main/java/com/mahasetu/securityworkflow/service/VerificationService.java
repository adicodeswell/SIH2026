package com.mahasetu.securityworkflow.service;

import com.mahasetu.securityworkflow.dto.CanonicalCitizenData;
import com.mahasetu.securityworkflow.dto.ResolvedConsentPolicy;
import com.mahasetu.securityworkflow.dto.SourceDataResult;
import com.mahasetu.securityworkflow.dto.verification.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class VerificationService {

    private static final Map<String, Set<String>> AUTHORITATIVE_DOMAINS = new HashMap<>();
    
    private static final DateTimeFormatter DATE_FORMAT_1 = DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter DATE_FORMAT_2 = DateTimeFormatter.ofPattern("dd-MM-uuuu").withResolverStyle(ResolverStyle.STRICT);

    static {
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
        List<VerificationReason> reasons = new ArrayList<>();
        
        if (results == null || results.isEmpty()) {
            reasons.add(new VerificationReason("NO_SOURCES_PROVIDED", null, null));
            return new VerificationResult(VerificationStatus.UNABLE_TO_VERIFY, Collections.emptyList(), Collections.emptyList(), reasons);
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
                reasons.add(new VerificationReason("UNAUTHORIZED_SOURCE", null, invalid.getSource()));
                continue;
            }

            if (sourceMap.containsKey(source)) {
                SourceDataResult invalid = new SourceDataResult();
                invalid.setSource(source);
                invalid.setStatus("FAILED");
                invalid.setError("Duplicate source results detected");
                sourceMap.put(source, invalid);
                reasons.add(new VerificationReason("INVALID_SOURCE_DATA", null, source));
            } else {
                String status = res.getStatus();
                if (!"SUCCESS".equalsIgnoreCase(status) && !"FAILED".equalsIgnoreCase(status)) {
                    SourceDataResult mapped = new SourceDataResult();
                    mapped.setSource(source);
                    mapped.setStatus("UNKNOWN_STATUS");
                    mapped.setError("Unexpected status: " + status);
                    mapped.setData(res.getData());
                    sourceMap.put(source, mapped);
                    reasons.add(new VerificationReason("INVALID_SOURCE_DATA", null, source));
                } else {
                    sourceMap.put(source, res);
                }
            }
        }
        
        List<SourceVerificationResult> sourceResults = new ArrayList<>();
        for (SourceDataResult res : sourceMap.values()) {
            sourceResults.add(new SourceVerificationResult(res.getSource(), res.getStatus(), res.getError()));
            if ("FAILED".equalsIgnoreCase(res.getStatus()) || "UNKNOWN_STATUS".equalsIgnoreCase(res.getStatus())) {
                reasons.add(new VerificationReason("SOURCE_UNAVAILABLE", null, res.getSource()));
            }
        }
        
        List<FieldVerificationResult> fieldResults = new ArrayList<>();
        Set<String> allFieldsToVerify = new TreeSet<>(policy.getRequiredFields());
        allFieldsToVerify.addAll(policy.getOptionalFields());
        
        for (String fieldName : allFieldsToVerify) {
            FieldVerificationResult fRes = verifyField(fieldName, sourceMap.values(), reasons);
            if (fRes != null) {
                fieldResults.add(fRes);
            }
        }
        
        VerificationStatus overallStatus = VerificationStatus.VERIFIED;
        
        boolean hasConflict = fieldResults.stream().anyMatch(f -> f.getStatus() == FieldVerificationStatus.CONFLICT);
        boolean hasMissingRequired = fieldResults.stream().anyMatch(f -> 
                f.getStatus() == FieldVerificationStatus.MISSING && policy.getRequiredFields().contains(f.getFieldName()));
        boolean hasUnableToVerifyRequired = fieldResults.stream().anyMatch(f -> 
                f.getStatus() == FieldVerificationStatus.UNABLE_TO_VERIFY && policy.getRequiredFields().contains(f.getFieldName()));
                
        if (hasConflict) {
            overallStatus = VerificationStatus.NOT_VERIFIED;
        } else if (hasMissingRequired || hasUnableToVerifyRequired) {
            overallStatus = VerificationStatus.PARTIALLY_VERIFIED;
            if (hasMissingRequired) {
                boolean allSourcesFailed = true;
                for (SourceDataResult res : sourceMap.values()) {
                    if ("SUCCESS".equalsIgnoreCase(res.getStatus())) {
                        allSourcesFailed = false;
                        break;
                    }
                }
                if (allSourcesFailed) {
                    overallStatus = VerificationStatus.UNABLE_TO_VERIFY;
                }
            }
        }
        
        return new VerificationResult(overallStatus, sourceResults, fieldResults, reasons);
    }
    
    private FieldVerificationResult verifyField(String fieldName, Collection<SourceDataResult> results, List<VerificationReason> reasons) {
        Map<String, String> rawValues = new TreeMap<>();
        Set<String> authoritativeSources = AUTHORITATIVE_DOMAINS.getOrDefault(fieldName, Collections.emptySet());
        
        for (SourceDataResult res : results) {
            if (!authoritativeSources.contains(res.getSource())) {
                continue;
            }
            if (!"SUCCESS".equalsIgnoreCase(res.getStatus())) {
                continue;
            }
            
            String val = null;
            if (res.getData() != null) {
                val = extractField(res.getData(), fieldName);
            }
            
            if (val != null && !val.trim().isEmpty()) {
                rawValues.put(res.getSource(), val.trim());
            } else {
                reasons.add(new VerificationReason("DATA_NOT_PROVIDED", fieldName, res.getSource()));
            }
        }
        
        if (rawValues.isEmpty()) {
            return new FieldVerificationResult(fieldName, FieldVerificationStatus.MISSING, Collections.emptyMap());
        }
        
        String firstNormalized = null;
        boolean conflict = false;
        boolean hasValid = false;
        boolean hasInvalid = false;
        Map<String, String> maskedValues = new TreeMap<>();
        
        for (Map.Entry<String, String> entry : rawValues.entrySet()) {
            String source = entry.getKey();
            String rawVal = entry.getValue();
            
            String normalized = null;
            boolean currentInvalid = false;
            try {
                normalized = normalize(fieldName, rawVal);
                if (normalized == null) {
                    currentInvalid = true;
                    reasons.add(new VerificationReason("INVALID_FORMAT", fieldName, source));
                }
            } catch (Exception e) {
                currentInvalid = true;
                reasons.add(new VerificationReason("INVALID_FORMAT", fieldName, source));
            }
            
            if (!currentInvalid) {
                hasValid = true;
                if (firstNormalized == null) {
                    firstNormalized = normalized;
                } else if (!firstNormalized.equals(normalized)) {
                    conflict = true;
                }
            } else {
                hasInvalid = true;
            }
            
            maskedValues.put(source, mask(fieldName, rawVal));
        }
        
        FieldVerificationStatus status;
        if (conflict) {
            status = FieldVerificationStatus.CONFLICT;
            for (String src : maskedValues.keySet()) {
                // To avoid duplicate DATA_CONFLICT reasons if we already added them per source...
                // Actually the prompt says "preserve both DATA_CONFLICT and SOURCE_UNAVAILABLE".
                // Adding DATA_CONFLICT for each source involved is fine.
                reasons.add(new VerificationReason("DATA_CONFLICT", fieldName, src));
            }
        } else if (hasInvalid) {
            status = FieldVerificationStatus.UNABLE_TO_VERIFY;
        } else if (hasValid) {
            status = FieldVerificationStatus.MATCH;
        } else {
            status = FieldVerificationStatus.UNABLE_TO_VERIFY;
        }
        
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
        if (value.isEmpty()) return null;
        
        if ("fullName".equals(fieldName) || "universityName".equals(fieldName)) {
            return value.toLowerCase().replaceAll("\\s+", " ");
        }
        
        if ("dateOfBirth".equals(fieldName)) {
            try {
                if (value.matches("\\d{2}-\\d{2}-\\d{4}")) {
                    return LocalDate.parse(value, DATE_FORMAT_2).toString();
                } else {
                    return LocalDate.parse(value, DATE_FORMAT_1).toString();
                }
            } catch (DateTimeParseException e) {
                return null;
            }
        }
        
        if ("annualFamilyIncome".equals(fieldName)) {
            try {
                BigDecimal bd = new BigDecimal(value);
                return bd.stripTrailingZeros().toPlainString();
            } catch (NumberFormatException e) {
                return null;
            }
        }
        
        if ("graduationYear".equals(fieldName)) {
            try {
                BigDecimal bd = new BigDecimal(value);
                if (bd.scale() <= 0 || bd.stripTrailingZeros().scale() <= 0) {
                    return bd.stripTrailingZeros().toPlainString();
                }
                return null;
            } catch (NumberFormatException e) {
                return null;
            }
        }
        
        if ("citizenId".equals(fieldName) || "aadhaarNumber".equals(fieldName) || "bankAccountNumber".equals(fieldName)) {
            return value;
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
