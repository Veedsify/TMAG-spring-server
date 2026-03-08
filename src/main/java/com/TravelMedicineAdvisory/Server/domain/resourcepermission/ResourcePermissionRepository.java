package com.TravelMedicineAdvisory.Server.domain.resourcepermission;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResourcePermissionRepository extends JpaRepository<ResourcePermission, Long> {
}
