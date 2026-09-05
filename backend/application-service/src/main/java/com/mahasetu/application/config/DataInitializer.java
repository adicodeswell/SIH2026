package com.mahasetu.application.config;

import com.mahasetu.application.entity.Department;
import com.mahasetu.application.entity.Service;
import com.mahasetu.application.repository.DepartmentRepository;
import com.mahasetu.application.repository.ServiceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Seeds demo data for the SIH 2026 demonstration.
 * Only runs under "dev", "demo", or "default" profiles.
 * Seeds are idempotent: if data already exists, no duplicate records are created.
 */
@Configuration
@Profile({"dev", "demo", "default"})
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    public CommandLineRunner seedDemoData(DepartmentRepository departmentRepository,
                                          ServiceRepository serviceRepository) {
        return args -> {
            // Seed departments if missing
            seedDepartment(departmentRepository, "DEPT-EDU", "Department of Education", "ACTIVE");
            seedDepartment(departmentRepository, "DEPT-EMP", "Department of Employment", "ACTIVE");
            seedDepartment(departmentRepository, "DEPT-REV", "Department of Revenue", "ACTIVE");
            seedDepartment(departmentRepository, "DEPT-SOC", "Department of Social Welfare", "ACTIVE");
            seedDepartment(departmentRepository, "DEPT-HEALTH", "Department of Health", "ACTIVE");

            // Seed services if missing
            seedService(serviceRepository, departmentRepository,
                    "EDU-SCHOLARSHIP-001", "DEPT-EDU",
                    "State Merit Scholarship",
                    "Financial assistance for meritorious students based on 10th and 12th board results. Covers tuition and hostel fees.",
                    "application-orchestration");
            seedService(serviceRepository, departmentRepository,
                    "EMP-SKILL-CERT-001", "DEPT-EMP",
                    "Skill Development Certificate",
                    "Government-recognized certificate for vocational training programs. Enhances employment prospects in skilled trades.",
                    "application-orchestration");
            seedService(serviceRepository, departmentRepository,
                    "REV-INCOME-CERT-001", "DEPT-REV",
                    "Income Certificate",
                    "Official document certifying annual family income issued by the Revenue Department for government scheme eligibility.",
                    "application-orchestration");
            seedService(serviceRepository, departmentRepository,
                    "SOC-DISABILITY-CERT-001", "DEPT-SOC",
                    "Disability Certificate",
                    "Certificate issued to persons with disabilities for availing reservations and welfare scheme benefits.",
                    "application-orchestration");
            seedService(serviceRepository, departmentRepository,
                    "HEALTH-CARD-001", "DEPT-HEALTH",
                    "Ayushman Maharashtra Health Card",
                    "State health insurance card providing cashless treatment up to ₹5 lakhs per family at empanelled hospitals.",
                    "application-orchestration");

            log.info("[DATA_INIT] Demo seed data verified/loaded successfully.");
        };
    }

    private void seedDepartment(DepartmentRepository repo, String code, String name, String status) {
        if (repo.findByDepartmentCode(code).isEmpty()) {
            Department dept = new Department();
            dept.setDepartmentCode(code);
            dept.setName(name);
            dept.setStatus(status);
            repo.save(dept);
            log.info("[DATA_INIT] Seeded department: {}", code);
        }
    }

    private void seedService(ServiceRepository serviceRepo, DepartmentRepository deptRepo,
                              String serviceCode, String deptCode,
                              String serviceName, String description,
                              String workflowKey) {
        if (serviceRepo.findByServiceCode(serviceCode).isEmpty()) {
            Department dept = deptRepo.findByDepartmentCode(deptCode)
                    .orElseThrow(() -> new IllegalStateException("Department not found: " + deptCode));
            Service svc = new Service();
            svc.setServiceCode(serviceCode);
            svc.setDepartment(dept);
            svc.setServiceName(serviceName);
            svc.setDescription(description);
            svc.setActive(true);
            svc.setWorkflowKey(workflowKey);
            serviceRepo.save(svc);
            log.info("[DATA_INIT] Seeded service: {}", serviceCode);
        }
    }
}
