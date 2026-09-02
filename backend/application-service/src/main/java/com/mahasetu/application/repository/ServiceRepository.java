package com.mahasetu.application.repository;

import com.mahasetu.application.entity.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import java.util.List;

@Repository
public interface ServiceRepository extends JpaRepository<Service, String> {
    Optional<Service> findByServiceCode(String serviceCode);
    List<Service> findByActiveTrue();
}
