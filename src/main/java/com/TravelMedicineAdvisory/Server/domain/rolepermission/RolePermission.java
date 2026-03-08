package com.TravelMedicineAdvisory.Server.domain.rolepermission;


import com.TravelMedicineAdvisory.Server.core.base.BaseEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import com.TravelMedicineAdvisory.Server.domain.role.Role;
import com.TravelMedicineAdvisory.Server.domain.permission.Permission;

@Entity
@Table(name = "role_permissions")
@SQLDelete(sql = "UPDATE role_permissions SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
public class RolePermission extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id")
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "permission_id")
    private Permission permission;

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Permission getPermission() {
        return permission;
    }

    public void setPermission(Permission permission) {
        this.permission = permission;
    }
}
