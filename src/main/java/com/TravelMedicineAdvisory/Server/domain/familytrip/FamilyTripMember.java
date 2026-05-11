package com.TravelMedicineAdvisory.Server.domain.familytrip;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.SQLDelete;

import com.TravelMedicineAdvisory.Server.core.base.BaseEntity;
import com.TravelMedicineAdvisory.Server.domain.travelplan.TravelPlan;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "family_trip_members", indexes = {
    @Index(name = "idx_family_members_trip", columnList = "family_trip_id"),
    @Index(name = "idx_family_members_session_hash", columnList = "session_token_hash", unique = true),
    @Index(name = "idx_family_members_email_lookup", columnList = "member_email")
})
@SQLDelete(sql = "UPDATE family_trip_members SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
public class FamilyTripMember extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_trip_id", nullable = false)
    private FamilyTrip familyTrip;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "travel_plan_id")
    private TravelPlan travelPlan;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FamilyMemberRelationship relationship;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "member_email")
    private String memberEmail;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "age_at_departure")
    private Integer ageAtDeparture;

    @Column(name = "included_in_base", nullable = false)
    private Boolean includedInBase = false;

    @Column(name = "questionnaire_responses_json", columnDefinition = "TEXT")
    private String questionnaireResponsesJson;

    @Column(name = "questionnaire_status", nullable = false)
    private String questionnaireStatus = "PENDING";

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @Column(name = "login_code")
    private String loginCode;

    @Column(name = "login_code_consumed_at")
    private LocalDateTime loginCodeConsumedAt;

    @Column(name = "session_token_hash")
    private String sessionTokenHash;

    @Column(name = "session_expires_at")
    private LocalDateTime sessionExpiresAt;

    @Column(name = "failed_login_attempts", nullable = false)
    private Integer failedLoginAttempts = 0;

    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;

    public FamilyTrip getFamilyTrip() { return familyTrip; }
    public void setFamilyTrip(FamilyTrip familyTrip) { this.familyTrip = familyTrip; }

    public TravelPlan getTravelPlan() { return travelPlan; }
    public void setTravelPlan(TravelPlan travelPlan) { this.travelPlan = travelPlan; }

    public FamilyMemberRelationship getRelationship() { return relationship; }
    public void setRelationship(FamilyMemberRelationship relationship) { this.relationship = relationship; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getMemberEmail() { return memberEmail; }
    public void setMemberEmail(String memberEmail) { this.memberEmail = memberEmail; }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public Integer getAgeAtDeparture() { return ageAtDeparture; }
    public void setAgeAtDeparture(Integer ageAtDeparture) { this.ageAtDeparture = ageAtDeparture; }

    public Boolean getIncludedInBase() { return includedInBase; }
    public void setIncludedInBase(Boolean includedInBase) { this.includedInBase = includedInBase; }

    public String getQuestionnaireResponsesJson() { return questionnaireResponsesJson; }
    public void setQuestionnaireResponsesJson(String questionnaireResponsesJson) { this.questionnaireResponsesJson = questionnaireResponsesJson; }

    public String getQuestionnaireStatus() { return questionnaireStatus; }
    public void setQuestionnaireStatus(String questionnaireStatus) { this.questionnaireStatus = questionnaireStatus; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }

    public String getLoginCode() { return loginCode; }
    public void setLoginCode(String loginCode) { this.loginCode = loginCode; }

    public LocalDateTime getLoginCodeConsumedAt() { return loginCodeConsumedAt; }
    public void setLoginCodeConsumedAt(LocalDateTime loginCodeConsumedAt) { this.loginCodeConsumedAt = loginCodeConsumedAt; }

    public String getSessionTokenHash() { return sessionTokenHash; }
    public void setSessionTokenHash(String sessionTokenHash) { this.sessionTokenHash = sessionTokenHash; }

    public LocalDateTime getSessionExpiresAt() { return sessionExpiresAt; }
    public void setSessionExpiresAt(LocalDateTime sessionExpiresAt) { this.sessionExpiresAt = sessionExpiresAt; }

    public Integer getFailedLoginAttempts() { return failedLoginAttempts; }
    public void setFailedLoginAttempts(Integer failedLoginAttempts) { this.failedLoginAttempts = failedLoginAttempts; }

    public LocalDateTime getLockedUntil() { return lockedUntil; }
    public void setLockedUntil(LocalDateTime lockedUntil) { this.lockedUntil = lockedUntil; }
}
