package com.TravelMedicineAdvisory.Server.domain.employee;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.TravelMedicineAdvisory.Server.domain.user.User;
import com.TravelMedicineAdvisory.Server.domain.report.UsageReportDto;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    @Query("SELECT COUNT(e) FROM Employee e WHERE e.deletedAt IS NULL")
    long countActiveEmployees();

    Optional<Employee> findByUser(User user);
    Page<Employee> findAllByCompanyId(Long companyId, Pageable pageable);
    List<Employee> findAllByCompanyId(Long companyId);
    Optional<Employee> findByUserId(Long userId);

    @Query("""
            SELECT new com.TravelMedicineAdvisory.Server.domain.report.UsageReportDto(
                e.name, e.email, e.department, e.creditsAllocated, e.creditsUsed, e.plansGenerated, e.status, e.updatedAt)
            FROM Employee e
            WHERE e.deletedAt IS NULL
            """)
    List<UsageReportDto> findUsageReportRows();

    @Query("""
            SELECT new com.TravelMedicineAdvisory.Server.domain.report.UsageReportDto(
                e.name, e.email, e.department, e.creditsAllocated, e.creditsUsed, e.plansGenerated, e.status, e.updatedAt)
            FROM Employee e
            WHERE e.company.id = :companyId AND e.deletedAt IS NULL
            """)
    List<UsageReportDto> findUsageReportRowsByCompanyId(@Param("companyId") Long companyId);
}
