package com.TravelMedicineAdvisory.Server.domain.company;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {

    public Optional<Company> findByCompanyCode(String companyCode);
}
