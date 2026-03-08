package com.TravelMedicineAdvisory.Server.domain.useronboarding;

import com.TravelMedicineAdvisory.Server.core.base.BaseEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import com.TravelMedicineAdvisory.Server.domain.user.User;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_onboardings")
@SQLDelete(sql = "UPDATE user_onboardings SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
public class UserOnboarding extends BaseEntity {

    @Column(name = "user_type")
    private String userType;
    private String nationality;
    @Column(name = "company_code")
    private String companyCode;
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;


    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public String getCompanyCode() {
        return companyCode;
    }

    public void setCompanyCode(String companyCode) {
        this.companyCode = companyCode;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
