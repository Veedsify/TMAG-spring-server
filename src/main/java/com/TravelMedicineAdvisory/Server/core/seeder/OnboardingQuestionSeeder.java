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
        if (!seederEnabled)
            return;
        // Always re-seed to pick up question structure changes
        if (repository.count() > 0) {
            repository.deleteAll();
            logger.info("Clearing existing onboarding questions for re-seed...");
        }

        logger.info("Seeding onboarding questions...");

        List<OnboardingQuestionCategory> categories = new ArrayList<>();

        // ─── Category 1: Travel Details ──────────────────────────
        OnboardingQuestionCategory travel = new OnboardingQuestionCategory();
        travel.setCategoryKey("travel_details");
        travel.setCategoryName("Travel Details");
        travel.setCategoryIcon("plane");
        travel.setCategoryDescription(
                "Tell us about your upcoming trip so we can provide destination-specific health advice.");
        travel.setDisplayOrder(1);
        travel.setIsOptional(false);
        travel.setQuestions(TRAVEL_QUESTIONS);
        categories.add(travel);

        // ─── Category 2: Medical History ─────────────────────────
        OnboardingQuestionCategory medical = new OnboardingQuestionCategory();
        medical.setCategoryKey("medical_history");
        medical.setCategoryName("Medical History");
        medical.setCategoryIcon("heart-pulse");
        medical.setCategoryDescription("This information is essential for safe and personalised recommendations.");
        medical.setDisplayOrder(2);
        medical.setIsOptional(false);
        medical.setQuestions(MEDICAL_QUESTIONS);
        categories.add(medical);

        // ─── Category 3: Vaccination History ─────────────────────
        OnboardingQuestionCategory vaccines = new OnboardingQuestionCategory();
        vaccines.setCategoryKey("vaccination_history");
        vaccines.setCategoryName("Vaccination History");
        vaccines.setCategoryIcon("syringe");
        vaccines.setCategoryDescription(
                "Please indicate which vaccines you have received. A best guess is fine if you do not have your records to hand.");
        vaccines.setDisplayOrder(3);
        vaccines.setIsOptional(false);
        vaccines.setQuestions(VACCINE_QUESTIONS);
        categories.add(vaccines);

        // ─── Category 4: Malaria & Tropical Disease ──────────────
        OnboardingQuestionCategory malaria = new OnboardingQuestionCategory();
        malaria.setCategoryKey("malaria_tropical");
        malaria.setCategoryName("Malaria & Tropical Disease Risk");
        malaria.setCategoryIcon("bug");
        malaria.setCategoryDescription(
                "This section helps us select the safest and most appropriate malaria prevention for your trip.");
        malaria.setDisplayOrder(4);
        malaria.setIsOptional(false);
        malaria.setQuestions(MALARIA_QUESTIONS);
        categories.add(malaria);

        // ─── Category 5: Safety & Preparedness ───────────────────
        OnboardingQuestionCategory safety = new OnboardingQuestionCategory();
        safety.setCategoryKey("safety_preparedness");
        safety.setCategoryName("Safety & Preparedness");
        safety.setCategoryIcon("shield-check");
        safety.setCategoryDescription(
                "This section covers some personal topics. Your answers, like everything else you've shared, are completely confidential and help us give you the most accurate advice possible.");
        safety.setDisplayOrder(5);
        safety.setIsOptional(true);
        safety.setQuestions(SAFETY_QUESTIONS);
        categories.add(safety);

        repository.saveAll(categories);
        logger.info("Seeded {} onboarding question categories.", categories.size());
    }

    // ═══════════════════════════════════════════════════════════════
    // JSON question definitions — each question includes a description
    // explaining why the question is asked or what it informs.
    // ═══════════════════════════════════════════════════════════════

    private static final String TRAVEL_QUESTIONS = """
            [
              {
                "key": "trip_itinerary",
                "text": "Trip Itinerary",
                "description": "Select your trip type and provide your destination details, cities, and travel dates.",
                "type": "trip_itinerary",
                "required": true
              },
              {
                "key": "travel_purpose",
                "text": "Main Purpose of Travel",
                "description": "Purpose affects risk exposure — medical volunteers face different risks than leisure travellers.",
                "type": "checkbox",
                "required": false,
                "options": [
                  {"value": "tourism", "label": "Tourism and Leisure"},
                  {"value": "business", "label": "Business"},
                  {"value": "visiting_family", "label": "Visiting Family or Friends"},
                  {"value": "study_work", "label": "Study or Work Assignment"},
                  {"value": "volunteer", "label": "Volunteer or Humanitarian Work"},
                  {"value": "pilgrimage", "label": "Religious Pilgrimage"}
                ]
              },
              {
                "key": "travel_companions",
                "text": "Who are you travelling with?",
                "description": "Group composition influences vaccination priorities — children require different schedules.",
                "type": "checkbox",
                "required": false,
                "options": [
                  {"value": "alone", "label": "Alone"},
                  {"value": "family", "label": "Family (including Children)"},
                  {"value": "friends", "label": "Friends"},
                  {"value": "colleagues", "label": "Colleagues"}
                ]
              },
              {
                "key": "children_ages",
                "text": "Ages of Children Travelling",
                "description": "Vaccine schedules and malaria prophylaxis differ significantly by age group.",
                "type": "text",
                "required": false,
                "placeholder": "e.g. 3, 7, 12",
                "conditionalOn": {"travel_companions": "family"}
              },
              {
                "key": "accommodation_type",
                "text": "Main Accommodation Type",
                "description": "Air-conditioned rooms significantly reduce mosquito exposure compared to basic accommodation.",
                "type": "checkbox",
                "required": false,
                "options": [
                  {"value": "hotel_ac", "label": "Hotel (Air-Conditioned)"},
                  {"value": "hotel_no_ac", "label": "Hotel (No Air-Conditioning)"},
                  {"value": "hostel", "label": "Hostel or Budget Accommodation"},
                  {"value": "staying_locals", "label": "Staying with Locals"},
                  {"value": "camping", "label": "Camping or Outdoors"},
                  {"value": "mix", "label": "Mix of the Above"}
                ]
              },
              {
                "key": "urban_rural",
                "text": "Will you spend time in rural or remote areas?",
                "description": "Rural and jungle travel greatly increases exposure to insect-borne diseases.",
                "type": "radio",
                "required": false,
                "options": [
                  {"value": "urban", "label": "Mostly Urban"},
                  {"value": "rural", "label": "Mostly Rural or Remote"},
                  {"value": "both", "label": "Both"}
                ]
              },
              {
                "key": "activities",
                "text": "Activities Planned",
                "description": "High-risk activities like animal contact or high-altitude travel require specific preventive measures.",
                "type": "checkbox",
                "required": false,
                "options": [
                  {"value": "hiking", "label": "Hiking or Trekking"},
                  {"value": "high_altitude", "label": "High Altitude Travel (above 2,500m)"},
                  {"value": "swimming", "label": "Swimming in Lakes, Rivers or Ocean"},
                  {"value": "scuba", "label": "Scuba Diving"},
                  {"value": "animal_contact", "label": "Animal, Farm or Wildlife Contact"},
                  {"value": "volunteering", "label": "Volunteering (Medical, Orphanages, Construction)"},
                  {"value": "mass_gatherings", "label": "Mass Gatherings or Festivals"},
                  {"value": "none", "label": "None of These"}
                ]
              },
              {
                "key": "food_type",
                "text": "Where will you mostly eat?",
                "description": "Street food and local market eating increases risk of food-borne illness and Hepatitis A.",
                "type": "radio",
                "required": false,
                "options": [
                  {"value": "restaurants", "label": "Hotels and Restaurants"},
                  {"value": "street_food", "label": "Street Food and Local Markets"},
                  {"value": "mix", "label": "Mix of Both"},
                  {"value": "self_catering", "label": "Self-Catering"}
                ]
              }
            ]
            """;

    private static final String MEDICAL_QUESTIONS = """
            [
              {
                "key": "date_of_birth",
                "text": "What is your date of birth?",
                "description": "Age is critical for vaccine schedules and malaria prophylaxis recommendations — children and over-65s require different protocols.",
                "type": "date",
                "required": true,
                "placeholder": "DD/MM/YYYY"
              },
              {
                "key": "medical_conditions",
                "text": "Do you have any ongoing medical conditions?",
                "description": "Certain conditions contraindicate specific vaccines or medications, such as live vaccines with immunosuppression.",
                "type": "checkbox",
                "required": false,
                "options": [
                  {"value": "none", "label": "None"},
                  {"value": "heart", "label": "Heart Disease or High Blood Pressure"},
                  {"value": "diabetes", "label": "Diabetes"},
                  {"value": "lung", "label": "Asthma or Lung Disease"},
                  {"value": "immune", "label": "Weakened Immune System"},
                  {"value": "epilepsy", "label": "Epilepsy or Seizures"},
                  {"value": "mental_health", "label": "Mental Health Condition"},
                  {"value": "kidney_liver", "label": "Kidney or Liver Disease"},
                  {"value": "blood", "label": "Blood Disorder (G6PD, Sickle Cell)"},
                  {"value": "other", "label": "Other"}
                ]
              },
              {
                "key": "conditions_description",
                "text": "Please describe your condition(s) and current treatment",
                "description": "Detailed treatment history helps ensure all recommendations are safe for your specific situation.",
                "type": "textarea",
                "required": false,
                "placeholder": "e.g. Type 2 diabetes, well controlled on Metformin 500mg twice daily",
                "conditionalOn": {"medical_conditions": "!none"}
              },
              {
                "key": "conditions_controlled",
                "text": "Are your conditions currently well-controlled?",
                "description": "Poorly controlled conditions may warrant additional precautions or GP review before travel.",
                "type": "radio",
                "required": false,
                "options": [
                  {"value": "yes", "label": "Yes"},
                  {"value": "no", "label": "No"}
                ],
                "conditionalOn": {"medical_conditions": "!none"}
              },
              {
                "key": "taking_medications",
                "text": "Are you currently taking any prescription medications?",
                "description": "Drug interactions between travel medications and existing prescriptions must be checked.",
                "type": "radio",
                "required": false,
                "options": [
                  {"value": "no", "label": "No"},
                  {"value": "yes", "label": "Yes"}
                ]
              },
              {
                "key": "medications_list",
                "text": "List all medications (name, dose, frequency)",
                "description": "Enables safe prescribing by identifying potential drug-drug interactions.",
                "type": "textarea",
                "required": false,
                "placeholder": "e.g. Ramipril 5mg once daily; Metformin 500mg twice daily",
                "conditionalOn": {"taking_medications": "yes"}
              },
              {
                "key": "medication_supply",
                "text": "Will you have enough medication for your entire trip plus one extra week?",
                "description": "Running out of essential medication abroad can be dangerous and difficult to resolve.",
                "type": "radio",
                "required": false,
                "options": [
                  {"value": "yes", "label": "Yes"},
                  {"value": "no", "label": "No, I will need guidance"}
                ],
                "conditionalOn": {"taking_medications": "yes"}
              },
              {
                "key": "severe_allergies",
                "text": "Do you have any severe allergies?",
                "description": "Allergy history is critical — some vaccines contain egg, gelatine, or antibiotic components.",
                "type": "radio",
                "required": false,
                "options": [
                  {"value": "no", "label": "No"},
                  {"value": "yes", "label": "Yes"}
                ]
              },
              {
                "key": "allergy_details",
                "text": "Please specify allergen and type of reaction",
                "description": "Precise details help us flag potentially dangerous vaccine components or medications.",
                "type": "textarea",
                "required": false,
                "placeholder": "e.g. Penicillin, anaphylaxis; Bee stings, severe swelling",
                "conditionalOn": {"severe_allergies": "yes"}
              },
              {
                "key": "carries_epipen",
                "text": "Do you carry an EpiPen?",
                "description": "Knowing whether emergency medication is carried allows us to give targeted preparedness advice.",
                "type": "radio",
                "required": false,
                "options": [
                  {"value": "yes", "label": "Yes"},
                  {"value": "no", "label": "No"}
                ],
                "conditionalOn": {"severe_allergies": "yes"}
              },
              {
                "key": "pregnant",
                "text": "Are you pregnant, or could you be pregnant during this trip?",
                "description": "Pregnancy contraindicates several vaccines and malaria medications — your safety is the priority.",
                "type": "radio",
                "required": false,
                "options": [
                  {"value": "no", "label": "No"},
                  {"value": "yes", "label": "Yes"},
                  {"value": "na", "label": "Not Applicable"}
                ]
              },
              {
                "key": "trimester",
                "text": "Current trimester",
                "description": "Risk-benefit decisions for vaccines and antimalarials vary significantly across trimesters.",
                "type": "radio",
                "required": false,
                "options": [
                  {"value": "1st", "label": "1st (0 to 12 weeks)"},
                  {"value": "2nd", "label": "2nd (13 to 26 weeks)"},
                  {"value": "3rd", "label": "3rd (27 weeks and above)"}
                ],
                "conditionalOn": {"pregnant": "yes"}
              },
              {
                "key": "recent_hospitalisation",
                "text": "Any hospitalisations or surgeries in the past 12 months?",
                "description": "Recent surgery or hospitalisation may affect fitness to fly or vaccination suitability.",
                "type": "radio",
                "required": false,
                "options": [
                  {"value": "no", "label": "No"},
                  {"value": "yes", "label": "Yes"}
                ]
              },
              {
                "key": "hospitalisation_details",
                "text": "Brief explanation",
                "description": "Helps us understand any residual health considerations from recent procedures.",
                "type": "textarea",
                "required": false,
                "placeholder": "e.g. Appendix surgery March 2024, fully recovered",
                "conditionalOn": {"recent_hospitalisation": "yes"}
              }
            ]
            """;

    private static final String VACCINE_QUESTIONS = """
            [
              {
                "key": "vaccination_records",
                "text": "Do you have access to your vaccination records?",
                "description": "Accurate records allow us to identify true gaps vs. vaccines you may have forgotten about.",
                "type": "radio",
                "required": false,
                "options": [
                  {"value": "yes", "label": "Yes, will upload or attach"},
                  {"value": "partial", "label": "Partial records only"},
                  {"value": "no", "label": "No records available"}
                ]
              },
              {
                "key": "vaccine_status",
                "text": "Please indicate the status of each vaccine",
                "description": "Your current vaccination status determines which vaccines are needed before departure.",
                "type": "vaccine_table",
                "required": false,
                "vaccines": [
                  {"id": "tetanus", "name": "Tetanus and Diphtheria", "description": "Td or Tdap booster, every 10 years"},
                  {"id": "mmr", "name": "MMR", "description": "Measles, Mumps, Rubella"},
                  {"id": "hepa", "name": "Hepatitis A", "description": "2 dose series"},
                  {"id": "hepb", "name": "Hepatitis B", "description": "3 dose series"},
                  {"id": "typhoid", "name": "Typhoid", "description": "Injectable or oral"},
                  {"id": "yellow", "name": "Yellow Fever", "description": "Required for some countries, certificate needed"},
                  {"id": "rabies", "name": "Rabies Pre-Exposure", "description": "3 dose series"},
                  {"id": "mening", "name": "Meningococcal", "description": "ACWY or B"},
                  {"id": "covid", "name": "COVID-19", "description": "Primary series plus boosters"},
                  {"id": "flu", "name": "Seasonal Influenza", "description": "Annual"}
                ]
              },
              {
                "key": "vaccine_reaction",
                "text": "Have you ever had a serious reaction to a vaccine?",
                "description": "Previous severe reactions change how — or whether — certain vaccines can be safely given.",
                "type": "radio",
                "required": false,
                "options": [
                  {"value": "no", "label": "No"},
                  {"value": "yes", "label": "Yes"}
                ]
              },
              {
                "key": "vaccine_reaction_details",
                "text": "Which vaccine and what happened?",
                "description": "Exact details allow us to identify safe alternatives and alert healthcare providers.",
                "type": "textarea",
                "required": false,
                "placeholder": "e.g. Yellow Fever vaccine, severe allergic reaction requiring hospitalisation",
                "conditionalOn": {"vaccine_reaction": "yes"}
              },
              {
                "key": "previous_travel",
                "text": "Have you travelled internationally in the past two years?",
                "description": "Recent travel history helps identify prior disease exposures and existing immunity.",
                "type": "radio",
                "required": false,
                "options": [
                  {"value": "no", "label": "No"},
                  {"value": "yes", "label": "Yes"}
                ]
              },
              {
                "key": "previous_travel_destinations",
                "text": "Where did you travel to?",
                "description": "Prior destinations may have conferred natural immunity to certain diseases.",
                "type": "multi_country",
                "required": false,
                "placeholder": "e.g. Nigeria",
                "conditionalOn": {"previous_travel": "yes"}
              },
              {
                "key": "previous_travel_illness",
                "text": "Did you experience any health problems during or after that travel?",
                "description": "Post-travel illness history informs your personal risk profile for future trips.",
                "type": "radio",
                "required": false,
                "options": [
                  {"value": "no", "label": "No"},
                  {"value": "yes", "label": "Yes"}
                ],
                "conditionalOn": {"previous_travel": "yes"}
              },
              {
                "key": "previous_illness_details",
                "text": "Please describe",
                "description": "Specific diagnoses help identify conditions that may recur or require extra precautions.",
                "type": "textarea",
                "required": false,
                "placeholder": "e.g. Severe diarrhoea for 5 days in India; diagnosed with dengue in Thailand",
                "conditionalOn": {"previous_travel_illness": "yes"}
              }
            ]
            """;

    private static final String MALARIA_QUESTIONS = """
            [
              {
                "key": "malaria_risk_area",
                "text": "Will you be travelling to an area with malaria risk?",
                "description": "Malaria prevention is only recommended where genuine risk exists — this prevents unnecessary medication.",
                "type": "radio",
                "required": false,
                "options": [
                  {"value": "yes", "label": "Yes"},
                  {"value": "no", "label": "No"},
                  {"value": "unsure", "label": "Unsure"}
                ]
              },
              {
                "key": "malaria_rural_overnight",
                "text": "Will you be sleeping overnight in rural or jungle areas?",
                "description": "Sleeping outdoors or in unscreened accommodation dramatically increases mosquito bite exposure.",
                "type": "radio",
                "required": false,
                "options": [
                  {"value": "yes", "label": "Yes"},
                  {"value": "no", "label": "No"}
                ],
                "conditionalOn": {"malaria_risk_area": "yes|unsure"}
              },
              {
                "key": "malaria_contraindications",
                "text": "Do you have any of the following?",
                "description": "Conditions like epilepsy or G6PD deficiency rule out specific antimalarial drugs entirely.",
                "type": "checkbox",
                "required": false,
                "options": [
                  {"value": "epilepsy", "label": "Epilepsy or Seizures"},
                  {"value": "mental_health", "label": "Depression, Anxiety or Mental Health Condition"},
                  {"value": "heart_rhythm", "label": "Heart Rhythm Problems"},
                  {"value": "g6pd", "label": "G6PD Deficiency"},
                  {"value": "none", "label": "None of These"}
                ],
                "conditionalOn": {"malaria_risk_area": "yes|unsure"}
              },
              {
                "key": "previous_malaria",
                "text": "Have you ever had malaria or taken malaria prevention medication?",
                "description": "Prior malaria or prophylaxis experience guides the safest and most effective current choice.",
                "type": "radio",
                "required": false,
                "options": [
                  {"value": "no", "label": "No"},
                  {"value": "had_malaria", "label": "Yes, I had malaria"},
                  {"value": "prophylaxis", "label": "Yes, I took prevention medication"}
                ]
              },
              {
                "key": "malaria_medication_details",
                "text": "Which medication did you take and did you experience any side effects?",
                "description": "Side effect history allows us to recommend a better-tolerated alternative.",
                "type": "textarea",
                "required": false,
                "placeholder": "e.g. Malarone, no side effects; Doxycycline, nausea and sun sensitivity",
                "conditionalOn": {"previous_malaria": "prophylaxis"}
              },
              {
                "key": "tropical_disease_history",
                "text": "Have you previously had any of these illnesses?",
                "description": "Previous infections may affect your current immunity status and vaccination decisions.",
                "type": "checkbox",
                "required": false,
                "options": [
                  {"value": "none", "label": "None"},
                  {"value": "dengue", "label": "Dengue Fever"},
                  {"value": "typhoid", "label": "Typhoid"},
                  {"value": "hep_a", "label": "Hepatitis A"},
                  {"value": "hep_b", "label": "Hepatitis B"},
                  {"value": "travelers_diarrhea", "label": "Severe Traveler's Diarrhea"},
                  {"value": "tb", "label": "Tuberculosis (TB)"},
                  {"value": "other", "label": "Other"}
                ]
              }
            ]
            """;

    private static final String SAFETY_QUESTIONS = """
            [
              {
                "key": "risk_behaviours",
                "text": "During your trip, might any of the following apply?",
                "description": "Confidential — helps us provide targeted advice on STI prevention and harm reduction.",
                "type": "checkbox",
                "required": false,
                "options": [
                  {"value": "sexual_activity", "label": "Sexual Activity with New or Casual Partners"},
                  {"value": "alcohol", "label": "Higher Than Usual Alcohol Use"},
                  {"value": "procedures", "label": "Tattoos, Piercings or Medical and Dental Procedures Abroad"},
                  {"value": "none", "label": "None of These"},
                  {"value": "prefer_not_say", "label": "Prefer Not to Say"}
                ]
              },
              {
                "key": "barrier_protection",
                "text": "Do you typically use barrier protection (condoms)?",
                "description": "Understanding current practices helps us give relevant rather than generic advice.",
                "type": "radio",
                "required": false,
                "options": [
                  {"value": "always", "label": "Always"},
                  {"value": "sometimes", "label": "Sometimes"},
                  {"value": "rarely", "label": "Rarely"},
                  {"value": "prefer_not_say", "label": "Prefer Not to Say"}
                ],
                "conditionalOn": {"risk_behaviours": "sexual_activity"}
              },
              {
                "key": "sti_prevention_info",
                "text": "Would you like information on STI prevention, PrEP or PEP?",
                "description": "PrEP and PEP can be life-changing — we want to make sure you have the right information.",
                "type": "radio",
                "required": false,
                "options": [
                  {"value": "yes", "label": "Yes Please"},
                  {"value": "no", "label": "No Thank You"}
                ],
                "conditionalOn": {"risk_behaviours": "sexual_activity"}
              },
              {
                "key": "mental_health_concerns",
                "text": "Do you have any concerns about the following?",
                "description": "Travel stress and unfamiliar environments can significantly affect mental wellbeing.",
                "type": "checkbox",
                "required": false,
                "options": [
                  {"value": "anxiety", "label": "Travel Related Anxiety or Stress"},
                  {"value": "support_abroad", "label": "Accessing Mental Health Support Abroad"},
                  {"value": "personal_safety", "label": "Personal Safety at Destination"},
                  {"value": "none", "label": "None"}
                ]
              },
              {
                "key": "travel_insurance",
                "text": "Do you have travel insurance covering medical treatment and evacuation?",
                "description": "Medical evacuation costs can be enormous — we can guide you on what adequate coverage looks like.",
                "type": "radio",
                "required": false,
                "options": [
                  {"value": "yes", "label": "Yes"},
                  {"value": "no", "label": "No"},
                  {"value": "planning", "label": "Planning to Get It"}
                ]
              },
              {
                "key": "healthcare_access",
                "text": "Have you looked into healthcare access at your destination?",
                "description": "Knowing your options before you fall ill saves critical time in an emergency.",
                "type": "radio",
                "required": false,
                "options": [
                  {"value": "yes", "label": "Yes, I know where to go"},
                  {"value": "no", "label": "No, I would welcome guidance"}
                ]
              },
              {
                "key": "emergency_contacts_informed",
                "text": "Do your emergency contacts know your travel plans and medical history?",
                "description": "Informed contacts can act faster and more effectively in a medical emergency.",
                "type": "radio",
                "required": false,
                "options": [
                  {"value": "yes", "label": "Yes"},
                  {"value": "no", "label": "No"}
                ]
              },
              {
                "key": "post_travel_guidance",
                "text": "Would you like guidance on symptoms to watch for after returning home?",
                "description": "Some travel-related illnesses only present weeks after return — early recognition is vital.",
                "type": "radio",
                "required": false,
                "options": [
                  {"value": "yes", "label": "Yes Please"},
                  {"value": "no", "label": "No, I know what to watch for"}
                ]
              },
              {
                "key": "additional_information",
                "text": "Is there anything else we should know when preparing your travel health plan?",
                "description": "Any unique circumstances about your health, travel plans, or concerns not covered above.",
                "type": "textarea",
                "required": false,
                "placeholder": "Any other relevant information about your health, travel plans or concerns..."
              }
            ]
            """;
}
