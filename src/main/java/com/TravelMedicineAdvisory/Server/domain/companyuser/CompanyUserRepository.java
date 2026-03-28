package com.TravelMedicineAdvisory.Server.domain.companyuser;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.TravelMedicineAdvisory.Server.domain.user.User;
import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyUserRepository extends JpaRepository<CompanyUser, Long> {
    Optional<CompanyUser> findByUser(User user);
    List<CompanyUser> findAllByUser(User user);

    @Query("SELECT cu FROM CompanyUser cu WHERE cu.company.id = :companyId AND cu.deletedAt IS NULL")
    List<CompanyUser> findAllByCompanyId(@Param("companyId") Long companyId);

    @Query("SELECT cu FROM CompanyUser cu WHERE cu.company.id = :companyId AND cu.role IN ('Administrator', 'HR', 'SuperAdmin') AND cu.deletedAt IS NULL")
    List<CompanyUser> findAdminsByCompanyId(@Param("companyId") Long companyId);
}
