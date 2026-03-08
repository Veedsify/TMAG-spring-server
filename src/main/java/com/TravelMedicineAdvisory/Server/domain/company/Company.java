package com.TravelMedicineAdvisory.Server.domain.company;

import com.TravelMedicineAdvisory.Server.core.base.BaseEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import com.TravelMedicineAdvisory.Server.core.storage.Attachment;
import java.util.List;

@Entity
@Table(name = "companies")
@SQLDelete(sql = "UPDATE companies SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
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
    @Column(name = "company_code", unique = true)
    private String companyCode;
    
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

    public String getCompanyCode() {
        return companyCode;
    }

    public void setCompanyCode(String companyCode) {
        this.companyCode = companyCode;
    }

    public Attachment getLogo() {
        return logo;
    }

    public void setLogo(Attachment logo) {
        this.logo = logo;
    }
}
