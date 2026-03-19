package com.TravelMedicineAdvisory.Server.domain.creditpricing;

import com.TravelMedicineAdvisory.Server.domain.company.BillingCurrency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CreditPricingRepository extends JpaRepository<CreditPricing, Long> {
    Optional<CreditPricing> findByCurrency(BillingCurrency currency);
    
    List<CreditPricing> findByIsActiveTrueOrderByDisplayOrderAsc();
    
    boolean existsByCurrency(BillingCurrency currency);
}
