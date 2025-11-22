Profile Microservice Integration

This backend integrates with an external Profile microservice (authentication and user management) that exposes endpoints such as `/api/auth/login`, `/api/auth/register`, and `/api/auth/validate-token`.

Configuration:

- Override the profile service base URL using environment or `.env.properties` key `PROFILE_SERVICE_BASE_URL`.
- Default used in development: `https://2306219575-be.hafizmuh.site`.

The system expects JWT tokens either sent via an HTTP-only cookie named `JWT_TOKEN` (recommended for browser sessions) or via `Authorization: Bearer <token>` header for non-browser clients.

Security behaviour:

- Requests with a valid token will have the external profile information validated at `/api/auth/validate-token` and the returned role (e.g., `Customer`, `Flight Airline`, `Superadmin`) is mapped to authorities `ROLE_CUSTOMER`, `ROLE_FLIGHT_AIRLINE`, and `ROLE_SUPERADMIN` respectively.
- Use the endpoints under `/api/admin` (protected) for admin statistics. Controllers use `@PreAuthorize` annotations to enforce RBAC.

If you want me to also implement server-side session caching for tokens or fallback behavior, tell me which approach you prefer.
