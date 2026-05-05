package com.TravelMedicineAdvisory.Server.domain.admin.analytics;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.TravelMedicineAdvisory.Server.domain.abuseflag.AbuseFlagRepository;
import com.TravelMedicineAdvisory.Server.domain.airequestlog.AiRequestLog;
import com.TravelMedicineAdvisory.Server.domain.airequestlog.AiRequestLogRepository;
import com.TravelMedicineAdvisory.Server.domain.company.CompanyRepository;
import com.TravelMedicineAdvisory.Server.domain.credit.CreditRepository;
import com.TravelMedicineAdvisory.Server.domain.employee.EmployeeRepository;
import com.TravelMedicineAdvisory.Server.domain.invoice.Invoice;
import com.TravelMedicineAdvisory.Server.domain.invoice.InvoiceRepository;
import com.TravelMedicineAdvisory.Server.domain.plans.GeneratedPlan;
import com.TravelMedicineAdvisory.Server.domain.plans.GeneratedPlanRepository;
import com.TravelMedicineAdvisory.Server.domain.systemsetting.SystemSettingRepository;
import com.TravelMedicineAdvisory.Server.domain.travelplan.TravelPlanRepository;
import com.TravelMedicineAdvisory.Server.domain.user.UserRepository;

@Service
public class AdminAnalyticsService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final CreditRepository creditRepository;
    private final TravelPlanRepository travelPlanRepository;
    private final AiRequestLogRepository aiRequestLogRepository;
    private final InvoiceRepository invoiceRepository;
    private final GeneratedPlanRepository generatedPlanRepository;
    private final SystemSettingRepository systemSettingRepository;
    private final EmployeeRepository employeeRepository;
    private final AbuseFlagRepository abuseFlagRepository;

    public AdminAnalyticsService(UserRepository userRepository, CompanyRepository companyRepository,
            CreditRepository creditRepository, TravelPlanRepository travelPlanRepository,
            AiRequestLogRepository aiRequestLogRepository, InvoiceRepository invoiceRepository,
            GeneratedPlanRepository generatedPlanRepository,
            SystemSettingRepository systemSettingRepository, EmployeeRepository employeeRepository,
            AbuseFlagRepository abuseFlagRepository) {
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.creditRepository = creditRepository;
        this.travelPlanRepository = travelPlanRepository;
        this.aiRequestLogRepository = aiRequestLogRepository;
        this.invoiceRepository = invoiceRepository;
        this.generatedPlanRepository = generatedPlanRepository;
        this.systemSettingRepository = systemSettingRepository;
        this.employeeRepository = employeeRepository;
        this.abuseFlagRepository = abuseFlagRepository;
    }

    private String getBaseCurrency() {
        return systemSettingRepository.findByKey("revenueBaseCurrency")
                .map(s -> s.getValue() != null ? s.getValue().toUpperCase() : "USD")
                .orElse("USD");
    }

    private Map<String, Double> getExchangeRates() {
        Map<String, Double> rates = new HashMap<>();
        systemSettingRepository.findByKey("exchangeRateNGN")
                .ifPresent(s -> {
                    try {
                        rates.put("NGN", Double.parseDouble(s.getValue()));
                    } catch (NumberFormatException ignored) {
                    }
                });
        systemSettingRepository.findByKey("exchangeRateEUR")
                .ifPresent(s -> {
                    try {
                        rates.put("EUR", Double.parseDouble(s.getValue()));
                    } catch (NumberFormatException ignored) {
                    }
                });
        systemSettingRepository.findByKey("exchangeRateGBP")
                .ifPresent(s -> {
                    try {
                        rates.put("GBP", Double.parseDouble(s.getValue()));
                    } catch (NumberFormatException ignored) {
                    }
                });
        return rates;
    }

    /**
     * Converts an invoice amount into the configured reporting (base) currency.
     * Rates are "1 unit of foreign currency = X baseCurrency" (e.g. NGN→USD).
     * Missing rates return 0 so NGN amounts are never summed as if they were USD
     * (or vice versa).
     */
    private double convertToBase(java.math.BigDecimal amount, String currency, String baseCurrency,
            Map<String, Double> rates) {
        if (amount == null)
            return 0;
        if (currency == null || currency.isBlank())
            return amount.doubleValue();
        String upper = currency.toUpperCase();
        String base = baseCurrency != null ? baseCurrency.toUpperCase() : "USD";
        if (upper.equals(base))
            return amount.doubleValue();
        Double rate = rates.get(upper);
        if (rate == null)
            return 0.0;
        return amount.doubleValue() * rate;
    }

    public AdminDashboardStatsResponse getDashboardStats() {
        AdminDashboardStatsResponse stats = new AdminDashboardStatsResponse();

        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime weekStart = todayStart.minusDays(7);
        LocalDateTime monthStart = todayStart.minusDays(30);

        stats.setTotalUsers(userRepository.countAllActive());
        stats.setTotalCompanies(companyRepository.countAllActive());

        long individualCredits = userRepository.sumCreditsByType("INDIVIDUAL");
        long corporateCredits = companyRepository.sumTotalCreditsActive();
        stats.setTotalCreditsIssued(individualCredits + corporateCredits);

        long individualUsed = creditRepository.sumConsumedByUserType("INDIVIDUAL");
        long corporateUsed = companyRepository.sumUsedCreditsActive();
        stats.setTotalCreditsConsumed(individualUsed + corporateUsed);

        stats.setTotalTravelPlans(travelPlanRepository.countAllActive());
        stats.setSuspendedUsers(userRepository.countSuspended());
        stats.setPendingInvoicesCount(invoiceRepository.countByStatus("pending"));
        stats.setTotalEmployees(employeeRepository.countActiveEmployees());
        stats.setUnresolvedAbuseFlags(abuseFlagRepository.countUnresolved());

        stats.setAiRequestsToday(aiRequestLogRepository.countCreatedSince(todayStart));
        stats.setAiRequestsLast7Days(aiRequestLogRepository.countCreatedSince(weekStart));
        stats.setTokensUsedToday(aiRequestLogRepository.sumTokensUsedSince(todayStart));

        long failedLast7d = aiRequestLogRepository.countCreatedSinceWithStatus(weekStart, "error");
        stats.setFailedAiCallsLast7Days(failedLast7d);

        long aiTotal30d = aiRequestLogRepository.countCreatedSince(monthStart);
        long aiSuccess30d = aiRequestLogRepository.countCreatedSinceWithStatus(monthStart, "success");
        double successRate = aiTotal30d == 0 ? 100.0 : Math.round(aiSuccess30d * 1000.0 / aiTotal30d) / 10.0;
        stats.setAiSuccessRateLast30Days(successRate);

        String baseCurrency = getBaseCurrency();
        Map<String, Double> rates = getExchangeRates();
        double revenue = invoiceRepository.findRevenueFieldsByStatus("paid").stream()
                .mapToDouble(inv -> convertToBase(inv.amount(), inv.currency(), baseCurrency, rates))
                .sum();
        stats.setRevenueOverview(revenue);
        stats.setRevenueBaseCurrency(baseCurrency);

        long failedCallsAllTime = aiRequestLogRepository.countByStatusActive("error");
        stats.setFailedAICalls(failedCallsAllTime);

        long activeUsersToday = userRepository.countActiveLastLoginBetween(todayStart, todayStart.plusDays(1));
        stats.setActiveUsersToday(activeUsersToday);

        long newUsersThisWeek = userRepository.countCreatedSince(weekStart);
        stats.setNewUsersThisWeek(newUsersThisWeek);

        String health = "healthy";
        if (stats.getUnresolvedAbuseFlags() != null && stats.getUnresolvedAbuseFlags() > 0) {
            health = "degraded";
        } else if (failedLast7d >= 10) {
            health = "degraded";
        }
        stats.setSystemHealthStatus(health);

        return stats;
    }

    public AdminAnalyticsResponse getAnalytics() {
        AdminAnalyticsResponse analytics = new AdminAnalyticsResponse();

        List<Map<String, Object>> topDestinations = travelPlanRepository.countActiveByDestination().stream()
                .limit(10)
                .map(row -> {
                    Map<String, Object> entry = new HashMap<>();
                    entry.put("name", row[0]);
                    entry.put("count", row[1]);
                    return entry;
                })
                .toList();
        analytics.setTopDestinations(topDestinations);

        analytics.setAvgCreditsPerUser(userRepository.averageCreditsActiveUsers());

        Map<String, Long> corpVsInd = new HashMap<>();
        corpVsInd.put("corporate", userRepository.countByType("COMPANY"));
        corpVsInd.put("individual", userRepository.countByType("INDIVIDUAL"));
        analytics.setCorporateVsIndividual(corpVsInd);

        // Peak usage times — aggregate AI request logs by hour of day (all 24 hours,
        // 0-filled)
        List<Map<String, Object>> peakTimes = new ArrayList<>();
        Map<Integer, Long> hourCounts = new HashMap<>();
        for (Object[] row : aiRequestLogRepository.countActiveByCreatedHour()) {
            hourCounts.put(((Number) row[0]).intValue(), ((Number) row[1]).longValue());
        }
        for (int hour = 0; hour < 24; hour++) {
            Map<String, Object> entry = new HashMap<>();
            entry.put("hour", hour);
            entry.put("requests", hourCounts.getOrDefault(hour, 0L));
            peakTimes.add(entry);
        }
        analytics.setPeakUsageTimes(peakTimes);

        // Requests per model — count AI logs grouped by model
        List<Map<String, Object>> requestsByModel = aiRequestLogRepository.countActiveByModel().stream()
                .map(row -> {
                    Map<String, Object> entry = new HashMap<>();
                    entry.put("model", row[0]);
                    entry.put("requests", row[1]);
                    return entry;
                })
                .collect(java.util.stream.Collectors.toList());
        analytics.setRequestsByModel(requestsByModel);

        // Monthly requests & revenue — aggregate by month from AI logs and invoices
        java.time.format.DateTimeFormatter monthFmt = java.time.format.DateTimeFormatter.ofPattern("MMM yyyy");
        String analyticsBaseCurrency = getBaseCurrency();
        Map<String, Double> currencyRates = getExchangeRates();
        // Use YearMonth as key for correct chronological sorting
        Map<java.time.YearMonth, Long> monthlyRequestsByYM = new java.util.TreeMap<>();
        Map<java.time.YearMonth, Double> monthlyRevenueByYM = new java.util.TreeMap<>();
        for (Object[] row : aiRequestLogRepository.countActiveByCreatedMonth()) {
            java.time.YearMonth ym = java.time.YearMonth.of(((Number) row[0]).intValue(), ((Number) row[1]).intValue());
            monthlyRequestsByYM.merge(ym, ((Number) row[2]).longValue(), Long::sum);
        }
        for (var inv : invoiceRepository.findRevenueFieldsByStatus("paid")) {
            if (inv.paidAt() != null && inv.amount() != null) {
                java.time.YearMonth ym = java.time.YearMonth.from(inv.paidAt());
                double converted = convertToBase(inv.amount(), inv.currency(), analyticsBaseCurrency,
                        currencyRates);
                monthlyRevenueByYM.merge(ym, converted, Double::sum);
            }
        }
        Set<java.time.YearMonth> allYMs = new java.util.TreeSet<>();
        allYMs.addAll(monthlyRequestsByYM.keySet());
        allYMs.addAll(monthlyRevenueByYM.keySet());
        List<Map<String, Object>> monthly = new ArrayList<>();
        for (java.time.YearMonth ym : allYMs) {
            Map<String, Object> entry = new HashMap<>();
            entry.put("month", ym.format(monthFmt));
            entry.put("requests", monthlyRequestsByYM.getOrDefault(ym, 0L));
            entry.put("revenue", monthlyRevenueByYM.getOrDefault(ym, 0.0));
            monthly.add(entry);
        }
        analytics.setMonthlyRequests(monthly);

        // Risk distribution — count travel plans by risk score bucket
        List<Map<String, Object>> riskDist = new ArrayList<>();
        long lowCount = travelPlanRepository.countActiveRiskBelow(40);
        long medCount = travelPlanRepository.countActiveRiskBetween(40, 70);
        long highCount = travelPlanRepository.countActiveRiskAtLeast(70);
        Map<String, Object> lowEntry = new HashMap<>();
        lowEntry.put("risk", "Low");
        lowEntry.put("count", lowCount);
        riskDist.add(lowEntry);
        Map<String, Object> medEntry = new HashMap<>();
        medEntry.put("risk", "Medium");
        medEntry.put("count", medCount);
        riskDist.add(medEntry);
        Map<String, Object> highEntry = new HashMap<>();
        highEntry.put("risk", "High");
        highEntry.put("count", highCount);
        riskDist.add(highEntry);
        analytics.setRiskDistribution(riskDist);

        // Daily active users — users who logged in each day of the past week
        List<Map<String, Object>> dailyActive = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        java.time.format.DateTimeFormatter dayFmt = java.time.format.DateTimeFormatter.ofPattern("EEE");
        for (int i = 6; i >= 0; i--) {
            LocalDateTime dayStart = now.minusDays(i).withHour(0).withMinute(0).withSecond(0).withNano(0);
            LocalDateTime dayEnd = dayStart.plusDays(1);
            long count = userRepository.countActiveLastLoginBetween(dayStart, dayEnd);
            Map<String, Object> entry = new HashMap<>();
            entry.put("day", dayStart.format(dayFmt));
            entry.put("users", count);
            dailyActive.add(entry);
        }
        analytics.setDailyActiveUsers(dailyActive);

        // Credit usage by type — real data from users and companies
        List<Map<String, Object>> creditUsage = new ArrayList<>();
        long indUsed = creditRepository.sumConsumedByUserType("INDIVIDUAL");
        long indRemaining = userRepository.sumCreditsByType("INDIVIDUAL");
        Map<String, Object> indMap = new HashMap<>();
        indMap.put("type", "Individual");
        indMap.put("used", indUsed);
        indMap.put("remaining", indRemaining);
        creditUsage.add(indMap);

        long corpUsed = companyRepository.sumUsedCreditsActive();
        long corpRemaining = companyRepository.sumTotalCreditsActive() - corpUsed;
        Map<String, Object> corpMap = new HashMap<>();
        corpMap.put("type", "Corporate");
        corpMap.put("used", corpUsed);
        corpMap.put("remaining", corpRemaining);
        creditUsage.add(corpMap);
        analytics.setCreditUsageByType(creditUsage);

        return analytics;
    }

    public List<AdminAIRequestLogResponse> getAILogs(Long userId, String status) {
        List<AiRequestLog> logs;

        if (userId != null) {
            logs = aiRequestLogRepository.findAll().stream()
                    .filter(log -> log.getUser() != null && log.getUser().getId().equals(userId))
                    .toList();
        } else if (status != null) {
            logs = aiRequestLogRepository.findAll().stream()
                    .filter(log -> status.equals(log.getStatus()))
                    .toList();
        } else {
            logs = aiRequestLogRepository.findAll();
        }

        return logs.stream().map(this::mapAILogToResponse).toList();
    }

    public AdminAIRequestLogResponse getAILog(Long id) {
        AiRequestLog log = aiRequestLogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("AI Request Log not found"));
        return mapAILogToResponse(log);
    }

    @Transactional
    public void flagAILog(Long id) {
        AiRequestLog log = aiRequestLogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("AI Request Log not found"));
        log.setStatus("flagged");
        aiRequestLogRepository.save(log);
    }

    public List<AdminTravelPlanResponse> getPlans(Long userId, Long companyId) {
        List<GeneratedPlan> plans;

        if (userId != null) {
            plans = generatedPlanRepository.findAllActiveByUserId(userId);
        } else if (companyId != null) {
            plans = generatedPlanRepository.findAllActiveByCompanyId(companyId);
        } else {
            plans = generatedPlanRepository.findAllActive();
        }

        return plans.stream().map(this::mapPlanToResponse).toList();
    }

    public AdminTravelPlanResponse getPlan(Long id) {
        GeneratedPlan plan = generatedPlanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Generated plan not found"));
        return mapPlanToResponse(plan);
    }

    @Transactional
    public void flagPlan(Long id) {
        GeneratedPlan plan = generatedPlanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Generated plan not found"));
        plan.setStatus("flagged");
        generatedPlanRepository.save(plan);
    }

    @Transactional
    public void archivePlan(Long id) {
        GeneratedPlan plan = generatedPlanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Generated plan not found"));
        plan.setStatus("archived");
        generatedPlanRepository.save(plan);
    }

    @Transactional
    public void deletePlan(Long id) {
        GeneratedPlan plan = generatedPlanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Generated plan not found"));
        plan.setStatus("deleted");
        generatedPlanRepository.save(plan);
    }

    public List<AdminInvoiceResponse> getInvoices() {
        List<Invoice> invoices = invoiceRepository.findAllActive();
        return invoices.stream().map(this::mapInvoiceToResponse).toList();
    }

    public AdminInvoiceResponse getInvoice(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));
        return mapInvoiceToResponse(invoice);
    }

    @Transactional
    public AdminInvoiceResponse createInvoice(Map<String, Object> body) {
        Invoice invoice = new Invoice();
        if (body.containsKey("description"))
            invoice.setDescription((String) body.get("description"));
        if (body.containsKey("amount"))
            invoice.setAmount(new java.math.BigDecimal(body.get("amount").toString()));
        if (body.containsKey("currency"))
            invoice.setCurrency((String) body.get("currency"));
        if (body.containsKey("status"))
            invoice.setStatus((String) body.get("status"));
        if (body.containsKey("paymentMethod"))
            invoice.setPaymentMethod((String) body.get("paymentMethod"));
        if (body.containsKey("companyId")) {
            Long companyId = ((Number) body.get("companyId")).longValue();
            companyRepository.findById(companyId).ifPresent(invoice::setCompany);
        }
        if (body.containsKey("userId")) {
            Long userId = ((Number) body.get("userId")).longValue();
            userRepository.findById(userId).ifPresent(invoice::setUser);
        }
        invoice.setIssuedAt(LocalDateTime.now());
        if (body.containsKey("dueDate")) {
            invoice.setDueDate(LocalDateTime.parse((String) body.get("dueDate")));
        }
        invoice.setStatus(invoice.getStatus() != null ? invoice.getStatus() : "pending");
        invoice = invoiceRepository.save(invoice);
        return mapInvoiceToResponse(invoice);
    }

    @Transactional
    public AdminInvoiceResponse updateInvoice(Long id, Map<String, Object> updates) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));
        if (updates.containsKey("description"))
            invoice.setDescription((String) updates.get("description"));
        if (updates.containsKey("amount"))
            invoice.setAmount(new java.math.BigDecimal(updates.get("amount").toString()));
        if (updates.containsKey("currency"))
            invoice.setCurrency((String) updates.get("currency"));
        if (updates.containsKey("status"))
            invoice.setStatus((String) updates.get("status"));
        if (updates.containsKey("paymentMethod"))
            invoice.setPaymentMethod((String) updates.get("paymentMethod"));
        invoice = invoiceRepository.save(invoice);
        return mapInvoiceToResponse(invoice);
    }

    @Transactional
    public void markInvoicePaid(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));
        invoice.setStatus("paid");
        invoice.setPaidAt(LocalDateTime.now());
        invoiceRepository.save(invoice);
    }

    private AdminAIRequestLogResponse mapAILogToResponse(AiRequestLog log) {
        AdminAIRequestLogResponse response = new AdminAIRequestLogResponse();
        response.setId(log.getId());

        if (log.getUser() != null) {
            response.setUserId(log.getUser().getId());
            response.setUserName(log.getUser().getName() != null ? log.getUser().getName() : log.getUser().getEmail());
        }

        if (log.getCompany() != null) {
            response.setCompanyId(log.getCompany().getId());
            response.setCompanyName(log.getCompany().getName());
        }

        response.setDestination(log.getDestination());
        response.setPromptSummary(log.getPromptSummary());
        response.setOutputSummary(log.getOutputSummary());
        response.setTokensUsed(log.getTokensUsed() != null ? log.getTokensUsed() : 0);
        response.setPlanGenerationTokensUsed(log.getPlanGenerationTokensUsed() != null ? log.getPlanGenerationTokensUsed() : 0);
        response.setSummaryGenerationTokensUsed(log.getSummaryGenerationTokensUsed() != null ? log.getSummaryGenerationTokensUsed() : 0);
        response.setProcessingTimeMs(log.getProcessingTimeMs() != null ? log.getProcessingTimeMs() : 0L);
        response.setStatus(log.getStatus() != null ? log.getStatus() : "success");
        response.setErrorMessage(log.getErrorMessage());
        response.setRiskLevel(log.getRiskLevel() != null ? log.getRiskLevel() : "low");
        response.setTimestamp(log.getCreatedAt());
        response.setModelUsed(log.getModelUsed());
        response.setCreditConsumed(
                log.getCreditConsumed() != null && log.getCreditConsumed().compareTo(java.math.BigDecimal.ZERO) > 0);

        return response;
    }

    private AdminTravelPlanResponse mapPlanToResponse(GeneratedPlan plan) {
        AdminTravelPlanResponse response = new AdminTravelPlanResponse();
        response.setId(plan.getId());

        if (plan.getUser() != null) {
            response.setUserId(plan.getUser().getId());
            response.setUserName(
                    plan.getUser().getName() != null ? plan.getUser().getName() : plan.getUser().getEmail());
        }

        if (plan.getCompany() != null) {
            response.setCompanyId(plan.getCompany().getId());
            response.setCompanyName(plan.getCompany().getName());
        }

        response.setDestination(plan.getDestination());
        response.setDuration(plan.getDuration() != null ? plan.getDuration().toString() : "");
        response.setPurpose(plan.getPurpose());
        response.setRiskScore(plan.getRiskScore() != null ? plan.getRiskScore() : 0);
        response.setVaccinations(extractTextArray(plan.getPlanJson(), "vaccinations", "vaccine"));
        response.setHealthAlerts(extractTextArray(plan.getPlanJson(), "healthRiskOverview", "summary"));
        response.setSafetyAdvisories(extractTextArray(plan.getPlanJson(), "nextSteps", null));
        response.setPlanJson(plan.getPlanJson());

        response.setStatus(plan.getStatus() != null ? plan.getStatus() : "active");
        response.setCreatedAt(plan.getCreatedAt());
        response.setCreditUsed(plan.getTravelPlan() != null && plan.getTravelPlan().getUser() != null);

        return response;
    }

    private List<String> extractTextArray(String json, String arrayField, String nestedKey) {
        if (json == null || json.isBlank()) {
            return new ArrayList<>();
        }
        try {
            com.fasterxml.jackson.databind.JsonNode root = new com.fasterxml.jackson.databind.ObjectMapper()
                    .readTree(json);
            com.fasterxml.jackson.databind.JsonNode array = root.path(arrayField);
            if (!array.isArray()) {
                return new ArrayList<>();
            }
            List<String> values = new ArrayList<>();
            for (com.fasterxml.jackson.databind.JsonNode item : array) {
                if (item.isTextual()) {
                    values.add(item.asText());
                } else if (nestedKey != null && item.has(nestedKey)) {
                    values.add(item.path(nestedKey).asText());
                }
            }
            return values;
        } catch (Exception ignored) {
            return new ArrayList<>();
        }
    }

    private AdminInvoiceResponse mapInvoiceToResponse(Invoice invoice) {
        AdminInvoiceResponse response = new AdminInvoiceResponse();
        response.setId(invoice.getId());

        if (invoice.getCompany() != null) {
            response.setCompanyId(invoice.getCompany().getId());
            response.setCompanyName(invoice.getCompany().getName());
        }

        if (invoice.getUser() != null) {
            response.setUserId(invoice.getUser().getId());
            response.setUserName(
                    invoice.getUser().getName() != null ? invoice.getUser().getName() : invoice.getUser().getEmail());
        }

        response.setAmount(invoice.getAmount());
        response.setCurrency(invoice.getCurrency());
        response.setStatus(invoice.getStatus() != null ? invoice.getStatus() : "pending");
        response.setDescription(invoice.getDescription());
        response.setIssuedAt(invoice.getIssuedAt());
        response.setPaidAt(invoice.getPaidAt());
        response.setDueDate(invoice.getDueDate());
        response.setPaymentMethod(invoice.getPaymentMethod());

        return response;
    }
}
