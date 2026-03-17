package com.TravelMedicineAdvisory.Server.domain.admin.credits;

import java.time.LocalDateTime;

public class AdminCreditLedgerResponse {
    private Long id;
    private Long userId;
    private String userName;
    private Long companyId;
    private String companyName;
    private String action;
    private Integer amount;
    private Integer balanceBefore;
    private Integer balanceAfter;
    private String reason;
    private String triggeredBy;
    private LocalDateTime timestamp;

    public AdminCreditLedgerResponse() {}

    public AdminCreditLedgerResponse(Long id, Long userId, String userName, Long companyId,
                                     String companyName, String action, Integer amount,
                                     Integer balanceBefore, Integer balanceAfter, String reason,
                                     String triggeredBy, LocalDateTime timestamp) {
        this.id = id;
        this.userId = userId;
        this.userName = userName;
        this.companyId = companyId;
        this.companyName = companyName;
        this.action = action;
        this.amount = amount;
        this.balanceBefore = balanceBefore;
        this.balanceAfter = balanceAfter;
        this.reason = reason;
        this.triggeredBy = triggeredBy;
        this.timestamp = timestamp;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public Long getCompanyId() { return companyId; }
    public void setCompanyId(Long companyId) { this.companyId = companyId; }
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public Integer getAmount() { return amount; }
    public void setAmount(Integer amount) { this.amount = amount; }
    public Integer getBalanceBefore() { return balanceBefore; }
    public void setBalanceBefore(Integer balanceBefore) { this.balanceBefore = balanceBefore; }
    public Integer getBalanceAfter() { return balanceAfter; }
    public void setBalanceAfter(Integer balanceAfter) { this.balanceAfter = balanceAfter; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getTriggeredBy() { return triggeredBy; }
    public void setTriggeredBy(String triggeredBy) { this.triggeredBy = triggeredBy; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
