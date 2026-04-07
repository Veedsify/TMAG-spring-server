package com.TravelMedicineAdvisory.Server.domain.ebook;

import com.TravelMedicineAdvisory.Server.core.base.BaseEntity;
import com.TravelMedicineAdvisory.Server.domain.user.User;
import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ebook_orders")
@SQLDelete(sql = "UPDATE ebook_orders SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
public class EbookOrder extends BaseEntity {

    @Column(name = "tx_ref", nullable = false)
    private String txRef;

    @Column(name = "flw_ref")
    private String flwRef;

    // Nullable — guest purchases won't have a linked user
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // Guest fields (filled when user is null)
    @Column(name = "guest_email")
    private String guestEmail;

    @Column(name = "guest_name")
    private String guestName;

    @Column(name = "guest_phone")
    private String guestPhone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ebook_id", nullable = false)
    private Ebook ebook;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ebook_version_id", nullable = false)
    private EbookVersion ebookVersion;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "amount_paid", precision = 10, scale = 2)
    private BigDecimal amountPaid;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(name = "currency_symbol", length = 10)
    private String currencySymbol;

    // "pending", "completed", "failed", "refunded"
    @Column(nullable = false)
    private String status = "pending";

    @Column(name = "flutterwave_status")
    private String flutterwaveStatus;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "failed_at")
    private LocalDateTime failedAt;

    @Column(name = "failed_reason")
    private String failedReason;

    @Column(name = "email_sent")
    private Boolean emailSent = false;

    @Column(name = "email_sent_at")
    private LocalDateTime emailSentAt;

    public String getTxRef() { return txRef; }
    public void setTxRef(String txRef) { this.txRef = txRef; }

    public String getFlwRef() { return flwRef; }
    public void setFlwRef(String flwRef) { this.flwRef = flwRef; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getGuestEmail() { return guestEmail; }
    public void setGuestEmail(String guestEmail) { this.guestEmail = guestEmail; }

    public String getGuestName() { return guestName; }
    public void setGuestName(String guestName) { this.guestName = guestName; }

    public String getGuestPhone() { return guestPhone; }
    public void setGuestPhone(String guestPhone) { this.guestPhone = guestPhone; }

    public Ebook getEbook() { return ebook; }
    public void setEbook(Ebook ebook) { this.ebook = ebook; }

    public EbookVersion getEbookVersion() { return ebookVersion; }
    public void setEbookVersion(EbookVersion ebookVersion) { this.ebookVersion = ebookVersion; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public BigDecimal getAmountPaid() { return amountPaid; }
    public void setAmountPaid(BigDecimal amountPaid) { this.amountPaid = amountPaid; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getCurrencySymbol() { return currencySymbol; }
    public void setCurrencySymbol(String currencySymbol) { this.currencySymbol = currencySymbol; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getFlutterwaveStatus() { return flutterwaveStatus; }
    public void setFlutterwaveStatus(String flutterwaveStatus) { this.flutterwaveStatus = flutterwaveStatus; }

    public LocalDateTime getPaidAt() { return paidAt; }
    public void setPaidAt(LocalDateTime paidAt) { this.paidAt = paidAt; }

    public LocalDateTime getFailedAt() { return failedAt; }
    public void setFailedAt(LocalDateTime failedAt) { this.failedAt = failedAt; }

    public String getFailedReason() { return failedReason; }
    public void setFailedReason(String failedReason) { this.failedReason = failedReason; }

    public Boolean getEmailSent() { return emailSent; }
    public void setEmailSent(Boolean emailSent) { this.emailSent = emailSent; }

    public LocalDateTime getEmailSentAt() { return emailSentAt; }
    public void setEmailSentAt(LocalDateTime emailSentAt) { this.emailSentAt = emailSentAt; }

    public String getBuyerEmail() {
        return user != null ? user.getEmail() : guestEmail;
    }

    public String getBuyerName() {
        if (user != null) {
            String first = user.getFirstName() != null ? user.getFirstName() : "";
            String last = user.getLastName() != null ? user.getLastName() : "";
            return (first + " " + last).trim();
        }
        return guestName != null ? guestName : "Valued Customer";
    }
}
