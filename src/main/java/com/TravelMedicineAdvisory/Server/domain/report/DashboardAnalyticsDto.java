package com.TravelMedicineAdvisory.Server.domain.report;

import java.util.List;

public record DashboardAnalyticsDto(
        List<NamedCountDto> plansByStatus,
        List<MonthCountDto> plansCreatedLastSixMonths,
        List<NamedCountDto> topDestinations,
        List<TopEmployeePlansDto> topEmployeesByPlans,
        List<NamedCountDto> creditRequestsByStatus) {}
