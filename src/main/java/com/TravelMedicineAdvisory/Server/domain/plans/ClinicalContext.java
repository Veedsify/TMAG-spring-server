package com.TravelMedicineAdvisory.Server.domain.plans;

import java.util.List;

/**
 * Pre-computed clinical decision support context extracted from questionnaire responses.
 * Used to enrich the AI system prompt with validated clinical flags before generation.
 */
public record ClinicalContext(
        String overallRiskLevel,
        HardStopResult hardStop,
        List<String> triggeredTrees,
        List<String> contraindications,
        List<SpecialistReferral> specialistReferrals,
        String destinationRiskLevel,
        String preferredLanguage
) {
    public record HardStopResult(
            boolean triggered,
            String condition,
            String reason,
            String recommendedSpecialist
    ) {
        public static HardStopResult none() {
            return new HardStopResult(false, null, null, null);
        }

        public static HardStopResult of(String condition, String reason, String specialist) {
            return new HardStopResult(true, condition, reason, specialist);
        }
    }

    public record SpecialistReferral(
            String condition,
            String specialist,
            String urgency
    ) {}
}
