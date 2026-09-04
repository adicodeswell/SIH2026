package com.mahasetu.application.repository;

import com.mahasetu.application.entity.ApplicationEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ApplicationEventRepository extends JpaRepository<ApplicationEvent, UUID> {
    List<ApplicationEvent> findByApplication_ApplicationNumberOrderByOccurredAtAsc(String applicationNumber);
}
