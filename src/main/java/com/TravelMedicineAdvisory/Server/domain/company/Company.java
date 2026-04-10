package com.TravelMedicineAdvisory.Server.domain.company;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.SQLDelete;

import com.TravelMedicineAdvisory.Server.core.base.BaseEntity;
import com.TravelMedicineAdvisory.Server.core.storage.Attachment;
import com.TravelMedicineAdvisory.Server.domain.companyplan.PlanEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "companies")
@SQLDelete(sql = "UPDATE companies SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
public class Company extends BaseEntity {

    private String name;
    private String industry;
    @Column(name = "total_credits")
    private Integer totalCredits;
    @Column(name = "used_credits")
    private Integer usedCredits;
    @Column(name = "employee_count")
    private Integer employeeCount;
    private String plan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "active_plan_id")
    private PlanEntity activePlan;
    @Column(name = "company_code", unique = true)
    private String companyCode;

    @Enumerated(EnumType.STRING)
    private Tier tier = Tier.STANDARD;

    @Column(name = "contract_renewal")
    private LocalDateTime contractRenewal;

    private String website;
    private String address;
    private String contactEmail;
    private String contactPhone;

    @Enumerated(EnumType.STRING)
    @Column(name = "billing_currency")
    private BillingCurrency billingCurrency = BillingCurrency.NGN;

    @Enumerated(EnumType.STRING)
    @Column(name = "billing_status")
    private BillingStatus billingStatus = BillingStatus.ACTIVE;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "logo_id")
    private Attachment logo;

    @OneToMany(mappedBy = "company")
    private List<com.TravelMedicineAdvisory.Server.domain.companyuser.CompanyUser> companyUsers;

    @OneToMany(mappedBy = "company")
    private List<com.TravelMedicineAdvisory.Server.domain.employee.Employee> employees;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIndustry() {
        return industry;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }

    public Integer getTotalCredits() {
        return totalCredits;
    }

    public void setTotalCredits(Integer totalCredits) {
        this.totalCredits = totalCredits;
    }

    public Integer getUsedCredits() {
        return usedCredits;
    }

    public void setUsedCredits(Integer usedCredits) {
        this.usedCredits = usedCredits;
    }

    public Integer getEmployeeCount() {
        return employeeCount;
    }

    public void setEmployeeCount(Integer employeeCount) {
        this.employeeCount = employeeCount;
    }

    public String getPlan() {
        return plan;
    }

    public void setPlan(String plan) {
        this.plan = plan;
    }

    public PlanEntity getActivePlan() {
        return activePlan;
    }

    public void setActivePlan(PlanEntity activePlan) {
        this.activePlan = activePlan;
    }

    public String getCompanyCode() {
        return companyCode;
    }

    public void setCompanyCode(String companyCode) {
        this.companyCode = companyCode;
    }

    public Tier getTier() {
        return tier;
    }

    public void setTier(Tier tier) {
        this.tier = tier;
    }

    public LocalDateTime getContractRenewal() {
        return contractRenewal;
    }

    public void setContractRenewal(LocalDateTime contractRenewal) {
        this.contractRenewal = contractRenewal;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public BillingCurrency getBillingCurrency() {
        return billingCurrency;
    }

    public void setBillingCurrency(BillingCurrency billingCurrency) {
        this.billingCurrency = billingCurrency;
    }

    public BillingStatus getBillingStatus() {
        return billingStatus;
    }

    public void setBillingStatus(BillingStatus billingStatus) {
        this.billingStatus = billingStatus;
    }

    public Attachment getLogo() {
        return logo;
    }

    public void setLogo(Attachment logo) {
        this.logo = logo;
    }

    @Transient
    public int getAvailableCredits() {
        int total = totalCredits != null ? totalCredits : 0;
        int used = usedCredits != null ? usedCredits : 0;
        return total - used;
    }
}
