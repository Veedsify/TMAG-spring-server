package com.TravelMedicineAdvisory.Server.domain.familytrip;

import com.TravelMedicineAdvisory.Server.core.base.BaseEntity;
import com.TravelMedicineAdvisory.Server.domain.travelplan.TravelPlan;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "family_trip_member_questionnaires")
public class FamilyTripMemberQuestionnaire extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_trip_member_id", nullable = false)
    private FamilyTripMember familyTripMember;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "travel_plan_id")
    private TravelPlan travelPlan;

    @Column(name = "responses_json", columnDefinition = "TEXT")
    private String responsesJson;

    @Column(name = "source")
    private String source;

    public FamilyTripMember getFamilyTripMember() { return familyTripMember; }
    public void setFamilyTripMember(FamilyTripMember familyTripMember) { this.familyTripMember = familyTripMember; }

    public TravelPlan getTravelPlan() { return travelPlan; }
    public void setTravelPlan(TravelPlan travelPlan) { this.travelPlan = travelPlan; }

    public String getResponsesJson() { return responsesJson; }
    public void setResponsesJson(String responsesJson) { this.responsesJson = responsesJson; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
}
