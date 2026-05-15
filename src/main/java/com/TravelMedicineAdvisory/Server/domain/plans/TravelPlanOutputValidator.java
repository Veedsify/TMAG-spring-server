package com.TravelMedicineAdvisory.Server.domain.plans;

import com.TravelMedicineAdvisory.Server.domain.plans.TravelPlanOutputSchemas.ActionSheetOutput;
import com.TravelMedicineAdvisory.Server.domain.plans.TravelPlanOutputSchemas.FamilyTravelHealthPlanOutput;
import com.TravelMedicineAdvisory.Server.domain.plans.TravelPlanOutputSchemas.TravelHealthPlanOutput;
import org.springframework.util.StringUtils;

/**
 * Validates that AI-generated output meets minimum content invariants after structured-output
 * deserialisation. Catches edge cases where the provider honours the schema's {@code required} set
 * with empty arrays or missing sections.
 */
public final class TravelPlanOutputValidator {

    private TravelPlanOutputValidator() {}

    /**
     * Validate a single-traveller dossier. Throws {@link PlanGenerationException} when critical
     * invariants are missing.
     */
    public static void validate(TravelHealthPlanOutput output) {
        if (output == null) {
            throw new PlanGenerationException("AI returned null output", null);
        }
        if (output.medicalDisclaimer() == null || output.medicalDisclaimer().isBlank()) {
            throw new PlanGenerationException("Generated plan is missing the mandatory medical disclaimer", null);
        }
        if (output.medicalCare() == null) {
            throw new PlanGenerationException("Generated plan is missing the medicalCare section", null);
        }
        if (output.medicalCare().emergencyContacts() == null || output.medicalCare().emergencyContacts().isEmpty()) {
            throw new PlanGenerationException("Generated plan is missing emergency contacts", null);
        }
        if (output.itineraryGuidance() == null) {
            throw new PlanGenerationException("Generated plan is missing the itineraryGuidance section", null);
        }
        if (output.itineraryGuidance().routeAdvice() == null || output.itineraryGuidance().routeAdvice().isEmpty()) {
            throw new PlanGenerationException("Generated plan is missing route advice", null);
        }
        if (output.recommendations() == null || output.recommendations().isEmpty()) {
            throw new PlanGenerationException("Generated plan contains no recommendations", null);
        }
        if (output.vaccinations() == null || output.vaccinations().isEmpty()) {
            throw new PlanGenerationException("Generated plan contains no vaccinations", null);
        }
        if (output.healthRiskOverview() == null || output.healthRiskOverview().isEmpty()) {
            throw new PlanGenerationException("Generated plan contains no health risk overview", null);
        }
    }

    /**
     * Validate a family dossier. Throws {@link PlanGenerationException} when critical invariants
     * are missing.
     */
    public static void validate(FamilyTravelHealthPlanOutput output) {
        if (output == null) {
            throw new PlanGenerationException("AI returned null output", null);
        }
        if (output.medicalDisclaimer() == null || output.medicalDisclaimer().isBlank()) {
            throw new PlanGenerationException("Generated family plan is missing the mandatory medical disclaimer", null);
        }
        if (output.members() == null || output.members().isEmpty()) {
            throw new PlanGenerationException("Generated family plan contains no members", null);
        }
        for (var member : output.members()) {
            if (member.memberId() == null) {
                throw new PlanGenerationException("Generated family plan member is missing memberId", null);
            }
        }
    }

    /**
     * Validate an Action Sheet output. Throws {@link PlanGenerationException} when critical
     * invariants are missing.
     */
    public static void validate(ActionSheetOutput output) {
        if (output == null) {
            throw new PlanGenerationException("AI returned null Action Sheet output", null);
        }
        if (!StringUtils.hasText(output.closingLine())) {
            throw new PlanGenerationException("Action Sheet is missing the closing line", null);
        }
    }

    /**
     * Lighter validation used with simplified schemas (Anthropic) that omit optional sections
     * like medicalCare, afterReturn, and specialist sections to stay within grammar size limits.
     */
    public static void validateLite(TravelHealthPlanOutput output) {
        if (output == null) {
            throw new PlanGenerationException("AI returned null output", null);
        }
        if (output.medicalDisclaimer() == null || output.medicalDisclaimer().isBlank()) {
            throw new PlanGenerationException("Generated plan is missing the mandatory medical disclaimer", null);
        }
        if (output.itineraryGuidance() == null) {
            throw new PlanGenerationException("Generated plan is missing the itineraryGuidance section", null);
        }
        if (output.itineraryGuidance().routeAdvice() == null || output.itineraryGuidance().routeAdvice().isEmpty()) {
            throw new PlanGenerationException("Generated plan is missing route advice", null);
        }
        if (output.recommendations() == null || output.recommendations().isEmpty()) {
            throw new PlanGenerationException("Generated plan contains no recommendations", null);
        }
        if (output.vaccinations() == null || output.vaccinations().isEmpty()) {
            throw new PlanGenerationException("Generated plan contains no vaccinations", null);
        }
        if (output.healthRiskOverview() == null || output.healthRiskOverview().isEmpty()) {
            throw new PlanGenerationException("Generated plan contains no health risk overview", null);
        }
    }
}
