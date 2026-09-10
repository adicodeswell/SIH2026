package com.mahasetu.securityworkflow.repository;

import com.mahasetu.securityworkflow.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

    List<AuditLog> findByApplicationId(String applicationId);
    List<AuditLog> findByApplicationIdOrderByOccurredAtAsc(String applicationId);

    List<AuditLog> findByActorId(String actorId);
}
