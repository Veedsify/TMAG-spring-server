package com.TravelMedicineAdvisory.Server.domain.admin.roles;

import java.util.List;

public class AdminRoleResponse {
    private Long id;
    private String name;
    private String description;
    private List<String> permissions;
    private Integer userCount;

    public AdminRoleResponse() {}

    public AdminRoleResponse(Long id, String name, String description, List<String> permissions, Integer userCount) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.permissions = permissions;
        this.userCount = userCount;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public List<String> getPermissions() { return permissions; }
    public void setPermissions(List<String> permissions) { this.permissions = permissions; }
    public Integer getUserCount() { return userCount; }
    public void setUserCount(Integer userCount) { this.userCount = userCount; }
}
