package com.mahasetu.application.repository;

import com.mahasetu.application.entity.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, String> {
    Optional<Application> findByApplicationNumber(String applicationNumber);
}
