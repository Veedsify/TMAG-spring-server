package com.TravelMedicineAdvisory.Server.core.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.util.StringUtils;

public final class QuestionnaireResponseSanitizer {

    private QuestionnaireResponseSanitizer() {
    }

    public static String sanitize(String responsesJson, ObjectMapper objectMapper) {
        if (!StringUtils.hasText(responsesJson)) {
            return "{}";
        }

        try {
            JsonNode root = objectMapper.readTree(responsesJson);
            if (!root.isObject()) {
                return responsesJson.trim();
            }

            ObjectNode copy = (ObjectNode) root.deepCopy();
            JsonNode itineraryNode = copy.get("trip_itinerary");
            if (itineraryNode != null && itineraryNode.isObject()) {
                removeLegacyTravelDetailFields(copy);
                removeItineraryAccommodationFields((ObjectNode) itineraryNode);
            }
            return objectMapper.writeValueAsString(copy);
        } catch (Exception ignored) {
            return responsesJson.trim();
        }
    }

    private static void removeLegacyTravelDetailFields(ObjectNode root) {
        root.remove("travel_countries");
        root.remove("travel_city_region");
        root.remove("departure_date");
        root.remove("return_date_or_duration");
    }

    private static void removeItineraryAccommodationFields(ObjectNode itinerary) {
        itinerary.remove("oneAccommodationType");
        itinerary.remove("returnAccommodationType");

        JsonNode multiLegsNode = itinerary.get("multiLegs");
        if (!(multiLegsNode instanceof ArrayNode multiLegs)) {
            return;
        }

        for (JsonNode legNode : multiLegs) {
            if (legNode instanceof ObjectNode legObject) {
                legObject.remove("accommodationType");
            }
        }
    }
}
