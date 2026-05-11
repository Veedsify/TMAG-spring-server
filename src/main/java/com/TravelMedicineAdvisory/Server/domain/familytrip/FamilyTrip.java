package com.TravelMedicineAdvisory.Server.domain.familytrip;

import java.time.LocalDateTime;

import org.hibernate.annotations.SQLDelete;

import com.TravelMedicineAdvisory.Server.core.base.BaseEntity;
import com.TravelMedicineAdvisory.Server.domain.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "family_trips", indexes = {
    @Index(name = "idx_family_trips_user_status", columnList = "user_id, status")
})
@SQLDelete(sql = "UPDATE family_trips SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
public class FamilyTrip extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_package_purchase_id")
    private FamilyPackagePurchase familyPackagePurchase;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FamilyTripStatus status;

    @Column(nullable = false)
    private String destination;

    @Column(nullable = false)
    private String country;

    @Column(nullable = false)
    private Integer duration;

    private String purpose;

    @Column(name = "trip_type")
    private String tripType;

    @Column(name = "trip_details_json", columnDefinition = "TEXT")
    private String tripDetailsJson;

    @Column(name = "base_fiat_cost", nullable = false)
    private Long baseFiatCost = 0L;

    @Column(name = "extra_member_count", nullable = false)
    private Integer extraMemberCount = 0;

    @Column(name = "extra_fiat_cost", nullable = false)
    private Long extraFiatCost = 0L;

    @Column(name = "total_fiat_cost", nullable = false)
    private Long totalFiatCost = 0L;

    @Column(nullable = false, length = 10)
    private String currency = "NGN";

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "travel_plan_id")
    private com.TravelMedicineAdvisory.Server.domain.travelplan.TravelPlan travelPlan;

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public FamilyPackagePurchase getFamilyPackagePurchase() { return familyPackagePurchase; }
    public void setFamilyPackagePurchase(FamilyPackagePurchase familyPackagePurchase) { this.familyPackagePurchase = familyPackagePurchase; }

    public FamilyTripStatus getStatus() { return status; }
    public void setStatus(FamilyTripStatus status) { this.status = status; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }

    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }

    public String getTripType() { return tripType; }
    public void setTripType(String tripType) { this.tripType = tripType; }

    public String getTripDetailsJson() { return tripDetailsJson; }
    public void setTripDetailsJson(String tripDetailsJson) { this.tripDetailsJson = tripDetailsJson; }

    public Long getBaseFiatCost() { return baseFiatCost; }
    public void setBaseFiatCost(Long baseFiatCost) { this.baseFiatCost = baseFiatCost; }

    public Integer getExtraMemberCount() { return extraMemberCount; }
    public void setExtraMemberCount(Integer extraMemberCount) { this.extraMemberCount = extraMemberCount; }

    public Long getExtraFiatCost() { return extraFiatCost; }
    public void setExtraFiatCost(Long extraFiatCost) { this.extraFiatCost = extraFiatCost; }

    public Long getTotalFiatCost() { return totalFiatCost; }
    public void setTotalFiatCost(Long totalFiatCost) { this.totalFiatCost = totalFiatCost; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }

    public com.TravelMedicineAdvisory.Server.domain.travelplan.TravelPlan getTravelPlan() { return travelPlan; }
    public void setTravelPlan(com.TravelMedicineAdvisory.Server.domain.travelplan.TravelPlan travelPlan) { this.travelPlan = travelPlan; }
}
