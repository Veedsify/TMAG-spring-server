package com.TravelMedicineAdvisory.Server.core.payment;

import com.TravelMedicineAdvisory.Server.core.cache.CacheNames;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class PaymentConfigService {

    private final FlutterwaveService flutterwaveService;

    public PaymentConfigService(FlutterwaveService flutterwaveService) {
        this.flutterwaveService = flutterwaveService;
    }

    @Cacheable(cacheNames = CacheNames.PAYMENTS_CONFIG)
    public Map<String, Object> getPaymentConfigData() {
        return Map.of(
                "provider", "flutterwave",
                "publicKey", flutterwaveService.getPublicKey() != null ? "set" : "not_set",
                "configured", flutterwaveService.isConfigured());
    }
}
