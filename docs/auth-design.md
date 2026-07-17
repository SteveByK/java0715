# Authentication and Authorization Design

## Business Goal

The project now models real banking access control instead of using a fixed API key. Operators must log in, receive short-lived access tokens, and hold explicit permissions before calling protected business APIs.

## Database

Flyway migration `V5__auth_identity_access.sql` creates:

- `auth_user`: login identity, BCrypt password hash, status, failed login count, lock deadline and last login time.
- `auth_role`: business roles such as `ADMIN`, `TELLER` and `AUDITOR`.
- `auth_permission`: fine-grained authorities such as `account:deposit`, `transfer:create` and `audit:read`.
- `auth_user_role`: normalized user-role mapping.
- `auth_role_permission`: normalized role-permission mapping.
- `auth_refresh_token`: opaque refresh-token hash, expiration and revocation state.
- `auth_login_log`: append-only login audit records with result, failure code, IP and user agent.

Seeded users:

```text
admin/admin123      full administrator
teller/teller123    front-office banking operator
auditor/auditor123  read-only audit and ledger operator
```

## Token Strategy

Access tokens are signed JWTs using HMAC SHA-256 and include user id, username, display name, roles, permissions, issue time and expiration time. The default access-token TTL is 15 minutes.

Refresh tokens are opaque random values. The database stores only a SHA-256 hash, not the raw token. Refresh rotates the token: the old row is revoked and a new refresh token is issued. Logout revokes the submitted refresh token.

Production deployments must override:

```text
BANK_JWT_SECRET
BANK_ACCESS_TOKEN_TTL
BANK_REFRESH_TOKEN_TTL
BANK_MAX_FAILED_LOGIN
BANK_LOCK_DURATION
```

## Login Risk Controls

Password verification uses BCrypt with strength 12. A user is temporarily locked after `BANK_MAX_FAILED_LOGIN` failures; the default lock duration is 15 minutes. Every success and failure writes `auth_login_log`, which supports security review and anomaly detection.

## RBAC Permissions

Business controllers use Spring method security with `@PreAuthorize`.

Examples:

```text
account:read       GET account balance
account:deposit    POST account deposit
transfer:create    POST domestic transfer
remittance:create  POST international remittance
ledger:read        GET ledger entries
audit:read         GET audit logs
outbox:publish     POST pending outbox publish
```

This keeps authorization close to the inbound adapter and lets the domain service remain focused on business rules.

## Frontend Flow

The React console shows a login screen when no session exists. After login it stores the access token, refresh token and current operator profile in local storage, then attaches `Authorization: Bearer <token>` to protected API calls.

The UI includes the three seeded demo users so the user can exercise administrator, teller and auditor behavior quickly.
