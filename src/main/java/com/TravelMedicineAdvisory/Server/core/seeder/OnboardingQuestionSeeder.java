package com.TravelMedicineAdvisory.Server.core.seeder;

import com.TravelMedicineAdvisory.Server.domain.useronboarding.OnboardingQuestionCategory;
import com.TravelMedicineAdvisory.Server.domain.useronboarding.OnboardingQuestionCategoryRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
        personal.setCategoryDescription("Identity and contact details used to personalise your advisory.");
        personal.setDisplayOrder(1);
        personal.setIsOptional(false);
        personal.setQuestions(removeDuplicateQuestionKeys(PERSONAL_INFORMATION_QUESTIONS, seenQuestionKeys, "personal_information"));
        categories.add(personal);

        OnboardingQuestionCategory travel = new OnboardingQuestionCategory();
        travel.setCategoryKey("travel_details");
        travel.setCategoryName("Travel Details");
        travel.setCategoryIcon("plane");
        travel.setCategoryDescription("Trip type, route, dates, flight details, purpose, and companions.");
        travel.setDisplayOrder(2);
        travel.setIsOptional(false);
        travel.setQuestions(removeDuplicateQuestionKeys(TRAVEL_QUESTIONS, seenQuestionKeys, "travel_details"));
        categories.add(travel);

        OnboardingQuestionCategory accommodation = new OnboardingQuestionCategory();
        accommodation.setCategoryKey("accommodation_environment");
        accommodation.setCategoryName("Accommodation & Environment");
        accommodation.setCategoryIcon("shield-check");
        accommodation.setCategoryDescription("Where you will stay and the environment type.");
        accommodation.setDisplayOrder(3);
        accommodation.setIsOptional(false);
        accommodation.setQuestions(removeDuplicateQuestionKeys(ACCOMMODATION_QUESTIONS, seenQuestionKeys, "accommodation_environment"));
        categories.add(accommodation);

        OnboardingQuestionCategory activities = new OnboardingQuestionCategory();
        activities.setCategoryKey("planned_activities");
        activities.setCategoryName("Planned Activities");
        activities.setCategoryIcon("bug");
        activities.setCategoryDescription("Activities that may affect travel health risks.");
        activities.setDisplayOrder(4);
        activities.setIsOptional(false);
        activities.setQuestions(removeDuplicateQuestionKeys(PLANNED_ACTIVITIES_QUESTIONS, seenQuestionKeys, "planned_activities"));
        categories.add(activities);

        OnboardingQuestionCategory medical = new OnboardingQuestionCategory();
        medical.setCategoryKey("medical_history");
        medical.setCategoryName("Medical History");
        medical.setCategoryIcon("heart-pulse");
        medical.setCategoryDescription("Medical conditions, medications, allergies, and obstetric history.");
        medical.setDisplayOrder(5);
        medical.setIsOptional(false);
        medical.setQuestions(removeDuplicateQuestionKeys(MEDICAL_QUESTIONS, seenQuestionKeys, "medical_history"));
        categories.add(medical);

        OnboardingQuestionCategory vaccines = new OnboardingQuestionCategory();
        vaccines.setCategoryKey("vaccination_history");
        vaccines.setCategoryName("Vaccination & Travel Health History");
        vaccines.setCategoryIcon("syringe");
        vaccines.setCategoryDescription("Your vaccine history and prior travel-health preparation.");
        vaccines.setDisplayOrder(6);
        vaccines.setIsOptional(false);
        vaccines.setQuestions(removeDuplicateQuestionKeys(VACCINE_QUESTIONS, seenQuestionKeys, "vaccination_history"));
        categories.add(vaccines);

        OnboardingQuestionCategory travelHistory = new OnboardingQuestionCategory();
        travelHistory.setCategoryKey("travel_history");
        travelHistory.setCategoryName("Travel History");
        travelHistory.setCategoryIcon("plane");
        travelHistory.setCategoryDescription("Your recent international travel and health experiences abroad.");
        travelHistory.setDisplayOrder(7);
        travelHistory.setIsOptional(false);
        travelHistory.setQuestions(removeDuplicateQuestionKeys(TRAVEL_HISTORY_QUESTIONS, seenQuestionKeys, "travel_history"));
        categories.add(travelHistory);

        OnboardingQuestionCategory awareness = new OnboardingQuestionCategory();
        awareness.setCategoryKey("awareness_preparation");
        awareness.setCategoryName("Awareness & Preparation");
        awareness.setCategoryIcon("shield-check");
        awareness.setCategoryDescription("Insurance and physician access before travel.");
        awareness.setDisplayOrder(8);
        awareness.setIsOptional(false);
        awareness.setQuestions(removeDuplicateQuestionKeys(AWARENESS_QUESTIONS, seenQuestionKeys, "awareness_preparation"));
        categories.add(awareness);

        OnboardingQuestionCategory risk = new OnboardingQuestionCategory();
        risk.setCategoryKey("personal_health_risk_behaviours");
        risk.setCategoryName("Personal Health & Risk Behaviours");
        risk.setCategoryIcon("bug");
        risk.setCategoryDescription("Optional but important. Responses are confidential and advisory-only.");
        risk.setDisplayOrder(9);
        risk.setIsOptional(true);
        risk.setQuestions(removeDuplicateQuestionKeys(RISK_BEHAVIOUR_QUESTIONS, seenQuestionKeys, "personal_health_risk_behaviours"));
        categories.add(risk);

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

        logger.info("Onboarding question categories synced. Created: {}, Updated: {}", createdCount, updatedCount);
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
            logger.warn("Failed to deduplicate onboarding questions for category '{}': {}", categoryKey, ex.getMessage());
            return questionsJson;
        }
    }

    // ── Section 1: Personal Information (Q1-Q8) ──────────────────────────────
    private static final String PERSONAL_INFORMATION_QUESTIONS = """
            [
              {"key":"full_name_passport","text":"Full name (as on passport):","type":"text","required":true},
              {"key":"date_of_birth","text":"Date of birth:","type":"date","required":true},
              {"key":"gender","text":"Biological sex (for health advisory purposes):","type":"radio","required":true,"options":[{"value":"male","label":"Male"},{"value":"female","label":"Female"},{"value":"prefer_not_to_say","label":"Prefer not to say"}]},
              {"key":"nationality","text":"Nationality:","type":"country","required":true},
              {"key":"current_residence_country","text":"Country of current residence:","type":"country","required":true},
              {"key":"email_address","text":"Email address:","type":"text","required":true,"placeholder":"name@example.com"},
              {"key":"phone_number","text":"Phone number:","type":"text","required":true,"placeholder":"+234..."},
              {"key":"preferred_language","text":"Preferred language for communication:","type":"radio","required":true,"options":[{"value":"english","label":"English"},{"value":"french","label":"French"},{"value":"arabic","label":"Arabic"},{"value":"other","label":"Other (please specify)"}]},
              {"key":"preferred_language_other","text":"Please specify your preferred language:","type":"text","required":false,"placeholder":"Please specify","conditionalOn":{"preferred_language":"other"}}
            ]
            """;

    // ── Section 2: Travel Details (Q9-Q17) ───────────────────────────────────
    private static final String TRAVEL_QUESTIONS = """
            [
              {"key":"trip_itinerary","text":"Trip type and itinerary","description":"Start by selecting Single trip, Round trip, or Multi-stop.","type":"trip_itinerary","required":true},
              {"key":"flight_details","text":"Flight & Travel Journey Details","description":"Please describe all legs of your complete itinerary (outbound, any internal flights, and return if known).","type":"textarea","required":false,"placeholder":"e.g. London → Lagos (direct), then Lagos → Abuja (domestic)"},
              {"key":"longest_flight_leg_hours","text":"Longest single flight leg (hours):","type":"text","required":false,"placeholder":"e.g. 6"},
              {"key":"total_flying_hours","text":"Total approximate flying time excluding layovers (hours):","type":"text","required":false,"placeholder":"e.g. 10"},
              {"key":"number_of_flight_legs","text":"Number of flight legs (e.g., 2 = outbound + return):","type":"text","required":false,"placeholder":"e.g. 2"},
              {"key":"airline_flight_numbers","text":"Airline(s) and flight number(s) (if known):","type":"text","required":false,"placeholder":"e.g. BA 206, AF 1234"},
              {"key":"short_domestic_flights","text":"Are any legs domestic or short regional flights (<4 hours)?","type":"radio","required":false,"options":[{"value":"yes","label":"Yes"},{"value":"no","label":"No"}]},
              {"key":"dvt_risk_factors","text":"Do you have any personal risk factors for blood clots (DVT/PE)?","description":"Examples: pregnancy, age over 40, previous clots, heart/lung disease, recent surgery, immobility, or certain medications.","type":"radio","required":false,"options":[{"value":"yes","label":"Yes (please briefly list)"},{"value":"no","label":"No"}]},
              {"key":"dvt_risk_factors_details","text":"Please list your DVT/PE risk factors:","type":"textarea","required":false,"placeholder":"e.g. Previous DVT, taking oral contraceptives","conditionalOn":{"dvt_risk_factors":"yes"}},
              {"key":"purpose_of_travel","text":"What is the purpose of your travel? (Select all that apply)","type":"checkbox","required":true,"options":[{"value":"leisure_tourism","label":"Leisure / tourism"},{"value":"business_work","label":"Business / work"},{"value":"study_relocation","label":"Study / relocation"},{"value":"visiting_family_friends","label":"Visiting family or friends"},{"value":"religious_pilgrimage","label":"Religious pilgrimage"},{"value":"other","label":"Other (please specify)"}]},
              {"key":"purpose_of_travel_other","text":"Please specify other purpose of travel:","type":"text","required":false,"placeholder":"Please specify","conditionalOn":{"purpose_of_travel":"other"}},
              {"key":"travel_companions","text":"Who will you be travelling with?","type":"radio","required":true,"options":[{"value":"alone","label":"Alone"},{"value":"family","label":"Family (note children's ages below)"},{"value":"friends","label":"Friends"},{"value":"colleagues","label":"Colleagues"}]},
              {"key":"travel_companions_children_ages","text":"If travelling with children, please note their ages:","type":"text","required":false,"placeholder":"e.g. 3, 7, 10","conditionalOn":{"travel_companions":"family"}}
            ]
            """;

    // ── Section 3: Accommodation & Environment (Q18-Q19) ─────────────────────
    private static final String ACCOMMODATION_QUESTIONS = """
            [
              {"key":"main_accommodation","text":"Where will you mainly be staying?","type":"radio","required":true,"options":[{"value":"hotel","label":"Hotel"},{"value":"short_term_rental","label":"Short-term rental"},{"value":"family_friends","label":"Family or friends"},{"value":"student_housing","label":"Student housing"},{"value":"other","label":"Other (please specify)"}]},
              {"key":"main_accommodation_other","text":"Please specify other accommodation:","type":"text","required":false,"placeholder":"Please specify","conditionalOn":{"main_accommodation":"other"}},
              {"key":"stay_environment","text":"What best describes your stay environment?","type":"radio","required":true,"options":[{"value":"urban","label":"Mainly urban"},{"value":"rural","label":"Mainly rural"},{"value":"both","label":"Mixed (urban and rural)"}]}
            ]
            """;

    // ── Section 4: Planned Activities (Q20-Q21) ──────────────────────────────
    private static final String PLANNED_ACTIVITIES_QUESTIONS = """
            [
              {"key":"planned_activities","text":"Do you plan to engage in any of the following? (Select all that apply)","type":"checkbox","required":false,"options":[{"value":"hiking_outdoor","label":"Hiking or outdoor adventure"},{"value":"swimming","label":"Swimming (ocean, lakes, rivers)"},{"value":"farm_animal_contact","label":"Farm or animal contact"},{"value":"volunteering_humanitarian","label":"Volunteering or humanitarian work"},{"value":"crowded_events","label":"Crowded events or festivals"},{"value":"altitude_travel","label":"High-altitude travel"},{"value":"healthcare_laboratory","label":"Healthcare or laboratory work"},{"value":"other","label":"Other (please specify)"},{"value":"none","label":"None of the above"}]},
              {"key":"planned_activities_other","text":"Please specify other planned activities:","type":"text","required":false,"placeholder":"Please specify","conditionalOn":{"planned_activities":"other"}},
              {"key":"activity_frequency","text":"How frequently or intensively will you engage in these activities? (Optional)","type":"radio","required":false,"options":[{"value":"occasional","label":"Occasional"},{"value":"moderate","label":"Moderate"},{"value":"frequent","label":"Frequent"}]},
              {"key":"additional_relevant_activities","text":"Is there any other activity you think may be relevant to your health? (Optional)","type":"textarea","required":false,"placeholder":"Share anything else relevant to your travel plans"}
            ]
            """;

    // ── Section 5: Medical History (Q22-Q29) ─────────────────────────────────
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
              {"key":"allergies","text":"Do you have any allergies (e.g., medications, foods, insect stings)?","type":"radio","required":true,"options":[{"value":"no","label":"No"},{"value":"yes","label":"Yes (please specify)"}]},
              {"key":"allergies_details","text":"Please specify your allergies:","type":"textarea","required":false,"placeholder":"e.g. Penicillin (anaphylaxis), peanuts","conditionalOn":{"allergies":"yes"}},
              {"key":"serious_illness_hospital_surgery_12_months","text":"Have you had any serious illness, hospital admission, or surgery in the past 12 months?","type":"radio","required":true,"options":[{"value":"no","label":"No"},{"value":"yes","label":"Yes (please describe)"}]},
              {"key":"serious_illness_details","text":"Please describe:","type":"textarea","required":false,"placeholder":"Please describe","conditionalOn":{"serious_illness_hospital_surgery_12_months":"yes"}},
              {"key":"pregnancy_status","text":"Are you pregnant?","type":"radio","required":false,"options":[{"value":"no","label":"No"},{"value":"yes","label":"Yes"},{"value":"not_sure","label":"Not sure"}],"conditionalOn":{"gender":"female|prefer_not_to_say"}},
              {"key":"pregnancy_gestational_age","text":"Estimated gestational age (if known):","type":"text","required":false,"placeholder":"e.g. 12 weeks","conditionalOn":{"pregnancy_status":"yes"}},
              {"key":"could_become_pregnant","text":"Could you become pregnant during this trip?","type":"radio","required":false,"options":[{"value":"no","label":"No"},{"value":"yes","label":"Yes"},{"value":"not_sure","label":"Not sure"}],"conditionalOn":{"gender":"female|prefer_not_to_say"}}
            ]
            """;

    // ── Section 6: Vaccination & Travel Health History (Q30-Q32) ─────────────
    private static final String VACCINE_QUESTIONS = """
            [
              {"key":"travel_related_vaccines_received","text":"Have you received any travel-related vaccines in the past?","type":"radio","required":true,"options":[{"value":"no","label":"No"},{"value":"yes","label":"Yes"},{"value":"not_sure","label":"Not sure"}]},
              {"key":"travel_related_vaccines_list","text":"If yes, please list any you remember (optional):","type":"textarea","required":false,"placeholder":"e.g. Yellow fever, Hepatitis A","conditionalOn":{"travel_related_vaccines_received":"yes"}},
              {"key":"routine_vaccinations_status","text":"Are you up to date with your routine vaccinations? (e.g., tetanus/diphtheria, MMR, influenza, COVID-19)","description":"Routine vaccines affect what travel vaccines you may need and in what sequence.","type":"radio","required":true,"options":[{"value":"yes_believe_so","label":"Yes, I believe so"},{"value":"no_overdue","label":"No / I am overdue on some"},{"value":"not_sure","label":"Not sure / I do not know my vaccination history"}]},
              {"key":"vaccine_reaction_history","text":"Have you ever had a reaction to a vaccine?","type":"radio","required":true,"options":[{"value":"no","label":"No"},{"value":"yes","label":"Yes (please specify)"}]},
              {"key":"vaccine_reaction_details_vaccine","text":"Vaccine name (if known):","type":"text","required":false,"placeholder":"e.g. Yellow fever","conditionalOn":{"vaccine_reaction_history":"yes"}},
              {"key":"vaccine_reaction_details_severity","text":"Type of reaction:","type":"radio","required":false,"options":[{"value":"mild","label":"Mild (fever, soreness)"},{"value":"moderate","label":"Moderate (rash, swelling)"},{"value":"severe","label":"Severe (e.g., anaphylaxis, hospitalization)"}],"conditionalOn":{"vaccine_reaction_history":"yes"}}
            ]
            """;

    // ── Section 7: Travel History (Q33-Q36) ──────────────────────────────────
    private static final String TRAVEL_HISTORY_QUESTIONS = """
            [
              {"key":"international_travel_last_12_months","text":"Have you travelled internationally in the last 12 months?","type":"radio","required":true,"options":[{"value":"no","label":"No"},{"value":"yes","label":"Yes"}]},
              {"key":"previous_travel_locations","text":"Where did you travel?","type":"multi_country","required":false,"conditionalOn":{"international_travel_last_12_months":"yes"}},
              {"key":"travel_frequency","text":"How often do you travel internationally?","type":"radio","required":true,"options":[{"value":"first_time","label":"First time"},{"value":"once_per_year","label":"Once per year"},{"value":"2_4_times_per_year","label":"2-4 times per year"},{"value":"5_plus_times_per_year","label":"5 or more times per year"}]},
              {"key":"previous_trip_health_preparations","text":"Before your previous trip, which of the following did you do? (Select all that apply)","type":"checkbox","required":false,"conditionalOn":{"international_travel_last_12_months":"yes"},"options":[{"value":"visited_travel_clinic","label":"Visited a travel health clinic"},{"value":"received_vaccinations","label":"Received recommended vaccinations"},{"value":"malaria_prophylaxis","label":"Took malaria prevention medication"},{"value":"travel_insurance","label":"Purchased travel health insurance"},{"value":"doctor_letter","label":"Obtained a doctor's letter or medical summary"},{"value":"none","label":"None of the above"},{"value":"other","label":"Other (please specify)"}]},
              {"key":"previous_trip_health_preparations_other","text":"Please specify other health preparation:","type":"text","required":false,"placeholder":"Please specify","conditionalOn":{"previous_trip_health_preparations":"other"}},
              {"key":"previous_trip_health_problems","text":"Did you experience any health problems during or after that trip? (Select all that apply)","type":"checkbox","required":false,"conditionalOn":{"international_travel_last_12_months":"yes"},"options":[{"value":"gastrointestinal_illness","label":"Gastrointestinal illness (diarrhoea, vomiting)"},{"value":"fever_or_flu_like","label":"Fever or flu-like symptoms"},{"value":"skin_rash_or_reaction","label":"Skin rash or reaction"},{"value":"respiratory_illness","label":"Respiratory illness"},{"value":"injury_or_accident","label":"Injury or accident"},{"value":"mental_health_difficulties","label":"Mental health difficulties"},{"value":"no_health_problems","label":"No health problems"},{"value":"other","label":"Other (please specify)"}]},
              {"key":"previous_trip_health_problems_other","text":"Please specify other health problems:","type":"text","required":false,"placeholder":"Please specify","conditionalOn":{"previous_trip_health_problems":"other"}}
            ]
            """;

    // ── Section 8: Awareness & Preparation (Q37-Q38) ─────────────────────────
    private static final String AWARENESS_QUESTIONS = """
            [
              {"key":"has_primary_care_physician","text":"Do you have a primary care physician or GP you can share this plan with?","type":"radio","required":true,"options":[{"value":"yes","label":"Yes"},{"value":"no","label":"No"},{"value":"not_applicable","label":"Not applicable"}]},
              {"key":"travel_insurance","text":"Do you have travel health insurance?","type":"radio","required":true,"options":[{"value":"yes","label":"Yes"},{"value":"no","label":"No"},{"value":"planning","label":"Planning to get one"}]}
            ]
            """;

    // ── Section 9: Personal Health & Risk Behaviours (Q39-Q43) ───────────────
    private static final String RISK_BEHAVIOUR_QUESTIONS = """
            [
              {"key":"risk_section_consent","text":"I understand and agree to answer this section","description":"Your responses are confidential and used only to provide accurate health advice.","type":"checkbox","required":false,"options":[{"value":"agree","label":"I understand and agree"}]},
              {"key":"anticipated_risk_behaviours","text":"During your travel, do you anticipate any of the following? (Select all that apply)","type":"checkbox","required":false,"conditionalOn":{"risk_section_consent":"agree"},"options":[{"value":"new_sexual_partners","label":"New sexual partners"},{"value":"casual_relationships","label":"Casual or short-term relationships"},{"value":"alcohol_consumption","label":"Alcohol consumption"},{"value":"recreational_drug_use","label":"Recreational drug use"},{"value":"tattoo_piercing_cosmetic","label":"Tattooing, piercing, or cosmetic procedures abroad"},{"value":"none","label":"None of the above"}]},
              {"key":"sexual_activity_protection","text":"If you anticipate sexual activity during travel, do you usually:","type":"radio","required":false,"conditionalOn":{"anticipated_risk_behaviours":"new_sexual_partners|casual_relationships"},"options":[{"value":"use_barrier","label":"Use barrier protection (e.g. condoms)"},{"value":"sometimes","label":"Use protection sometimes"},{"value":"no_protection","label":"Do not use protection"},{"value":"prefer_not_to_say","label":"Prefer not to say"}]},
              {"key":"sti_history","text":"Have you ever been diagnosed with a sexually transmitted infection (STI)?","type":"radio","required":false,"conditionalOn":{"risk_section_consent":"agree"},"options":[{"value":"yes","label":"Yes"},{"value":"no","label":"No"},{"value":"prefer_not_to_say","label":"Prefer not to say"}]},
              {"key":"substance_use_adherence_risk","text":"Do you anticipate alcohol or substance use that may affect your medication adherence or judgment during travel?","type":"radio","required":false,"conditionalOn":{"risk_section_consent":"agree"},"options":[{"value":"yes","label":"Yes"},{"value":"no","label":"No"},{"value":"prefer_not_to_say","label":"Prefer not to say"}]},
              {"key":"lifestyle_additional_context","text":"Is there anything about your lifestyle or planned activities that you would like us to consider in your health plan? (Optional)","type":"textarea","required":false,"placeholder":"Any other relevant lifestyle or risk context","conditionalOn":{"risk_section_consent":"agree"}}
            ]
            """;
}
