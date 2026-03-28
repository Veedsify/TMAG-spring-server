package com.TravelMedicineAdvisory.Server.domain.admin.companies;

import java.time.LocalDateTime;
import java.util.List;

public class AdminCompanyResponse {
    private Long id;
    private String name;
    private String industry;
    private String website;
    private Integer creditsPurchased;
    private Integer creditsRemaining;
    private Integer plansGenerated;
    private Integer activeEmployees;
    private String billingStatus;
    private LocalDateTime contractRenewal;
    private String tier;
    private List<String> hrAdmins;
    private String contactEmail;
    private String contactPhone;
    private String address;
    private String billingCurrency;
    private LocalDateTime createdAt;

    public AdminCompanyResponse() {}

    public AdminCompanyResponse(Long id, String name, String industry, String website,
                                Integer creditsPurchased, Integer creditsRemaining,
                                Integer plansGenerated, Integer activeEmployees,
                                String billingStatus, LocalDateTime contractRenewal,
                                String tier, List<String> hrAdmins, String contactEmail,
                                String contactPhone, String address, String billingCurrency,
                                LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.industry = industry;
        this.website = website;
        this.creditsPurchased = creditsPurchased;
        this.creditsRemaining = creditsRemaining;
        this.plansGenerated = plansGenerated;
        this.activeEmployees = activeEmployees;
        this.billingStatus = billingStatus;
        this.contractRenewal = contractRenewal;
        this.tier = tier;
        this.hrAdmins = hrAdmins;
        this.contactEmail = contactEmail;
        this.contactPhone = contactPhone;
        this.address = address;
        this.billingCurrency = billingCurrency;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getIndustry() { return industry; }
    public void setIndustry(String industry) { this.industry = industry; }
    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }
    public Integer getCreditsPurchased() { return creditsPurchased; }
    public void setCreditsPurchased(Integer creditsPurchased) { this.creditsPurchased = creditsPurchased; }
    public Integer getCreditsRemaining() { return creditsRemaining; }
    public void setCreditsRemaining(Integer creditsRemaining) { this.creditsRemaining = creditsRemaining; }
    public Integer getPlansGenerated() { return plansGenerated; }
    public void setPlansGenerated(Integer plansGenerated) { this.plansGenerated = plansGenerated; }
    public Integer getActiveEmployees() { return activeEmployees; }
    public void setActiveEmployees(Integer activeEmployees) { this.activeEmployees = activeEmployees; }
    public String getBillingStatus() { return billingStatus; }
    public void setBillingStatus(String billingStatus) { this.billingStatus = billingStatus; }
    public LocalDateTime getContractRenewal() { return contractRenewal; }
    public void setContractRenewal(LocalDateTime contractRenewal) { this.contractRenewal = contractRenewal; }
    public String getTier() { return tier; }
    public void setTier(String tier) { this.tier = tier; }
    public List<String> getHrAdmins() { return hrAdmins; }
    public void setHrAdmins(List<String> hrAdmins) { this.hrAdmins = hrAdmins; }
    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }
    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getBillingCurrency() { return billingCurrency; }
    public void setBillingCurrency(String billingCurrency) { this.billingCurrency = billingCurrency; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
