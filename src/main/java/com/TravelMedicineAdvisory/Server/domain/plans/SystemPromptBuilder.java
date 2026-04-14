package com.TravelMedicineAdvisory.Server.domain.plans;

import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

/**
 * Builds the complete AI system prompt for travel health plan generation.
 *
 * The prompt is structured in 11 parts:
 * 1. Role & Identity
 * 2. Scope & Hard Limits
 * 2A. Destination Validation Rules
 * 3. Input Format (questionnaire mapping from OnboardingQuestionSeeder)
 * 4. Processing Rules (execution order, cross-links, intensity modifier)
 * 5. Decision Trees Reference Library (all 14 trees with full clinical logic)
 * 6. Dynamic Pre-Computed Clinical Context (flags from ClinicalContextExtractor)
 * 7. Hard Stop Conditions
 * 8. Output Format & JSON Schema
 * 9. Mandatory Coverage Requirements
 * 10. Tone Rules & Mandatory Disclaimer
 * 11. Mandatory Disclaimer
 *
 * Questionnaire data comes from:
 * - {@link com.TravelMedicineAdvisory.Server.domain.travelplanquestionnaire.TravelPlanQuestionnaire}
 *   (responsesJson — per-trip questionnaire, JSON keyed by question keys)
 * - {@link com.TravelMedicineAdvisory.Server.domain.useronboarding.UserOnboarding}
 *   (responsesJson — initial health onboarding, keyed by OnboardingQuestionCategory)
 *
 * The ClinicalContext pre-evaluates all 14 decision trees against questionnaire responses
 * so the AI receives both the full clinical reference library AND traveller-specific flags.
 */
@Component
public class SystemPromptBuilder {

    public String build(ClinicalContext context) {
        StringBuilder prompt = new StringBuilder();

        // ================================================================
        // PART 1 — ROLE & IDENTITY
        // ================================================================
        prompt.append("═══════════════════════════════════════════════════════════\n");
        prompt.append("PART 1 — ROLE & IDENTITY\n");
        prompt.append("═══════════════════════════════════════════════════════════\n\n");
        prompt.append(ClinicalRules.ROLE_IDENTITY).append("\n\n");

        // ================================================================
        // PART 2 — SCOPE & HARD LIMITS
        // ================================================================
        prompt.append("═══════════════════════════════════════════════════════════\n");
        prompt.append("PART 2 — SCOPE & HARD LIMITS\n");
        prompt.append("═══════════════════════════════════════════════════════════\n\n");
        prompt.append(ClinicalRules.SCOPE_LIMITS).append("\n\n");

        // ================================================================
        // PART 2A — DESTINATION VALIDATION RULES
        // ================================================================
        prompt.append("═══════════════════════════════════════════════════════════\n");
        prompt.append("PART 2A — DESTINATION VALIDATION RULES\n");
        prompt.append("═══════════════════════════════════════════════════════════\n\n");
        prompt.append(ClinicalRules.DESTINATION_VALIDATION_RULES).append("\n\n");

        // ================================================================
        // PART 3 — INPUT FORMAT
        // ================================================================
        prompt.append("═══════════════════════════════════════════════════════════\n");
        prompt.append("PART 3 — INPUT FORMAT\n");
        prompt.append("═══════════════════════════════════════════════════════════\n\n");
        prompt.append(ClinicalRules.INPUT_FORMAT).append("\n\n");

        prompt.append("Questionnaire structure (from OnboardingQuestionSeeder):\n");
        prompt.append("  • Sections 1–9 correspond to onboarding categories:\n");
        prompt.append("    Section 1: Personal Information (date_of_birth, gender, preferred_language)\n");
        prompt.append("    Section 2: Travel Details (trip_itinerary, longest_flight_leg_hours, purpose_of_travel, travel_companions)\n");
        prompt.append("    Section 3: Accommodation & Environment (main_accommodation, stay_environment)\n");
        prompt.append("    Section 4: Planned Activities (planned_activities, altitude_travel, activity_frequency)\n");
        prompt.append("    Section 5: Medical History (chronic_medical_conditions, immunocompromised, current_medications, allergies, pregnancy_status, could_become_pregnant)\n");
        prompt.append("    Section 6: Vaccination & Travel Health (travel_related_vaccines_received, routine_vaccinations_status, vaccine_reaction_history)\n");
        prompt.append("    Section 7: Travel History (international_travel_last_12_months, travel_frequency, previous_trip_health_preparations, previous_trip_health_problems)\n");
        prompt.append("    Section 8: Awareness & Preparation (has_primary_care_physician, travel_insurance)\n");
        prompt.append("    Section 9: Personal Health & Risk Behaviours (anticipated_risk_behaviours, sexual_activity_protection, sti_history, substance_use_adherence_risk)\n\n");

        // ================================================================
        // PART 4 — PROCESSING RULES
        // ================================================================
        prompt.append("═══════════════════════════════════════════════════════════\n");
        prompt.append("PART 4 — PROCESSING RULES\n");
        prompt.append("═══════════════════════════════════════════════════════════\n\n");
        prompt.append(ClinicalRules.PROCESSING_RULES).append("\n\n");

        // ================================================================
        // PART 5 — DECISION TREES (Reference Library — ALL 14)
        // ================================================================
        prompt.append("═══════════════════════════════════════════════════════════\n");
        prompt.append("PART 5 — DECISION TREES (Reference Library)\n");
        prompt.append("═══════════════════════════════════════════════════════════\n\n");
        prompt.append("All 14 decision trees are embedded below. You MUST apply every applicable\n");
        prompt.append("tree to this traveller. Cross-links are mandatory — never apply a tree in isolation.\n\n");
        prompt.append(ClinicalRules.ALL_DECISION_TREES).append("\n\n");

        // ================================================================
        // PART 6 — PRE-COMPUTED CLINICAL CONTEXT (traveller-specific flags)
        // ================================================================
        prompt.append("═══════════════════════════════════════════════════════════\n");
        prompt.append("PART 6 — PRE-COMPUTED CLINICAL CONTEXT FOR THIS TRAVELLER\n");
        prompt.append("═══════════════════════════════════════════════════════════\n\n");
        prompt.append("The following clinical flags have been pre-evaluated from the questionnaire responses.\n");
        prompt.append("Use these to guide your advisory generation — they represent the server-side\n");
        prompt.append("evaluation of the 14 decision trees above against this specific traveller.\n\n");

        prompt.append("Overall Risk Level: ").append(context.overallRiskLevel()).append("\n");
        prompt.append("Destination Risk Level: ").append(context.destinationRiskLevel()).append("\n");
        prompt.append("Preferred Language (Q8): ").append(context.preferredLanguage()).append("\n\n");

        if (context.hardStop().triggered()) {
            prompt.append("⚠  HARD STOP TRIGGERED  ⚠\n");
            prompt.append("Condition: ").append(context.hardStop().condition()).append("\n");
            prompt.append("Reason: ").append(context.hardStop().reason()).append("\n");
            prompt.append("Recommended Specialist: ").append(context.hardStop().recommendedSpecialist()).append("\n\n");
            prompt.append("DO NOT generate a standard advisory. Generate ONLY a hard stop response with:\n");
            prompt.append("1. Clear explanation of the triggered condition\n");
            prompt.append("2. Why this presents significant risk\n");
            prompt.append("3. Statement: 'We strongly recommend you do not travel at this time without specialist clearance.'\n");
            prompt.append("4. Specialist referral information\n");
            prompt.append("5. Mandatory disclaimer\n\n");
        } else {
            prompt.append("Triggered Decision Trees (pre-evaluated):\n");
            if (context.triggeredTrees().isEmpty()) {
                prompt.append("  (None — standard risk profile)\n");
            } else {
                for (String tree : context.triggeredTrees()) {
                    prompt.append("  • ").append(tree).append("\n");
                }
            }
            prompt.append("\n");

            prompt.append("Active Contraindications (medications & vaccines):\n");
            if (context.contraindications().isEmpty()) {
                prompt.append("  (None identified)\n");
            } else {
                for (String contraindication : context.contraindications()) {
                    prompt.append("  • ").append(contraindication).append("\n");
                }
            }
            prompt.append("\n");

            prompt.append("Specialist Referrals Required:\n");
            if (context.specialistReferrals().isEmpty()) {
                prompt.append("  (None required)\n");
            } else {
                for (ClinicalContext.SpecialistReferral referral : context.specialistReferrals()) {
                    prompt.append("  • ").append(referral.condition())
                            .append(" → ").append(referral.specialist())
                            .append(" [").append(referral.urgency()).append("]\n");
                }
            }
            prompt.append("\n");
        }

        // ================================================================
        // PART 7 — HARD STOP CONDITIONS
        // ================================================================
        prompt.append("═══════════════════════════════════════════════════════════\n");
        prompt.append("PART 7 — HARD STOP CONDITIONS\n");
        prompt.append("═══════════════════════════════════════════════════════════\n\n");
        prompt.append(ClinicalRules.HARD_STOP_CONDITIONS).append("\n\n");

        // ================================================================
        // PART 8 — OUTPUT FORMAT & JSON SCHEMA
        // ================================================================
        prompt.append("═══════════════════════════════════════════════════════════\n");
        prompt.append("PART 8 — OUTPUT FORMAT & JSON SCHEMA\n");
        prompt.append("═══════════════════════════════════════════════════════════\n\n");

        prompt.append("Advisory Section Order (text advisory — Part 7 of original system-prompt.txt):\n");
        prompt.append(ClinicalRules.OUTPUT_FORMAT).append("\n\n");

        prompt.append("Return ONLY valid JSON matching this exact schema:\n");
        prompt.append(buildJsonSchema(context)).append("\n\n");

        // ================================================================
        // PART 9 — MANDATORY COVERAGE REQUIREMENTS
        // ================================================================
        prompt.append("═══════════════════════════════════════════════════════════\n");
        prompt.append("PART 9 — MANDATORY COVERAGE REQUIREMENTS\n");
        prompt.append("═══════════════════════════════════════════════════════════\n\n");
        prompt.append("You MUST include all categories below, even if risk is minimal.\n");
        prompt.append("For low-relevance items, use level LOW and brief explanation.\n\n");

        prompt.append("Health Risk Overview (mandatory categories in order):\n");
        for (String category : ClinicalRules.MANDATORY_HEALTH_RISK_CATEGORIES) {
            prompt.append("  • ").append(category).append("\n");
        }
        prompt.append("\n");

        prompt.append("Vaccination Topics (mandatory in order):\n");
        for (String topic : ClinicalRules.MANDATORY_VACCINATION_TOPICS) {
            prompt.append("  • ").append(topic).append("\n");
        }
        prompt.append("\n");

        prompt.append("Recommendation Topics (mandatory in order):\n");
        for (String topic : ClinicalRules.MANDATORY_RECOMMENDATION_TOPICS) {
            prompt.append("  • ").append(topic).append("\n");
        }
        prompt.append("\n");

        // ================================================================
        // PART 10 — TONE & LANGUAGE RULES
        // ================================================================
        prompt.append("═══════════════════════════════════════════════════════════\n");
        prompt.append("PART 10 — TONE & LANGUAGE RULES\n");
        prompt.append("═══════════════════════════════════════════════════════════\n\n");
        prompt.append(ClinicalRules.TONE_RULES).append("\n\n");

        // ================================================================
        // PART 11 — MANDATORY DISCLAIMER
        // ================================================================
        prompt.append("═══════════════════════════════════════════════════════════\n");
        prompt.append("PART 11 — MANDATORY DISCLAIMER\n");
        prompt.append("═══════════════════════════════════════════════════════════\n\n");
        prompt.append("Append this exact disclaimer to EVERY advisory:\n\n");
        prompt.append(ClinicalRules.MANDATORY_DISCLAIMER).append("\n");

        return prompt.toString();
    }

    private String buildJsonSchema(ClinicalContext context) {
        return """
                {
                  "reportTitle": "string",
                  "travellerName": "string",
                  "destination": "string",
                  "travelDates": "string",
                  "overallRiskLevel": "LOW|MEDIUM|HIGH|VERY_HIGH",
                  "hardStop": null | {
                    "conditionTriggered": "string",
                    "reason": "string",
                    "recommendedSpecialist": "string"
                  },
                  "tripAtGlance": {
                    "durationDays": number,
                    "purpose": "string",
                    "travelling": "string",
                    "accommodation": "string",
                    "insurance": "string"
                  },
                  "healthRiskOverview": [
                    {"category":"string","level":"LOW|MODERATE|HIGH","summary":"string"}
                  ],
                  "vaccinations": [
                    {"vaccine":"string","status":"string","recommendation":"string","action":"string"}
                  ],
                  "malariaPrevention": null | {
                    "riskLevel": "LOW|MODERATE|HIGH|NOT_INDICATED",
                    "recommendedAgent": "string",
                    "rationale": "string",
                    "mosquitoProtection": ["string"],
                    "contraindications": "string"
                  },
                  "recommendations": [
                    {"title":"string","details":"string"}
                  ],
                  "clinicalFlags": ["string"],
                  "contraindications": ["string"],
                  "specialistReferrals": [
                    {"condition":"string","specialist":"string","urgency":"ROUTINE|BEFORE_TRAVEL|URGENT"}
                  ],
                  "flightHealth": null | {
                    "vteRiskLevel": "LOW|MEDIUM|HIGH",
                    "preventionMeasures": ["string"],
                    "medifClearanceRequired": "boolean",
                    "medicationTimingGuidance": "string"
                  },
                  "medicalConditions": [
                    {"condition":"string","precautions":"string"}
                  ],
                  "medicationLogistics": {
                    "packaging":"string",
                    "supplyRule":"string",
                    "destinationLegalityCheck":"boolean",
                    "coldChainRequired":"boolean"
                  },
                  "sexualHealth": null | {
                    "riskLevel": "LOW|MODERATE|HIGH",
                    "preventionAdvice": ["string"],
                    "prepPepDiscussion": "boolean"
                  },
                  "pregnancyGuidance": null | {
                    "trimesterSpecificAdvice": "string",
                    "liveVaccineContraindications": ["string"],
                    "antimalarialSafety": "string",
                    "airlineRestrictions": "string",
                    "contraceptionCounselling": "string"
                  },
                  "afterReturn": {
                    "within1Week":["string"],
                    "within4Weeks":["string"],
                    "beyond4Weeks":["string"],
                    "redFlag":"string"
                  },
                  "medicalCare": {
                    "clinics":[{"name":"string","address":"string","phone":"string","distance":"string","notes":"string"}],
                    "embassyContacts":[{"name":"string","details":"string"}],
                    "emergencyContacts":[{"label":"string","value":"string"}]
                  },
                  "itineraryGuidance": {
                    "tripType":"ONE_WAY|RETURN|MULTI_STOP",
                    "summary":"string",
                    "routeAdvice":[{"stop":"string","country":"string","guidance":"string"}],
                    "returnGuidance":["string"]
                  },
                  "nextSteps": ["string"],
                  "medicalDisclaimer":"string"
                }

                CRITICAL INSTRUCTIONS:

                1. The user prompt has a "Current trip" section: that is the ONLY authoritative source for
                   destination, country, trip length, purpose, and travel dates in your JSON output.

                2. For RETURN trips, when Departure date and Return date appear in that section, set travelDates
                   to a clear human-readable range using those exact dates, and set tripAtGlance.durationDays
                   to the inclusive calendar day count (must match "Trip length" in the user prompt).

                3. A separate "Traveller health context" block contains questionnaire JSON for medical
                   personalisation only; never infer this trip's destination or itinerary from it
                   (server still treats the Current trip block as sole truth).

                4. Use concise, practical medical-travel guidance. Do not include markdown fences.

                5. Pre-computed clinical context above provides:
                   - overallRiskLevel: Use this value in your JSON output
                   - clinicalFlags: Copy the "Triggered Decision Trees" list to this field
                   - contraindications: Copy the "Active Contraindications" list to this field
                   - specialistReferrals: Copy the "Specialist Referrals Required" list to this field
                   - hardStop: If "HARD STOP TRIGGERED" appears above, populate this object; otherwise null

                6. MANDATORY — Apply all 14 decision trees (Part 5 above) to this traveller:
                   - Evaluate each tree against the questionnaire responses in the user prompt.
                   - Apply cross-links between trees — no tree operates in isolation.
                   - Where trees produce conflicting risk levels, take the HIGHEST level.
                   - Multiple risk factors converging → escalate recommendations proportionally.

                7. MANDATORY — healthRiskOverview must include EXACTLY one object per category
                   (even if risk is minimal — use level LOW and a brief explanation):
                   • "Food and water safety"
                   • "Vector-borne diseases"
                   • "Respiratory infections"
                   • "Environmental health (heat, sun, air quality)"
                   • "Injuries and road traffic safety"
                   • "Rabies and animal contact"
                   • "Blood-borne and sexual health"
                   • "Altitude-related illness"

                8. MANDATORY — vaccinations must include one object per topic (even if not indicated):
                   • "Routine immunizations (e.g. MMR, varicella, dTdap, polio/IPV)"
                   • "Influenza"
                   • "COVID-19"
                   • "Hepatitis A"
                   • "Hepatitis B"
                   • "Typhoid"
                   • "Yellow fever"
                   • "Japanese encephalitis"
                   • "Meningococcal"
                   • "Rabies pre-exposure"
                   • "Cholera (oral vaccine)"

                9. MANDATORY — recommendations must include at least one object per topic:
                   • "Pre-travel review & vaccination records"
                   • "Food and water hygiene"
                   • "Vector bite prevention"
                   • "Sun, heat, and environmental precautions"
                   • "Injury and road safety"
                   • "Sexual health and blood exposure"
                   • "Jet lag, sleep, and mental wellbeing"
                   • "Malaria and other chemoprophylaxis (state if not indicated)"
                   • "Traveller-specific considerations (from health context)"

                10. Optional sections (set to null if not applicable):
                    - malariaPrevention: if destination is not malaria-risk
                    - flightHealth: if flight profile is not clinically significant
                    - sexualHealth: if Section 9 responses indicate no risk behaviours
                    - pregnancyGuidance: if Q28 = No AND Q29 = No

                11. itineraryGuidance requirements:
                    - tripType must exactly mirror the Current trip "Trip Type" value.
                    - routeAdvice:
                      • ONE_WAY: include exactly one stop guidance row.
                      • RETURN: include outbound stop guidance and include practical return-phase reminders in returnGuidance.
                      • MULTI_STOP: include one guidance row per stop from Trip Stops JSON, in listed order.
                    - returnGuidance:
                      • RETURN: include at least 4 actionable bullets for the return leg and immediate post-return period.
                      • ONE_WAY or MULTI_STOP: returnGuidance may be an empty array unless there is explicit return information.

                12. medicalDisclaimer: Copy the exact disclaimer text from Part 11 above.
                """;
    }
}
