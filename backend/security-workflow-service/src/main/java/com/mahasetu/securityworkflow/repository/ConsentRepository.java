package com.mahasetu.securityworkflow.repository;

import com.mahasetu.securityworkflow.entity.Consent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConsentRepository extends JpaRepository<Consent, UUID> {

    List<Consent> findByCitizenId(String citizenId);

    // Old method for tests that might mock it - keeping just in case

    // New method for Phase 2 application-bound consent
    Optional<Consent> findFirstByCitizenIdAndApplicationIdAndServiceCodeAndStatusOrderByGrantedAtDesc(String citizenId, String applicationId, String serviceCode, String status);
}
