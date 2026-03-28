package com.TravelMedicineAdvisory.Server.domain.companysetting;

import java.util.Map;

public record CompanySettingRequest(
    Long companyId,
    Map<String, SettingValue> settings
) {
    public record SettingValue(String value, String type) {}
}
