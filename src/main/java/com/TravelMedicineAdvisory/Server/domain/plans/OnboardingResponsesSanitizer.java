package com.TravelMedicineAdvisory.Server.domain.plans;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.util.StringUtils;

import java.util.Set;

/**
 * Questionnaire answers are stored as flat JSON keys in {@code UserOnboarding#responsesJson}.
 * Trip destination, cities, and dates from onboarding must not influence generation — users submit a
 * fresh itinerary on each plan; only {@link #stripItineraryFields(String)} removes those keys.
 */
final class OnboardingResponsesSanitizer {

    private static final Set<String> ITINERARY_KEYS = Set.of("trip_itinerary");

    private OnboardingResponsesSanitizer() {
    }

    static String stripItineraryFields(String responsesJson, ObjectMapper objectMapper) {
        if (!StringUtils.hasText(responsesJson)) {
            return "{}";
        }
        try {
            JsonNode root = objectMapper.readTree(responsesJson);
            if (!root.isObject()) {
                return responsesJson.trim();
            }
            ObjectNode copy = (ObjectNode) root.deepCopy();
            for (String key : ITINERARY_KEYS) {
                copy.remove(key);
            }
            return objectMapper.writeValueAsString(copy);
        } catch (Exception e) {
            return responsesJson.trim();
        }
    }
}
