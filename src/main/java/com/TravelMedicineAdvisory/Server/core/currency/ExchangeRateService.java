package com.TravelMedicineAdvisory.Server.core.currency;

import com.TravelMedicineAdvisory.Server.domain.systemsetting.SystemSetting;
import com.TravelMedicineAdvisory.Server.domain.systemsetting.SystemSettingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ExchangeRateService {

    private static final Logger logger = LoggerFactory.getLogger(ExchangeRateService.class);
    private static final String BASE_CURRENCY = "USD";
    private static final String RATE_API_URL = "https://open.er-api.com/v6/latest/" + BASE_CURRENCY;

    private final SystemSettingRepository systemSettingRepository;
    private final Map<String, BigDecimal> rates = new ConcurrentHashMap<>();
    private LocalDateTime lastFetched;

    public ExchangeRateService(SystemSettingRepository systemSettingRepository) {
        this.systemSettingRepository = systemSettingRepository;
        fetchRates();
    }

    @Scheduled(fixedRate = 3600000) // Refresh every hour
    public void scheduledFetch() {
        fetchRates();
    }

    public void fetchRates() {
        try {
            RestClient restClient = RestClient.create();
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restClient.get()
                    .uri(RATE_API_URL)
                    .retrieve()
                    .body(Map.class);

            if (response != null && "success".equals(response.get("result"))) {
                @SuppressWarnings("unchecked")
                Map<String, Number> rateMap = (Map<String, Number>) response.get("rates");
                if (rateMap != null) {
                    rates.clear();
                    rateMap.forEach((currency, rate) ->
                            rates.put(currency.toUpperCase(), BigDecimal.valueOf(rate.doubleValue())));
                    lastFetched = LocalDateTime.now();

                    // Override with admin-configured rates from DB
                    applyAdminRates();

                    logger.info("Exchange rates fetched successfully. {} currencies loaded.", rates.size());
                }
            }
        } catch (Exception e) {
            logger.error("Failed to fetch exchange rates: {}", e.getMessage());
            if (rates.isEmpty()) {
                rates.put("USD", BigDecimal.ONE);
                rates.put("EUR", BigDecimal.valueOf(0.92));
                rates.put("GBP", BigDecimal.valueOf(0.79));
                rates.put("NGN", BigDecimal.valueOf(1550.0));
                rates.put("INR", BigDecimal.valueOf(83.5));
                rates.put("CAD", BigDecimal.valueOf(1.36));
                rates.put("AUD", BigDecimal.valueOf(1.53));
                rates.put("KES", BigDecimal.valueOf(153.0));
                rates.put("ZAR", BigDecimal.valueOf(18.2));
                rates.put("GHS", BigDecimal.valueOf(14.5));
                applyAdminRates();
                logger.info("Using fallback exchange rates.");
            }
        }
    }

    private void applyAdminRates() {
        try {
            List<SystemSetting> allSettings = systemSettingRepository.findAll();
            for (SystemSetting setting : allSettings) {
                if (setting.getKey() != null && setting.getKey().startsWith("exchangeRate")) {
                    String currencyCode = setting.getKey().substring("exchangeRate".length()).toUpperCase();
                    if (!currencyCode.isEmpty() && setting.getValue() != null) {
                        try {
                            BigDecimal rate = new BigDecimal(setting.getValue());
                            rates.put(currencyCode, rate);
                            logger.debug("Admin override: {} = {}", currencyCode, rate);
                        } catch (NumberFormatException ignored) {}
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("Could not apply admin-configured rates: {}", e.getMessage());
        }
    }

    public void refreshAdminRates() {
        applyAdminRates();
    }

    public Map<String, BigDecimal> getRates() {
        return Map.copyOf(rates);
    }

    public BigDecimal convert(BigDecimal amount, String fromCurrency, String toCurrency) {
        if (amount == null) return BigDecimal.ZERO;
        if (fromCurrency == null || toCurrency == null) return amount;

        String from = fromCurrency.toUpperCase();
        String to = toCurrency.toUpperCase();

        if (from.equals(to)) return amount;

        BigDecimal fromRate = rates.getOrDefault(from, BigDecimal.ONE);
        BigDecimal toRate = rates.getOrDefault(to, BigDecimal.ONE);

        BigDecimal inUsd = amount.divide(fromRate, 6, RoundingMode.HALF_UP);
        return inUsd.multiply(toRate).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal convertFromUsd(BigDecimal usdAmount, String toCurrency) {
        if (usdAmount == null) return BigDecimal.ZERO;
        if (toCurrency == null || "USD".equalsIgnoreCase(toCurrency)) return usdAmount;

        BigDecimal toRate = rates.getOrDefault(toCurrency.toUpperCase(), BigDecimal.ONE);
        return usdAmount.multiply(toRate).setScale(2, RoundingMode.HALF_UP);
    }

    public String getCurrencySymbol(String currencyCode) {
        if (currencyCode == null) return "$";
        return switch (currencyCode.toUpperCase()) {
            case "USD" -> "$";
            case "EUR" -> "€";
            case "GBP" -> "£";
            case "NGN" -> "₦";
            case "INR" -> "₹";
            case "CAD" -> "C$";
            case "AUD" -> "A$";
            case "KES" -> "KSh";
            case "ZAR" -> "R";
            case "GHS" -> "GH₵";
            case "JPY" -> "¥";
            case "CNY" -> "¥";
            case "BRL" -> "R$";
            case "MXN" -> "MX$";
            case "CHF" -> "CHF";
            case "SGD" -> "S$";
            case "AED" -> "د.إ";
            default -> currencyCode;
        };
    }

    public LocalDateTime getLastFetched() {
        return lastFetched;
    }
}
