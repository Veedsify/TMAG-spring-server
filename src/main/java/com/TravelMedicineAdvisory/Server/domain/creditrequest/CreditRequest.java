package com.TravelMedicineAdvisory.Server.domain.creditrequest;

import java.time.LocalDateTime;

import org.hibernate.annotations.SQLDelete;

import com.TravelMedicineAdvisory.Server.core.base.BaseEntity;
import com.TravelMedicineAdvisory.Server.domain.company.Company;
import com.TravelMedicineAdvisory.Server.domain.employee.Employee;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "credit_requests", indexes = {
        @Index(name = "idx_credit_requests_company_active_created", columnList = "company_id, deleted_at, created_at"),
        @Index(name = "idx_credit_requests_company_status", columnList = "company_id, status"),
        @Index(name = "idx_credit_requests_employee_created", columnList = "employee_id, created_at")
})
@SQLDelete(sql = "UPDATE credit_requests SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
public class CreditRequest extends BaseEntity {

    private Integer creditsRequested;
    private String reason;
    private String status;
    
    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    public Integer getCreditsRequested() { return creditsRequested; }
    public void setCreditsRequested(Integer creditsRequested) { this.creditsRequested = creditsRequested; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }

    public Company getCompany() { return company; }
    public void setCompany(Company company) { this.company = company; }

    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }
}
