package com.TravelMedicineAdvisory.Server.domain.countryaccommodation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CountryAccommodationRepository extends JpaRepository<CountryAccommodation, Long> {
}
