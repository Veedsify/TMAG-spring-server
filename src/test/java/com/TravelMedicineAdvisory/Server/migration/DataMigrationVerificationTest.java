package com.TravelMedicineAdvisory.Server.migration;

import com.TravelMedicineAdvisory.Server.domain.company.CompanyRepository;
import com.TravelMedicineAdvisory.Server.domain.employee.Employee;
import com.TravelMedicineAdvisory.Server.domain.employee.EmployeeRepository;
import com.TravelMedicineAdvisory.Server.domain.user.User;
import com.TravelMedicineAdvisory.Server.domain.user.UserRepository;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Disabled("Run manually against the migrated database instance")
class DataMigrationVerificationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Test
    void verifyAllUsersHaveValidEmails() {
        List<User> users = userRepository.findAll();
        assertTrue(users.stream().allMatch(u -> u.getEmail() != null && u.getEmail().contains("@")),
                "All users should have valid email addresses containing '@'");
    }

    @Test
    void verifyForeignKeyIntegrity() {
        List<Employee> employees = employeeRepository.findAll();
        employees.forEach(e -> {
            if (e.getCompany() != null) {
                assertNotNull(companyRepository.findById(e.getCompany().getId()).orElse(null),
                        "Employee company ID must exist in companies table");
            }
        });
    }

    @Test
    void verifyNoOrphanedRoles() {
        List<User> users = userRepository.findAll();
        users.forEach(u -> {
            if (u.getRole() != null) {
                assertNotNull(u.getRole().getName(), "User role must be correctly mapped");
            }
        });
    }
}
