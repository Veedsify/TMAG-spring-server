# RBAC Permissions Reference

## Roles (5)

| ID | Role Name       | Description                                         |
|----|-----------------|-----------------------------------------------------|
| 1  | SuperAdmin      | Full access to everything                           |
| 2  | Administrator   | Manage all content, users, and settings             |
| 3  | HR              | Manage employees, view company data                 |
| 4  | CustomerSupport | Read-only on users/companies, manage FAQ            |
| 5  | Individual      | Personal profile, travel plans, read public content |

---

## Resources (25) × Actions (5) = 125 Permissions

### Actions

| Action   | Description                 |
|----------|-----------------------------|
| `create` | Create new records          |
| `read`   | View single record          |
| `update` | Modify existing records     |
| `delete` | Remove records              |
| `list`   | View list/paginated records |

### Resources

`user`, `authorization`, `media`, `profile`, `abuse_flag`, `ai_request_log`, `blog_post`, `company`, `company_user`,
`country`, `country_accommodation`, `country_health_alert`, `credit`, `employee`, `faq_item`, `health_profile`,
`invoice`, `notification`, `plan_usage_ledger`, `pricing_plan`, `system_log`, `system_setting`, `travel_plan`,
`travel_request`, `user_onboarding`

### Permission format: `resource:action` (e.g. `user:read`, `company:delete`)

---

## Permissions by Role

### SuperAdmin — ALL 125 permissions

Full access to every resource and action.

---

### Administrator (65 permissions)

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
| pricing_plan   | ✅      | ✅    | ✅      | ✅      | ✅    |
| system_setting | ✅      | ✅    | ✅      | ✅      | ✅    |
| notification   | ✅      | ✅    | ✅      | ✅      | ✅    |
| system_log     | ✅      | ✅    | ✅      | ✅      | ✅    |

---

### HR (12 permissions)

| Resource       | create | read | update | delete | list |
|----------------|--------|------|--------|--------|------|
| company        | ❌      | ✅    | ✅      | ❌      | ❌    |
| employee       | ✅      | ✅    | ✅      | ✅      | ✅    |
| travel_request | ❌      | ✅    | ❌      | ❌      | ❌    |
| travel_plan    | ❌      | ✅    | ❌      | ❌      | ❌    |
| invoice        | ❌      | ✅    | ❌      | ❌      | ❌    |
| credit         | ❌      | ✅    | ❌      | ❌      | ❌    |

---

### CustomerSupport (8 permissions)

| Resource       | create | read | update | delete | list |
|----------------|--------|------|--------|--------|------|
| user           | ❌      | ✅    | ✅      | ❌      | ❌    |
| company        | ❌      | ✅    | ❌      | ❌      | ❌    |
| employee       | ❌      | ✅    | ❌      | ❌      | ❌    |
| travel_plan    | ❌      | ✅    | ❌      | ❌      | ❌    |
| travel_request | ❌      | ✅    | ❌      | ❌      | ❌    |
| faq_item       | ❌      | ✅    | ✅      | ❌      | ❌    |

---

### Individual (17 permissions)

| Resource       | create | read | update | delete | list |
|----------------|--------|------|--------|--------|------|
| profile        | ❌      | ✅    | ✅      | ❌      | ❌    |
| health_profile | ✅      | ✅    | ✅      | ✅      | ✅    |
| travel_plan    | ✅      | ✅    | ✅      | ✅      | ✅    |
| travel_request | ✅      | ✅    | ✅      | ✅      | ✅    |
| country        | ❌      | ✅    | ❌      | ❌      | ❌    |
| blog_post      | ❌      | ✅    | ❌      | ❌      | ❌    |
| faq_item       | ❌      | ✅    | ❌      | ❌      | ❌    |
| pricing_plan   | ❌      | ✅    | ❌      | ❌      | ❌    |
| notification   | ❌      | ✅    | ❌      | ❌      | ❌    |

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
