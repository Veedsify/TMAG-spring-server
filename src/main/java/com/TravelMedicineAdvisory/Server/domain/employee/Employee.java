package com.TravelMedicineAdvisory.Server.domain.employee;

import com.TravelMedicineAdvisory.Server.core.base.BaseEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;

import com.TravelMedicineAdvisory.Server.domain.company.Company;
import com.TravelMedicineAdvisory.Server.domain.user.User;

@Entity
@Table(name = "employees", indexes = {
    @Index(name = "idx_employees_company_active", columnList = "company_id, deleted_at"),
    @Index(name = "idx_employees_company_status", columnList = "company_id, status"),
    @Index(name = "idx_employees_user", columnList = "user_id")
})
@SQLDelete(sql = "UPDATE employees SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")

public class Employee extends BaseEntity {

    private String name;
    private String email;
    private String department;
    @Column(name = "credits_used")
    private Integer creditsUsed;
    @Column(name = "credits_allocated")
    private Integer creditsAllocated;
    private String status;
    @Column(name = "plans_generated")
    private Integer plansGenerated;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public Integer getCreditsUsed() {
        return creditsUsed;
    }

    public void setCreditsUsed(Integer creditsUsed) {
        this.creditsUsed = creditsUsed;
    }

    public Integer getCreditsAllocated() {
        return creditsAllocated;
    }

    public void setCreditsAllocated(Integer creditsAllocated) {
        this.creditsAllocated = creditsAllocated;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getPlansGenerated() {
        return plansGenerated;
    }

    public void setPlansGenerated(Integer plansGenerated) {
        this.plansGenerated = plansGenerated;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
