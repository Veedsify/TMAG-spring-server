package com.TravelMedicineAdvisory.Server.domain.admin.analytics;

import com.TravelMedicineAdvisory.Server.domain.airequestlog.AiRequestLog;
import com.TravelMedicineAdvisory.Server.domain.airequestlog.AiRequestLogRepository;
import com.TravelMedicineAdvisory.Server.domain.company.Company;
import com.TravelMedicineAdvisory.Server.domain.company.CompanyRepository;
import com.TravelMedicineAdvisory.Server.domain.credit.CreditRepository;
import com.TravelMedicineAdvisory.Server.domain.invoice.Invoice;
import com.TravelMedicineAdvisory.Server.domain.invoice.InvoiceRepository;
import com.TravelMedicineAdvisory.Server.domain.travelplan.TravelPlan;
import com.TravelMedicineAdvisory.Server.domain.travelplan.TravelPlanRepository;
import com.TravelMedicineAdvisory.Server.domain.user.User;
import com.TravelMedicineAdvisory.Server.domain.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class AdminAnalyticsService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final CreditRepository creditRepository;
    private final TravelPlanRepository travelPlanRepository;
    private final AiRequestLogRepository aiRequestLogRepository;
    private final InvoiceRepository invoiceRepository;

    public AdminAnalyticsService(UserRepository userRepository, CompanyRepository companyRepository,
                                 CreditRepository creditRepository, TravelPlanRepository travelPlanRepository,
                                 AiRequestLogRepository aiRequestLogRepository, InvoiceRepository invoiceRepository) {
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.creditRepository = creditRepository;
        this.travelPlanRepository = travelPlanRepository;
        this.aiRequestLogRepository = aiRequestLogRepository;
        this.invoiceRepository = invoiceRepository;
    }

    public AdminDashboardStatsResponse getDashboardStats() {
        AdminDashboardStatsResponse stats = new AdminDashboardStatsResponse();
        
        stats.setTotalUsers(userRepository.countAllActive());
        stats.setTotalCompanies(companyRepository.countAllActive());
        
        long individualCredits = userRepository.findByType("INDIVIDUAL").stream()
                .mapToInt(u -> u.getCredits() != null ? u.getCredits() : 0)
                .sum();
        long corporateCredits = companyRepository.findAllActive().stream()
                .mapToInt(c -> c.getTotalCredits() != null ? c.getTotalCredits() : 0)
                .sum();
        stats.setTotalCreditsIssued(individualCredits + corporateCredits);
        
        long individualUsed = userRepository.findByType("INDIVIDUAL").stream()
                .mapToInt(u -> {
                    List<com.TravelMedicineAdvisory.Server.domain.credit.Credit> credits = 
                        creditRepository.findLedgerByUserId(u.getId());
                    return credits.stream()
                        .filter(c -> "consume".equals(c.getType()))
                        .mapToInt(c -> Math.abs(c.getAmount()))
                        .sum();
                })
                .sum();
        stats.setTotalCreditsConsumed(individualUsed);
        
        LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        long aiRequestsToday = aiRequestLogRepository.count();
        stats.setAiRequestsToday(aiRequestsToday);
        
        Long revenue = invoiceRepository.sumPaidInvoices();
        stats.setRevenueOverview(revenue != null ? revenue : 0L);
        
        long failedCalls = aiRequestLogRepository.findAll().stream()
                .filter(log -> "error".equals(log.getStatus()))
                .count();
        stats.setFailedAICalls(failedCalls);
        
        stats.setSystemHealthStatus("healthy");
        
        stats.setActiveUsersToday(1L);
        stats.setNewUsersThisWeek(3L);
        
        return stats;
    }

    public AdminAnalyticsResponse getAnalytics() {
        AdminAnalyticsResponse analytics = new AdminAnalyticsResponse();
        
        List<Map<String, Object>> topDestinations = new ArrayList<>();
        List<TravelPlan> plans = travelPlanRepository.findAllActive();
        Map<String, Long> destCounts = new HashMap<>();
        for (TravelPlan plan : plans) {
            String dest = plan.getDestination();
            if (dest != null) {
                destCounts.merge(dest, 1L, Long::sum);
            }
        }
        destCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .forEach(e -> {
                    Map<String, Object> entry = new HashMap<>();
                    entry.put("name", e.getKey());
                    entry.put("count", e.getValue());
                    topDestinations.add(entry);
                });
        analytics.setTopDestinations(topDestinations);
        
        List<User> users = userRepository.findAllActive();
        double avgCredits = users.stream()
                .mapToInt(u -> u.getCredits() != null ? u.getCredits() : 0)
                .average()
                .orElse(0.0);
        analytics.setAvgCreditsPerUser(avgCredits);
        
        Map<String, Long> corpVsInd = new HashMap<>();
        corpVsInd.put("corporate", userRepository.countByType("COMPANY"));
        corpVsInd.put("individual", userRepository.countByType("INDIVIDUAL"));
        analytics.setCorporateVsIndividual(corpVsInd);
        
        List<Map<String, Object>> peakTimes = new ArrayList<>();
        for (int hour = 8; hour <= 17; hour++) {
            Map<String, Object> entry = new HashMap<>();
            entry.put("hour", hour);
            entry.put("requests", 50 + new Random().nextInt(50));
            peakTimes.add(entry);
        }
        analytics.setPeakUsageTimes(peakTimes);
        
        List<Map<String, Object>> monthly = new ArrayList<>();
        String[] months = {"Aug", "Sep", "Oct", "Nov", "Dec", "Jan"};
        for (String month : months) {
            Map<String, Object> entry = new HashMap<>();
            entry.put("month", month);
            entry.put("requests", 100 + new Random().nextInt(200));
            entry.put("revenue", 40000 + new Random().nextInt(80000));
            monthly.add(entry);
        }
        analytics.setMonthlyRequests(monthly);
        
        List<Map<String, Object>> dailyActive = new ArrayList<>();
        String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        for (String day : days) {
            Map<String, Object> entry = new HashMap<>();
            entry.put("day", day);
            entry.put("users", 15 + new Random().nextInt(50));
            dailyActive.add(entry);
        }
        analytics.setDailyActiveUsers(dailyActive);
        
        List<Map<String, Object>> creditUsage = new ArrayList<>();
        Map<String, Object> ind = new HashMap<>();
        ind.put("type", "Individual");
        ind.put("used", 440);
        ind.put("remaining", 280);
        creditUsage.add(ind);
        Map<String, Object> corp = new HashMap<>();
        corp.put("type", "Corporate");
        corp.put("used", 340);
        corp.put("remaining", 560);
        creditUsage.add(corp);
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
        List<TravelPlan> plans;
        
        if (userId != null) {
            plans = travelPlanRepository.findAllActiveByUserId(userId);
        } else if (companyId != null) {
            plans = travelPlanRepository.findAllActiveByCompanyId(companyId);
        } else {
            plans = travelPlanRepository.findAllActive();
        }

        return plans.stream().map(this::mapPlanToResponse).toList();
    }

    public AdminTravelPlanResponse getPlan(Long id) {
        TravelPlan plan = travelPlanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Travel Plan not found"));
        return mapPlanToResponse(plan);
    }

    @Transactional
    public void flagPlan(Long id) {
        TravelPlan plan = travelPlanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Travel Plan not found"));
        plan.setStatus("flagged");
        travelPlanRepository.save(plan);
    }

    @Transactional
    public void archivePlan(Long id) {
        TravelPlan plan = travelPlanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Travel Plan not found"));
        plan.setStatus("archived");
        travelPlanRepository.save(plan);
    }

    @Transactional
    public void deletePlan(Long id) {
        travelPlanRepository.deleteById(id);
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
        response.setProcessingTimeMs(log.getProcessingTimeMs() != null ? log.getProcessingTimeMs() : 0L);
        response.setStatus(log.getStatus() != null ? log.getStatus() : "success");
        response.setErrorMessage(log.getErrorMessage());
        response.setRiskLevel(log.getRiskLevel() != null ? log.getRiskLevel() : "low");
        response.setTimestamp(log.getCreatedAt());
        response.setModelUsed(log.getModelUsed());
        response.setCreditConsumed(log.getCreditConsumed() != null && log.getCreditConsumed().compareTo(java.math.BigDecimal.ZERO) > 0);
        
        return response;
    }

    private AdminTravelPlanResponse mapPlanToResponse(TravelPlan plan) {
        AdminTravelPlanResponse response = new AdminTravelPlanResponse();
        response.setId(plan.getId());
        
        if (plan.getUser() != null) {
            response.setUserId(plan.getUser().getId());
            response.setUserName(plan.getUser().getName() != null ? plan.getUser().getName() : plan.getUser().getEmail());
        }
        
        if (plan.getCompany() != null) {
            response.setCompanyId(plan.getCompany().getId());
            response.setCompanyName(plan.getCompany().getName());
        }
        
        response.setDestination(plan.getDestination());
        response.setDuration(plan.getDuration() != null ? plan.getDuration().toString() : "");
        response.setPurpose(plan.getPurpose());
        response.setRiskScore(plan.getRiskScore() != null ? plan.getRiskScore() : 0);
        
        List<String> vaccinations = plan.getVaccinations() != null ? 
                Arrays.asList(plan.getVaccinations().split(",")) : new ArrayList<>();
        response.setVaccinations(vaccinations);
        
        List<String> healthAlerts = plan.getHealthAlerts() != null ?
                Arrays.asList(plan.getHealthAlerts().split(",")) : new ArrayList<>();
        response.setHealthAlerts(healthAlerts);
        
        List<String> safetyAdvisories = plan.getSafetyAdvisories() != null ?
                Arrays.asList(plan.getSafetyAdvisories().split(",")) : new ArrayList<>();
        response.setSafetyAdvisories(safetyAdvisories);
        
        response.setStatus(plan.getStatus() != null ? plan.getStatus() : "active");
        response.setCreatedAt(plan.getCreatedAt());
        response.setCreditUsed(true);
        
        return response;
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
            response.setUserName(invoice.getUser().getName() != null ? invoice.getUser().getName() : invoice.getUser().getEmail());
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
