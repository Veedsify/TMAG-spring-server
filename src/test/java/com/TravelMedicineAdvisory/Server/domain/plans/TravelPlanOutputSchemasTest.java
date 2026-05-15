package com.TravelMedicineAdvisory.Server.domain.plans;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class TravelPlanOutputSchemasTest {

    @Test
    void expandsAnthropicSingleTravellerParallelArraysIntoCanonicalDossier() {
        var core = new TravelPlanOutputSchemas.AnthropicTravelHealthPlanCore(
                "Travel Health Plan", "Ada Lovelace", "Lagos", "1 Jun to 10 Jun",
                "MODERATE", 10, "Business", "Solo", "Hotel", "Insurance active",
                List.of("Food and water", "Mosquito-borne disease"),
                List.of("MEDIUM", "HIGH"),
                List.of("Use bottled water", "Use bite prevention"),
                List.of("Yellow fever", "Typhoid"),
                List.of("Required", "Recommended"),
                List.of("Arrange before departure", "Discuss with clinician"),
                List.of("Bring certificate", "Complete before travel"),
                List.of("Book pre-travel consult"),
                List.of("Review vaccines and malaria prevention"),
                List.of("Live vaccine contraindication"),
                "Multi-city", "Review route-specific risk",
                List.of("Lagos"), List.of("Nigeria"), List.of("Higher malaria risk"),
                List.of("Seek care for fever"),
                List.of("Local Emergency"), List.of("112"),
                List.of("Carry vaccine records"),
                "This advice is not a substitute for care from your clinician.");
        var supp = new TravelPlanOutputSchemas.AnthropicTravelHealthPlanSupplemental(
                "MODERATE", List.of("Compression stockings", "Walk every 2 hours"), true, "Take anticoagulant before flight",
                "HIGH", "Doxycycline", "Resistance confirmed", List.of("DEET 30% repellent", "Bed net"), "None",
                List.of("Asthma"), List.of("Carry inhaler, avoid triggers"),
                "Original packaging", "Carry 30-day supply", true, false,
                List.of("Travel medicine"), List.of("Dr. Smith"), List.of("HIGH"),
                "LOW", List.of("Use condoms"), false,
                null, null, null, null, null,
                List.of("Monitor temperature"), List.of("Follow up with GP"), null, "Fever with rash requires immediate care",
                List.of("City General Hospital"), List.of("123 Main St"), List.of("+234 123 4567"), List.of("2km"), null,
                null, null);
 
        var expanded = TravelPlanOutputSchemas.expandAnthropic(core, supp);

        TravelPlanOutputValidator.validateLite(expanded);
        assertEquals("Ada Lovelace", expanded.travellerName());
        assertEquals("Business", expanded.tripAtGlance().purpose());
        assertEquals("MODERATE", expanded.flightHealth().vteRiskLevel());
        assertEquals("Doxycycline", expanded.malariaPrevention().recommendedAgent());
        assertEquals("Asthma", expanded.medicalConditions().getFirst().condition());
        assertEquals("Original packaging", expanded.medicationLogistics().packaging());
        assertEquals("Dr. Smith", expanded.specialistReferrals().getFirst().specialist());
        assertEquals("LOW", expanded.sexualHealth().riskLevel());
        assertEquals("Fever with rash requires immediate care", expanded.afterReturn().redFlag());
        assertEquals("City General Hospital", expanded.medicalCare().clinics().getFirst().name());
        assertEquals("112", expanded.medicalCare().emergencyContacts().getFirst().value());
        assertEquals("Food and water", expanded.healthRiskOverview().getFirst().category());
        assertEquals("Yellow fever", expanded.vaccinations().getFirst().vaccine());
        assertEquals("Book pre-travel consult", expanded.recommendations().getFirst().title());
        assertEquals("Lagos", expanded.itineraryGuidance().routeAdvice().getFirst().stop());
    }

    @Test
    void expandsAnthropicFamilyParallelArraysIntoCanonicalMembers() {
        var anthropicOutput = new TravelPlanOutputSchemas.AnthropicFamilyTravelHealthPlanOutput(
                "Accra", "Ghana", "Family trip", List.of("Yellow fever"),
                List.of(101, 102), List.of("Parent", "Child"), List.of("self", "child"),
                List.of("main applicant", "daughter"), List.of("you", "your daughter"),
                List.of(42, 8), List.of("Review routine vaccines", "Paediatric vaccine review"),
                List.of("Yellow fever certificate needed", "Discuss typhoid vaccine"),
                List.of("Carry regular medicines", "Pack paediatric medicines"),
                List.of("Food and water precautions", "Mosquito bite prevention"),
                List.of("Book clinic appointment", "Book paediatric appointment"),
                List.of(false, false),
                "This advice is not a substitute for care from your clinician.");

        var expanded = TravelPlanOutputSchemas.expandAnthropic(anthropicOutput);

        TravelPlanOutputValidator.validate(expanded);
        assertEquals(2, expanded.members().size());
        assertEquals(101L, expanded.members().getFirst().memberId());
        assertEquals("main applicant", expanded.members().getFirst().relationshipToMainApplicant());
        assertEquals("your daughter", expanded.members().get(1).displayLabel());
        assertFalse(expanded.members().get(1).vaccinations().isEmpty());
    }
 
    @Test
    void geminiSchemasRequireMandatoryDisclaimerAndActionSheetCaps() {
        var singleProps = TravelPlanOutputSchemas.SINGLE_TRAVELLER.geminiSchema().properties().orElseThrow();
        assertTrue(TravelPlanOutputSchemas.SINGLE_TRAVELLER.geminiSchema().required().orElseThrow()
                .contains("medicalDisclaimer"));
        assertFalse(singleProps.containsKey("clinicalFlags"));
        assertTrue(TravelPlanOutputSchemas.FAMILY.geminiSchema().required().orElseThrow()
                .contains("medicalDisclaimer"));
        assertTrue(TravelPlanOutputSchemas.ACTION_SHEET.geminiSchema().required().orElseThrow()
                .contains("closingLine"));
 
        var actionProps = TravelPlanOutputSchemas.ACTION_SHEET.geminiSchema().properties().orElseThrow();
        assertEquals(4L, actionProps.get("section1CriticalBeforeDeparture").maxItems().orElseThrow());
        assertEquals(18L, actionProps.get("section3Vaccines").maxItems().orElseThrow());
        assertEquals(8L, actionProps.get("section4PackAndRoutine").maxItems().orElseThrow());
    }
 
    @Test
    void mandatoryDisclaimerIsServerOwned() {
        var output = new TravelPlanOutputSchemas.TravelHealthPlanOutput(
                "Plan", "Traveller", "Paris", "1 Jun", "LOW", null,
                new TravelPlanOutputSchemas.TripAtGlance(1, "Leisure", "Solo", "Hotel", "Yes"),
                List.of(), List.of(), null, List.of(), List.of(), List.of(), List.of(), null,
                List.of(), null, null, null, null, null, null, List.of(), null);
 
        var fixed = TravelPlanOutputSchemas.withMandatoryDisclaimer(output);
 
        assertEquals(ClinicalRules.MANDATORY_DISCLAIMER, fixed.medicalDisclaimer());
    }
 
    @Test
    void decisionFlagsAreStrippedBeforePersistence() {
        var output = new TravelPlanOutputSchemas.TravelHealthPlanOutput(
                "Plan", "Traveller", "Paris", "1 Jun", "LOW", null,
                new TravelPlanOutputSchemas.TripAtGlance(1, "Leisure", "Solo", "Hotel", "Yes"),
                List.of(), List.of(), null, List.of(), List.of("TREE_1_HIGH_RISK"), List.of(), List.of(), null,
                List.of(), null, null, null, null, null, null, List.of(),
                ClinicalRules.MANDATORY_DISCLAIMER);
 
        var stripped = TravelPlanOutputSchemas.withoutDecisionFlags(output);
 
        assertEquals(null, stripped.clinicalFlags());
    }
}
