package com.TravelMedicineAdvisory.Server.domain.companysetting;

import java.util.Map;

public record CompanySettingResponse(
    Long companyId,
    Map<String, SettingValue> settings
) {
    public record SettingValue(Object value, String type) {}
}
