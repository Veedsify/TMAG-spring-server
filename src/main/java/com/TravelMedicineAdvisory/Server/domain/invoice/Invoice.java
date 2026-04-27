package com.TravelMedicineAdvisory.Server.domain.invoice;

import com.TravelMedicineAdvisory.Server.core.base.BaseEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;

import com.TravelMedicineAdvisory.Server.domain.company.Company;
import com.TravelMedicineAdvisory.Server.domain.user.User;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "invoices", indexes = {
    @Index(name = "idx_invoices_active_created", columnList = "deleted_at, created_at"),
    @Index(name = "idx_invoices_company_active_created", columnList = "company_id, deleted_at, created_at"),
    @Index(name = "idx_invoices_user_active_created", columnList = "user_id, deleted_at, created_at"),
    @Index(name = "idx_invoices_status_active_created", columnList = "status, deleted_at, created_at"),
    @Index(name = "idx_invoices_status_active_paid", columnList = "status, deleted_at, paid_at")
})
@SQLDelete(sql = "UPDATE invoices SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")

public class Invoice extends BaseEntity {

    @Column(precision = 10, scale = 2)
    private BigDecimal amount;
    private String currency;
    private String status;
    @Column(columnDefinition = "TEXT")
    private String description;
    @Column(name = "issued_at")
    private LocalDateTime issuedAt;
    @Column(name = "due_date")
    private LocalDateTime dueDate;
    @Column(name = "paid_at")
    private LocalDateTime paidAt;
    @Column(name = "payment_method")
    private String paymentMethod;
    
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

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(LocalDateTime issuedAt) {
        this.issuedAt = issuedAt;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
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
