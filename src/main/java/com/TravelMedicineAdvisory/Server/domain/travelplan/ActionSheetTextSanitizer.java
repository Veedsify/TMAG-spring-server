package com.TravelMedicineAdvisory.Server.domain.travelplan;

import com.TravelMedicineAdvisory.Server.domain.plans.TravelPlanOutputSchemas.ActionSheetFacility;
import com.TravelMedicineAdvisory.Server.domain.plans.TravelPlanOutputSchemas.ActionSheetOutput;
import com.TravelMedicineAdvisory.Server.domain.plans.TravelPlanOutputSchemas.ActionSheetSection5;
import com.TravelMedicineAdvisory.Server.domain.plans.TravelPlanOutputSchemas.ActionSheetVaccine;
import java.util.ArrayList;
import java.util.List;
import org.springframework.util.StringUtils;

/**
 * Sanitises an {@link ActionSheetOutput} record: removes hyphens/em-dashes/underscores per the
 * prompt's legacy hyphen rule, filters internal keys like {@code TREE_<digits>_<UPPERCASE>}, and
 * clamps array sizes to match the prompt's hard caps.
 */
public final class ActionSheetTextSanitizer {

    private static final int MAX_SECTION_1 = 4;
    private static final int MAX_SECTION_4 = 8;
    private static final int MAX_FACILITIES = 2;
    private static final int MAX_VACCINES = 18;
    private static final int MAX_TEXT_CHARS = 260;

    public static final String CLOSING_LINE_EXACT =
            "Full clinical dossier attached. Share with your travel medicine clinician at your pre-travel appointment.";

    private ActionSheetTextSanitizer() {}

    /**
     * Return a clamped and sanitised copy of the input.
     */
    public static ActionSheetOutput scrub(ActionSheetOutput input) {
        if (input == null) return null;

        List<String> s1 = clampStrings(input.section1CriticalBeforeDeparture(), MAX_SECTION_1);
        List<String> s4 = clampStrings(input.section4PackAndRoutine(), MAX_SECTION_4);

        String snap = clean(input.section2TripSnapshot());

        List<ActionSheetVaccine> vaccines = input.section3Vaccines() == null
                ? List.of()
                : input.section3Vaccines().stream()
                        .limit(MAX_VACCINES)
                        .map(v -> new ActionSheetVaccine(clean(v.vaccine()), clean(v.status()), clean(v.action())))
                        .toList();

        ActionSheetSection5 s5 = input.section5() == null
                ? new ActionSheetSection5("", List.of(), "", "")
                : new ActionSheetSection5(
                        clean(input.section5().redFlagsLine()),
                        clampFacilities(input.section5().facilities()),
                        clean(input.section5().localEmergencyNumber()),
                        clean(input.section5().insuranceEmergencyLine()));

        String closing = input.closingLine();
        if (!CLOSING_LINE_EXACT.equals(closing)) {
            closing = CLOSING_LINE_EXACT;
        }

        return new ActionSheetOutput(s1, snap, vaccines, s4, s5, closing);
    }

    private static List<String> clampStrings(List<String> items, int max) {
        if (items == null) return List.of();
        List<String> out = new ArrayList<>();
        for (String item : items) {
            if (out.size() >= max) break;
            String c = clean(item);
            if (StringUtils.hasText(c) && !isInternalKey(c)) {
                out.add(c);
            }
        }
        return out;
    }

    private static List<ActionSheetFacility> clampFacilities(List<ActionSheetFacility> items) {
        if (items == null) return List.of();
        List<ActionSheetFacility> out = new ArrayList<>();
        for (ActionSheetFacility f : items) {
            if (out.size() >= MAX_FACILITIES) break;
            if (f != null) {
                out.add(new ActionSheetFacility(clean(f.name()), clean(f.location())));
            }
        }
        return out;
    }

    private static String clean(String value) {
        if (!StringUtils.hasText(value)) return "";
        String cleaned = value.trim()
                .replaceAll("\\s+", " ")
                .replace('-', ' ')
                .replace('_', ' ')
                .replace('\u2013', ' ')
                .replace('\u2014', ' ')
                .trim()
                .replaceAll("\\s+", " ");
        return cleaned.length() <= MAX_TEXT_CHARS
                ? cleaned
                : cleaned.substring(0, MAX_TEXT_CHARS).trim();
    }

    private static boolean isInternalKey(String value) {
        return value != null && value.matches("TREE_\\d+_[A-Z_]+");
    }
}
