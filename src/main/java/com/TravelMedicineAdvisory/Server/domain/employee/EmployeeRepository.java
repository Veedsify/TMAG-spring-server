package com.TravelMedicineAdvisory.Server.domain.employee;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.TravelMedicineAdvisory.Server.domain.user.User;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByUser(User user);
    Page<Employee> findAllByCompanyId(Long companyId, Pageable pageable);
}
