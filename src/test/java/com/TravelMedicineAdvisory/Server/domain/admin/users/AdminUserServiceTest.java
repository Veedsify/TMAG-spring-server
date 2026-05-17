package com.TravelMedicineAdvisory.Server.domain.admin.users;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.TravelMedicineAdvisory.Server.core.queue.QueueService;
import com.TravelMedicineAdvisory.Server.domain.abuseflag.AbuseFlagRepository;
import com.TravelMedicineAdvisory.Server.domain.admin.credits.AdminCreditService;
import com.TravelMedicineAdvisory.Server.domain.companyuser.CompanyUserRepository;
import com.TravelMedicineAdvisory.Server.domain.credit.CreditRepository;
import com.TravelMedicineAdvisory.Server.domain.employee.EmployeeRepository;
import com.TravelMedicineAdvisory.Server.domain.travelplan.TravelPlanRepository;
import com.TravelMedicineAdvisory.Server.domain.user.AvatarUrlService;
import com.TravelMedicineAdvisory.Server.domain.user.User;
import com.TravelMedicineAdvisory.Server.domain.user.UserRepository;

class AdminUserServiceTest {

    private UserRepository userRepository;
    private AdminUserService service;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        CreditRepository creditRepository = mock(CreditRepository.class);
        AbuseFlagRepository abuseFlagRepository = mock(AbuseFlagRepository.class);
        TravelPlanRepository travelPlanRepository = mock(TravelPlanRepository.class);
        AvatarUrlService avatarUrlService = mock(AvatarUrlService.class);

        when(creditRepository.findLedgerByUserId(any())).thenReturn(List.of());
        when(abuseFlagRepository.findAll()).thenReturn(List.of());
        when(travelPlanRepository.countByUserId(any())).thenReturn(0L);
        when(avatarUrlService.toFullUrl(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service = new AdminUserService(
                userRepository,
                creditRepository,
                mock(AdminCreditService.class),
                mock(EmployeeRepository.class),
                mock(CompanyUserRepository.class),
                abuseFlagRepository,
                travelPlanRepository,
                mock(PasswordEncoder.class),
                mock(QueueService.class),
                avatarUrlService);
    }

    @Test
    void updatePersistsNameAndNameParts() {
        User user = new User();
        user.setId(10L);
        user.setEmail("old@example.com");
        user.setIsActive(true);
        when(userRepository.findById(10L)).thenReturn(Optional.of(user));

        AdminUserResponse response = service.update(10L, java.util.Map.of(
                "name", "Ada Lovelace",
                "email", "ada@example.com"));

        assertThat(user.getName()).isEqualTo("Ada Lovelace");
        assertThat(user.getFirstName()).isEqualTo("Ada");
        assertThat(user.getLastName()).isEqualTo("Lovelace");
        assertThat(response.getName()).isEqualTo("Ada Lovelace");
        assertThat(response.getEmail()).isEqualTo("ada@example.com");
    }

    @Test
    void suspendAndActivateToggleIsActiveWithoutSoftDeleting() {
        User user = new User();
        user.setId(10L);
        user.setEmail("user@example.com");
        user.setIsActive(true);
        when(userRepository.findById(10L)).thenReturn(Optional.of(user));

        service.suspend(10L);
        assertThat(user.getIsActive()).isFalse();
        assertThat(user.getDeletedAt()).isNull();
        assertThat(service.findById(10L).getStatus()).isEqualTo("suspended");

        service.activate(10L);
        assertThat(user.getIsActive()).isTrue();
        assertThat(service.findById(10L).getStatus()).isEqualTo("active");
    }
}
