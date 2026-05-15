package com.TravelMedicineAdvisory.Server.domain.plans;

import static java.util.Map.entry;

import com.TravelMedicineAdvisory.Server.core.ai.AiOutputSpec;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.genai.types.Schema;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class TravelPlanOutputSchemas {

    private TravelPlanOutputSchemas() {}

    // ── Single-traveller dossier records ──────────────────────────────────

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record TravelHealthPlanOutput(
            String reportTitle, String travellerName, String destination, String travelDates,
            String overallRiskLevel, HardStop hardStop, TripAtGlance tripAtGlance,
            List<HealthRiskItem> healthRiskOverview, List<VaccinationItem> vaccinations,
            MalariaPrevention malariaPrevention, List<Recommendation> recommendations,
            List<String> clinicalFlags, List<String> contraindications,
            List<SpecialistReferral> specialistReferrals, FlightHealth flightHealth,
            List<MedicalConditionItem> medicalConditions, MedicationLogistics medicationLogistics,
            SexualHealth sexualHealth, PregnancyGuidance pregnancyGuidance, AfterReturn afterReturn,
            MedicalCare medicalCare, ItineraryGuidance itineraryGuidance, List<String> nextSteps,
            String medicalDisclaimer) {}

    /**
     * Anthropic structured outputs compile the Java class into a grammar. The schema is split
     * into two records ({@link AnthropicTravelHealthPlanCore} + {@link AnthropicTravelHealthPlanSupplemental})
     * to stay within the compiled-grammar size limit. The service calls both and merges via
     * {@link #expandAnthropic(AnthropicTravelHealthPlanCore, AnthropicTravelHealthPlanSupplemental)}.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record AnthropicTravelHealthPlanCore(
            String reportTitle, String travellerName, String destination, String travelDates,
            String overallRiskLevel, Integer tripAtGlanceDurationDays, String tripAtGlancePurpose,
            String tripAtGlanceTravelling, String tripAtGlanceAccommodation, String tripAtGlanceInsurance,
            List<String> healthRiskCategories, List<String> healthRiskLevels, List<String> healthRiskSummaries,
            List<String> vaccinationVaccines, List<String> vaccinationStatuses,
            List<String> vaccinationRecommendations, List<String> vaccinationActions,
            List<String> recommendationTitles, List<String> recommendationDetails,
            List<String> contraindications,
            String itineraryTripType, String itinerarySummary,
            List<String> routeAdviceStops, List<String> routeAdviceCountries, List<String> routeAdviceGuidance,
            List<String> returnGuidance,
            List<String> emergencyContactLabels, List<String> emergencyContactValues,
            List<String> nextSteps, String medicalDisclaimer) {}
 
    /** Supplemental rich sections that would overflow Anthropic grammar in a single call. */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record AnthropicTravelHealthPlanSupplemental(
            String flightHealthVteRiskLevel, List<String> flightHealthPreventionMeasures,
            Boolean flightHealthMedifClearanceRequired, String flightHealthMedicationTimingGuidance,
            String malariaPreventionRiskLevel, String malariaPreventionRecommendedAgent,
            String malariaPreventionRationale, List<String> malariaPreventionMosquitoProtection,
            String malariaPreventionContraindications,
            List<String> medicalConditionNames, List<String> medicalConditionPrecautions,
            String medicationLogisticsPackaging, String medicationLogisticsSupplyRule,
            Boolean medicationLogisticsDestinationLegalityCheck, Boolean medicationLogisticsColdChainRequired,
            List<String> specialistReferralConditions, List<String> specialistReferralSpecialists,
            List<String> specialistReferralUrgencies,
            String sexualHealthRiskLevel, List<String> sexualHealthPreventionAdvice,
            Boolean sexualHealthPrepPepDiscussion,
            String pregnancyGuidanceTrimesterAdvice, List<String> pregnancyGuidanceLiveVaccineContraindications,
            String pregnancyGuidanceAntimalarialSafety, String pregnancyGuidanceAirlineRestrictions,
            String pregnancyGuidanceContraceptionCounselling,
            List<String> afterReturnWithin1Week, List<String> afterReturnWithin4Weeks,
            List<String> afterReturnBeyond4Weeks, String afterReturnRedFlag,
            List<String> clinicNames, List<String> clinicAddresses, List<String> clinicPhones,
            List<String> clinicDistances, List<String> clinicNotes,
            List<String> embassyContactNames, List<String> embassyContactDetails) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record TripAtGlance(Integer durationDays, String purpose, String travelling, String accommodation,
            String insurance) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record HealthRiskItem(String category, String level, String summary) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record VaccinationItem(String vaccine, String status, String recommendation, String action) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Recommendation(String title, String details) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record MedicalCare(List<Clinic> clinics, List<EmbassyContact> embassyContacts,
            List<EmergencyContact> emergencyContacts) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Clinic(String name, String address, String phone, String distance, String notes) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record EmergencyContact(String label, String value) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record EmbassyContact(String name, String details) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ItineraryGuidance(String tripType, String summary, List<RouteAdvice> routeAdvice,
            List<String> returnGuidance) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record RouteAdvice(String stop, String country, String guidance) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record MalariaPrevention(String riskLevel, String recommendedAgent, String rationale,
            List<String> mosquitoProtection, String contraindications) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record FlightHealth(String vteRiskLevel, List<String> preventionMeasures,
            Boolean medifClearanceRequired, String medicationTimingGuidance) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record MedicalConditionItem(String condition, String precautions) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record MedicationLogistics(String packaging, String supplyRule, Boolean destinationLegalityCheck,
            Boolean coldChainRequired) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record SexualHealth(String riskLevel, List<String> preventionAdvice, Boolean prepPepDiscussion) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record PregnancyGuidance(String trimesterSpecificAdvice, List<String> liveVaccineContraindications,
            String antimalarialSafety, String airlineRestrictions, String contraceptionCounselling) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record AfterReturn(List<String> within1Week, List<String> within4Weeks, List<String> beyond4Weeks,
            String redFlag) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record HardStop(String conditionTriggered, String reason, String recommendedSpecialist) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record SpecialistReferral(String condition, String specialist, String urgency) {}

    // ── Family dossier records ────────────────────────────────────────────

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record FamilyTravelHealthPlanOutput(
            String destination, String country, String tripSummary,
            List<String> generalVaccinations, List<FamilyMember> members,
            String medicalDisclaimer) {}

    /** Flattened Anthropic family schema. Member arrays are parallel by index. */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record AnthropicFamilyTravelHealthPlanOutput(
            String destination, String country, String tripSummary, List<String> generalVaccinations,
            List<Integer> memberIds, List<String> memberNames, List<String> relationships,
            List<String> relationshipToMainApplicants, List<String> displayLabels,
            List<Integer> ageAtDepartures, List<String> executiveSummaries,
            List<String> vaccinationSummaries, List<String> medicationSummaries,
            List<String> healthConsiderationSummaries, List<String> travellerSpecific,
            List<Boolean> hardStops, String medicalDisclaimer) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record FamilyMember(
            Long memberId, String memberName, String relationship, String relationshipToMainApplicant,
            String displayLabel, Integer ageAtDeparture, String executiveSummary,
            List<MemberVaccination> vaccinations, List<MemberMedication> medications,
            List<MemberHealthConsideration> healthConsiderations,
            String travellerSpecific, Boolean hardStop) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record MemberVaccination(String name, String recommendation, String rationale, String timing) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record MemberMedication(String name, String indication, String dosage, String notes) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record MemberHealthConsideration(String category, String advice) {}

    // ── Action Sheet records ──────────────────────────────────────────────

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ActionSheetOutput(
            List<String> section1CriticalBeforeDeparture, String section2TripSnapshot,
            List<ActionSheetVaccine> section3Vaccines, List<String> section4PackAndRoutine,
            ActionSheetSection5 section5, String closingLine) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ActionSheetVaccine(String vaccine, String status, String action) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ActionSheetSection5(
            String redFlagsLine, List<ActionSheetFacility> facilities,
            String localEmergencyNumber, String insuranceEmergencyLine) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ActionSheetFacility(String name, String location) {}

    public static TravelHealthPlanOutput expandAnthropic(
            AnthropicTravelHealthPlanCore core, AnthropicTravelHealthPlanSupplemental supp) {
        if (core == null) return null;
        TripAtGlance glance = new TripAtGlance(
                core.tripAtGlanceDurationDays(), core.tripAtGlancePurpose(), core.tripAtGlanceTravelling(),
                core.tripAtGlanceAccommodation(), core.tripAtGlanceInsurance());
        ItineraryGuidance itinerary = new ItineraryGuidance(
                core.itineraryTripType(), core.itinerarySummary(), routeAdvice(core), safe(core.returnGuidance()));
        MedicalCare medCare = buildMedicalCareFromCore(core);
 
        FlightHealth flight = null;
        MalariaPrevention malaria = null;
        List<MedicalConditionItem> conditions = List.of();
        MedicationLogistics medLog = null;
        List<SpecialistReferral> referrals = List.of();
        SexualHealth sexual = null;
        PregnancyGuidance preg = null;
        AfterReturn after = null;
        if (supp != null) {
            flight = blankStr(supp.flightHealthVteRiskLevel())
                    && noItems(supp.flightHealthPreventionMeasures())
                    && supp.flightHealthMedifClearanceRequired() == null
                    && blankStr(supp.flightHealthMedicationTimingGuidance())
                    ? null
                    : new FlightHealth(
                            blankStr(supp.flightHealthVteRiskLevel()) ? null : supp.flightHealthVteRiskLevel(),
                            safe(supp.flightHealthPreventionMeasures()),
                            supp.flightHealthMedifClearanceRequired(),
                            blankStr(supp.flightHealthMedicationTimingGuidance()) ? null : supp.flightHealthMedicationTimingGuidance());
            malaria = blankStr(supp.malariaPreventionRiskLevel())
                    && blankStr(supp.malariaPreventionRecommendedAgent())
                    && blankStr(supp.malariaPreventionRationale())
                    && noItems(supp.malariaPreventionMosquitoProtection())
                    && blankStr(supp.malariaPreventionContraindications())
                    ? null
                    : new MalariaPrevention(
                            blankStr(supp.malariaPreventionRiskLevel()) ? null : supp.malariaPreventionRiskLevel(),
                            blankStr(supp.malariaPreventionRecommendedAgent()) ? null : supp.malariaPreventionRecommendedAgent(),
                            blankStr(supp.malariaPreventionRationale()) ? null : supp.malariaPreventionRationale(),
                            safe(supp.malariaPreventionMosquitoProtection()),
                            blankStr(supp.malariaPreventionContraindications()) ? null : supp.malariaPreventionContraindications());
            conditions = buildParallelMedicalConditions(supp.medicalConditionNames(), supp.medicalConditionPrecautions());
            medLog = blankStr(supp.medicationLogisticsPackaging())
                    && blankStr(supp.medicationLogisticsSupplyRule())
                    && supp.medicationLogisticsDestinationLegalityCheck() == null
                    && supp.medicationLogisticsColdChainRequired() == null
                    ? null
                    : new MedicationLogistics(
                            blankStr(supp.medicationLogisticsPackaging()) ? null : supp.medicationLogisticsPackaging(),
                            blankStr(supp.medicationLogisticsSupplyRule()) ? null : supp.medicationLogisticsSupplyRule(),
                            supp.medicationLogisticsDestinationLegalityCheck(),
                            supp.medicationLogisticsColdChainRequired());
            referrals = buildParallelSpecialistReferrals(
                    supp.specialistReferralConditions(), supp.specialistReferralSpecialists(),
                    supp.specialistReferralUrgencies());
            sexual = blankStr(supp.sexualHealthRiskLevel())
                    && noItems(supp.sexualHealthPreventionAdvice())
                    && supp.sexualHealthPrepPepDiscussion() == null
                    ? null
                    : new SexualHealth(
                            blankStr(supp.sexualHealthRiskLevel()) ? null : supp.sexualHealthRiskLevel(),
                            safe(supp.sexualHealthPreventionAdvice()),
                            supp.sexualHealthPrepPepDiscussion());
            preg = blankStr(supp.pregnancyGuidanceTrimesterAdvice())
                    && noItems(supp.pregnancyGuidanceLiveVaccineContraindications())
                    && blankStr(supp.pregnancyGuidanceAntimalarialSafety())
                    && blankStr(supp.pregnancyGuidanceAirlineRestrictions())
                    && blankStr(supp.pregnancyGuidanceContraceptionCounselling())
                    ? null
                    : new PregnancyGuidance(
                            blankStr(supp.pregnancyGuidanceTrimesterAdvice()) ? null : supp.pregnancyGuidanceTrimesterAdvice(),
                            safe(supp.pregnancyGuidanceLiveVaccineContraindications()),
                            blankStr(supp.pregnancyGuidanceAntimalarialSafety()) ? null : supp.pregnancyGuidanceAntimalarialSafety(),
                            blankStr(supp.pregnancyGuidanceAirlineRestrictions()) ? null : supp.pregnancyGuidanceAirlineRestrictions(),
                            blankStr(supp.pregnancyGuidanceContraceptionCounselling()) ? null : supp.pregnancyGuidanceContraceptionCounselling());
            after = noItems(supp.afterReturnWithin1Week())
                    && noItems(supp.afterReturnWithin4Weeks())
                    && noItems(supp.afterReturnBeyond4Weeks())
                    && blankStr(supp.afterReturnRedFlag())
                    ? null
                    : new AfterReturn(
                            safe(supp.afterReturnWithin1Week()), safe(supp.afterReturnWithin4Weeks()),
                            safe(supp.afterReturnBeyond4Weeks()),
                            blankStr(supp.afterReturnRedFlag()) ? null : supp.afterReturnRedFlag());
            MedicalCare suppCare = buildMedicalCareFromSupp(supp);
            if (suppCare != null) {
                medCare = mergeMedicalCare(medCare, suppCare);
            }
        }
        return new TravelHealthPlanOutput(
                core.reportTitle(), core.travellerName(), core.destination(), core.travelDates(),
                core.overallRiskLevel(), null, glance,
                healthRisks(core), vaccinations(core), malaria, recommendations(core),
                null, safe(core.contraindications()), referrals, flight, conditions, medLog,
                sexual, preg, after, medCare, itinerary, safe(core.nextSteps()), core.medicalDisclaimer());
    }

    public static FamilyTravelHealthPlanOutput expandAnthropic(AnthropicFamilyTravelHealthPlanOutput output) {
        if (output == null) {
            return null;
        }
        int count = maxSize(output.memberIds(), output.memberNames(), output.relationships(),
                output.relationshipToMainApplicants(), output.displayLabels(), output.ageAtDepartures(),
                output.executiveSummaries(), output.vaccinationSummaries(), output.medicationSummaries(),
                output.healthConsiderationSummaries(), output.travellerSpecific(), output.hardStops());
        List<FamilyMember> members = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            Integer memberId = at(output.memberIds(), i, null);
            String vaccination = at(output.vaccinationSummaries(), i, "");
            String medication = at(output.medicationSummaries(), i, "");
            String consideration = at(output.healthConsiderationSummaries(), i, "");
            members.add(new FamilyMember(
                    memberId != null ? memberId.longValue() : null,
                    at(output.memberNames(), i, ""),
                    at(output.relationships(), i, ""),
                    at(output.relationshipToMainApplicants(), i, ""),
                    at(output.displayLabels(), i, ""),
                    at(output.ageAtDepartures(), i, null),
                    at(output.executiveSummaries(), i, ""),
                    hasText(vaccination) ? List.of(new MemberVaccination("Trip-specific vaccines", vaccination, "", "")) : List.of(),
                    hasText(medication) ? List.of(new MemberMedication("Medication guidance", "Travel", "", medication)) : List.of(),
                    hasText(consideration) ? List.of(new MemberHealthConsideration("Health considerations", consideration)) : List.of(),
                    at(output.travellerSpecific(), i, ""),
                    at(output.hardStops(), i, Boolean.FALSE)));
        }
        return new FamilyTravelHealthPlanOutput(
                output.destination(), output.country(), output.tripSummary(), safe(output.generalVaccinations()),
                members, output.medicalDisclaimer());
    }
 
    public static TravelHealthPlanOutput withMandatoryDisclaimer(TravelHealthPlanOutput output) {
        if (output == null) {
            return null;
        }
        String disclaimer = ClinicalRules.MANDATORY_DISCLAIMER;
        if (disclaimer.equals(output.medicalDisclaimer())) {
            return output;
        }
        return new TravelHealthPlanOutput(
                output.reportTitle(), output.travellerName(), output.destination(), output.travelDates(),
                output.overallRiskLevel(), output.hardStop(), output.tripAtGlance(),
                output.healthRiskOverview(), output.vaccinations(), output.malariaPrevention(),
                output.recommendations(), output.clinicalFlags(), output.contraindications(),
                output.specialistReferrals(), output.flightHealth(), output.medicalConditions(),
                output.medicationLogistics(), output.sexualHealth(), output.pregnancyGuidance(),
                output.afterReturn(), output.medicalCare(), output.itineraryGuidance(),
                output.nextSteps(), disclaimer);
    }
 
    public static TravelHealthPlanOutput withoutDecisionFlags(TravelHealthPlanOutput output) {
        if (output == null || output.clinicalFlags() == null) {
            return output;
        }
        return new TravelHealthPlanOutput(
                output.reportTitle(), output.travellerName(), output.destination(), output.travelDates(),
                output.overallRiskLevel(), output.hardStop(), output.tripAtGlance(),
                output.healthRiskOverview(), output.vaccinations(), output.malariaPrevention(),
                output.recommendations(), null, output.contraindications(),
                output.specialistReferrals(), output.flightHealth(), output.medicalConditions(),
                output.medicationLogistics(), output.sexualHealth(), output.pregnancyGuidance(),
                output.afterReturn(), output.medicalCare(), output.itineraryGuidance(),
                output.nextSteps(), output.medicalDisclaimer());
    }
 
    public static FamilyTravelHealthPlanOutput withMandatoryDisclaimer(FamilyTravelHealthPlanOutput output) {
        if (output == null) {
            return null;
        }
        String disclaimer = ClinicalRules.MANDATORY_DISCLAIMER;
        if (disclaimer.equals(output.medicalDisclaimer())) {
            return output;
        }
        return new FamilyTravelHealthPlanOutput(
                output.destination(), output.country(), output.tripSummary(), output.generalVaccinations(),
                output.members(), disclaimer);
    }

    private static List<HealthRiskItem> healthRisks(AnthropicTravelHealthPlanCore output) {
        int count = maxSize(output.healthRiskCategories(), output.healthRiskLevels(), output.healthRiskSummaries());
        List<HealthRiskItem> items = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            String category = at(output.healthRiskCategories(), i, "");
            String level = at(output.healthRiskLevels(), i, "");
            String summary = at(output.healthRiskSummaries(), i, "");
            if (hasText(category) || hasText(level) || hasText(summary)) {
                items.add(new HealthRiskItem(category, level, summary));
            }
        }
        return items;
    }

    private static List<VaccinationItem> vaccinations(AnthropicTravelHealthPlanCore output) {
        int count = maxSize(output.vaccinationVaccines(), output.vaccinationStatuses(),
                output.vaccinationRecommendations(), output.vaccinationActions());
        List<VaccinationItem> items = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            String vaccine = at(output.vaccinationVaccines(), i, "");
            String status = at(output.vaccinationStatuses(), i, "");
            String recommendation = at(output.vaccinationRecommendations(), i, "");
            String action = at(output.vaccinationActions(), i, "");
            if (hasText(vaccine) || hasText(status) || hasText(recommendation) || hasText(action)) {
                items.add(new VaccinationItem(vaccine, status, recommendation, action));
            }
        }
        return items;
    }

    private static List<Recommendation> recommendations(AnthropicTravelHealthPlanCore output) {
        int count = maxSize(output.recommendationTitles(), output.recommendationDetails());
        List<Recommendation> items = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            String title = at(output.recommendationTitles(), i, "");
            String details = at(output.recommendationDetails(), i, "");
            if (hasText(title) || hasText(details)) {
                items.add(new Recommendation(title, details));
            }
        }
        return items;
    }

    private static List<RouteAdvice> routeAdvice(AnthropicTravelHealthPlanCore output) {
        int count = maxSize(output.routeAdviceStops(), output.routeAdviceCountries(), output.routeAdviceGuidance());
        List<RouteAdvice> items = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            String stop = at(output.routeAdviceStops(), i, "");
            String country = at(output.routeAdviceCountries(), i, "");
            String guidance = at(output.routeAdviceGuidance(), i, "");
            if (hasText(stop) || hasText(country) || hasText(guidance)) {
                items.add(new RouteAdvice(stop, country, guidance));
            }
        }
        return items;
    }
 
    private static List<MedicalConditionItem> buildParallelMedicalConditions(List<String> names, List<String> precautions) {
        int count = maxSize(names, precautions);
        List<MedicalConditionItem> items = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            String n = at(names, i, "");
            String p = at(precautions, i, "");
            if (hasText(n) || hasText(p)) {
                items.add(new MedicalConditionItem(n, p));
            }
        }
        return items;
    }
 
    private static List<SpecialistReferral> buildParallelSpecialistReferrals(List<String> conditions,
            List<String> specialists, List<String> urgencies) {
        int count = maxSize(conditions, specialists, urgencies);
        List<SpecialistReferral> items = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            String c = at(conditions, i, "");
            String s = at(specialists, i, "");
            String u = at(urgencies, i, "");
            if (hasText(c) || hasText(s) || hasText(u)) {
                items.add(new SpecialistReferral(c, s, u));
            }
        }
        return items;
    }
 
    private static MedicalCare buildMedicalCareFromCore(AnthropicTravelHealthPlanCore core) {
        List<EmergencyContact> emergencies = buildParallelEmergencies(
                core.emergencyContactLabels(), core.emergencyContactValues());
        if (emergencies.isEmpty()) {
            return null;
        }
        return new MedicalCare(List.of(), List.of(), emergencies);
    }
 
    private static MedicalCare buildMedicalCareFromSupp(AnthropicTravelHealthPlanSupplemental supp) {
        List<Clinic> clinics = buildParallelClinics(
                supp.clinicNames(), supp.clinicAddresses(), supp.clinicPhones(),
                supp.clinicDistances(), supp.clinicNotes());
        List<EmbassyContact> embassies = buildParallelEmbassies(
                supp.embassyContactNames(), supp.embassyContactDetails());
        if (clinics.isEmpty() && embassies.isEmpty()) {
            return null;
        }
        return new MedicalCare(clinics, embassies, List.of());
    }
 
    private static MedicalCare mergeMedicalCare(MedicalCare core, MedicalCare supp) {
        if (core == null) return supp;
        if (supp == null) return core;
        return new MedicalCare(
                supp.clinics(), supp.embassyContacts(),
                core.emergencyContacts());
    }
 
    private static List<Clinic> buildParallelClinics(List<String> names, List<String> addresses,
            List<String> phones, List<String> distances, List<String> notes) {
        int count = maxSize(names, addresses, phones, distances, notes);
        List<Clinic> items = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            String n = at(names, i, null);
            String a = at(addresses, i, null);
            String p = at(phones, i, null);
            String d = at(distances, i, null);
            String nt = at(notes, i, null);
            if (n != null || a != null || p != null || d != null || nt != null) {
                items.add(new Clinic(n, a, p, d, nt));
            }
        }
        return items;
    }
 
    private static List<EmbassyContact> buildParallelEmbassies(List<String> names, List<String> details) {
        int count = maxSize(names, details);
        List<EmbassyContact> items = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            String n = at(names, i, null);
            String d = at(details, i, null);
            if (n != null || d != null) {
                items.add(new EmbassyContact(n, d));
            }
        }
        return items;
    }
 
    private static List<EmergencyContact> buildParallelEmergencies(List<String> labels, List<String> values) {
        int count = maxSize(labels, values);
        List<EmergencyContact> items = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            String l = at(labels, i, null);
            String v = at(values, i, null);
            if (l != null || v != null) {
                items.add(new EmergencyContact(l, v));
            }
        }
        return items;
    }
 
    private static boolean blankStr(String s) {
        return s == null || s.isBlank();
    }
 
    private static boolean noItems(List<?> items) {
        return items == null || items.isEmpty();
    }

    private static <T> List<T> safe(List<T> items) {
        return items == null ? List.of() : items;
    }

    private static <T> T at(List<T> items, int index, T fallback) {
        return items != null && index >= 0 && index < items.size() ? items.get(index) : fallback;
    }

    @SafeVarargs
    private static int maxSize(List<?>... lists) {
        int max = 0;
        for (List<?> list : lists) {
            if (list != null && list.size() > max) {
                max = list.size();
            }
        }
        return max;
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    // ── Schema helper ─────────────────────────────────────────────────────

    private static Schema strProp() {
        return Schema.builder().type(new com.google.genai.types.Type(com.google.genai.types.Type.Known.STRING)).build();
    }
 
    private static Schema strProp(long maxLength) {
        return Schema.builder()
                .type(new com.google.genai.types.Type(com.google.genai.types.Type.Known.STRING))
                .maxLength(maxLength)
                .build();
    }

    private static Schema intProp() {
        return Schema.builder().type(new com.google.genai.types.Type(com.google.genai.types.Type.Known.INTEGER)).build();
    }

    private static Schema boolProp() {
        return Schema.builder().type(new com.google.genai.types.Type(com.google.genai.types.Type.Known.BOOLEAN)).build();
    }

    private static Schema arr(Schema items) {
        return Schema.builder().type(new com.google.genai.types.Type(com.google.genai.types.Type.Known.ARRAY)).items(items).build();
    }
 
    private static Schema arr(Schema items, long maxItems) {
        return Schema.builder()
                .type(new com.google.genai.types.Type(com.google.genai.types.Type.Known.ARRAY))
                .items(items)
                .maxItems(maxItems)
                .build();
    }

    private static Schema obj(Map<String, Schema> props) {
        List<String> propertyNames = new ArrayList<>(props.keySet());
        return Schema.builder()
                .type(new com.google.genai.types.Type(com.google.genai.types.Type.Known.OBJECT))
                .properties(props)
                .required(propertyNames)
                .propertyOrdering(propertyNames)
                .build();
    }

    // ── Gemini Schema definitions ─────────────────────────────────────────

    private static final Schema HR_ITEM = obj(Map.of("category", strProp(), "level", strProp(), "summary", strProp()));
    private static final Schema VAX_ITEM = obj(Map.of("vaccine", strProp(), "status", strProp(), "recommendation", strProp(), "action", strProp()));
    private static final Schema REC_ITEM = obj(Map.of("title", strProp(), "details", strProp()));
    private static final Schema CLINIC = obj(Map.of("name", strProp(), "address", strProp(), "phone", strProp(), "distance", strProp(), "notes", strProp()));
    private static final Schema EMBASSY = obj(Map.of("name", strProp(), "details", strProp()));
    private static final Schema EMERGENCY = obj(Map.of("label", strProp(), "value", strProp()));
    private static final Schema MED_CARE = obj(Map.of("clinics", arr(CLINIC), "embassyContacts", arr(EMBASSY), "emergencyContacts", arr(EMERGENCY)));
    private static final Schema ROUTE = obj(Map.of("stop", strProp(), "country", strProp(), "guidance", strProp()));
    private static final Schema ITIN = obj(Map.of(
            "tripType", strProp(), "summary", strProp(),
            "routeAdvice", arr(ROUTE), "returnGuidance", arr(strProp())));
    private static final Schema MALARIA = obj(Map.of(
            "riskLevel", strProp(), "recommendedAgent", strProp(), "rationale", strProp(),
            "mosquitoProtection", arr(strProp()), "contraindications", strProp()));
    private static final Schema FLIGHT = obj(Map.of(
            "vteRiskLevel", strProp(), "preventionMeasures", arr(strProp()),
            "medifClearanceRequired", boolProp(), "medicationTimingGuidance", strProp()));
    private static final Schema MED_COND = obj(Map.of("condition", strProp(), "precautions", strProp()));
    private static final Schema MED_LOG = obj(Map.of(
            "packaging", strProp(), "supplyRule", strProp(),
            "destinationLegalityCheck", boolProp(), "coldChainRequired", boolProp()));
    private static final Schema SEXUAL = obj(Map.of(
            "riskLevel", strProp(), "preventionAdvice", arr(strProp()), "prepPepDiscussion", boolProp()));
    private static final Schema PREG = obj(Map.of(
            "trimesterSpecificAdvice", strProp(), "liveVaccineContraindications", arr(strProp()),
            "antimalarialSafety", strProp(), "airlineRestrictions", strProp(), "contraceptionCounselling", strProp()));
    private static final Schema RETURN = obj(Map.of(
            "within1Week", arr(strProp()), "within4Weeks", arr(strProp()),
            "beyond4Weeks", arr(strProp()), "redFlag", strProp()));
    private static final Schema STOP = obj(Map.of("conditionTriggered", strProp(), "reason", strProp(), "recommendedSpecialist", strProp()));
    private static final Schema SPEC_REF = obj(Map.of("condition", strProp(), "specialist", strProp(), "urgency", strProp()));
    private static final Schema GLANCE = obj(Map.of(
            "durationDays", intProp(), "purpose", strProp(), "travelling", strProp(),
            "accommodation", strProp(), "insurance", strProp()));
    private static final Schema MEM_VAX = obj(Map.of("name", strProp(), "recommendation", strProp(), "rationale", strProp(), "timing", strProp()));
    private static final Schema MEM_MED = obj(Map.of("name", strProp(), "indication", strProp(), "dosage", strProp(), "notes", strProp()));
    private static final Schema MEM_HEALTH = obj(Map.of("category", strProp(), "advice", strProp()));
    private static final Schema FAM_MEMBER = obj(Map.ofEntries(
            entry("memberId", intProp()), entry("memberName", strProp()), entry("relationship", strProp()),
            entry("relationshipToMainApplicant", strProp()), entry("displayLabel", strProp()),
            entry("ageAtDeparture", intProp()), entry("executiveSummary", strProp()),
            entry("vaccinations", arr(MEM_VAX)), entry("medications", arr(MEM_MED)),
            entry("healthConsiderations", arr(MEM_HEALTH)), entry("travellerSpecific", strProp()),
            entry("hardStop", boolProp())));
    private static final Schema AS_VAX = obj(Map.of("vaccine", strProp(80), "status", strProp(80), "action", strProp(180)));
    private static final Schema AS_FAC = obj(Map.of("name", strProp(120), "location", strProp(160)));
    private static final Schema AS_SEC5 = obj(Map.of(
            "redFlagsLine", strProp(260), "facilities", arr(AS_FAC, 2),
            "localEmergencyNumber", strProp(120), "insuranceEmergencyLine", strProp(180)));

    private static final Schema SINGLE_TRAVELLER_GEMINI = obj(Map.ofEntries(
            entry("reportTitle", strProp()), entry("travellerName", strProp()), entry("destination", strProp()),
            entry("travelDates", strProp()), entry("overallRiskLevel", strProp()), entry("hardStop", STOP),
            entry("tripAtGlance", GLANCE), entry("healthRiskOverview", arr(HR_ITEM)),
            entry("vaccinations", arr(VAX_ITEM)), entry("malariaPrevention", MALARIA),
            entry("recommendations", arr(REC_ITEM)),
            entry("contraindications", arr(strProp())), entry("specialistReferrals", arr(SPEC_REF)),
            entry("flightHealth", FLIGHT), entry("medicalConditions", arr(MED_COND)),
            entry("medicationLogistics", MED_LOG), entry("sexualHealth", SEXUAL),
            entry("pregnancyGuidance", PREG), entry("afterReturn", RETURN), entry("medicalCare", MED_CARE),
            entry("itineraryGuidance", ITIN), entry("nextSteps", arr(strProp())),
            entry("medicalDisclaimer", strProp())));

    private static final Schema FAMILY_GEMINI = obj(Map.ofEntries(
            entry("destination", strProp()), entry("country", strProp()), entry("tripSummary", strProp()),
            entry("generalVaccinations", arr(strProp())), entry("members", arr(FAM_MEMBER)),
            entry("medicalDisclaimer", strProp())));

    private static final Schema ACTION_SHEET_GEMINI = obj(Map.ofEntries(
            entry("section1CriticalBeforeDeparture", arr(strProp(180), 4)), entry("section2TripSnapshot", strProp(240)),
            entry("section3Vaccines", arr(AS_VAX, 18)), entry("section4PackAndRoutine", arr(strProp(180), 8)),
            entry("section5", AS_SEC5), entry("closingLine", strProp(140))));

    // ── Flat Gemini schemas matching Anthropic Java types ────────────────

    private static final Schema SCHEMA_ANTHROPIC_CORE = obj(Map.ofEntries(
            entry("reportTitle", strProp()), entry("travellerName", strProp()), entry("destination", strProp()),
            entry("travelDates", strProp()), entry("overallRiskLevel", strProp()),
            entry("tripAtGlanceDurationDays", intProp()), entry("tripAtGlancePurpose", strProp()),
            entry("tripAtGlanceTravelling", strProp()), entry("tripAtGlanceAccommodation", strProp()),
            entry("tripAtGlanceInsurance", strProp()), entry("healthRiskCategories", arr(strProp())),
            entry("healthRiskLevels", arr(strProp())), entry("healthRiskSummaries", arr(strProp())),
            entry("vaccinationVaccines", arr(strProp())), entry("vaccinationStatuses", arr(strProp())),
            entry("vaccinationRecommendations", arr(strProp())), entry("vaccinationActions", arr(strProp())),
            entry("recommendationTitles", arr(strProp())), entry("recommendationDetails", arr(strProp())),
            entry("contraindications", arr(strProp())),
            entry("itineraryTripType", strProp()), entry("itinerarySummary", strProp()),
            entry("routeAdviceStops", arr(strProp())), entry("routeAdviceCountries", arr(strProp())),
            entry("routeAdviceGuidance", arr(strProp())), entry("returnGuidance", arr(strProp())),
            entry("emergencyContactLabels", arr(strProp())), entry("emergencyContactValues", arr(strProp())),
            entry("nextSteps", arr(strProp())), entry("medicalDisclaimer", strProp())));
 
    private static final Schema SCHEMA_ANTHROPIC_SUPP = obj(Map.ofEntries(
            entry("flightHealthVteRiskLevel", strProp()), entry("flightHealthPreventionMeasures", arr(strProp())),
            entry("flightHealthMedifClearanceRequired", boolProp()), entry("flightHealthMedicationTimingGuidance", strProp()),
            entry("malariaPreventionRiskLevel", strProp()), entry("malariaPreventionRecommendedAgent", strProp()),
            entry("malariaPreventionRationale", strProp()), entry("malariaPreventionMosquitoProtection", arr(strProp())),
            entry("malariaPreventionContraindications", strProp()),
            entry("medicalConditionNames", arr(strProp())), entry("medicalConditionPrecautions", arr(strProp())),
            entry("medicationLogisticsPackaging", strProp()), entry("medicationLogisticsSupplyRule", strProp()),
            entry("medicationLogisticsDestinationLegalityCheck", boolProp()), entry("medicationLogisticsColdChainRequired", boolProp()),
            entry("specialistReferralConditions", arr(strProp())), entry("specialistReferralSpecialists", arr(strProp())),
            entry("specialistReferralUrgencies", arr(strProp())),
            entry("sexualHealthRiskLevel", strProp()), entry("sexualHealthPreventionAdvice", arr(strProp())),
            entry("sexualHealthPrepPepDiscussion", boolProp()),
            entry("pregnancyGuidanceTrimesterAdvice", strProp()), entry("pregnancyGuidanceLiveVaccineContraindications", arr(strProp())),
            entry("pregnancyGuidanceAntimalarialSafety", strProp()), entry("pregnancyGuidanceAirlineRestrictions", strProp()),
            entry("pregnancyGuidanceContraceptionCounselling", strProp()),
            entry("afterReturnWithin1Week", arr(strProp())), entry("afterReturnWithin4Weeks", arr(strProp())),
            entry("afterReturnBeyond4Weeks", arr(strProp())), entry("afterReturnRedFlag", strProp()),
            entry("clinicNames", arr(strProp())), entry("clinicAddresses", arr(strProp())),
            entry("clinicPhones", arr(strProp())), entry("clinicDistances", arr(strProp())),
            entry("clinicNotes", arr(strProp())),
            entry("embassyContactNames", arr(strProp())), entry("embassyContactDetails", arr(strProp()))));

    private static final Schema FAMILY_ANTHROPIC = obj(Map.ofEntries(
            entry("destination", strProp()), entry("country", strProp()), entry("tripSummary", strProp()),
            entry("generalVaccinations", arr(strProp())), entry("memberIds", arr(intProp())),
            entry("memberNames", arr(strProp())), entry("relationships", arr(strProp())),
            entry("relationshipToMainApplicants", arr(strProp())), entry("displayLabels", arr(strProp())),
            entry("ageAtDepartures", arr(intProp())), entry("executiveSummaries", arr(strProp())),
            entry("vaccinationSummaries", arr(strProp())), entry("medicationSummaries", arr(strProp())),
            entry("healthConsiderationSummaries", arr(strProp())), entry("travellerSpecific", arr(strProp())),
            entry("hardStops", arr(boolProp())), entry("medicalDisclaimer", strProp())));

    // ── Public spec constants ─────────────────────────────────────────────

    public static final AiOutputSpec<TravelHealthPlanOutput> SINGLE_TRAVELLER =
            new AiOutputSpec<>(TravelHealthPlanOutput.class, SINGLE_TRAVELLER_GEMINI, "single-traveller");

    /** Core plan fields for Anthropic structured-output grammar limits. */
    public static final AiOutputSpec<AnthropicTravelHealthPlanCore> SINGLE_TRAVELLER_ANTHROPIC_CORE =
            new AiOutputSpec<>(AnthropicTravelHealthPlanCore.class, SCHEMA_ANTHROPIC_CORE, "single-traveller-anthro-core");
 
    /** Supplemental rich sections for Anthropic (second call after core). */
    public static final AiOutputSpec<AnthropicTravelHealthPlanSupplemental> SINGLE_TRAVELLER_ANTHROPIC_SUPP =
            new AiOutputSpec<>(AnthropicTravelHealthPlanSupplemental.class, SCHEMA_ANTHROPIC_SUPP, "single-traveller-anthro-supp");

    public static final AiOutputSpec<FamilyTravelHealthPlanOutput> FAMILY =
            new AiOutputSpec<>(FamilyTravelHealthPlanOutput.class, FAMILY_GEMINI, "family");

    /** Simplified Java type for Anthropic structured-output grammar limits. */
    public static final AiOutputSpec<AnthropicFamilyTravelHealthPlanOutput> FAMILY_LITE =
            new AiOutputSpec<>(AnthropicFamilyTravelHealthPlanOutput.class, FAMILY_ANTHROPIC, "family-lite");

    public static final AiOutputSpec<ActionSheetOutput> ACTION_SHEET =
            new AiOutputSpec<>(ActionSheetOutput.class, ACTION_SHEET_GEMINI, "action-sheet");
}
