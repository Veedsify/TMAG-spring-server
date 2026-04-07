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
        return ResponseEntity.ok(new SuccessResponse("Exchange rates retrieved", Map.of(
                "base", "USD",
                "rates", exchangeRateService.getRates(),
                "lastFetched", exchangeRateService.getLastFetched()
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
        List<Map<String, String>> currencies = List.of(
                Map.of("code", "USD", "name", "US Dollar", "symbol", "$"),
                Map.of("code", "EUR", "name", "Euro", "symbol", "€"),
                Map.of("code", "GBP", "name", "British Pound", "symbol", "£"),
                Map.of("code", "NGN", "name", "Nigerian Naira", "symbol", "₦"),
                Map.of("code", "INR", "name", "Indian Rupee", "symbol", "₹"),
                Map.of("code", "CAD", "name", "Canadian Dollar", "symbol", "C$"),
                Map.of("code", "AUD", "name", "Australian Dollar", "symbol", "A$"),
                Map.of("code", "KES", "name", "Kenyan Shilling", "symbol", "KSh"),
                Map.of("code", "ZAR", "name", "South African Rand", "symbol", "R"),
                Map.of("code", "GHS", "name", "Ghanaian Cedi", "symbol", "GH₵"),
                Map.of("code", "JPY", "name", "Japanese Yen", "symbol", "¥"),
                Map.of("code", "CNY", "name", "Chinese Yuan", "symbol", "¥"),
                Map.of("code", "BRL", "name", "Brazilian Real", "symbol", "R$"),
                Map.of("code", "AED", "name", "UAE Dirham", "symbol", "د.إ"),
                Map.of("code", "SGD", "name", "Singapore Dollar", "symbol", "S$"),
                Map.of("code", "CHF", "name", "Swiss Franc", "symbol", "CHF")
        );
        return ResponseEntity.ok(new SuccessResponse("Supported currencies", currencies));
    }
}
