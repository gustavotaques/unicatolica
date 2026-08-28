# Epic 1 Context: Cadastro, Login e Controle de Acesso

<!-- Compiled from planning artifacts. Edit freely. Regenerate with compile-epic-context if planning docs change. -->

## Goal

Epic 1 delivers the project's technical foundation (monorepo scaffold, CI/CD, migrations, base OpenAPI contract, JWT security filter, audit log) and the full identity/access flow: a student registers with an institutional email, confirms that email before first login, authenticates into a JWT session, logs out, and every action in the system is gated by authentication and restricted by profile/role. This is a must-have for week 1 and is the foundation every other epic builds on — Comunidades' auto-join (RF24.1) depends on the course captured at signup here, and every later module reuses the JWT filter, error envelope, and audit log established in Story 1.1.

## Stories

- Story 1.1: Fundação do projeto (scaffold e infraestrutura) — monorepo, docker-compose, CI/CD, OpenAPI base, JWT filter scaffold, audit log, health check
- Story 1.2: Cadastro de aluno com e-mail institucional
- Story 1.3: Confirmação de e-mail antes do primeiro login
- Story 1.4: Login e emissão de sessão JWT
- Story 1.5: Bloqueio de acesso sem autenticação e restrição por perfil
- Story 1.6: Logout e invalidação de sessão

## Requirements & Constraints

- Registration requires institutional-domain email only; external-domain emails are rejected with an explanatory message. Duplicate emails are rejected. Email format and password policy are validated per-field.
- Users under 18 are blocked from registering.
- A completed registration does not create an active session and does not allow login — the account is persisted in an "email not confirmed" state until the confirmation flow (link/token) completes. Login attempts with an unconfirmed email must return a message distinct from "invalid credentials" (never conflate the two).
- Registration captures name, institutional email, password, and course — the course is the minimum needed to trigger auto-join into that course's community (handled in Epic 2, not built here); full academic profile editing (name/course/period/interests after signup) belongs to Epic 4.
- Successful login issues a JWT with fixed claims `sub` (user id) and `roles` (global profile/role), transported only via `Authorization: Bearer` header — never cookies.
- Invalid credentials return one generic message, without indicating which field is wrong.
- Every non-`@PermitAll` endpoint must reject missing/expired/invalid tokens with 401. Authorization checks beyond authentication (permission for a specific action) return 403 when the resource's existence need not be hidden, or 404 when it must be hidden from unauthorized users — this 403-vs-404 mapping is fixed, not a per-module choice.
- Logout invalidates the session; subsequent requests with the same token must then be rejected with 401.
- All error responses use the standard envelope `{"error":{"code","message","details"}}` with the HTTP status mapped per AD-5's fixed scenario map (401/403/404/400-422/409/500).
- Success criteria for the epic: a new user can go from signup through email confirmation, login, and logout without training (RNF01); operations meet p95 ≤ 2s (RNF03, at risk on free-tier cold starts); OWASP ASVS 4.0.3 baseline applies to auth, session management, and input validation (RNF04); login and administrative changes are recorded in the centralized audit log (RNF07).

## Technical Decisions

- Backend: Java + Quarkus, multimodular monolith (one deploy unit, one package per module: `identidade`, etc.), JAX-RS Resource → Service → Repository per module, single security filter in front of everything. Frontend: Angular SPA, deployed separately. TDD is mechanically enforced by CI gate (no green test suite, no merge); SOLID/DDD are design discipline, not automatically checked.
- A single JAX-RS filter validates the JWT on every authenticable request before it reaches any module, except endpoints explicitly listed in an `@PermitAll` allowlist that lives only in the filter itself (e.g. `POST /auth/login`, `POST /auth/registro`). Fine-grained per-profile authorization (RF13) is each module's own responsibility, not the filter's. Community-admin role (local scope) is an independent axis from global profile role — nothing in week 1 requires composing the two.
- CORS is configured once, centrally (`quarkus.http.cors`), never per module/filter.
- `openapi.yaml` (OpenAPI 3.1.0) at repo root is the source of truth, agreed before either side implements a new endpoint. CI validates real response bodies against the schema per endpoint, not just generated-interface compilation. All list endpoints use the shared `PageResponse` component via `$ref` — no endpoint invents its own pagination shape.
- Repo layout: monorepo with `frontend/`, `backend/` (package per module), `openapi.yaml`, `docker-compose.yml`, `.env.example`, `.github/workflows/` at root (assumption, not an explicit team decision, but the architecture's recommendation).
- Local dev: `docker-compose.yml` brings up Postgres + Quarkus dev mode + Angular dev server, sharing `.env.example`; no developer installs Postgres outside the compose setup.
- CI/CD: GitHub Actions runs build + tests + OpenAPI validation on every PR; merge to `main` requires green CI (no mandatory human review, by team decision); merge triggers automatic deploy via native GitHub↔Render integration.
- Hosting: Angular as Render Static Site; Quarkus as Render Web Service via Docker; Postgres managed on Neon (persistent free tier) — never Render's free Postgres (expires after 30+14 days).
- Migrations: Liquibase, changelogs versioned per module (`db/changelog/{modulo}/*.xml`), included by a stable master changelog via `<includeAll>`. Changeset IDs are prefixed by module (e.g. `identidade-002-...`), never a global counter, to avoid collisions across parallel PRs.
- Observability: health check via `quarkus-smallrye-health` at `/q/health`; structured JSON logs to stdout. No staging environment in week 1 — only local (docker-compose) and production (Render).
- Audit log: a single `log_auditoria` table, written only via an injectable `AuditoriaService` (cross-cutting infrastructure, not owned by any of the 12 feature modules). Active from week 1; this epic's login events are the first thing that writes to it.
- Data conventions: entity/table/column names in Portuguese (domain's ubiquitous language); technical classes (`Resource`/`Service`/`Repository`) and REST paths in English. IDs are `bigint`/identity (not UUID). Timestamps use `Instant` (ISO-8601 UTC); date-only values use `LocalDate`. Request/response JSON fields use Portuguese camelCase (e.g. `nomeCompleto`).
- Module boundary rule (applies once other modules start reading identity data): cross-module reads via JPA association are allowed but must be read-only at the transaction level (DTO/projection or `FlushMode.MANUAL`); writes to another module's data must go through a published Java interface of the owning module. Direct repository/table access across modules is forbidden, enforced by an automated architecture test (e.g. ArchUnit) in CI.
- ERD for week 1 scope: `USUARIO` has one `PERFIL`, administers/joins `COMUNIDADE` via `COMUNIDADE_MEMBRO`, and authors `PUBLICACAO`.

## UX & Interaction Patterns

- Login, Cadastro, and "Verifique seu e-mail" are three distinct screens in the flow: Cadastro (name, institutional email, password) → Verifique seu e-mail (intermediate screen confirming signup and prompting the user to check email before first login, with a resend-confirmation option) → Login → Início.
- Login and Cadastro screens exist in the Figma prototype and must be restyled to the "Campus Clean" design system (tokens for color/typography, per Epic 14). "Verifique seu e-mail" has no existing mockup — build it spine-only from the flow description.
- Specific microcopy is fixed for these error states (do not invent alternate wording): duplicate email at signup → "Esse e-mail já tem uma conta. Esqueceu a senha?"; wrong domain at signup → "Use seu e-mail institucional para se cadastrar."; unconfirmed email at login → "Confirme seu e-mail antes de entrar. Reenviar confirmação" (must read as distinct from the generic invalid-credentials message).
- General voice/tone rule: second person ("você"), direct, errors explain what happened and what to do without blaming the user.
- Accessibility floor (WCAG 2.2 AA): visible keyboard focus on every interactive element; form validation errors must be announced/labeled, not conveyed by color alone.

## Cross-Story Dependencies

- Story 1.1 (scaffold) is a prerequisite for all other stories in this epic and for every other epic — it establishes the JWT filter, error envelope, OpenAPI contract, audit log, and CI/CD pipeline that later stories and modules build on.
- Story 1.2's capture of "curso" at signup is what makes Epic 2's auto-join to a course community (Story 2.3, RF24.1) possible without needing the full Academic Profile module (Epic 4) to exist first.
- Story 1.3 (email confirmation) gates Story 1.4 (login) — login must check confirmation status before issuing a session.
- Story 1.5's authentication/authorization behavior (401/403/404 via the JWT filter and per-module checks) is the mechanism every other epic's endpoints rely on for access control.
