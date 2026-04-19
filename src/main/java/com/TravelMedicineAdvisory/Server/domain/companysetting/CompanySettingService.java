package com.TravelMedicineAdvisory.Server.domain.companysetting;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.TravelMedicineAdvisory.Server.core.cache.CacheNames;
import com.TravelMedicineAdvisory.Server.core.notifications.AdminNotificationService;
import com.TravelMedicineAdvisory.Server.core.queue.JobType;
import com.TravelMedicineAdvisory.Server.core.queue.QueueService;
import com.TravelMedicineAdvisory.Server.domain.company.BillingCurrency;
import com.TravelMedicineAdvisory.Server.domain.company.Company;
import com.TravelMedicineAdvisory.Server.domain.company.CompanyRepository;
import com.TravelMedicineAdvisory.Server.domain.user.UserRepository;

@Service
@Transactional
public class CompanySettingService {

    private final CompanySettingRepository repository;
    private final CompanyRepository companyRepository;
    private final AdminNotificationService adminNotificationService;

    public CompanySettingService(CompanySettingRepository repository, CompanyRepository companyRepository,
            UserRepository userRepository, QueueService queueService,
            AdminNotificationService adminNotificationService) {
        this.repository = repository;
        this.companyRepository = companyRepository;
        this.adminNotificationService = adminNotificationService;
    }

    @Cacheable(cacheNames = CacheNames.COMPANY_SETTINGS, key = "#companyId")
    @Transactional(readOnly = true)
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

    @CacheEvict(cacheNames = CacheNames.COMPANY_SETTINGS, key = "#companyId")
    public CompanySettingResponse upsert(Long companyId, CompanySettingRequest request) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new NoSuchElementException("Company not found"));

        for (var entry : request.settings().entrySet()) {
            String key = entry.getKey();
            CompanySettingRequest.SettingValue val = entry.getValue();

            CompanySetting existingSetting = repository.findByCompanyIdAndKeyAndDeletedAtIsNull(companyId, key)
                    .orElse(null);
            String oldValue = existingSetting != null ? existingSetting.getValue() : null;

            CompanySetting setting = new CompanySetting();
            setting.setCompany(company);
            setting.setKey(key);
            setting.setValue(val.value());
            setting.setType(val.type() != null
                    ? CompanySetting.SettingType.valueOf(val.type().toUpperCase())
                    : CompanySetting.SettingType.STRING);

            repository.save(setting);

            if ("two_factor_enabled".equals(key) && oldValue != null && !oldValue.equals(val.value())) {
                sendTwoFactorEmail(company, "true".equalsIgnoreCase(val.value()));
            }
        }

        return getByCompany(companyId);
    }

    private void sendTwoFactorEmail(Company company, boolean enabled) {
        JobType jobType = enabled ? JobType.EMAIL_TWO_FACTOR_ENABLED : JobType.EMAIL_TWO_FACTOR_DISABLED;
        String subject = enabled ? "Two-factor authentication enabled" : "Two-factor authentication disabled";
        
        adminNotificationService.notifyCompanyAdmins(
                company.getId(),
                subject,
                jobType,
                Map.of());
    }

    @CacheEvict(cacheNames = CacheNames.COMPANY_SETTINGS, key = "#companyId")
    public void updateBillingCurrency(Long companyId, BillingCurrency currency) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new NoSuchElementException("Company not found"));

        String oldCurrency = company.getBillingCurrency() != null ? company.getBillingCurrency().name() : "USD";

        company.setBillingCurrency(currency);
        companyRepository.save(company);

        CompanySetting setting = repository.findByCompanyIdAndKeyAndDeletedAtIsNull(companyId, "pref_currency")
                .orElse(new CompanySetting());
        setting.setCompany(company);
        setting.setKey("pref_currency");
        setting.setValue(currency.name());
        setting.setType(CompanySetting.SettingType.STRING);
        repository.save(setting);

        sendBillingCurrencyEmail(company, oldCurrency, currency.name());
    }

    private void sendBillingCurrencyEmail(Company company, String oldCurrency, String newCurrency) {
        adminNotificationService.notifyCompanyAdmins(
                company.getId(),
                "Billing currency updated for " + company.getName(),
                JobType.EMAIL_BILLING_CURRENCY_CHANGED,
                Map.of(
                        "oldCurrency", oldCurrency,
                        "newCurrency", newCurrency));
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
