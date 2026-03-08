package com.TravelMedicineAdvisory.Server.domain.credit;

import com.TravelMedicineAdvisory.Server.core.base.BaseEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import com.TravelMedicineAdvisory.Server.domain.company.Company;
import com.TravelMedicineAdvisory.Server.domain.user.User;
import java.math.BigDecimal;

@Entity
@Table(name = "credits")
@SQLDelete(sql = "UPDATE credits SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
public class Credit extends BaseEntity {

    @Column(precision = 10, scale = 2)
    private BigDecimal amount;
    private String type;
    private String reference;
    @Column(name = "balance_after", precision = 10, scale = 2)
    private BigDecimal balanceAfter;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;


    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
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

    public BigDecimal getBalanceAfter() {
        return balanceAfter;
    }

    public void setBalanceAfter(BigDecimal balanceAfter) {
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
