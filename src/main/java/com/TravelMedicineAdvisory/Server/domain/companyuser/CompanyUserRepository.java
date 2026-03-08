package com.TravelMedicineAdvisory.Server.domain.companyuser;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyUserRepository extends JpaRepository<CompanyUser, Long> {
}
