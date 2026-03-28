package com.TravelMedicineAdvisory.Server.domain.companysetting;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.TravelMedicineAdvisory.Server.domain.company.BillingCurrency;
import com.TravelMedicineAdvisory.Server.domain.company.Company;
import com.TravelMedicineAdvisory.Server.domain.company.CompanyRepository;

@Service
@Transactional
public class CompanySettingService {

    private final CompanySettingRepository repository;
    private final CompanyRepository companyRepository;

    public CompanySettingService(CompanySettingRepository repository, CompanyRepository companyRepository) {
        this.repository = repository;
        this.companyRepository = companyRepository;
    }

    public CompanySettingResponse getByCompany(Long companyId) {
        if (!companyRepository.existsById(companyId)) {
            throw new NoSuchElementException("Company not found");
        }
        List<CompanySetting> settings = repository.findByCompanyId(companyId);
        Map<String, CompanySettingResponse.SettingValue> map = new HashMap<>();
        for (CompanySetting s : settings) {
            map.put(s.getKey(), new CompanySettingResponse.SettingValue(parseValue(s), s.getType().name()));
        }
        return new CompanySettingResponse(companyId, map);
    }

    public CompanySettingResponse upsert(Long companyId, CompanySettingRequest request) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new NoSuchElementException("Company not found"));

        for (var entry : request.settings().entrySet()) {
            String key = entry.getKey();
            CompanySettingRequest.SettingValue val = entry.getValue();

            CompanySetting setting = repository.findByCompanyIdAndKeyAndDeletedAtIsNull(companyId, key)
                    .orElse(new CompanySetting());

            setting.setCompany(company);
            setting.setKey(key);
            setting.setValue(val.value());
            setting.setType(val.type() != null
                    ? CompanySetting.SettingType.valueOf(val.type().toUpperCase())
                    : CompanySetting.SettingType.STRING);

            repository.save(setting);
        }

        return getByCompany(companyId);
    }

    public void updateBillingCurrency(Long companyId, BillingCurrency currency) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new NoSuchElementException("Company not found"));

        company.setBillingCurrency(currency);
        companyRepository.save(company);

        CompanySetting setting = repository.findByCompanyIdAndKeyAndDeletedAtIsNull(companyId, "pref_currency")
                .orElse(new CompanySetting());
        setting.setCompany(company);
        setting.setKey("pref_currency");
        setting.setValue(currency.name());
        setting.setType(CompanySetting.SettingType.STRING);
        repository.save(setting);
    }

    private Object parseValue(CompanySetting setting) {
        if (setting.getValue() == null) return null;
        return switch (setting.getType()) {
            case BOOLEAN -> "true".equalsIgnoreCase(setting.getValue());
            case NUMBER -> {
                try { yield Integer.parseInt(setting.getValue()); }
                catch (NumberFormatException e) { yield setting.getValue(); }
            }
            default -> setting.getValue();
        };
    }
}
