package com.TravelMedicineAdvisory.Server.domain.companyuser;

import com.TravelMedicineAdvisory.Server.core.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.SQLDelete;
import com.TravelMedicineAdvisory.Server.domain.company.Company;
import com.TravelMedicineAdvisory.Server.domain.user.User;

@Entity
@Table(name = "company_users", indexes = {
        @Index(name = "idx_company_users_user_active", columnList = "user_id, deleted_at"),
        @Index(name = "idx_company_users_company_active", columnList = "company_id, deleted_at"),
        @Index(name = "idx_company_users_company_role_active", columnList = "company_id, role, deleted_at")
})
@SQLDelete(sql = "UPDATE company_users SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
public class CompanyUser extends BaseEntity {

    private String role;

    @Column(name = "credits_allocated", nullable = false)
    private Integer creditsAllocated = 0;

    @Column(name = "credits_used", nullable = false)
    private Integer creditsUsed = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;


    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Integer getCreditsAllocated() {
        return creditsAllocated;
    }

    public void setCreditsAllocated(Integer creditsAllocated) {
        this.creditsAllocated = creditsAllocated;
    }

    public Integer getCreditsUsed() {
        return creditsUsed;
    }

    public void setCreditsUsed(Integer creditsUsed) {
        this.creditsUsed = creditsUsed;
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
