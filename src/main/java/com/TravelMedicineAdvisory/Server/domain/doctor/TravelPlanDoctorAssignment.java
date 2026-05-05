package com.TravelMedicineAdvisory.Server.domain.doctor;

import java.time.LocalDateTime;

import org.hibernate.annotations.SQLDelete;

import com.TravelMedicineAdvisory.Server.core.base.BaseEntity;
import com.TravelMedicineAdvisory.Server.domain.travelplan.TravelPlan;
import com.TravelMedicineAdvisory.Server.domain.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "travel_plan_doctor_assignments", indexes = {
        @Index(name = "idx_travel_plan_doctor_assignments_plan", columnList = "travel_plan_id"),
        @Index(name = "idx_travel_plan_doctor_assignments_doctor", columnList = "doctor_id")
}, uniqueConstraints = @UniqueConstraint(name = "uk_travel_plan_doctor_assignment", columnNames = {
        "travel_plan_id", "doctor_id"
}))
@SQLDelete(sql = "UPDATE travel_plan_doctor_assignments SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
public class TravelPlanDoctorAssignment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "travel_plan_id", nullable = false)
    private TravelPlan travelPlan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private User doctor;

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt = LocalDateTime.now();

    public TravelPlan getTravelPlan() { return travelPlan; }
    public void setTravelPlan(TravelPlan travelPlan) { this.travelPlan = travelPlan; }
    public User getDoctor() { return doctor; }
    public void setDoctor(User doctor) { this.doctor = doctor; }
    public LocalDateTime getAssignedAt() { return assignedAt; }
    public void setAssignedAt(LocalDateTime assignedAt) { this.assignedAt = assignedAt; }
}
