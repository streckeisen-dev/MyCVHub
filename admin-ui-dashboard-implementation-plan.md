# Implementation Plan: Admin UI & Platform Activity Dashboard

**Feature ID:** FEAT-ADMIN-01
**Related requirements:** `admin-ui-dashboard-requirements.md`
**Status:** Draft v1.0

---

## 1. Implementation Principles

- Keep admin identity and auth separate from applicant identity and auth as much as possible.
- Use the existing backend, database, Liquibase, and frontend stack; do not introduce paid or external analytics infrastructure.
- Treat admin auth as the highest-risk part of the feature. Implement and test that boundary before building the dashboard UI on top.
- Dashboard time-series start at feature deployment. Do not attempt to invent historic signup/login data.
- Capture activity asynchronously in-process and best-effort for v1. Occasional lost analytics events are acceptable; failed telemetry must not block applicant login/signup.

---

## 2. Phase 1: Backend Schema

Add a Liquibase migration under `backend/src/main/resources/db/changelog/sql`.

Create:

- `admin_account`
  - `id`
  - `username`
  - `password`
  - `role`
  - `is_active`
  - `must_change_password`
  - `temporary_password_expires_at`
  - `created_at`
  - `last_login_at`
  - optional `password_changed_at`
- `admin_audit_log`
  - `id`
  - `admin_id`
  - `actor_source`
  - `action`
  - `result`
  - `target_type`
  - `target_id`
  - `timestamp`
- `user_activity_event`
  - `id`
  - `applicant_account_id`
  - `event_type`
  - `timestamp`
- `platform_daily_metric`
  - `metric_date`
  - `metric_name`
  - `metric_value`
  - `updated_at`

Indexes and constraints:

- Unique admin username.
- Check/admin enum constraint for `ADMIN` and `SUPER_ADMIN`.
- Check/event enum constraint for `LOGIN` and `SIGNUP`.
- Index `user_activity_event(event_type, timestamp)`.
- Index `user_activity_event(applicant_account_id, timestamp)`.
- Unique key on `platform_daily_metric(metric_date, metric_name)`.

Open decision for implementation:

- Store `applicant_account_id` as a scalar value without a database FK. This preserves pre-aggregation login/signup counts if an applicant deletes their account before the nightly aggregation job runs; raw rows are still purged after 90 days.

---

## 3. Phase 2: Super-Admin Bootstrap

Add a startup seed component:

- Reads `ADMIN_SEED_USERNAME`.
- If absent, does nothing.
- If present and no matching admin exists, creates an inactive bootstrap `SUPER_ADMIN`.
- Does not create a usable password.
- Does not print or log secrets.

Add an admin bootstrap command/profile:

```bash
java -jar /app/app.jar \
  --spring.profiles.active=prod,admin-bootstrap \
  --spring.main.web-application-type=none \
  --my-cv.admin.bootstrap.username=admin@example.com
```

Command behavior:

- Validate target account exists and is `SUPER_ADMIN`.
- Require an explicit flag, such as `--my-cv.admin.bootstrap.reset-existing=true`, if resetting an already-active account outside the initial bootstrap state.
- Generate a 20+ character cryptographically random temporary password.
- Store only the encoded hash with the normal `PasswordEncoder`.
- Set `is_active = true`.
- Set `must_change_password = true`.
- Set `temporary_password_expires_at`, recommended 15-60 minutes.
- Invalidate any previous temporary password.
- Print the temporary password once to stdout.
- Write an audit entry without password or hash.

DigitalOcean App Platform operation:

- Use the App Platform Console for the backend service.
- Run the command inside the running production container so it has production DB access.
- Do not use a deploy-time job or cron job for temporary-password delivery if the password is printed to output.

---

## 4. Phase 3: Admin Auth Boundary

Add backend admin packages, likely:

- `backend/src/main/kotlin/ch/streckeisen/mycv/backend/admin/account`
- `backend/src/main/kotlin/ch/streckeisen/mycv/backend/admin/auth`
- `backend/src/main/kotlin/ch/streckeisen/mycv/backend/admin/dashboard`
- `backend/src/main/kotlin/ch/streckeisen/mycv/backend/admin/activity`

Implement:

- `AdminAccountEntity`
- `AdminRole`
- `AdminAccountRepository`
- `AdminPrincipal`
- `AdminUserDetailsService`
- `AdminAuthenticationService`
- `AdminAuthTokenService`
- `AdminAuthenticationResource`
- `@RequiresAdminRole` or equivalent server-side enforcement

Token/cookie requirements:

- Use separate admin cookie names, for example `adminAccessToken` and `adminRefreshToken`.
- Scope admin access cookie to admin routes/endpoints where practical.
- Admin refresh endpoint should be separate, for example `/api/admin/auth/refresh`.
- Applicant tokens must fail admin endpoints.
- Admin tokens must not satisfy applicant account endpoints.

Admin auth endpoints:

- `POST /api/admin/auth/login`
- `POST /api/admin/auth/refresh`
- `GET /api/admin/auth/login/verify`
- `POST /api/admin/auth/change-password`
- `POST /api/admin/auth/logout`

Forced password-change rule:

- Admins with `mustChangePassword = true` may only access change-password, refresh, and logout.
- Dashboard/admin data endpoints return 403 until the password is changed.

Rate limiting/lockout:

- Implement database-backed per-username throttling for admin login in v1, shared by all app instances.
- Use generic failure messages.
- Audit failed attempts.
- Configure thresholds with `ADMIN_LOGIN_RATE_LIMIT_MAX_FAILED_ATTEMPTS` and `ADMIN_LOGIN_RATE_LIMIT_LOCKOUT_MINUTES`.
- Unlock path: wait for the lockout window to expire. If the password itself must be reset, re-run the documented bootstrap reset command.

---

## 5. Phase 4: Activity Capture

Add:

- `ActivityEventType`
- `UserActivityEventEntity`
- `UserActivityEventRepository`
- `UserActivityEventService`
- An application event/listener pair for async capture

Capture events:

- `SIGNUP` after successful applicant password signup transaction commits.
- `LOGIN` after successful applicant password login.
- `LOGIN` after successful OAuth login.

Preferred implementation:

- Publish domain/application events from auth flows.
- Persist them with `@TransactionalEventListener(phase = AFTER_COMMIT)`.
- Run persistence asynchronously via Spring `@Async` or a configured executor.
- Log failures, but do not fail applicant auth/signup.

Document limitation:

- In-process async events can be lost if the container exits before persistence. Acceptable for v1 analytics.

---

## 6. Phase 5: Aggregation And Retention

Add a daily scheduled job using the existing backend scheduler pattern.

Aggregation behavior:

- Compute daily signup count from `SIGNUP`.
- Compute daily active users from distinct applicant ids with `LOGIN`.
- Upsert into `platform_daily_metric`.
- Reruns must update/replace values, not double-count.
- Use server time for day boundaries.

Dashboard summary behavior:

- Total users, verified/unverified split, and OAuth/password split can query current account tables.
- DAU/WAU/MAU can query raw `LOGIN` events within the 90-day retention window.
- Signup and active-user charts should read daily aggregates.

Retention behavior:

- Run purge after successful aggregation.
- Delete raw `user_activity_event` rows older than 90 days.
- Keep daily aggregate rows indefinitely.

---

## 7. Phase 6: Admin Dashboard API

Create read-only admin dashboard endpoints under `/api/admin/dashboard`.

Recommended endpoint:

- `GET /api/admin/dashboard?range=30d`

DTO should include:

- `totalRegisteredUsers`
- `dailyActiveUsers`
- `weeklyActiveUsers`
- `monthlyActiveUsers`
- `signupSeries`
- `activeUserSeries`
- `verifiedAccountCount`
- `unverifiedAccountCount`
- `oauthSignupCount`
- `passwordSignupCount`
- `asOf`

Do not expose:

- Applicant names
- Applicant emails
- Raw event rows
- Drill-down lists of individual users

---

## 8. Phase 7: Frontend Admin UI

Add separate frontend admin files:

- `frontend/src/pages/admin/AdminLoginPage.tsx`
- `frontend/src/pages/admin/AdminDashboardPage.tsx`
- `frontend/src/pages/admin/AdminChangePasswordPage.tsx`
- `frontend/src/layouts/AdminLayout.tsx`
- `frontend/src/context/AdminAuthorizationContext.tsx`
- `frontend/src/components/admin/AdminSecurityCheck.tsx`
- `frontend/src/api/AdminAuthApi.ts`
- `frontend/src/api/AdminDashboardApi.ts`
- `frontend/src/types/admin/*`

Routing:

- Add `/admin/login`.
- Add `/admin/change-password`.
- Add `/admin/dashboard`.
- Redirect `/admin` to `/admin/dashboard` for authenticated admins.
- Keep admin routes out of normal applicant navigation.

UI behavior:

- Admin login uses admin auth endpoints only.
- Admin dashboard uses admin dashboard endpoints only.
- If `mustChangePassword = true`, redirect to admin password-change page.
- Show logged-in admin username and logout action in `AdminLayout`.
- Display "as of" timestamp on the dashboard.
- Indicate that chart history starts at deployment if there is no earlier data.

---

## 9. Phase 8: Tests

Backend tests:

- `AdminAuthenticationServiceTest`
- `AdminAuthTokenServiceTest`
- `AdminBootstrapServiceTest`
- `AdminEndpointSecurityAspectTest` or equivalent
- `UserActivityEventServiceTest`
- `AdminDashboardServiceTest`
- Aggregation job tests
- Retention purge tests

Critical backend cases:

- Valid active admin login succeeds.
- Inactive admin cannot authenticate.
- Expired temporary password cannot authenticate.
- `mustChangePassword = true` admin cannot call dashboard API.
- Applicant token cannot call `/api/admin/**`.
- Admin token cannot call applicant endpoints.
- Failed login attempts are rate-limited/locked and audit-logged.
- Activity write failure does not fail applicant login/signup.
- Aggregation rerun is idempotent.
- Purge happens only after aggregate generation succeeds.

Frontend tests:

- `AdminSecurityCheck` anonymous/applicant/admin states.
- Admin login error handling.
- Forced password-change redirect.
- Dashboard render with summary counts and empty/no-history chart state.

---

## 10. Suggested Rollout Order

1. Merge schema and backend bootstrap.
2. Verify super-admin bootstrap manually in a non-production environment using the Docker image.
3. Implement admin auth boundary and tests.
4. Deploy admin auth without dashboard data if useful for early verification.
5. Add activity capture.
6. Add aggregation and retention.
7. Add admin dashboard API.
8. Add frontend admin UI.
9. Run full backend/frontend test suites.
10. Perform production bootstrap through DigitalOcean App Platform Console.
