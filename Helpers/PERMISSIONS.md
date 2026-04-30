# RBAC Permissions Reference

## Roles (6)

| ID | Role Name       | Description                                         |
|----|-----------------|-----------------------------------------------------|
| 1  | SuperAdmin      | Full access to everything                           |
| 2  | Administrator   | Manage all content, users, and settings             |
| 3  | HR              | Manage employees, view company data                 |
| 4  | CustomerSupport | Read-only on users/companies, manage FAQ            |
| 5  | Individual      | Personal profile, travel plans, read public content |
| 6  | Doctor          | Review travel plans and health information          |

---

## Resources (31) × Actions (5) = 155 Permissions

### Actions

| Action   | Description                 |
|----------|-----------------------------|
| `create` | Create new records          |
| `read`   | View single record          |
| `update` | Modify existing records     |
| `delete` | Remove records              |
| `list`   | View list/paginated records |

### Resources

`user`, `authorization`, `media`, `profile`, `abuse_flag`, `ai_request_log`, `api_key`, `blog_post`, `company`,
`company_user`, `country`, `country_accommodation`, `country_health_alert`, `credit`, `data_export`, `doctor`,
`ebook`, `employee`, `faq_item`, `health_profile`, `invoice`, `notification`, `plan_generation_context`,
`plan_usage_ledger`, `pricing_plan`, `report`, `system_log`, `system_setting`, `travel_plan`, `travel_request`,
`user_onboarding`

### Permission format: `resource:action` (e.g. `user:read`, `company:delete`)

---

## Permissions by Role

### SuperAdmin — ALL 155 permissions

Full access to every resource and action.

---

### Administrator (105 permissions)

| Resource       | create | read | update | delete | list |
|----------------|--------|------|--------|--------|------|
| user           | ✅      | ✅    | ✅      | ✅      | ✅    |
| authorization  | ✅      | ✅    | ✅      | ✅      | ✅    |
| media          | ✅      | ✅    | ✅      | ✅      | ✅    |
| profile        | ✅      | ✅    | ✅      | ✅      | ✅    |
| company        | ✅      | ✅    | ✅      | ✅      | ✅    |
| employee       | ✅      | ✅    | ✅      | ✅      | ✅    |
| country        | ✅      | ✅    | ✅      | ✅      | ✅    |
| blog_post      | ✅      | ✅    | ✅      | ✅      | ✅    |
| faq_item       | ✅      | ✅    | ✅      | ✅      | ✅    |
| credit         | ✅      | ✅    | ✅      | ✅      | ✅    |
| pricing_plan   | ✅      | ✅    | ✅      | ✅      | ✅    |
| system_setting | ✅      | ✅    | ✅      | ✅      | ✅    |
| notification   | ✅      | ✅    | ✅      | ✅      | ✅    |
| system_log     | ✅      | ✅    | ✅      | ✅      | ✅    |
| travel_plan    | ✅      | ✅    | ✅      | ✅      | ✅    |
| api_key        | ✅      | ✅    | ✅      | ✅      | ✅    |
| data_export    | ✅      | ✅    | ✅      | ✅      | ✅    |
| doctor         | ✅      | ✅    | ✅      | ✅      | ✅    |
| ebook          | ✅      | ✅    | ✅      | ✅      | ✅    |
| plan_generation_context | ✅ | ✅ | ✅ | ✅ | ✅ |
| report         | ✅      | ✅    | ✅      | ✅      | ✅    |

---

### HR (33 permissions)

| Resource       | create | read | update | delete | list |
|----------------|--------|------|--------|--------|------|
| company        | ❌      | ✅    | ✅      | ❌      | ❌    |
| employee       | ✅      | ✅    | ✅      | ✅      | ✅    |
| travel_request | ❌      | ✅    | ❌      | ❌      | ✅    |
| travel_plan    | ❌      | ✅    | ❌      | ❌      | ✅    |
| invoice        | ❌      | ✅    | ❌      | ❌      | ✅    |
| credit         | ✅      | ✅    | ❌      | ❌      | ✅    |
| company_user   | ✅      | ✅    | ✅      | ✅      | ✅    |
| data_export    | ❌      | ✅    | ❌      | ❌      | ✅    |
| plan_usage_ledger | ❌   | ✅    | ❌      | ❌      | ✅    |
| pricing_plan   | ❌      | ✅    | ❌      | ❌      | ✅    |
| report         | ❌      | ✅    | ❌      | ❌      | ✅    |
| user_onboarding | ❌     | ✅    | ✅      | ❌      | ❌    |

---

### CustomerSupport (19 permissions)

| Resource       | create | read | update | delete | list |
|----------------|--------|------|--------|--------|------|
| user           | ❌      | ✅    | ✅      | ❌      | ❌    |
| company        | ❌      | ✅    | ❌      | ❌      | ❌    |
| employee       | ❌      | ✅    | ❌      | ❌      | ❌    |
| travel_plan    | ❌      | ✅    | ❌      | ❌      | ✅    |
| travel_request | ❌      | ✅    | ❌      | ❌      | ✅    |
| faq_item       | ❌      | ✅    | ✅      | ❌      | ❌    |
| credit         | ❌      | ✅    | ❌      | ❌      | ✅    |
| invoice        | ❌      | ✅    | ❌      | ❌      | ✅    |
| notification   | ❌      | ✅    | ❌      | ❌      | ✅    |
| report         | ❌      | ✅    | ❌      | ❌      | ❌    |
| user_onboarding | ❌     | ✅    | ❌      | ❌      | ❌    |

---

### Individual (32 permissions)

| Resource       | create | read | update | delete | list |
|----------------|--------|------|--------|--------|------|
| profile        | ❌      | ✅    | ✅      | ❌      | ❌    |
| health_profile | ✅      | ✅    | ✅      | ✅      | ✅    |
| travel_plan    | ✅      | ✅    | ✅      | ✅      | ✅    |
| travel_request | ✅      | ✅    | ✅      | ✅      | ✅    |
| country        | ❌      | ✅    | ❌      | ❌      | ❌    |
| blog_post      | ❌      | ✅    | ❌      | ❌      | ❌    |
| faq_item       | ❌      | ✅    | ❌      | ❌      | ❌    |
| pricing_plan   | ❌      | ✅    | ❌      | ❌      | ✅    |
| report         | ❌      | ✅    | ❌      | ❌      | ❌    |
| notification   | ❌      | ✅    | ❌      | ❌      | ❌    |
| credit         | ✅      | ✅    | ❌      | ❌      | ✅    |
| ebook          | ❌      | ✅    | ❌      | ❌      | ✅    |
| user_onboarding | ✅     | ✅    | ✅      | ❌      | ❌    |

---

### Doctor (20 permissions)

| Resource       | create | read | update | delete | list |
|----------------|--------|------|--------|--------|------|
| profile        | ❌      | ✅    | ✅      | ❌      | ❌    |
| doctor         | ✅      | ✅    | ✅      | ❌      | ❌    |
| travel_plan    | ❌      | ✅    | ✅      | ❌      | ✅    |
| health_profile | ❌      | ✅    | ❌      | ❌      | ❌    |
| country        | ❌      | ✅    | ❌      | ❌      | ❌    |
| blog_post      | ❌      | ✅    | ❌      | ❌      | ❌    |
| ebook          | ❌      | ✅    | ❌      | ❌      | ✅    |
| faq_item       | ❌      | ✅    | ❌      | ❌      | ❌    |
| notification   | ❌      | ✅    | ❌      | ❌      | ✅    |
| pricing_plan   | ❌      | ✅    | ❌      | ❌      | ✅    |
| user_onboarding | ❌     | ✅    | ❌      | ❌      | ❌    |

---

## Usage in Controllers

### Role-based (broad):

```java

@PreAuthorize("hasRole('SUPERADMIN')")
@DeleteMapping("/{id}")
public ResponseEntity<SuccessResponse> delete(@PathVariable Long id) { ...}
```

### Permission-based (fine-grained):

```java

@PreAuthorize("hasAuthority('user:delete')")
@DeleteMapping("/{id}")
public ResponseEntity<SuccessResponse> delete(@PathVariable Long id) { ...}

@PreAuthorize("hasAuthority('country:read')")
@GetMapping("/{id}")
public ResponseEntity<SuccessResponse> getById(@PathVariable Long id) { ...}
```

### Multiple options:

```java

@PreAuthorize("hasRole('SUPERADMIN') or hasAuthority('employee:create')")
@PostMapping
public ResponseEntity<SuccessResponse> create(@RequestBody EmployeeRequest request) { ...}

@PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMINISTRATOR')")
@GetMapping
public ResponseEntity<SuccessResponse> getAll(Pageable pageable) { ...}
```

### Notes:

- `hasRole('X')` checks for authority `ROLE_X` (prefix added automatically)
- `hasAuthority('X')` checks for exact string match
- Permissions are loaded from `role_permissions` table via `CustomUserDetailsService`
- Seeded in `DataSeeder.seedRolePermissions()`
- Public endpoints (countries, FAQ, pricing, etc.) are configured in `SecurityConfig.java` and don't need annotations

---

## Key Files

| File                                                  | Purpose                                                |
|-------------------------------------------------------|--------------------------------------------------------|
| `config/SecurityConfig.java`                          | Route-level public/authenticated access                |
| `security/CustomUserDetailsService.java`              | Loads role + permissions as Spring authorities         |
| `security/JwtAuthenticationFilter.java`               | Extracts JWT, authenticates user per request           |
| `core/seeder/DataSeeder.java`                         | Seeds roles, permissions, and role↔permission mappings |
| `domain/rolepermission/RolePermissionRepository.java` | `findByRoleId()` query for loading permissions         |
