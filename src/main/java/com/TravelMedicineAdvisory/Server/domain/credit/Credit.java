package com.TravelMedicineAdvisory.Server.domain.credit;

import org.hibernate.annotations.SQLDelete;

import com.TravelMedicineAdvisory.Server.core.base.BaseEntity;
import com.TravelMedicineAdvisory.Server.domain.company.Company;
import com.TravelMedicineAdvisory.Server.domain.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "credits", indexes = {
        @Index(name = "idx_credits_user_created", columnList = "user_id, created_at"),
        @Index(name = "idx_credits_company_created", columnList = "company_id, created_at"),
        @Index(name = "idx_credits_user_type", columnList = "user_id, type"),
        @Index(name = "idx_credits_company_type", columnList = "company_id, type"),
        @Index(name = "idx_credits_type_reference", columnList = "type, reference")
})
@SQLDelete(sql = "UPDATE credits SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
public class Credit extends BaseEntity {

    @Column(precision = 10, scale = 2)
    private Integer amount;
    private String type;
    private String reference;
    @Column(name = "balance_after", precision = 10, scale = 2)
    private Integer balanceAfter;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;


    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public Integer getBalanceAfter() {
        return balanceAfter;
    }

    public void setBalanceAfter(Integer balanceAfter) {
        this.balanceAfter = balanceAfter;
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
