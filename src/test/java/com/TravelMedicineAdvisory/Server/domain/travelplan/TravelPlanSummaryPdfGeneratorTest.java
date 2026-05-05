package com.TravelMedicineAdvisory.Server.domain.travelplan;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.TravelMedicineAdvisory.Server.core.ai.AiGenerationClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

class TravelPlanSummaryPdfGeneratorTest {

    @Test
    void buildHtml_containsFiveSectionHeadingsVaccineColumnsAndClosingLine() {
        ObjectMapper om = new ObjectMapper();
        TravelPlanSummaryPdfGenerator gen = new TravelPlanSummaryPdfGenerator(om, Mockito.mock(AiGenerationClient.class));

        ObjectNode summary = om.createObjectNode();
        summary.putArray("section1CriticalBeforeDeparture").add("Book yellow fever vaccination at least ten days before travel");
        summary.put("section2TripSnapshot", "Home country to Testland, 14 days, leisure, MEDIUM risk");
        ArrayNode vaccines = summary.putArray("section3Vaccines");
        vaccines.addObject()
                .put("vaccine", "Yellow fever")
                .put("status", "Required")
                .put("action", "Arrange before departure");
        summary.putArray("section4PackAndRoutine").add("Insect repellent 50 percent DEET");
        ObjectNode s5 = summary.putObject("section5");
        s5.put("redFlagsLine", "High fever with confusion needs urgent care");
        ArrayNode fac = s5.putArray("facilities");
        fac.addObject().put("name", "Travel Clinic A").put("location", "Downtown");
        fac.addObject().put("name", "Hospital B").put("location", "Ring Road");
        s5.put("localEmergencyNumber", "112");
        s5.put("insuranceEmergencyLine", "Call insurer emergency line on card reverse");
        summary.put("closingLine", TravelPlanSummaryPdfGenerator.CLOSING_LINE_EXACT);

        TravelPlan plan = new TravelPlan();
        plan.setDestination("Testland");
        plan.setCountry("Home country");
        plan.setDuration(14);
        plan.setPurpose("leisure");

        String html = gen.buildHtml(plan, summary);

        assertTrue(html.contains(TravelPlanSummaryPdfGenerator.SECTION_1_HEADING));
        assertTrue(html.contains(TravelPlanSummaryPdfGenerator.SECTION_2_HEADING));
        assertTrue(html.contains(TravelPlanSummaryPdfGenerator.SECTION_3_HEADING));
        assertTrue(html.contains(TravelPlanSummaryPdfGenerator.SECTION_4_HEADING));
        assertTrue(html.contains(TravelPlanSummaryPdfGenerator.SECTION_5_HEADING));
        assertTrue(html.contains(">Vaccine</th>"));
        assertTrue(html.contains(">Status</th>"));
        assertTrue(html.contains(">Action</th>"));
        assertTrue(html.contains(TravelPlanSummaryPdfGenerator.CLOSING_LINE_EXACT));
        assertFalse(html.contains("artificial intelligence"));
        assertFalse(html.contains("This travel health summary was generated"));
    }

    @Test
    void buildHtml_escapesAngleBracketsInDynamicContent() {
        ObjectMapper om = new ObjectMapper();
        TravelPlanSummaryPdfGenerator gen = new TravelPlanSummaryPdfGenerator(om, Mockito.mock(AiGenerationClient.class));

        ObjectNode summary = om.createObjectNode();
        summary.putArray("section1CriticalBeforeDeparture").add("Ignore <script>alert(1)</script>");
        summary.put("section2TripSnapshot", "Line");
        ArrayNode vaccines = summary.putArray("section3Vaccines");
        vaccines.addObject().put("vaccine", "<evil>").put("status", "Unknown").put("action", "Confirm");
        summary.putArray("section4PackAndRoutine");
        ObjectNode s5 = summary.putObject("section5");
        s5.put("redFlagsLine", "");
        s5.putArray("facilities");
        s5.put("localEmergencyNumber", "");
        s5.put("insuranceEmergencyLine", "");
        summary.put("closingLine", TravelPlanSummaryPdfGenerator.CLOSING_LINE_EXACT);

        TravelPlan plan = new TravelPlan();
        plan.setDestination("X");

        String html = gen.buildHtml(plan, summary);

        assertTrue(html.contains("&lt;script&gt;"));
        assertTrue(html.contains("&lt;evil&gt;"));
        assertFalse(html.contains("<script>"));
    }
}
