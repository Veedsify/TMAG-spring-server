### RBAC Architecture

On login, each user gets loaded with authorities like:

ROLE_SUPERADMIN, users:read, users:write, users:delete, countries:read, ...

How to use in any controller:

```
// Role-based (broad)
@PreAuthorize("hasRole('SUPERADMIN')")
@DeleteMapping("/{id}")
```
```
// Permission-based (fine-grained, matches your 125 seeded permissions)
@PreAuthorize("hasAuthority('users:delete')")
@DeleteMapping("/{id}")
```
```// Multiple roles
@PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMINISTRATOR')")

// Combine role + permission
@PreAuthorize("hasRole('SUPERADMIN') or hasAuthority('countries:write')")
```

How to add custom middleware:

Create a filter in middlewares/:
```
@Component
@Order(1)  // execution order
public class MyFilter extends OncePerRequestFilter {
@Override
protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain) {
// your logic here
chain.doFilter(req, res);
}
}
```

Register in SecurityConfig if it needs to run at a specific point:

`.addFilterBefore(myFilter, UsernamePasswordAuthenticationFilter.class)`