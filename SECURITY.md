# Security Policy & Architecture

## Security Measures Implemented

1. **Stateless JWT Authentication**:
   - Cryptographically signed JSON Web Tokens (HMAC-SHA256).
   - Identity extracted securely from `SecurityContextHolder` rather than trusting body parameters.

2. **Role-Based Access Control (RBAC)**:
   - Method-level `@PreAuthorize("hasRole('ADMIN')")` safeguards on all administrative endpoints.
   - Separate customer and admin route filtering in `SecurityConfig`.

3. **Password Security**:
   - Passwords securely hashed with BCrypt (10 rounds work factor).
   - Raw passwords never logged or returned in responses.

4. **ACID Transaction & Stock Integrity**:
   - Order placement, cancellation, and exchange stock adjustments executed inside `@Transactional` boundaries to prevent race conditions or negative inventory.

5. **Cross-Origin & Data Protection**:
   - CORS explicitly configured with allowed methods and origins.
   - Centralized `@RestControllerAdvice` prevents stack trace disclosure to clients.
