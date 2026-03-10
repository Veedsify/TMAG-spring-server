package com.TravelMedicineAdvisory.Server.domain.companyuser;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.TravelMedicineAdvisory.Server.domain.user.User;
import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyUserRepository extends JpaRepository<CompanyUser, Long> {
    Optional<CompanyUser> findByUser(User user);
    List<CompanyUser> findAllByUser(User user);
}
