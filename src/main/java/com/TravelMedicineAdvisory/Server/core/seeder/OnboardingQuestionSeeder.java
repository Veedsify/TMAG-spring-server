package com.TravelMedicineAdvisory.Server.core.seeder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.TravelMedicineAdvisory.Server.domain.useronboarding.OnboardingQuestionCategory;
import com.TravelMedicineAdvisory.Server.domain.useronboarding.OnboardingQuestionCategoryRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

@Component
@Order(2)
public class OnboardingQuestionSeeder implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(OnboardingQuestionSeeder.class);

    @Value("${app.features.seeder.enabled:true}")
    private boolean seederEnabled;

    private final OnboardingQuestionCategoryRepository repository;
    private final ObjectMapper objectMapper;

    public OnboardingQuestionSeeder(OnboardingQuestionCategoryRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (!seederEnabled) {
            return;
        }

        logger.info("Seeding onboarding questions...");

        List<OnboardingQuestionCategory> categories = new ArrayList<>();
        Set<String> seenQuestionKeys = new HashSet<>();

        OnboardingQuestionCategory personal = new OnboardingQuestionCategory();
        personal.setCategoryKey("personal_information");
        personal.setCategoryName("Personal Information");
        personal.setCategoryIcon("shield-check");
        personal.setCategoryDescription("Please confirm your personal details below.");
        personal.setDisplayOrder(1);
        personal.setIsOptional(false);
        personal.setQuestions(
                removeDuplicateQuestionKeys(PERSONAL_INFORMATION_QUESTIONS, seenQuestionKeys, "personal_information"));
        categories.add(personal);

        OnboardingQuestionCategory travel = new OnboardingQuestionCategory();
        travel.setCategoryKey("travel_details");
        travel.setCategoryName("Travel Itinerary");
        travel.setCategoryIcon("plane");
        travel.setCategoryDescription("Trip type, route, dates, flight details, and companions.");
        travel.setDisplayOrder(2);
        travel.setIsOptional(false);
        travel.setQuestions(removeDuplicateQuestionKeys(TRAVEL_QUESTIONS, seenQuestionKeys, "travel_details"));
        categories.add(travel);

        OnboardingQuestionCategory accommodation = new OnboardingQuestionCategory();
        accommodation.setCategoryKey("accommodation_environment");
        accommodation.setCategoryName("Accommodation & Environment");
        accommodation.setCategoryIcon("shield-check");
        accommodation.setCategoryDescription("Where you will stay, your environment, and the purpose of your trip.");
        accommodation.setDisplayOrder(3);
        accommodation.setIsOptional(false);
        accommodation.setQuestions(removeDuplicateQuestionKeys(ACCOMMODATION_AND_PURPOSE_QUESTIONS, seenQuestionKeys,
                "accommodation_environment"));
        categories.add(accommodation);

        OnboardingQuestionCategory activities = new OnboardingQuestionCategory();
        activities.setCategoryKey("planned_activities");
        activities.setCategoryName("Planned Activities");
        activities.setCategoryIcon("bug");
        activities.setCategoryDescription("Activities that may affect travel health risks.");
        activities.setDisplayOrder(4);
        activities.setIsOptional(false);
        activities.setQuestions(
                removeDuplicateQuestionKeys(PLANNED_ACTIVITIES_QUESTIONS, seenQuestionKeys, "planned_activities"));
        categories.add(activities);

        OnboardingQuestionCategory medical = new OnboardingQuestionCategory();
        medical.setCategoryKey("medical_history");
        medical.setCategoryName("Medical History");
        medical.setCategoryIcon("heart-pulse");
        medical.setCategoryDescription("Medical conditions, medications, allergies, and recent illnesses.");
        medical.setDisplayOrder(5);
        medical.setIsOptional(false);
        medical.setQuestions(removeDuplicateQuestionKeys(MEDICAL_QUESTIONS, seenQuestionKeys, "medical_history"));
        categories.add(medical);

        OnboardingQuestionCategory vaccines = new OnboardingQuestionCategory();
        vaccines.setCategoryKey("vaccination_history");
        vaccines.setCategoryName("Vaccinations & Past Travel History");
        vaccines.setCategoryIcon("syringe");
        vaccines.setCategoryDescription("Your vaccine history and prior international travel health experiences.");
        vaccines.setDisplayOrder(6);
        vaccines.setIsOptional(false);
        vaccines.setQuestions(removeDuplicateQuestionKeys(VACCINE_AND_TRAVEL_HISTORY_QUESTIONS, seenQuestionKeys,
                "vaccination_history"));
        categories.add(vaccines);

        OnboardingQuestionCategory awareness = new OnboardingQuestionCategory();
        awareness.setCategoryKey("awareness_preparation");
        awareness.setCategoryName("Awareness & Preparation");
        awareness.setCategoryIcon("shield-check");
        awareness.setCategoryDescription(
                "Let us know about your travel insurance and access to healthcare before your trip.");
        awareness.setDisplayOrder(7);
        awareness.setIsOptional(false);
        awareness.setQuestions(
                removeDuplicateQuestionKeys(AWARENESS_QUESTIONS, seenQuestionKeys, "awareness_preparation"));
        categories.add(awareness);

        OnboardingQuestionCategory risk = new OnboardingQuestionCategory();
        risk.setCategoryKey("personal_health_risk_behaviours");
        risk.setCategoryName("Personal Health & Risk Behaviours");
        risk.setCategoryIcon("bug");
        risk.setCategoryDescription(
                "Optional but important. Responses are confidential and used only to provide accurate health advice.");
        risk.setDisplayOrder(8);
        risk.setIsOptional(true);
        risk.setQuestions(removeDuplicateQuestionKeys(RISK_BEHAVIOUR_QUESTIONS, seenQuestionKeys,
                "personal_health_risk_behaviours"));
        categories.add(risk);

        // Collect the set of desired category keys for cleanup
        Set<String> desiredCategoryKeys = new HashSet<>();
        for (OnboardingQuestionCategory cat : categories) {
            desiredCategoryKeys.add(cat.getCategoryKey());
        }

        int createdCount = 0;
        int updatedCount = 0;
        for (OnboardingQuestionCategory category : categories) {
            Optional<OnboardingQuestionCategory> existing = repository.findByCategoryKey(category.getCategoryKey());
            if (existing.isPresent()) {
                OnboardingQuestionCategory entity = existing.get();
                entity.setCategoryName(category.getCategoryName());
                entity.setCategoryIcon(category.getCategoryIcon());
                entity.setCategoryDescription(category.getCategoryDescription());
                entity.setDisplayOrder(category.getDisplayOrder());
                entity.setIsOptional(category.getIsOptional());
                entity.setQuestions(category.getQuestions());
                repository.save(entity);
                updatedCount++;
            } else {
                repository.save(category);
                createdCount++;
            }
        }

        // Remove stale sections that no longer exist in the seeder
        int deletedCount = 0;
        List<OnboardingQuestionCategory> existingAll = repository.findAll();
        for (OnboardingQuestionCategory existing : existingAll) {
            if (!desiredCategoryKeys.contains(existing.getCategoryKey())) {
                repository.delete(existing);
                logger.info("Deleted stale onboarding question category: {}", existing.getCategoryKey());
                deletedCount++;
            }
        }

        logger.info("Onboarding question categories synced. Created: {}, Updated: {}, Deleted: {}", createdCount,
                updatedCount, deletedCount);
    }

    private String removeDuplicateQuestionKeys(String questionsJson, Set<String> seenKeys, String categoryKey) {
        try {
            JsonNode root = objectMapper.readTree(questionsJson);
            if (!root.isArray()) {
                return questionsJson;
            }

            ArrayNode filtered = objectMapper.createArrayNode();
            for (JsonNode questionNode : root) {
                JsonNode keyNode = questionNode.get("key");
                String key = keyNode != null ? keyNode.asText().trim() : "";
                if (key.isEmpty()) {
                    filtered.add(questionNode);
                    continue;
                }
                if (!seenKeys.add(key)) {
                    logger.warn("Skipping duplicate onboarding question key '{}' from category '{}'", key, categoryKey);
                    continue;
                }
                filtered.add(questionNode);
            }
            return objectMapper.writeValueAsString(filtered);
        } catch (Exception ex) {
            logger.warn("Failed to deduplicate onboarding questions for category '{}': {}", categoryKey,
                    ex.getMessage());
            return questionsJson;
        }
    }

    // ── Section 1: Personal Information (Q1-Q6) ──────────────────────────────
    private static final String PERSONAL_INFORMATION_QUESTIONS = """
            [
              {"key":"full_name_passport","text":"Full name (as on passport):","type":"text","required":true},
              {"key":"date_of_birth","text":"Date of birth:","type":"date","required":true},
              {"key":"gender","text":"Biological sex (for health advisory purposes):","type":"radio","required":true,"options":[{"value":"male","label":"Male"},{"value":"female","label":"Female"},{"value":"prefer_not_to_say","label":"Prefer not to say"}]},
              {"key":"nationality","text":"Nationality:","type":"country","required":true},
              {"key":"current_residence_country","text":"Country of current residence:","type":"country","required":true},
              {"key":"email_address","text":"Email address:","type":"text","required":true,"placeholder":"name@example.com"}
            ]
            """;

    // ── Section 2: Travel Itinerary (trip details, no purpose) ───────────────
    private static final String TRAVEL_QUESTIONS = """
            [
              {"key":"trip_itinerary","text":"Trip type and itinerary","description":"Choose trip type, then set dates with the calendar fields (not free text). Return and transit flows include quick +day shortcuts after you pick a departure.","type":"trip_itinerary","required":true},
              {"key":"flight_details","text":"Flight & Travel Journey Details","description":"Please describe all legs of your complete itinerary (outbound, any internal flights, and return if known). (optional)","type":"textarea","required":false,"placeholder":"e.g. London → Lagos (direct), then Lagos → Abuja (domestic)"},
              {"key":"longest_flight_leg_hours","text":"Longest single flight leg (hours)  (optional):","type":"text","required":false,"placeholder":"e.g. 6"},
              {"key":"total_flying_hours","text":"Total approximate flying time excluding layovers (hours) (optional):","type":"text","required":false,"placeholder":"e.g. 10"},
              {"key":"number_of_flight_legs","text":"Number of flight legs (e.g., 2 = outbound + return) (optional):","type":"text","required":false,"placeholder":"e.g. 2"},
              {"key":"long_haul_flight_legs","text":"Are any legs longer than 8 – 10 hours? (optional)","type":"radio","required":false,"options":[{"value":"yes","label":"Yes"},{"value":"no","label":"No"}]},
              {"key":"travel_companions","text":"Who will you be travelling with?","type":"radio","required":true,"options":[{"value":"alone","label":"Alone"},{"value":"family","label":"Family (note children's ages below)"},{"value":"friends","label":"Friends"},{"value":"colleagues","label":"Colleagues"}]},
              {"key":"travel_companions_children_ages","text":"If travelling with children, please note their ages:","type":"text","required":false,"placeholder":"e.g. 3, 7, 10","conditionalOn":{"travel_companions":"family"}}
            ]
            """;

    // ── Section 3: Accommodation & Environment + Purpose of Travel ───────────
    private static final String ACCOMMODATION_AND_PURPOSE_QUESTIONS = """
            [
              {"key":"main_accommodation","text":"Where will you mainly be staying?","type":"radio","required":true,"options":[{"value":"hotel","label":"Hotel"},{"value":"short_term_rental","label":"Short-term rental"},{"value":"family_friends","label":"Family or friends"},{"value":"student_housing","label":"Student housing"},{"value":"other","label":"Other (please specify)"}]},
              {"key":"main_accommodation_other","text":"Please specify other accommodation:","type":"text","required":false,"placeholder":"Please specify","conditionalOn":{"main_accommodation":"other"}},
              {"key":"stay_environment","text":"What best describes your stay environment?","type":"radio","required":true,"options":[{"value":"urban","label":"Mainly urban"},{"value":"rural","label":"Mainly rural"},{"value":"both","label":"Mixed (urban and rural)"}]},
              {"key":"purpose_of_travel","text":"What is the purpose of your travel? (Select all that apply)","type":"checkbox","required":true,"options":[{"value":"leisure_tourism","label":"Leisure / tourism"},{"value":"business_work","label":"Business / work"},{"value":"study_relocation","label":"Study / relocation"},{"value":"visiting_family_friends","label":"Visiting family or friends"},{"value":"religious_pilgrimage","label":"Religious pilgrimage"},{"value":"other","label":"Other (please specify)"}]},
              {"key":"purpose_of_travel_other","text":"Please specify other purpose of travel:","type":"text","required":false,"placeholder":"Please specify","conditionalOn":{"purpose_of_travel":"other"}}
            ]
            """;

    // ── Section 5: Planned Activities ────────────────────────────────────────
    private static final String PLANNED_ACTIVITIES_QUESTIONS = """
            [
              {"key":"planned_activities","text":"Do you plan to engage in any of the following? (Select all that apply)","type":"checkbox","required":false,"options":[{"value":"hiking_outdoor","label":"Hiking or outdoor adventure"},{"value":"swimming","label":"Swimming (ocean, lakes, rivers)"},{"value":"farm_animal_contact","label":"Farm or animal contact"},{"value":"volunteering_humanitarian","label":"Volunteering or humanitarian work"},{"value":"crowded_events","label":"Crowded events or festivals"},{"value":"food_tasting","label":"Food tasting / culinary experiences"},{"value":"altitude_travel","label":"High-altitude travel"},{"value":"healthcare_laboratory","label":"Healthcare or laboratory work"},{"value":"other","label":"Other (please specify)"},{"value":"none","label":"None of the above"}]},
              {"key":"planned_activities_other","text":"Please specify other planned activities:","type":"text","required":false,"placeholder":"Please specify","conditionalOn":{"planned_activities":"other"}},
              {"key":"additional_relevant_activities","text":"Is there any other activity you think may be relevant to your health? (Optional)","type":"textarea","required":false,"placeholder":"Share anything else relevant to your travel plans"}
            ]
            """;

    // ── Section 6: Medical History ────────────────────────────────────────────
    private static final String MEDICAL_QUESTIONS = """
            [
              {"key":"chronic_medical_conditions","text":"Do you have any ongoing or chronic medical conditions (e.g., diabetes, asthma, heart disease)?","type":"radio","required":true,"options":[{"value":"no","label":"No"},{"value":"yes","label":"Yes (please list)"}]},
              {"key":"chronic_medical_conditions_details","text":"Please list your ongoing medical conditions:","type":"textarea","required":false,"placeholder":"e.g. Type 2 diabetes, asthma","conditionalOn":{"chronic_medical_conditions":"yes"}},
              {"key":"poorly_controlled_conditions","text":"Are any of your medical conditions poorly controlled or recently worsened?","type":"radio","required":true,"options":[{"value":"no","label":"No"},{"value":"yes","label":"Yes (please specify)"}]},
              {"key":"poorly_controlled_conditions_details","text":"Please specify which conditions are poorly controlled:","type":"textarea","required":false,"placeholder":"Please specify","conditionalOn":{"poorly_controlled_conditions":"yes"}},
              {"key":"immunocompromised","text":"Do you have any condition or take any treatment that weakens your immune system (e.g., HIV, cancer therapy, long-term steroids)?","type":"radio","required":true,"options":[{"value":"no","label":"No"},{"value":"yes","label":"Yes (please specify)"}]},
              {"key":"immunocompromised_details","text":"Please specify:","type":"textarea","required":false,"placeholder":"Please specify","conditionalOn":{"immunocompromised":"yes"}},
              {"key":"current_medications","text":"Are you currently taking any medications (prescription or over-the-counter)?","type":"radio","required":true,"options":[{"value":"no","label":"No"},{"value":"yes","label":"Yes (please list and indicate what they are for)"}]},
              {"key":"current_medications_details","text":"Please list your medications and what they are for:","type":"textarea","required":false,"placeholder":"e.g. Metformin 500mg twice daily for diabetes","conditionalOn":{"current_medications":"yes"}},
              {"key":"allergies","text":"Do you have any known allergies (e.g., medications, foods, insect stings)?","type":"radio","required":true,"options":[{"value":"no","label":"No"},{"value":"yes","label":"Yes (please specify)"}]},
              {"key":"allergies_details","text":"Please specify your allergies:","type":"textarea","required":false,"placeholder":"e.g. Penicillin (anaphylaxis), peanuts","conditionalOn":{"allergies":"yes"}},
              {"key":"serious_illness_hospital_surgery_12_months","text":"Have you had any serious illness, hospital admission, or surgery in the past 12 months?","type":"radio","required":true,"options":[{"value":"no","label":"No"},{"value":"yes","label":"Yes (please describe)"}]},
              {"key":"serious_illness_details","text":"Please describe:","type":"textarea","required":false,"placeholder":"Please describe","conditionalOn":{"serious_illness_hospital_surgery_12_months":"yes"}},
              {"key":"dvt_risk_factors","text":"Do you have any personal risk factors for blood clots (DVT/PE)?","description":"Examples: pregnancy, age over 40, previous clots, heart/lung disease, recent surgery, immobility, or certain medications.","type":"radio","required":false,"options":[{"value":"yes","label":"Yes (please briefly list)"},{"value":"no","label":"No"}]},
              {"key":"dvt_risk_factors_details","text":"Please list your DVT/PE risk factors:","type":"textarea","required":false,"placeholder":"e.g. Previous DVT, taking oral contraceptives","conditionalOn":{"dvt_risk_factors":"yes"}},
              {"key":"pregnancy_status","text":"Are you pregnant?","type":"radio","required":false,"options":[{"value":"no","label":"No"},{"value":"yes","label":"Yes"},{"value":"not_sure","label":"Not sure"}],"conditionalOn":{"gender":"female|prefer_not_to_say"}},
              {"key":"pregnancy_gestational_age","text":"Estimated gestational age (if known):","type":"text","required":false,"placeholder":"e.g. 12 weeks","conditionalOn":{"pregnancy_status":"yes"}},
              {"key":"could_become_pregnant","text":"Could you become pregnant during this trip?","type":"radio","required":false,"options":[{"value":"no","label":"No"},{"value":"yes","label":"Yes"},{"value":"not_sure","label":"Not sure"}],"conditionalOn":{"gender":"female|prefer_not_to_say"}}
            ]
            """;

    // ── Section 7: Vaccinations & Past Travel History (combined) ─────────────
    private static final String VACCINE_AND_TRAVEL_HISTORY_QUESTIONS = """
            [
              {"key":"travel_related_vaccines_received","text":"Have you received any travel-related vaccines in the past?","type":"radio","required":true,"options":[{"value":"no","label":"No"},{"value":"yes","label":"Yes"},{"value":"not_sure","label":"Not sure"}]},
              {"key":"travel_related_vaccines_list","text":"If yes, please list any you remember (optional):","type":"textarea","required":false,"placeholder":"e.g. Yellow fever, Hepatitis A","conditionalOn":{"travel_related_vaccines_received":"yes"}},
              {"key":"routine_vaccinations_status","text":"Are you up to date with your routine vaccinations? (e.g., tetanus/diphtheria, MMR, influenza, COVID-19)","type":"radio","required":true,"options":[{"value":"yes_believe_so","label":"Yes"},{"value":"no_overdue","label":"No / Overdue"},{"value":"not_sure","label":"Not sure"}]},
              {"key":"vaccine_reaction_history","text":"Have you ever had a reaction to a vaccine?","type":"radio","required":true,"options":[{"value":"no","label":"No"},{"value":"yes","label":"Yes (please specify)"}]},
              {"key":"vaccine_reaction_details_vaccine","text":"Vaccine name (if known):","type":"text","required":false,"placeholder":"e.g. Yellow fever","conditionalOn":{"vaccine_reaction_history":"yes"}},
              {"key":"vaccine_reaction_details_severity","text":"Type of reaction:","type":"radio","required":false,"options":[{"value":"mild","label":"Mild"},{"value":"moderate","label":"Moderate"},{"value":"severe","label":"Severe"}],"conditionalOn":{"vaccine_reaction_history":"yes"}},
              {"key":"international_travel_last_12_months","text":"Have you travelled internationally in the last 12 months?","type":"radio","required":true,"options":[{"value":"no","label":"No"},{"value":"yes","label":"Yes"}]},
              {"key":"previous_travel_locations","text":"Where did you travel?","type":"multi_country","required":false,"conditionalOn":{"international_travel_last_12_months":"yes"}},
              {"key":"previous_trip_health_preparations","text":"Before your previous trip, which of the following did you do? (Select all that apply)","type":"checkbox","required":false,"conditionalOn":{"international_travel_last_12_months":"yes"},"options":[{"value":"visited_travel_clinic","label":"Visited a travel health clinic"},{"value":"received_vaccinations","label":"Received recommended vaccinations"},{"value":"malaria_prophylaxis","label":"Took malaria prevention medication"},{"value":"travel_insurance","label":"Purchased travel health insurance"},{"value":"doctor_letter","label":"Obtained a doctor's letter or medical summary"},{"value":"none","label":"None of the above"},{"value":"other","label":"Other (please specify)"}]},
              {"key":"previous_trip_health_preparations_other","text":"Please specify other health preparation:","type":"text","required":false,"placeholder":"Please specify","conditionalOn":{"previous_trip_health_preparations":"other"}},
              {"key":"previous_trip_health_problems","text":"Did you experience any health problems during or after that trip? (Select all that apply)","type":"checkbox","required":false,"conditionalOn":{"international_travel_last_12_months":"yes"},"options":[{"value":"gastrointestinal_illness","label":"Gastrointestinal illness (diarrhoea, vomiting)"},{"value":"fever_or_flu_like","label":"Fever or flu-like symptoms"},{"value":"skin_rash_or_reaction","label":"Skin rash or reaction"},{"value":"respiratory_illness","label":"Respiratory illness"},{"value":"injury_or_accident","label":"Injury or accident"},{"value":"mental_health_difficulties","label":"Mental health difficulties"},{"value":"no_health_problems","label":"No health problems"},{"value":"other","label":"Other (please specify)"}]},
              {"key":"previous_trip_health_problems_other","text":"Please specify other health problems:","type":"text","required":false,"placeholder":"Please specify","conditionalOn":{"previous_trip_health_problems":"other"}}
            ]
            """;

    // ── Section 7: Awareness & Preparation ───────────────────────────────────
    private static final String AWARENESS_QUESTIONS = """
            [
              {"key":"has_primary_care_physician","text":"Do you have a primary care physician or GP you can share this plan with?","type":"radio","required":true,"options":[{"value":"yes","label":"Yes"},{"value":"no","label":"No"}]},
              {"key":"travel_insurance","text":"Do you have travel health insurance?","type":"radio","required":true,"options":[{"value":"yes","label":"Yes"},{"value":"no","label":"No"}]}
            ]
            """;

    // ── Section 9: Personal Health & Risk Behaviours ──────────────────────────
    // Note: consent is handled on the section intro card in the frontend.
    // Questions are only shown after the user has agreed on the intro page.
    private static final String RISK_BEHAVIOUR_QUESTIONS = """
            [
              {"key":"anticipated_risk_behaviours","text":"During your travel, do you anticipate any of the following? (Select all that apply)","type":"checkbox","required":false,"options":[{"value":"new_sexual_partners","label":"New sexual partners"},{"value":"casual_relationships","label":"Casual or short-term relationships"},{"value":"alcohol_consumption","label":"Alcohol consumption"},{"value":"recreational_drug_use","label":"Recreational drug use"},{"value":"tattoo_piercing_cosmetic","label":"Tattooing, piercing, or cosmetic procedures abroad"},{"value":"none","label":"None of the above"}]},
              {"key":"sexual_activity_protection","text":"If you anticipate sexual activity during travel, do you usually:","type":"radio","required":false,"conditionalOn":{"anticipated_risk_behaviours":"new_sexual_partners|casual_relationships"},"options":[{"value":"use_barrier","label":"Use barrier protection (e.g. condoms)"},{"value":"sometimes","label":"Use protection sometimes"},{"value":"no_protection","label":"Do not use protection"},{"value":"prefer_not_to_say","label":"Prefer not to say"}]},
              {"key":"sti_history","text":"Have you ever been diagnosed with a sexually transmitted infection (STI)?","type":"radio","required":false,"options":[{"value":"yes","label":"Yes"},{"value":"no","label":"No"},{"value":"prefer_not_to_say","label":"Prefer not to say"}]},
              {"key":"substance_use_adherence_risk","text":"Do you anticipate alcohol or substance use that may affect your medication adherence or judgment during travel?","type":"radio","required":false,"options":[{"value":"yes","label":"Yes"},{"value":"no","label":"No"},{"value":"prefer_not_to_say","label":"Prefer not to say"}]},
              {"key":"lifestyle_additional_context","text":"Is there anything about your lifestyle or planned activities that you would like us to consider in your health plan? (Optional)","type":"textarea","required":false,"placeholder":"Any other relevant lifestyle or risk context"}
            ]
            """;
}
