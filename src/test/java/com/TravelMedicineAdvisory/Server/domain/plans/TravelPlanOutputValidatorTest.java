package com.TravelMedicineAdvisory.Server.domain.plans;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;

class TravelPlanOutputValidatorTest {

    @Test
    void fullSingleTravellerValidationRejectsMissingEmergencyContacts() {
        var output = new TravelPlanOutputSchemas.TravelHealthPlanOutput(
                "Plan", "Traveller", "Paris", "1 Jun", "LOW", null,
                new TravelPlanOutputSchemas.TripAtGlance(1, "Leisure", "Solo", "Hotel", "Yes"),
                List.of(new TravelPlanOutputSchemas.HealthRiskItem("Food and water", "LOW", "Use usual precautions")),
                List.of(new TravelPlanOutputSchemas.VaccinationItem("Routine vaccines", "Review", "Confirm current", "Review records")),
                null,
                List.of(new TravelPlanOutputSchemas.Recommendation("Book consult", "Review itinerary")),
                List.of(), List.of(), List.of(), null, List.of(), null, null, null, null,
                new TravelPlanOutputSchemas.MedicalCare(List.of(), List.of(), List.of()),
                new TravelPlanOutputSchemas.ItineraryGuidance("Single city", "Low complexity",
                        List.of(new TravelPlanOutputSchemas.RouteAdvice("Paris", "France", "Routine precautions")),
                        List.of()),
                List.of("Carry insurance details"),
                "This advice is not a substitute for care from your clinician.");

        assertThrows(PlanGenerationException.class, () -> TravelPlanOutputValidator.validate(output));
    }

    @Test
    void liteValidationAllowsAnthropicSchemaOmittedMedicalCare() {
        var output = new TravelPlanOutputSchemas.TravelHealthPlanOutput(
                "Plan", "Traveller", "Paris", "1 Jun", "LOW", null,
                new TravelPlanOutputSchemas.TripAtGlance(1, "Leisure", "Solo", "Hotel", "Yes"),
                List.of(new TravelPlanOutputSchemas.HealthRiskItem("Food and water", "LOW", "Use usual precautions")),
                List.of(new TravelPlanOutputSchemas.VaccinationItem("Routine vaccines", "Review", "Confirm current", "Review records")),
                null,
                List.of(new TravelPlanOutputSchemas.Recommendation("Book consult", "Review itinerary")),
                List.of(), List.of(), List.of(), null, List.of(), null, null, null, null,
                null,
                new TravelPlanOutputSchemas.ItineraryGuidance("Single city", "Low complexity",
                        List.of(new TravelPlanOutputSchemas.RouteAdvice("Paris", "France", "Routine precautions")),
                        List.of()),
                List.of("Carry insurance details"),
                "This advice is not a substitute for care from your clinician.");

        TravelPlanOutputValidator.validateLite(output);
    }
}
