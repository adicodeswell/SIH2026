package com.mahasetu.application.repository;

import com.mahasetu.application.entity.Citizen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CitizenRepository extends JpaRepository<Citizen, String> {
    Optional<Citizen> findByCitizenId(String citizenId);
    boolean existsByCitizenId(String citizenId);
    boolean existsByMobile(String mobile);
}
