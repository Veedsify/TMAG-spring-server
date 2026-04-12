package com.TravelMedicineAdvisory.Server.domain.plans;

import com.TravelMedicineAdvisory.Server.domain.travelplan.TravelPlan;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Extracts clinical decision support context from questionnaire responses.
 * Evaluates the 14 decision trees and hard stop conditions to produce structured
 * clinical flags that enrich the AI system prompt.
 */
@Service
public class ClinicalContextExtractor {

    private static final Logger log = LoggerFactory.getLogger(ClinicalContextExtractor.class);

    private final ObjectMapper objectMapper;

    public ClinicalContextExtractor(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public ClinicalContext extract(TravelPlan travelPlan, String questionnaireJson) {
        try {
            JsonNode questionnaire = parseQuestionnaire(questionnaireJson);
            
            // Check hard stops first
            ClinicalContext.HardStopResult hardStop = evaluateHardStops(travelPlan, questionnaire);
            if (hardStop.triggered()) {
                return buildHardStopContext(hardStop, travelPlan);
            }

            // Evaluate all 14 decision trees in the prescribed order
            List<String> triggeredTrees = new ArrayList<>();
            List<String> contraindications = new ArrayList<>();
            List<ClinicalContext.SpecialistReferral> referrals = new ArrayList<>();

            evaluateTree1Age(travelPlan, questionnaire, triggeredTrees, contraindications, referrals);
            evaluateTree2Pregnancy(questionnaire, triggeredTrees, contraindications, referrals);
            evaluateTree3Destination(travelPlan, triggeredTrees);
            evaluateTree4Duration(travelPlan, triggeredTrees);
            evaluateTree5Flight(travelPlan, questionnaire, triggeredTrees, contraindications, referrals);
            evaluateTree6Purpose(travelPlan, triggeredTrees);
            evaluateTree7Companions(questionnaire, triggeredTrees);
            evaluateTree8Accommodation(questionnaire, triggeredTrees);
            evaluateTree9Activities(travelPlan, questionnaire, triggeredTrees, contraindications, referrals);
            evaluateTree10MedicalHistory(questionnaire, triggeredTrees, contraindications, referrals);
            evaluateTree11Medications(questionnaire, triggeredTrees, contraindications, referrals);
            evaluateTree12VaccinationHistory(questionnaire, triggeredTrees, contraindications, referrals);
            evaluateTree13TravelHistory(questionnaire, triggeredTrees);
            evaluateTree14RiskBehaviours(questionnaire, triggeredTrees, contraindications, referrals);

            String destinationRisk = classifyDestinationRisk(travelPlan);
            String overallRisk = calculateOverallRisk(travelPlan, triggeredTrees.size());
            String preferredLanguage = extractPreferredLanguage(questionnaire);

            return new ClinicalContext(
                    overallRisk,
                    ClinicalContext.HardStopResult.none(),
                    triggeredTrees,
                    contraindications,
                    referrals,
                    destinationRisk,
                    preferredLanguage
            );
        } catch (Exception ex) {
            log.warn("Failed to extract clinical context: {}", ex.getMessage());
            return buildMinimalContext(travelPlan);
        }
    }

    private JsonNode parseQuestionnaire(String questionnaireJson) {
        if (!StringUtils.hasText(questionnaireJson)) {
            return objectMapper.createObjectNode();
        }
        try {
            return objectMapper.readTree(questionnaireJson);
        } catch (Exception ex) {
            log.warn("Failed to parse questionnaire JSON: {}", ex.getMessage());
            return objectMapper.createObjectNode();
        }
    }

    private ClinicalContext.HardStopResult evaluateHardStops(TravelPlan travelPlan, JsonNode questionnaire) {
        // HARD STOP 1: Pregnancy >36 weeks singleton OR >32 weeks multiples AND long-haul flight
        JsonNode pregnancy = questionnaire.path("pregnancy");
        if (pregnancy.path("isPregnant").asBoolean(false)) {
            int weeks = pregnancy.path("gestationalWeeks").asInt(0);
            boolean multiples = pregnancy.path("multiples").asBoolean(false);
            int cutoff = multiples ? ClinicalRules.PREGNANCY_AIRLINE_CUTOFF_MULTIPLES 
                                   : ClinicalRules.PREGNANCY_AIRLINE_CUTOFF_SINGLETON;
            
            if (weeks > cutoff && isLongHaulFlight(travelPlan)) {
                return ClinicalContext.HardStopResult.of(
                        ClinicalRules.HARD_STOP_1,
                        String.format("Pregnancy at %d weeks %s with long-haul flight presents significant risk. " +
                                "Most airlines prohibit travel beyond %d weeks. Risk of in-flight complications is unacceptably high.",
                                weeks, multiples ? "(multiples)" : "(singleton)", cutoff),
                        "Obstetrician"
                );
            }
        }

        // HARD STOP 2: Immunocompromised AND live vaccine mandatory entry requirement
        boolean immunocompromised = isImmunocompromised(questionnaire);
        if (immunocompromised && requiresLiveVaccineEntry(travelPlan)) {
            return ClinicalContext.HardStopResult.of(
                    ClinicalRules.HARD_STOP_2,
                    "Immunocompromised status contraindicates live vaccines (e.g., yellow fever), " +
                            "but destination requires it for entry with no waiver pathway available. " +
                            "Travel is not medically advisable without specialist clearance.",
                    "Travel medicine specialist or infectious disease specialist"
            );
        }

        // HARD STOP 3: Poorly controlled condition AND high-risk destination
        boolean poorlyControlled = questionnaire.path("medicalHistory").path("poorlyControlled").asBoolean(false);
        if (poorlyControlled && "HIGH".equals(classifyDestinationRisk(travelPlan))) {
            return ClinicalContext.HardStopResult.of(
                    ClinicalRules.HARD_STOP_3,
                    "Poorly controlled or recently worsened major medical condition combined with high-risk destination. " +
                            "Risk of medical emergency without adequate local healthcare infrastructure is unacceptably high.",
                    "Treating physician and travel medicine specialist"
            );
        }

        // HARD STOP 4: Specialist "do not travel" flag
        if (questionnaire.path("medicalHistory").path("specialistDoNotTravel").asBoolean(false)) {
            return ClinicalContext.HardStopResult.of(
                    ClinicalRules.HARD_STOP_4,
                    "Treating specialist has flagged this traveller as 'do not travel' due to medical condition severity.",
                    "Treating specialist"
            );
        }

        return ClinicalContext.HardStopResult.none();
    }

    private void evaluateTree1Age(TravelPlan travelPlan, JsonNode questionnaire,
                                  List<String> triggeredTrees, List<String> contraindications,
                                  List<ClinicalContext.SpecialistReferral> referrals) {
        int age = questionnaire.path("age").asInt(0);
        if (age <= 0) {
            // Try to calculate from date of birth
            String dob = questionnaire.path("q2").path("dateOfBirth").asText("");
            if (StringUtils.hasText(dob)) {
                try {
                    java.time.LocalDate birthDate = java.time.LocalDate.parse(dob);
                    age = java.time.Period.between(birthDate, java.time.LocalDate.now()).getYears();
                } catch (Exception ignored) {
                    // Age unknown — skip tree
                    return;
                }
            }
        }
        if (age <= 0) return;

        triggeredTrees.add("TREE_1_AGE");

        if (age < 1) {
            contraindications.add("Most live vaccines deferred for infants <1 year");
            referrals.add(new ClinicalContext.SpecialistReferral(
                    "Infant <1 year",
                    "Paediatric infectious disease or travel medicine specialist",
                    "BEFORE_TRAVEL"
            ));
        }

        if (age >= 65) {
            contraindications.add("Heightened VTE risk for long-haul flights");
            triggeredTrees.add("TREE_1_GERIATRIC");
        }

        if (age < 18 || age >= 65) {
            int flightHours = estimateFlightHours(travelPlan);
            if (flightHours >= ClinicalRules.VTE_MEDIUM_THRESHOLD) {
                referrals.add(new ClinicalContext.SpecialistReferral(
                        "Age " + age + " with long-haul flight",
                        "Physician (fitness-to-fly assessment)",
                        "BEFORE_TRAVEL"
                ));
            }
        }
    }

    private void evaluateTree2Pregnancy(JsonNode questionnaire, List<String> triggeredTrees,
                                        List<String> contraindications, List<ClinicalContext.SpecialistReferral> referrals) {
        JsonNode pregnancy = questionnaire.path("pregnancy");
        if (pregnancy.path("isPregnant").asBoolean(false)) {
            triggeredTrees.add("TREE_2_PREGNANCY");
            contraindications.add("All live vaccines (yellow fever, MMR, varicella, oral typhoid)");
            contraindications.add("Doxycycline for malaria prophylaxis");
            contraindications.add("Primaquine and tafenoquine");

            int weeks = pregnancy.path("gestationalWeeks").asInt(0);
            if (weeks >= ClinicalRules.PREGNANCY_OBSTETRIC_LETTER_REQUIRED) {
                referrals.add(new ClinicalContext.SpecialistReferral(
                        "Pregnancy ≥28 weeks",
                        "Obstetrician (airline clearance letter required)",
                        "BEFORE_TRAVEL"
                ));
            }
        }

        if (pregnancy.path("couldBecomePregnant").asBoolean(false)) {
            triggeredTrees.add("TREE_2_REPRODUCTIVE_RISK");
            contraindications.add("Teratogenic antimalarials (doxycycline, primaquine, tafenoquine) without effective contraception");
        }
    }

    private void evaluateTree3Destination(TravelPlan travelPlan, List<String> triggeredTrees) {
        String risk = classifyDestinationRisk(travelPlan);
        if ("HIGH".equals(risk)) {
            triggeredTrees.add("TREE_3_HIGH_RISK_DESTINATION");
        } else if ("MEDIUM".equals(risk)) {
            triggeredTrees.add("TREE_3_MEDIUM_RISK_DESTINATION");
        }
    }

    private void evaluateTree4Duration(TravelPlan travelPlan, List<String> triggeredTrees) {
        Integer durationDays = travelPlan.getDuration();
        if (durationDays == null || durationDays <= 0) return;

        triggeredTrees.add("TREE_4_DURATION");

        if (durationDays > ClinicalRules.DURATION_MEDIUM) {
            triggeredTrees.add("TREE_4_LONG_STAY");
        }
    }

    private void evaluateTree5Flight(TravelPlan travelPlan, JsonNode questionnaire,
                                     List<String> triggeredTrees, List<String> contraindications,
                                     List<ClinicalContext.SpecialistReferral> referrals) {
        int flightHours = estimateFlightHours(travelPlan);
        if (flightHours <= 0) return;

        boolean vteRiskFactors = questionnaire.path("vteRiskFactors").asBoolean(false);
        boolean pregnancy = questionnaire.path("pregnancy").path("isPregnant").asBoolean(false);
        boolean cardiorespiratory = questionnaire.path("medicalHistory").path("cardiorespiratory").asBoolean(false);

        triggeredTrees.add("TREE_5_FLIGHT");

        if (flightHours >= ClinicalRules.VTE_HIGH_THRESHOLD ||
            (flightHours >= ClinicalRules.VTE_MEDIUM_THRESHOLD && (vteRiskFactors || pregnancy))) {
            triggeredTrees.add("TREE_5_VTE_HIGH_RISK");
            referrals.add(new ClinicalContext.SpecialistReferral(
                    "High VTE risk (long-haul flight + risk factors)",
                    "Physician (LMWH consideration)",
                    "BEFORE_TRAVEL"
            ));
        }

        if (cardiorespiratory && flightHours >= ClinicalRules.VTE_MEDIUM_THRESHOLD) {
            referrals.add(new ClinicalContext.SpecialistReferral(
                    "Cardiorespiratory condition with long-haul flight",
                    "Physician (MEDIF clearance for airline)",
                    "BEFORE_TRAVEL"
            ));
        }
    }

    private void evaluateTree6Purpose(TravelPlan travelPlan, List<String> triggeredTrees) {
        String purpose = travelPlan.getPurpose();
        if (purpose == null) return;

        String normalized = purpose.toLowerCase();

        if (normalized.contains("visiting family") || normalized.contains("vfr")) {
            triggeredTrees.add("TREE_6_VFR");
        }
        if (normalized.contains("pilgrimage") || normalized.contains("religious") ||
            normalized.contains("hajj") || normalized.contains("umrah")) {
            triggeredTrees.add("TREE_6_PILGRIMAGE");
        }
        if (normalized.contains("humanitarian") || normalized.contains("volunteer") ||
            normalized.contains("healthcare") || normalized.contains("lab")) {
            triggeredTrees.add("TREE_6_HIGH_RISK_PURPOSE");
        }
        if (normalized.contains("study") || normalized.contains("relocation")) {
            triggeredTrees.add("TREE_6_LONG_STAY");
        }
    }

    private void evaluateTree7Companions(JsonNode questionnaire, List<String> triggeredTrees) {
        JsonNode companions = questionnaire.path("companions");
        if (!companions.isMissingNode()) {
            if (companions.path("alone").asBoolean(false)) {
                triggeredTrees.add("TREE_7_SOLO_TRAVELLER");
            }
            if (companions.path("withFamily").asBoolean(false) ||
                companions.path("withChildren").asBoolean(false) ||
                companions.path("withElderly").asBoolean(false)) {
                triggeredTrees.add("TREE_7_VULNERABLE_COMPANIONS");
            }
        }
    }

    private void evaluateTree8Accommodation(JsonNode questionnaire, List<String> triggeredTrees) {
        JsonNode accommodation = questionnaire.path("accommodation");
        if (!accommodation.isMissingNode()) {
            String type = accommodation.path("type").asText("").toLowerCase();
            if (type.contains("family") || type.contains("friend") || type.contains("student")) {
                triggeredTrees.add("TREE_8_INCREASED_INFECTION_RISK");
            }
            if (type.contains("rural")) {
                triggeredTrees.add("TREE_8_RURAL");
            }
        }
    }

    private void evaluateTree9Activities(TravelPlan travelPlan, JsonNode questionnaire,
                                         List<String> triggeredTrees, List<String> contraindications,
                                         List<ClinicalContext.SpecialistReferral> referrals) {
        JsonNode activities = questionnaire.path("activities");
        if (activities.isMissingNode()) return;

        if (activities.path("animalContact").asBoolean(false) || activities.path("caving").asBoolean(false)) {
            triggeredTrees.add("TREE_9_RABIES_RISK");
            referrals.add(new ClinicalContext.SpecialistReferral(
                    "Animal contact or caving in rabies-endemic area",
                    "Travel medicine clinic (rabies pre-exposure prophylaxis)",
                    "ROUTINE"
            ));
        }

        if (activities.path("scubaDiving").asBoolean(false)) {
            triggeredTrees.add("TREE_9_DIVING_RISK");
            referrals.add(new ClinicalContext.SpecialistReferral(
                    "Scuba diving planned",
                    "Dive medicine physician (fitness-to-dive assessment)",
                    "BEFORE_TRAVEL"
            ));
        }

        if (activities.path("freshwaterSwimming").asBoolean(false)) {
            triggeredTrees.add("TREE_9_SCHISTOSOMIASIS_RISK");
        }

        if (activities.path("highAltitude").asBoolean(false)) {
            triggeredTrees.add("TREE_9_ALTITUDE_RISK");
            contraindications.add("Acetazolamide contraindicated in sulfonamide allergy");
        }

        if (activities.path("healthcareWork").asBoolean(false) || activities.path("labWork").asBoolean(false)) {
            triggeredTrees.add("TREE_9_BLOODBORNE_RISK");
            referrals.add(new ClinicalContext.SpecialistReferral(
                    "Healthcare/lab work — blood-borne pathogen risk",
                    "Occupational health (hepatitis B status, post-exposure protocol)",
                    "BEFORE_TRAVEL"
            ));
        }

        if (activities.path("crowdedEvents").asBoolean(false)) {
            triggeredTrees.add("TREE_9_RESPIRATORY_RISK");
        }
    }

    private void evaluateTree10MedicalHistory(JsonNode questionnaire, List<String> triggeredTrees,
                                              List<String> contraindications, List<ClinicalContext.SpecialistReferral> referrals) {
        JsonNode medicalHistory = questionnaire.path("medicalHistory");
        if (medicalHistory.isMissingNode()) return;

        if (isImmunocompromised(questionnaire)) {
            triggeredTrees.add("TREE_10_IMMUNOCOMPROMISED");
            contraindications.add("All live vaccines");
            contraindications.add("Mefloquine (prefer atovaquone-proguanil for malaria)");
            referrals.add(new ClinicalContext.SpecialistReferral(
                    "Immunocompromised status",
                    "Infectious disease specialist or travel medicine specialist",
                    "BEFORE_TRAVEL"
            ));
        }

        if (medicalHistory.path("cardiorespiratory").asBoolean(false)) {
            triggeredTrees.add("TREE_10_CARDIORESPIRATORY");
            contraindications.add("Mefloquine (cardiac conduction risk)");
            referrals.add(new ClinicalContext.SpecialistReferral(
                    "Cardiorespiratory condition",
                    "Cardiologist or pulmonologist (fitness-to-fly assessment)",
                    "BEFORE_TRAVEL"
            ));
        }

        if (medicalHistory.path("diabetes").asBoolean(false) || medicalHistory.path("metabolic").asBoolean(false)) {
            triggeredTrees.add("TREE_10_METABOLIC");
        }

        if (medicalHistory.path("neuropsychiatric").asBoolean(false) ||
            medicalHistory.path("epilepsy").asBoolean(false) ||
            medicalHistory.path("depression").asBoolean(false)) {
            triggeredTrees.add("TREE_10_NEUROPSYCHIATRIC");
            contraindications.add("Mefloquine (neuropsychiatric contraindication)");
        }

        if (medicalHistory.path("g6pdDeficiency").asBoolean(false) ||
            medicalHistory.path("sickleCell").asBoolean(false) ||
            medicalHistory.path("vteHistory").asBoolean(false)) {
            triggeredTrees.add("TREE_10_HAEMATOLOGIC");
            if (medicalHistory.path("g6pdDeficiency").asBoolean(false)) {
                contraindications.add("Primaquine and tafenoquine");
            }
        }

        if (medicalHistory.path("recentSurgery").asBoolean(false) ||
            medicalHistory.path("recentIllness").asBoolean(false)) {
            triggeredTrees.add("TREE_10_RECENT_SURGERY");
            referrals.add(new ClinicalContext.SpecialistReferral(
                    "Recent surgery or serious illness (<12 months)",
                    "Treating physician (fitness-to-travel assessment)",
                    "BEFORE_TRAVEL"
            ));
        }

        if (medicalHistory.path("poorlyControlled").asBoolean(false)) {
            triggeredTrees.add("TREE_10_POORLY_CONTROLLED");
        }
    }

    private void evaluateTree11Medications(JsonNode questionnaire, List<String> triggeredTrees,
                                           List<String> contraindications, List<ClinicalContext.SpecialistReferral> referrals) {
        JsonNode medications = questionnaire.path("medications");
        if (medications.isMissingNode()) return;

        if (medications.path("antiretrovirals").asBoolean(false)) {
            triggeredTrees.add("TREE_11_ANTIRETROVIRALS");
        }

        if (medications.path("anticonvulsants").asBoolean(false)) {
            triggeredTrees.add("TREE_11_ANTICONVULSANTS");
            contraindications.add("Mefloquine");
            contraindications.add("Doxycycline (reduced efficacy with enzyme inducers)");
        }

        if (medications.path("anticoagulants").asBoolean(false)) {
            triggeredTrees.add("TREE_11_ANTICOAGULANTS");
            referrals.add(new ClinicalContext.SpecialistReferral(
                    "Anticoagulant therapy",
                    "Physician (INR monitoring logistics, injury-risk activity counselling)",
                    "ROUTINE"
            ));
        }

        if (medications.path("insulin").asBoolean(false)) {
            triggeredTrees.add("TREE_11_INSULIN");
        }

        if (medications.path("immunosuppressants").asBoolean(false)) {
            triggeredTrees.add("TREE_11_IMMUNOSUPPRESSANTS");
        }

        if (medications.path("photosensitising").asBoolean(false)) {
            triggeredTrees.add("TREE_11_PHOTOSENSITISING");
        }

        JsonNode allergies = questionnaire.path("allergies");
        if (allergies.isMissingNode()) return;

        if (allergies.path("anaphylaxis").asBoolean(false)) {
            triggeredTrees.add("TREE_11_ANAPHYLAXIS");
            referrals.add(new ClinicalContext.SpecialistReferral(
                    "Anaphylaxis history",
                    "All vaccines in equipped facility (30-min observation); epinephrine auto-injector",
                    "BEFORE_TRAVEL"
            ));
        }

        if (allergies.path("gelatin").asBoolean(false)) {
            contraindications.add("MMR, varicella, some flu formulations (gelatin-containing)");
        }

        if (allergies.path("egg").asBoolean(false)) {
            triggeredTrees.add("TREE_11_EGG_ALLERGY");
        }

        if (allergies.path("sulfonamide").asBoolean(false)) {
            contraindications.add("Sulfadoxine-pyrimethamine (Fansidar)");
            contraindications.add("Acetazolamide (altitude sickness prevention)");
        }

        if (allergies.path("penicillin").asBoolean(false)) {
            contraindications.add("Penicillin-based antibiotics (use azithromycin or fluoroquinolones for standby)");
        }

        if (allergies.path("nsaid").asBoolean(false)) {
            triggeredTrees.add("TREE_11_NSAID_ALLERGY");
        }
    }

    private void evaluateTree12VaccinationHistory(JsonNode questionnaire, List<String> triggeredTrees,
                                                  List<String> contraindications, List<ClinicalContext.SpecialistReferral> referrals) {
        JsonNode vaccinationHistory = questionnaire.path("vaccinationHistory");
        if (vaccinationHistory.isMissingNode()) return;

        String routineStatus = vaccinationHistory.path("routineStatus").asText("").toLowerCase();
        if ("not up to date".equals(routineStatus) || "not sure".equals(routineStatus)) {
            triggeredTrees.add("TREE_12_ROUTINE_GAPS");
        }

        JsonNode reactions = questionnaire.path("vaccineReactions");
        if (!reactions.isMissingNode() && reactions.path("severe").asBoolean(false)) {
            triggeredTrees.add("TREE_12_SEVERE_REACTION");
            referrals.add(new ClinicalContext.SpecialistReferral(
                    "Previous severe vaccine reaction (anaphylaxis/hospitalisation)",
                    "Administer vaccines in equipped facility only; specialist review",
                    "BEFORE_TRAVEL"
            ));
        }
    }

    private void evaluateTree13TravelHistory(JsonNode questionnaire, List<String> triggeredTrees) {
        JsonNode travelHistory = questionnaire.path("travelHistory");
        if (travelHistory.isMissingNode()) return;

        if (travelHistory.path("firstTimeTraveller").asBoolean(false) ||
            travelHistory.path("noTravelLast12Months").asBoolean(false)) {
            triggeredTrees.add("TREE_13_FIRST_TIME");
        }

        if (travelHistory.path("frequentTraveller").asBoolean(false)) {
            triggeredTrees.add("TREE_13_FREQUENT");
        }

        if (travelHistory.path("previousHealthProblems").asBoolean(false)) {
            triggeredTrees.add("TREE_13_PREVIOUS_HEALTH_ISSUES");
        }
    }

    private void evaluateTree14RiskBehaviours(JsonNode questionnaire, List<String> triggeredTrees,
                                              List<String> contraindications, List<ClinicalContext.SpecialistReferral> referrals) {
        JsonNode riskBehaviours = questionnaire.path("riskBehaviours");
        if (riskBehaviours.isMissingNode()) return;

        if (riskBehaviours.path("newSexualPartners").asBoolean(false) ||
            riskBehaviours.path("casualRelationships").asBoolean(false)) {
            triggeredTrees.add("TREE_14_SEXUAL_RISK");
        }

        if (riskBehaviours.path("inconsistentBarrierProtection").asBoolean(false)) {
            triggeredTrees.add("TREE_14_UNPROTECTED_SEX");
        }

        if (riskBehaviours.path("alcoholUse").asBoolean(false) ||
            riskBehaviours.path("recreationalDrugs").asBoolean(false)) {
            triggeredTrees.add("TREE_14_SUBSTANCE_USE");
            contraindications.add("Mefloquine with alcohol/psychoactive substances (heightened VTE and injury risk)");
        }

        if (riskBehaviours.path("tattooing").asBoolean(false) ||
            riskBehaviours.path("piercing").asBoolean(false)) {
            triggeredTrees.add("TREE_14_TATTOO_PIERCING");
        }
    }

    private boolean isImmunocompromised(JsonNode questionnaire) {
        JsonNode medicalHistory = questionnaire.path("medicalHistory");
        for (String condition : ClinicalRules.IMMUNOCOMPROMISED_CONDITIONS) {
            if (medicalHistory.path(condition).asBoolean(false)) {
                return true;
            }
        }
        return false;
    }

    private boolean requiresLiveVaccineEntry(TravelPlan travelPlan) {
        // Yellow fever is the most common mandatory live vaccine for entry
        // This is a simplified check - in production, would query a destination requirements database
        String country = travelPlan.getCountry();
        if (country == null) {
            return false;
        }
        String normalized = country.toLowerCase();
        // Sub-Saharan Africa and parts of South America commonly require yellow fever
        return normalized.contains("africa") || normalized.contains("brazil") || 
               normalized.contains("peru") || normalized.contains("colombia");
    }

    private boolean isLongHaulFlight(TravelPlan travelPlan) {
        // Estimate based on destination - in production, would use actual flight data
        return estimateFlightHours(travelPlan) >= ClinicalRules.VTE_MEDIUM_THRESHOLD;
    }

    private int estimateFlightHours(TravelPlan travelPlan) {
        // Simplified estimation based on destination
        // In production, would use actual flight itinerary data
        String country = travelPlan.getCountry();
        if (country == null) {
            return 0;
        }
        String normalized = country.toLowerCase();
        
        // Rough estimates for common routes (from North America/Europe)
        if (normalized.contains("africa") || normalized.contains("asia") || 
            normalized.contains("australia") || normalized.contains("new zealand")) {
            return 12;
        }
        if (normalized.contains("south america") || normalized.contains("middle east")) {
            return 8;
        }
        if (normalized.contains("europe") || normalized.contains("caribbean")) {
            return 4;
        }
        return 2;
    }

    private String classifyDestinationRisk(TravelPlan travelPlan) {
        String country = travelPlan.getCountry();
        String destination = travelPlan.getDestination();
        
        if (country == null && destination == null) {
            return "MEDIUM";
        }
        
        String searchText = (country + " " + destination).toLowerCase();
        
        for (String lowRisk : ClinicalRules.LOW_RISK_DESTINATIONS) {
            if (searchText.contains(lowRisk)) {
                return "LOW";
            }
        }
        
        for (String highRisk : ClinicalRules.HIGH_RISK_DESTINATIONS) {
            if (searchText.contains(highRisk)) {
                return "HIGH";
            }
        }
        
        return "MEDIUM";
    }

    private String calculateOverallRisk(TravelPlan travelPlan, int triggeredTreeCount) {
        String destinationRisk = classifyDestinationRisk(travelPlan);
        Integer riskScore = travelPlan.getRiskScore();
        
        // Multiple risk factors converging → escalate
        if (triggeredTreeCount >= 5 || (riskScore != null && riskScore >= 80)) {
            return "VERY_HIGH";
        }
        if ("HIGH".equals(destinationRisk) || triggeredTreeCount >= 3 || (riskScore != null && riskScore >= 60)) {
            return "HIGH";
        }
        if ("MEDIUM".equals(destinationRisk) || triggeredTreeCount >= 1 || (riskScore != null && riskScore >= 40)) {
            return "MEDIUM";
        }
        return "LOW";
    }

    private String extractPreferredLanguage(JsonNode questionnaire) {
        String lang = questionnaire.path("preferredLanguage").asText("");
        return StringUtils.hasText(lang) ? lang : "en";
    }

    private ClinicalContext buildHardStopContext(ClinicalContext.HardStopResult hardStop, TravelPlan travelPlan) {
        return new ClinicalContext(
                "VERY_HIGH",
                hardStop,
                List.of("HARD_STOP_TRIGGERED"),
                List.of(),
                List.of(),
                classifyDestinationRisk(travelPlan),
                "en"
        );
    }

    private ClinicalContext buildMinimalContext(TravelPlan travelPlan) {
        return new ClinicalContext(
                "MEDIUM",
                ClinicalContext.HardStopResult.none(),
                List.of(),
                List.of(),
                List.of(),
                classifyDestinationRisk(travelPlan),
                "en"
        );
    }
}
