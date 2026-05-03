package com.TravelMedicineAdvisory.Server.domain.doctor;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DoctorApplicationRepository extends JpaRepository<DoctorApplication, Long> {
    List<DoctorApplication> findByStatusAndDeletedAtIsNullOrderByCreatedAtDesc(DoctorApplicationStatus status);
    long countByStatusAndDeletedAtIsNull(DoctorApplicationStatus status);
    Optional<DoctorApplication> findFirstByEmailIgnoreCaseAndStatusInAndDeletedAtIsNull(
            String email,
            List<DoctorApplicationStatus> statuses);
}
