package com.TravelMedicineAdvisory.Server.core.seeder;

import com.TravelMedicineAdvisory.Server.domain.useronboarding.OnboardingQuestionCategory;
import com.TravelMedicineAdvisory.Server.domain.useronboarding.OnboardingQuestionCategoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@Order(2)
public class OnboardingQuestionSeeder implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(OnboardingQuestionSeeder.class);

    @Value("${app.features.seeder.enabled:true}")
    private boolean seederEnabled;

    private final OnboardingQuestionCategoryRepository repository;

    public OnboardingQuestionSeeder(OnboardingQuestionCategoryRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (!seederEnabled) {
            return;
        }

        logger.info("Seeding onboarding questions...");

        List<OnboardingQuestionCategory> categories = new ArrayList<>();

        OnboardingQuestionCategory basic = new OnboardingQuestionCategory();
        basic.setCategoryKey("basic_information");
        basic.setCategoryName("Basic Information");
        basic.setCategoryIcon("shield-check");
        basic.setCategoryDescription("Identity and contact details used to personalise your advisory.");
        basic.setDisplayOrder(1);
        basic.setIsOptional(false);
        basic.setQuestions(BASIC_INFORMATION_QUESTIONS);
        categories.add(basic);

        OnboardingQuestionCategory travel = new OnboardingQuestionCategory();
        travel.setCategoryKey("travel_details");
        travel.setCategoryName("Travel Details");
        travel.setCategoryIcon("plane");
        travel.setCategoryDescription("Trip type, route, dates, and purpose.");
        travel.setDisplayOrder(2);
        travel.setIsOptional(false);
        travel.setQuestions(TRAVEL_QUESTIONS);
        categories.add(travel);

        OnboardingQuestionCategory accommodation = new OnboardingQuestionCategory();
        accommodation.setCategoryKey("accommodation_environment");
        accommodation.setCategoryName("Accommodation & Environment");
        accommodation.setCategoryIcon("shield-check");
        accommodation.setCategoryDescription("Where you will stay and the environment you'll spend most time in.");
        accommodation.setDisplayOrder(3);
        accommodation.setIsOptional(false);
        accommodation.setQuestions(ACCOMMODATION_QUESTIONS);
        categories.add(accommodation);

        OnboardingQuestionCategory activities = new OnboardingQuestionCategory();
        activities.setCategoryKey("planned_activities");
        activities.setCategoryName("Planned Activities");
        activities.setCategoryIcon("bug");
        activities.setCategoryDescription("Activities that may affect travel health risks.");
        activities.setDisplayOrder(4);
        activities.setIsOptional(false);
        activities.setQuestions(PLANNED_ACTIVITIES_QUESTIONS);
        categories.add(activities);

        OnboardingQuestionCategory medical = new OnboardingQuestionCategory();
        medical.setCategoryKey("medical_history");
        medical.setCategoryName("Medical History");
        medical.setCategoryIcon("heart-pulse");
        medical.setCategoryDescription("Medical history and medication context for safe recommendations.");
        medical.setDisplayOrder(5);
        medical.setIsOptional(false);
        medical.setQuestions(MEDICAL_QUESTIONS);
        categories.add(medical);

        OnboardingQuestionCategory vaccines = new OnboardingQuestionCategory();
        vaccines.setCategoryKey("vaccination_history");
        vaccines.setCategoryName("Vaccination & Travel Health History");
        vaccines.setCategoryIcon("syringe");
        vaccines.setCategoryDescription("Your vaccine and prior travel-health preparation history.");
        vaccines.setDisplayOrder(6);
        vaccines.setIsOptional(false);
        vaccines.setQuestions(VACCINE_QUESTIONS);
        categories.add(vaccines);

        OnboardingQuestionCategory awareness = new OnboardingQuestionCategory();
        awareness.setCategoryKey("awareness_preparation");
        awareness.setCategoryName("Awareness & Preparation");
        awareness.setCategoryIcon("shield-check");
        awareness.setCategoryDescription("Preparation status before travel.");
        awareness.setDisplayOrder(7);
        awareness.setIsOptional(false);
        awareness.setQuestions(AWARENESS_QUESTIONS);
        categories.add(awareness);

        OnboardingQuestionCategory afterTravel = new OnboardingQuestionCategory();
        afterTravel.setCategoryKey("after_travel");
        afterTravel.setCategoryName("After Travel");
        afterTravel.setCategoryIcon("plane");
        afterTravel.setCategoryDescription("Post-travel guidance preferences and additional context.");
        afterTravel.setDisplayOrder(8);
        afterTravel.setIsOptional(false);
        afterTravel.setQuestions(AFTER_TRAVEL_QUESTIONS);
        categories.add(afterTravel);

        OnboardingQuestionCategory risk = new OnboardingQuestionCategory();
        risk.setCategoryKey("personal_health_risk_behaviours");
        risk.setCategoryName("Personal Health & Risk Behaviours");
        risk.setCategoryIcon("bug");
        risk.setCategoryDescription("Optional but important. Responses are confidential and advisory-only.");
        risk.setDisplayOrder(9);
        risk.setIsOptional(true);
        risk.setQuestions(RISK_BEHAVIOUR_QUESTIONS);
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

    private static final String BASIC_INFORMATION_QUESTIONS = """
            [
              {
                "key": "full_name_passport",
                "text": "Full name (as on passport):",
                "type": "text",
                "required": true
              },
              {
                "key": "date_of_birth",
                "text": "Date of birth:",
                "type": "date",
                "required": true
              },
              {
                "key": "gender",
                "text": "Biological sex (for health advisory purposes):",
                "type": "radio",
                "required": true,
                "options": [
                  {"value": "male", "label": "Male"},
                  {"value": "female", "label": "Female"},
                  {"value": "prefer_not_to_say", "label": "Prefer not to say"}
                ]
              },
              {
                "key": "nationality",
                "text": "Nationality:",
                "type": "country",
                "required": true
              },
              {
                "key": "current_residence_country",
                "text": "Country of current residence:",
                "type": "country",
                "required": true
              },
              {
                "key": "email_address",
                "text": "Email address:",
                "type": "text",
                "required": true,
                "placeholder": "name@example.com"
              },
              {
                "key": "phone_number",
                "text": "Phone number:",
                "type": "text",
                "required": true,
                "placeholder": "+234..."
              }
            ]
            """;

    private static final String TRAVEL_QUESTIONS = """
            [
              {
                "key": "trip_itinerary",
                "text": "Trip type and itinerary",
                "description": "Start by selecting Single trip, Round trip, or Multi-stop.",
                "type": "trip_itinerary",
                "required": true
              },
              {
                "key": "purpose_of_travel",
                "text": "Purpose of travel (select all that apply):",
                "type": "checkbox",
                "required": true,
                "options": [
                  {"value": "leisure_tourism", "label": "Leisure / tourism"},
                  {"value": "business_work", "label": "Business / work"},
                  {"value": "study_relocation", "label": "Study / relocation"},
                  {"value": "visiting_family_friends", "label": "Visiting family or friends"},
                  {"value": "religious_pilgrimage", "label": "Religious pilgrimage"},
                  {"value": "other", "label": "Other (please specify)"}
                ]
              },
              {
                "key": "purpose_of_travel_other",
                "text": "Please specify other purpose of travel",
                "type": "text",
                "required": false,
                "placeholder": "Please specify",
                "conditionalOn": {"purpose_of_travel": "other"}
              },
              {
                "key": "travel_companions",
                "text": "Will you be travelling:",
                "type": "radio",
                "required": true,
                "options": [
                  {"value": "alone", "label": "Alone"},
                  {"value": "family", "label": "With family"},
                  {"value": "friends", "label": "With friends"},
                  {"value": "colleagues", "label": "With colleagues"}
                ]
              }
            ]
            """;

    private static final String ACCOMMODATION_QUESTIONS = """
            [
              {
                "key": "main_accommodation",
                "text": "Where will you mainly be staying?",
                "type": "radio",
                "required": true,
                "options": [
                  {"value": "hotel", "label": "Hotel"},
                  {"value": "short_term_rental", "label": "Short-term rental"},
                  {"value": "family_friends", "label": "Family / friends"},
                  {"value": "student_housing", "label": "Student housing"},
                  {"value": "other", "label": "Other"}
                ]
              },
              {
                "key": "main_accommodation_other",
                "text": "Please specify other accommodation",
                "type": "text",
                "required": false,
                "placeholder": "Please specify",
                "conditionalOn": {"main_accommodation": "other"}
              },
              {
                "key": "stay_environment",
                "text": "Will you be staying mostly in:",
                "type": "radio",
                "required": true,
                "options": [
                  {"value": "urban", "label": "Urban areas"},
                  {"value": "rural", "label": "Rural areas"},
                  {"value": "both", "label": "Both"}
                ]
              }
            ]
            """;

    private static final String PLANNED_ACTIVITIES_QUESTIONS = """
            [
              {
                "key": "planned_activities",
                "text": "Do you plan to engage in any of the following? (Select all that apply)",
                "type": "checkbox",
                "required": false,
                "options": [
                  {"value": "hiking_outdoor", "label": "Hiking or outdoor adventure"},
                  {"value": "swimming", "label": "Swimming (ocean, lakes, rivers)"},
                  {"value": "farm_animal_contact", "label": "Farm or animal contact"},
                  {"value": "volunteering_humanitarian", "label": "Volunteering or humanitarian work"},
                  {"value": "crowded_events", "label": "Crowded events or festivals"},
                  {"value": "altitude_travel", "label": "Altitude travel"},
                  {"value": "healthcare_laboratory", "label": "Healthcare or laboratory work"},
                  {"value": "other", "label": "Other (please specify)"},
                  {"value": "none", "label": "None of the above"}
                ]
              },
              {
                "key": "planned_activities_other",
                "text": "Please specify other planned activities",
                "type": "text",
                "required": false,
                "placeholder": "Please specify",
                "conditionalOn": {"planned_activities": "other"}
              },
              {
                "key": "additional_relevant_activities",
                "text": "Any other activities you think may be relevant to your health? (Optional)",
                "type": "textarea",
                "required": false,
                "placeholder": "Share anything else relevant to your travel plans"
              }
            ]
            """;

    private static final String MEDICAL_QUESTIONS = """
            [
              {
                "key": "long_term_medical_conditions",
                "text": "Do you have any long-term medical conditions?",
                "type": "radio",
                "required": true,
                "options": [
                  {"value": "yes", "label": "Yes (please list)"},
                  {"value": "no", "label": "No"}
                ]
              },
              {
                "key": "long_term_medical_conditions_details",
                "text": "Please list your long-term medical conditions",
                "type": "textarea",
                "required": false,
                "placeholder": "Please list your conditions",
                "conditionalOn": {"long_term_medical_conditions": "yes"}
              },
              {
                "key": "current_medications",
                "text": "Are you currently taking any medications (prescription or over-the-counter)?",
                "type": "radio",
                "required": true,
                "options": [
                  {"value": "yes", "label": "Yes (please list)"},
                  {"value": "no", "label": "No"}
                ]
              },
              {
                "key": "current_medications_details",
                "text": "Please list your medications",
                "type": "textarea",
                "required": false,
                "placeholder": "Medication name and dose",
                "conditionalOn": {"current_medications": "yes"}
              },
              {
                "key": "allergies",
                "text": "Do you have any allergies (medications, food, insect bites, etc.)?",
                "type": "radio",
                "required": true,
                "options": [
                  {"value": "yes", "label": "Yes (please specify)"},
                  {"value": "no", "label": "No"}
                ]
              },
              {
                "key": "allergies_details",
                "text": "Please specify your allergies",
                "type": "textarea",
                "required": false,
                "placeholder": "Please specify",
                "conditionalOn": {"allergies": "yes"}
              },
              {
                "key": "serious_illness_hospital_surgery_12_months",
                "text": "Have you had any serious illness, hospital admission, or surgery in the past 12 months?",
                "type": "radio",
                "required": true,
                "options": [
                  {"value": "yes", "label": "Yes"},
                  {"value": "no", "label": "No"}
                ]
              },
              {
                "key": "pregnancy_status",
                "text": "Are you pregnant, or could you become pregnant during this trip?",
                "type": "radio",
                "required": false,
                "options": [
                  {"value": "yes", "label": "Yes"},
                  {"value": "no", "label": "No"},
                  {"value": "not_sure", "label": "Not sure"}
                ],
                "conditionalOn": {"gender": "female|prefer_not_to_say"}
              }
            ]
            """;

    private static final String VACCINE_QUESTIONS = """
            [
              {
                "key": "travel_related_vaccines_received",
                "text": "Have you received any travel-related vaccines in the past?",
                "type": "radio",
                "required": true,
                "options": [
                  {"value": "yes", "label": "Yes"},
                  {"value": "no", "label": "No"},
                  {"value": "not_sure", "label": "Not sure"}
                ]
              },
              {
                "key": "travel_related_vaccines_list",
                "text": "If yes, please list any you remember (optional):",
                "type": "textarea",
                "required": false,
                "placeholder": "e.g. Yellow fever, Hepatitis A",
                "conditionalOn": {"travel_related_vaccines_received": "yes"}
              },
              {
                "key": "international_travel_last_12_months",
                "text": "Have you travelled internationally in the last 12 months?",
                "type": "radio",
                "required": true,
                "options": [
                  {"value": "yes", "label": "Yes"},
                  {"value": "no", "label": "No"}
                ]
              },
              {
                "key": "previous_travel_locations",
                "text": "Where did you travel? (Only shown if previous answer is Yes)",
                "type": "multi_country",
                "required": false,
                "conditionalOn": {"international_travel_last_12_months": "yes"}
              },
              {
                "key": "previous_trip_health_preparations",
                "text": "What health preparations did you make before that trip? (Select all that apply)",
                "type": "checkbox",
                "required": false,
                "conditionalOn": {"international_travel_last_12_months": "yes"},
                "options": [
                  {"value": "visited_travel_clinic", "label": "Visited a travel health clinic"},
                  {"value": "received_vaccinations", "label": "Received vaccinations"},
                  {"value": "malaria_prophylaxis", "label": "Obtained malaria prophylaxis"},
                  {"value": "travel_insurance", "label": "Purchased travel insurance"},
                  {"value": "doctor_letter", "label": "Got a doctor's letter / medical summary"},
                  {"value": "none", "label": "None"},
                  {"value": "other", "label": "Other (please specify)"}
                ]
              },
              {
                "key": "previous_trip_health_preparations_other",
                "text": "Please specify other health preparation",
                "type": "text",
                "required": false,
                "placeholder": "Please specify",
                "conditionalOn": {"previous_trip_health_preparations": "other"}
              },
              {
                "key": "previous_trip_health_problems",
                "text": "Did you experience any health problems during or after that trip? (Select all that apply)",
                "type": "checkbox",
                "required": false,
                "conditionalOn": {"international_travel_last_12_months": "yes"},
                "options": [
                  {"value": "gastrointestinal_illness", "label": "Gastrointestinal illness (diarrhoea, vomiting)"},
                  {"value": "fever_or_flu_like", "label": "Fever or flu-like symptoms"},
                  {"value": "skin_rash_or_reaction", "label": "Skin rash or reaction"},
                  {"value": "respiratory_illness", "label": "Respiratory illness"},
                  {"value": "injury_or_accident", "label": "Injury or accident"},
                  {"value": "mental_health_difficulties", "label": "Mental health difficulties"},
                  {"value": "no_health_problems", "label": "No health problems"},
                  {"value": "other", "label": "Other (please specify)"}
                ]
              },
              {
                "key": "previous_trip_health_problems_other",
                "text": "Please specify other health problems",
                "type": "text",
                "required": false,
                "placeholder": "Please specify",
                "conditionalOn": {"previous_trip_health_problems": "other"}
              }
            ]
            """;

    private static final String AWARENESS_QUESTIONS = """
            [
              {
                "key": "has_primary_care_physician",
                "text": "Do you have a primary care physician or GP you can share this plan with?",
                "type": "radio",
                "required": true,
                "options": [
                  {"value": "yes", "label": "Yes"},
                  {"value": "no", "label": "No"},
                  {"value": "not_applicable", "label": "Not applicable"}
                ]
              },
              {
                "key": "travel_insurance",
                "text": "Do you already have travel insurance?",
                "type": "radio",
                "required": true,
                "options": [
                  {"value": "yes", "label": "Yes"},
                  {"value": "no", "label": "No"},
                  {"value": "planning", "label": "Planning to get one"}
                ]
              }
            ]
            """;

    private static final String AFTER_TRAVEL_QUESTIONS = """
            [
              {
                "key": "wants_post_travel_guidance",
                "text": "Would you like to receive post-travel health guidance after you return?",
                "description": "Post-travel assessment is included in the Platinum plan. If selected and you are on a lower tier, you will be offered an upgrade.",
                "type": "radio",
                "required": true,
                "options": [
                  {"value": "yes", "label": "Yes"},
                  {"value": "no", "label": "No"}
                ]
              },
              {
                "key": "additional_considerations",
                "text": "Is there anything else you would like us to consider when preparing your travel health plan? (Optional)",
                "type": "textarea",
                "required": false,
                "placeholder": "Any additional context you'd like us to consider"
              }
            ]
            """;

    private static final String RISK_BEHAVIOUR_QUESTIONS = """
            [
              {
                "key": "risk_section_consent",
                "text": "I understand and agree to proceed with this section",
                "description": "The following responses are confidential and used only for advisory purposes.",
                "type": "checkbox",
                "required": false,
                "options": [
                  {"value": "agree", "label": "I understand and agree"}
                ]
              },
              {
                "key": "anticipated_risk_behaviours",
                "text": "During your travel, do you anticipate any of the following? (Select all that apply)",
                "type": "checkbox",
                "required": false,
                "conditionalOn": {"risk_section_consent": "agree"},
                "options": [
                  {"value": "new_sexual_partners", "label": "New sexual partners"},
                  {"value": "casual_relationships", "label": "Casual or short-term relationships"},
                  {"value": "alcohol_consumption", "label": "Alcohol consumption"},
                  {"value": "recreational_drug_use", "label": "Recreational drug use"},
                  {"value": "tattoo_piercing_cosmetic", "label": "Tattooing, piercing, or cosmetic procedures abroad"},
                  {"value": "none", "label": "None of the above"}
                ]
              },
              {
                "key": "sexual_activity_protection",
                "text": "If you anticipate sexual activity during travel, do you usually:",
                "type": "radio",
                "required": false,
                "conditionalOn": {"anticipated_risk_behaviours": "new_sexual_partners|casual_relationships"},
                "options": [
                  {"value": "use_barrier", "label": "Use barrier protection (e.g. condoms)"},
                  {"value": "sometimes", "label": "Use protection sometimes"},
                  {"value": "no_protection", "label": "Not use protection"},
                  {"value": "prefer_not_to_say", "label": "Prefer not to say"}
                ]
              },
              {
                "key": "sti_history",
                "text": "Have you ever been diagnosed with a sexually transmitted infection (STI)?",
                "type": "radio",
                "required": false,
                "conditionalOn": {"risk_section_consent": "agree"},
                "options": [
                  {"value": "yes", "label": "Yes"},
                  {"value": "no", "label": "No"},
                  {"value": "prefer_not_to_say", "label": "Prefer not to say"}
                ]
              },
              {
                "key": "substance_use_adherence_risk",
                "text": "Do you anticipate alcohol or substance use that may affect medication adherence or judgment during travel?",
                "type": "radio",
                "required": false,
                "conditionalOn": {"risk_section_consent": "agree"},
                "options": [
                  {"value": "yes", "label": "Yes"},
                  {"value": "no", "label": "No"},
                  {"value": "prefer_not_to_say", "label": "Prefer not to say"}
                ]
              },
              {
                "key": "lifestyle_additional_context",
                "text": "Is there anything about your lifestyle or planned activities during travel that you would like us to factor into your health plan? (Optional)",
                "type": "textarea",
                "required": false,
                "placeholder": "Any other relevant lifestyle or risk context",
                "conditionalOn": {"risk_section_consent": "agree"}
              }
            ]
            """;
}

