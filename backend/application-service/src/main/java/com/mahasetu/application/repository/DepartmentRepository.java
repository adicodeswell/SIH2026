package com.mahasetu.application.repository;

import com.mahasetu.application.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, String> {
    Optional<Department> findByDepartmentCode(String departmentCode);
}
