package com.TravelMedicineAdvisory.Server.domain.admin.credits;

import com.TravelMedicineAdvisory.Server.domain.company.Company;
import com.TravelMedicineAdvisory.Server.domain.company.CompanyRepository;
import com.TravelMedicineAdvisory.Server.domain.credit.Credit;
import com.TravelMedicineAdvisory.Server.domain.credit.CreditRepository;
import com.TravelMedicineAdvisory.Server.domain.user.User;
import com.TravelMedicineAdvisory.Server.domain.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class AdminCreditService {

    private final CreditRepository creditRepository;
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;

    public AdminCreditService(CreditRepository creditRepository, UserRepository userRepository,
                             CompanyRepository companyRepository) {
        this.creditRepository = creditRepository;
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
    }

    public List<AdminCreditLedgerResponse> getLedger(Long userId, Long companyId) {
        List<Credit> credits;
        
        if (userId != null && companyId != null) {
            credits = creditRepository.findLedgerByUserIdOrCompanyId(userId, companyId);
        } else if (userId != null) {
            credits = creditRepository.findLedgerByUserId(userId);
        } else if (companyId != null) {
            credits = creditRepository.findLedgerByCompanyId(companyId);
        } else {
            credits = creditRepository.findAllLedger();
        }

        return credits.stream().map(this::mapToResponse).toList();
    }

    @Transactional
    public void adjustCredits(Map<String, Object> body) {
        Long userId = body.get("userId") != null ? ((Number) body.get("userId")).longValue() : null;
        Long companyId = body.get("companyId") != null ? ((Number) body.get("companyId")).longValue() : null;
        Integer amount = ((Number) body.get("amount")).intValue();
        String reason = (String) body.get("reason");
        String action = (String) body.get("action");

        if (userId != null) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            Integer currentCredits = user.getCredits() != null ? user.getCredits() : 0;
            
            Credit credit = new Credit();
            credit.setUser(user);
            credit.setAmount(amount);
            credit.setType(action != null ? action : "admin_add");
            credit.setBalanceAfter(currentCredits + amount);
            credit.setReference(reason != null ? reason : "Admin adjustment");
            
            creditRepository.save(credit);
            
            user.setCredits(currentCredits + amount);
            userRepository.save(user);
        } else if (companyId != null) {
            Company company = companyRepository.findById(companyId)
                    .orElseThrow(() -> new RuntimeException("Company not found"));
            
            Integer currentCredits = company.getTotalCredits() != null ? company.getTotalCredits() : 0;
            
            Credit credit = new Credit();
            credit.setCompany(company);
            credit.setAmount(amount);
            credit.setType(action != null ? action : "admin_add");
            credit.setBalanceAfter(currentCredits + amount);
            credit.setReference(reason != null ? reason : "Admin adjustment");
            
            creditRepository.save(credit);
            
            company.setTotalCredits(currentCredits + amount);
            companyRepository.save(company);
        } else {
            throw new RuntimeException("Either userId or companyId must be provided");
        }
    }

    private AdminCreditLedgerResponse mapToResponse(Credit credit) {
        Long userId = null;
        String userName = null;
        Long compId = null;
        String companyName = null;

        if (credit.getUser() != null) {
            userId = credit.getUser().getId();
            userName = credit.getUser().getName() != null ? credit.getUser().getName() : credit.getUser().getEmail();
        }
        if (credit.getCompany() != null) {
            compId = credit.getCompany().getId();
            companyName = credit.getCompany().getName();
        }

        String action = credit.getType() != null ? credit.getType() : "consume";
        String triggeredBy = "admin";
        
        if ("consume".equals(action)) {
            triggeredBy = "system";
        }

        return new AdminCreditLedgerResponse(
            credit.getId(),
            userId,
            userName,
            compId,
            companyName,
            action,
            credit.getAmount(),
            credit.getBalanceAfter() != null ? credit.getBalanceAfter() - credit.getAmount() : 0,
            credit.getBalanceAfter(),
            credit.getReference(),
            triggeredBy,
            credit.getCreatedAt()
        );
    }
}
