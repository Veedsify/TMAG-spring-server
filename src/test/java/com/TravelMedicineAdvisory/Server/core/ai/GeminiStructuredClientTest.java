package com.TravelMedicineAdvisory.Server.core.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.TravelMedicineAdvisory.Server.domain.plans.TravelPlanOutputSchemas;
import org.junit.jupiter.api.Test;

class GeminiStructuredClientTest {

    @Test
    void planCallsUseLargeEnoughOutputBudgetForStructuredJson() {
        assertEquals(24_576,
                GeminiStructuredClient.effectiveMaxOutputTokens(TravelPlanOutputSchemas.SINGLE_TRAVELLER, 4_096));
        assertEquals(32_000,
                GeminiStructuredClient.effectiveMaxOutputTokens(TravelPlanOutputSchemas.SINGLE_TRAVELLER, 32_000));
    }

    @Test
    void summaryCallsUseSeparateSmallerMinimumBudget() {
        assertEquals(4_096,
                GeminiStructuredClient.effectiveMaxOutputTokens(TravelPlanOutputSchemas.ACTION_SHEET, 2_048));
        assertEquals(8_192,
                GeminiStructuredClient.effectiveMaxOutputTokens(TravelPlanOutputSchemas.ACTION_SHEET, 8_192));
    }
}
