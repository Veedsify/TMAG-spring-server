package com.TravelMedicineAdvisory.Server.core.currency;

import com.TravelMedicineAdvisory.Server.core.types.SuccessResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/exchange-rates")
public class ExchangeRateController {

    private final ExchangeRateService exchangeRateService;

    public ExchangeRateController(ExchangeRateService exchangeRateService) {
        this.exchangeRateService = exchangeRateService;
    }

    @GetMapping
    public ResponseEntity<SuccessResponse> getRates() {
        var snap = exchangeRateService.getExchangeRatesSnapshot();
        return ResponseEntity.ok(new SuccessResponse("Exchange rates retrieved", Map.of(
                "base", "USD",
                "rates", snap.rates(),
                "lastFetched", snap.lastFetched()
        )));
    }

    @GetMapping("/convert")
    public ResponseEntity<SuccessResponse> convert(
            @RequestParam BigDecimal amount,
            @RequestParam String from,
            @RequestParam String to) {
        BigDecimal converted = exchangeRateService.convert(amount, from, to);
        return ResponseEntity.ok(new SuccessResponse("Converted", Map.of(
                "originalAmount", amount,
                "from", from.toUpperCase(),
                "to", to.toUpperCase(),
                "convertedAmount", converted,
                "symbol", exchangeRateService.getCurrencySymbol(to)
        )));
    }

    @GetMapping("/currencies")
    public ResponseEntity<SuccessResponse> getSupportedCurrencies() {
        List<Map<String, String>> currencies = exchangeRateService.getSupportedCurrencies();
        return ResponseEntity.ok(new SuccessResponse("Supported currencies", currencies));
    }
}
