package com.TravelMedicineAdvisory.Server.core.currency;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;

/**
 * Subset of the JSON returned by {@code https://open.er-api.com/v6/latest/USD}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record OpenErApiLatestResponse(
        String result,
        Map<String, Double> rates
) {}
