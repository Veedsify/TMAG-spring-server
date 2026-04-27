package com.TravelMedicineAdvisory.Server.domain.travelplan;

import java.time.LocalDateTime;

public interface ElevatedPlanProjection {
    Long getId();
    String getDestination();
    Integer getDuration();
    String getPurpose();
    Integer getRiskScore();
    String getTravellerFirstName();
    String getTravellerLastName();
    String getTravellerName();
    String getTravellerEmail();
    String getDoctorFirstName();
    String getDoctorLastName();
    String getDoctorName();
    String getDoctorFeedback();
    String getPdfPreviewUrl();
    String getSummaryPreviewUrl();
    LocalDateTime getElevatedAt();
}
