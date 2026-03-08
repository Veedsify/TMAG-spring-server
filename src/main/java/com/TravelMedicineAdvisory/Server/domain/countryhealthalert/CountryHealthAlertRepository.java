package com.TravelMedicineAdvisory.Server.domain.countryhealthalert;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CountryHealthAlertRepository extends JpaRepository<CountryHealthAlert, Long> {
}
