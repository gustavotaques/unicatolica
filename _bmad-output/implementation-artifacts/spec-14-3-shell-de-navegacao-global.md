---
title: 'Story 14.3: Global navigation shell'
type: 'feature'
created: '2026-08-30'
status: 'done'
review_loop_iteration: 1
baseline_commit: 'e19474c614e89c152b845525d3c6e91fbfb7fb8f'
context:
  - '{project-root}/_bmad-output/implementation-artifacts/epic-14-context.md'
  - '{project-root}/_bmad-output/planning-artifacts/ux-designs/ux-unicatolica-2026-08-17/EXPERIENCE.md'
  - '{project-root}/_bmad-output/planning-artifacts/ux-designs/ux-unicatolica-2026-08-17/DESIGN.md'
---

<frozen-after-approval reason="human-owned intent — do not modify unless human renegotiates">

## Intent

**Problem:** There is no navigation shell. `app.html` is a bare `<router-outlet />`, the only authenticated screen (`feed`) carries a throwaway logout button from Story 1.6, and users get no role-aware, consistent way to move across the platform (UX-DR7). `AuthService` never decodes the JWT, so the frontend cannot read the role claim needed to gate moderation items.

**Approach:** Add a `Shell` layout component (sticky sidebar + topbar avatar dropdown) under `src/app/layout/shell/`, mount it as a guarded parent route wrapping the authenticated routes, and give `AuthService` a minimal JWT payload decode so the sidebar shows Denúncias / Solicitações de fixação only for `MODERADOR` / `ADMINISTRADOR`. Right-hand discovery panel and responsive collapse are out of scope (deferred / Story 14.4).

## Boundaries & Constraints

**Always:**
- Shell at `src/app/layout/shell/shell.{ts,html,scss,spec.ts}`, standalone, `selector: 'app-shell'`, mirroring existing standalone patterns (`login.ts`: `inject()`, signals). Role gating is a `computed()` derived once, not a method re-run per `@for` item.
- Sidebar items and role visibility come verbatim from EXPERIENCE.md "Navegação global": Início, Buscar, Mensagens, Notificações, Criar enquete, Suas comunidades, Descobrir comunidades for every authenticated user; Denúncias and Solicitações de fixação only when `AuthService.possuiPerfil('MODERADOR') || possuiPerfil('ADMINISTRADOR')`.
- Only "Início" has a route today: render it `<a routerLink="/feed" routerLinkActive>` with `aria-current="page"` when active. Every other item renders inert: a `<span>` (not `<a>`), `aria-disabled="true"`, not focusable, no click handler.
- The sidebar's primary navigation is a `<nav aria-label>` that is itself the sidebar container element — not a `<nav>` wrapped inside `<aside>` (that gets announced as a complementary landmark, not navigation).
- Topbar: avatar `<button>` top-right, `aria-haspopup="menu"` + `aria-expanded`, toggles a dropdown (`role="menu"`, items `role="menuitem"`) with Perfil, Configurações (both inert) and Sair. Dropdown closes on Escape, on outside click, and after any item activates; on every close path focus returns to the avatar `<button>`. Sair calls `AuthService.logout()` then `Router.navigateByUrl('/login').catch(() => {})`.
- `AuthService` gains `perfis(): string[]` and `possuiPerfil(perfil: string): boolean`, backed by a private payload-only JWT decode (base64url segment, UTF-8-safe, `JSON.parse`, `[]` on any failure). It reads the role claim named by a single exported constant (value `roles` — the backend sets `smallrye.jwt.path.groups=roles`, so the emitted token carries the perfil under `roles`, never `groups`). That constant is the one source of truth shared by `AuthService` and its spec. No new dependency; `login` / `logout` / token storage unchanged.
- `app.routes.ts`: wrap authenticated routes in a parent `{ path: '', loadComponent: Shell, canActivate: [authGuard], canActivateChild: [authGuard], children: [...] }` and nest `feed` under it. `login` / `cadastro` / `confirmar-email` and the leading `'' -> redirectTo: 'login'` stay outside the shell.
- Every colour / font / spacing / radius / shadow is a `var(--uc-*)` token or `.uc-text-*` utility. Active nav item: `background: var(--uc-color-orange-tint); color: var(--uc-color-orange); font-weight: var(--uc-font-weight-semibold)`. Dropdown: `var(--uc-shadow-overlay)` + `var(--uc-radius-md)`. Every interactive element gets a `:focus-visible` outline `2px solid var(--uc-color-maroon)` at `2px` offset (matches `button.scss`). Sidebar and topbar use `position: sticky; top: 0` so they stay in view on a long page; sidebar is `overflow-y: auto`.
- Dimensions DESIGN.md writes as "~" and explicitly defers (sidebar width ~220px, topbar height, dropdown min-width) are allowed as commented structural literals in `shell.scss`. Viewport height uses `100dvh` with a `100vh` fallback. Other allowed literals: `1px`/`2px` borders/outlines, `0`, unitless weights.

**Ask First:**
- Introducing a real icon set / SVG icon system (mockup uses emoji placeholders: keep them `aria-hidden` or omit).
- Any nav-item behaviour beyond show/hide + inert (counters, community sub-list, collapse).
- Dropdown motion beyond a single ~150ms CSS fade (Story 14.5 owns motion).

**Never:**
- Right-hand discovery panel (~260px) — deferred this story.
- Responsive / mobile collapse — Story 14.4.
- Full ARIA menu keyboard navigation (arrow-key roving focus between menu items) — Story 14.9. This story does focus-return-to-trigger only.
- Restyling Login, Cadastro, Verifique seu e-mail or any feature-screen body — Story 14.7. `feed` edits are limited to deleting the Story 1.6 logout stub.
- New `--uc-*` token or any edit to `_tokens.scss` / `_typography.scss`.
- A route or screen for Perfil, Configurações, Buscar, Mensagens, Notificações, Criar enquete, comunidades, Denúncias, Solicitações de fixação.

## I/O & Edge-Case Matrix

| Scenario | Input / State | Expected Output / Behavior | Error Handling |
|----------|--------------|---------------------------|----------------|
| aluno loads a shell screen | token role claim `roles: ["ALUNO"]` | sidebar shows the 7 common items; Denúncias + Solicitações de fixação absent; dropdown has Perfil, Configurações, Sair | N/A |
| moderator loads | token `roles: ["MODERADOR"]` | above plus Denúncias + Solicitações de fixação | N/A |
| admin loads | token `roles: ["ADMINISTRADOR"]` | same as moderator | N/A |
| malformed / missing token | `obterToken()` null or non-JWT | `perfis()` returns `[]`; moderation items hidden; no throw | swallow decode error, return `[]` |
| token payload has multibyte chars | valid JWT, non-ASCII claim value | decode still succeeds; `perfis()` reads `roles` correctly | UTF-8-safe decode |
| unauthenticated hit on `/feed` | no token in storage | router redirects to `/login`; shell never renders | parent `canActivate` |
| click avatar button | dropdown closed | dropdown opens, `aria-expanded="true"` | N/A |
| Escape / outside click / item activated | dropdown open | dropdown closes, `aria-expanded="false"`, focus back on avatar button | N/A |
| click Sair | dropdown open | `AuthService.logout()` called, then navigate `/login` | navigate rejection swallowed |
| navigate to `/feed` | authenticated | Shell renders; "Início" has `aria-current="page"` and active style | N/A |
| routeless nav item clicked | rendered | `<span aria-disabled="true">`, not tab-focusable, no navigation | N/A |

</frozen-after-approval>

## Code Map

- `frontend/src/app/core/auth/auth.service.ts` -- MODIFY. Export `const JWT_ROLES_CLAIM = 'roles'`. Add `perfis(): string[]` (decode payload, read `payload[JWT_ROLES_CLAIM]`, keep only strings, `[]` otherwise) + `possuiPerfil(perfil)` + a private `decodificarPayloadJwt()`: split token, base64url -> base64, decode UTF-8-safe (`TextDecoder` on the byte array, or `decodeURIComponent(escape(atob(...)))`), `JSON.parse`, all in try/catch returning `null`. Do not touch `login` / `logout` / `obterToken` / storage key.
- `frontend/src/app/core/auth/auth.service.spec.ts` -- MODIFY. Fixture builder keys the payload with `JWT_ROLES_CLAIM` (imported, not a literal). Cases: `roles` parsed to a string array; claim absent -> `[]`; malformed token -> `[]`; no token -> `[]`; multibyte payload still decodes; `possuiPerfil` true and false.
- `frontend/src/app/layout/shell/shell.ts` -- NEW. `selector: 'app-shell'`, standalone, `imports: [RouterOutlet, RouterLink, RouterLinkActive]`, `inject(AuthService)` + `inject(Router)`. `menuAberto = signal(false)`; `ehPrivilegiado = computed(() => auth.possuiPerfil('MODERADOR') || auth.possuiPerfil('ADMINISTRADOR'))`; `NAV_ITENS` typed const (`label`, `path: string | null`, `privileged?: boolean`); `alternarMenu(ev)` (stops propagation), `fecharMenu()` (also refocuses the avatar `<button>` via a `viewChild`/`ElementRef`), `sair()`. Host listeners `document:keydown.escape` + `document:click` -> `fecharMenu()`.
- `frontend/src/app/layout/shell/shell.html` -- NEW. Sidebar as `<nav class="shell__sidebar" aria-label="Navegação principal">` (the landmark itself) iterating `NAV_ITENS` (skip `privileged` unless `ehPrivilegiado()`; `<a routerLink>` when `path`, else inert `<span aria-disabled="true">`); `<header>` topbar with avatar `<button #avatar>` + conditional dropdown; `<main><router-outlet /></main>`.
- `frontend/src/app/layout/shell/shell.scss` -- NEW. Grid: sticky sidebar + fluid main + sticky topbar row; `100dvh`/`100vh`. Token-only colours/type/spacing; deferred `~` dimensions as commented literals; `:focus-visible` maroon outline; dropdown `var(--uc-shadow-overlay)` + `var(--uc-radius-md)` + single 150ms fade.
- `frontend/src/app/layout/shell/shell.spec.ts` -- NEW. Vitest + TestBed, setup mirroring `login.spec.ts` (`provideRouter`, seeded `localStorage` token via the shared claim constant). Every I/O row (role sets, malformed token, dropdown open/close, Escape refocuses avatar, outside-click close, Sair -> logout + `/login`, `aria-current` on `/feed`, inert items non-focusable) plus a `shell.scss` style-contract block: references `var(--uc-` at least once, no hex colour literal, no `font-size`/`color` numeric-or-hex hardcode.
- `frontend/src/app/app.routes.ts` -- MODIFY. Add the Shell parent route with `canActivate: [authGuard]` AND `canActivateChild: [authGuard]`; move `feed` under it as a child. Keep public routes and the leading `'' -> redirectTo: 'login'`. `authGuard` import stays.
- `frontend/src/app/app.routes.spec.ts` -- NEW. `RouterTestingHarness` + `provideRouter(routes)` with a stubbed `AuthService.obterToken()`: navigating to `/feed` with no token lands on `/login`; with a token, the Shell + feed render.
- `frontend/src/app/features/feed/feed.ts` -- MODIFY. Drop `sair()`, the `AuthService` / `Router` injections and their imports -> bare `export class Feed {}`.
- `frontend/src/app/features/feed/feed.html` -- MODIFY. Remove `<button ...>Sair</button>`.
- `frontend/src/app/features/feed/feed.spec.ts` -- MODIFY. Remove the "clicar em Sair" test and its `vi` / `localStorage` scaffolding; keep "cria o componente".
- READ-ONLY refs: `frontend/src/app/app.ts` + `app.html` stay `<router-outlet />` (Shell is a child-route layout, not the root); `frontend/src/styles/_tokens.scss` + `tokens.spec.ts` freeze the `--uc-*` set exactly; `frontend/src/app/ui/ui-styles.spec.ts` globs `src/app/ui/**` only, so it does not guard the shell; `EXPERIENCE.md` "Navegação global" table = item/role contract; `DESIGN.md` + `mockups/home-comunidade.html` = sidebar visual contract; `backend/src/main/resources/application.properties` (`smallrye.jwt.path.groups=roles`) + `backend/.../identidade/aplicacao/AuthServiceTest.java` (parses a real emitted token, must `setGroupsPath("roles")`) prove the wire claim name is `roles`.

## Tasks & Acceptance

**Execution:**
- [x] `frontend/src/app/core/auth/auth.service.ts` -- export `JWT_ROLES_CLAIM`, add `perfis()` + `possuiPerfil()` with a UTF-8-safe payload-only JWT decode reading that claim.
- [x] `frontend/src/app/core/auth/auth.service.spec.ts` -- cover parsed / absent / malformed / no-token / multibyte and `possuiPerfil`, fixtures keyed by the imported constant.
- [x] `frontend/src/app/layout/shell/shell.ts` + `shell.html` -- `<nav>` sidebar (role-filtered via `computed()`, inert when routeless) + topbar avatar dropdown (Perfil/Configurações inert, Sair -> logout + `/login`, focus-return on close) + `<router-outlet />`.
- [x] `frontend/src/app/layout/shell/shell.scss` -- sticky sidebar/topbar, `100dvh` fallback, token-only styling, active-item treatment, `:focus-visible` outline, dropdown overlay shadow; deferred dimensions as commented literals.
- [x] `frontend/src/app/layout/shell/shell.spec.ts` -- I/O matrix rows + `shell.scss` style-contract block.
- [x] `frontend/src/app/app.routes.ts` -- Shell parent route with `canActivate` + `canActivateChild: [authGuard]` wrapping `feed`.
- [x] `frontend/src/app/app.routes.spec.ts` -- unauthenticated `/feed` redirects to `/login` over the real route table; authenticated renders the shell.
- [x] `frontend/src/app/features/feed/feed.ts` + `feed.html` + `feed.spec.ts` -- remove the Story 1.6 logout stub and its test.

**Acceptance Criteria:**
- Given an authenticated aluno on any screen under the shell, when it renders, then the sidebar shows exactly the seven common items and the avatar dropdown exposes Perfil, Configurações and Sair, with Denúncias and Solicitações de fixação absent.
- Given an authenticated user whose JWT role claim (`roles`) contains `MODERADOR` or `ADMINISTRADOR`, when the shell renders, then Denúncias and Solicitações de fixação also appear in the sidebar. A token shaped like the backend's real output (perfil under `roles`) drives this, asserted with the shared claim constant.
- Given no token in storage, when the user navigates to `/feed`, then the router redirects to `/login` and the shell never renders, verified over the real `routes` array.
- Given a routeless nav item, when rendered, then it is a non-focusable `aria-disabled` element that does not navigate on click; only "Início" is a link (to `/feed`) and carries `aria-current="page"` when active.
- Given the avatar dropdown is open, when the user presses Escape, clicks outside, or activates an item, then it closes and focus returns to the avatar button; activating Sair calls `AuthService.logout()` and navigates to `/login`.
- Given `cd frontend && npm run build && npm test`, when they run, then both pass, `tokens.spec.ts` / `ui-styles.spec.ts` / all pre-existing specs still pass, and no file under `src/app/` outside `layout/`, `core/auth/`, `app.routes.ts` / `app.routes.spec.ts` and `features/feed/` is modified.

## Spec Change Log

- **2026-08-30 — iteration 1 (step-04 review loopback).**
  - **Triggering finding:** verification-gap + edge-case-hunter — the spec and implementation read the JWT `groups` claim, but the backend sets `smallrye.jwt.path.groups=roles` (`application.properties:31`) and its `AuthServiceTest` must call `setGroupsPath("roles")` to read a real emitted token, so the perfil ships under `roles`, never `groups`. Every test fixture also used `groups`, so all 144 tests passed while `perfis()` would return `[]` for every real user in production and no moderator/admin would ever see Denúncias / Solicitações de fixação.
  - **Amended:** every `groups` -> `roles` across the frozen I/O Matrix, Boundaries, Code Map, Design Notes and ACs; the claim name is now one exported constant (`JWT_ROLES_CLAIM`) shared by `AuthService` and its spec, and an AC/fixture requires a backend-shaped token. Folded in the same-review fixes: parent shell route gets `canActivate` as well as `canActivateChild`; sidebar nav is a `<nav>` landmark, not wrapped in `<aside>`; dropdown returns focus to the avatar button on every close path; `ehPrivilegiado` is a `computed()`; `navigateByUrl(...).catch()`; UTF-8-safe payload decode; `position: sticky` sidebar/topbar with `100dvh` fallback; new `app.routes.spec.ts` covering the auth-guard wiring over the real route table.
  - **Known-bad state avoided:** a shell that ships with the moderation navigation permanently invisible in production while the whole unit suite is green.
  - **KEEP:** the architecture was otherwise sound — child-route layout under a parent `path: ''`; payload-only decode that degrades to "no roles" (so a tampered token can only hide privileged items); routeless items rendered inert, not hidden; token-only SCSS enforced by a style-contract block in `shell.spec.ts`; the I/O-matrix-driven `shell.spec.ts`. Re-derivation should reproduce that shape with the corrections above.

- **2026-08-30 — iteration 2 (step-04 review, patch-only — no re-derivation).** The critical iteration-1 fix (claim name `roles`) verified correct: `npm run build` + 149 tests green, `app.routes.spec.ts` and `auth.service.spec.ts` decode backend-shaped tokens. No high-severity or functional defect found in the second review. Patches applied to the existing implementation:
  - **Claim-constant regression guard:** `auth.service.spec.ts` gains `expect(JWT_ROLES_CLAIM).toBe('roles')` and one decode test whose payload uses a hand-written literal `roles` key (not the imported symbol), so a future edit to the constant or backend config drift is caught — the iteration-1 fixtures all built the token from the same constant they read, so nothing failed if it changed.
  - **`NAV_ITENS` order corrected to EXPERIENCE.md "Navegação global":** Denúncias and Solicitações de fixação sit between "Criar enquete" and "Suas comunidades", not last. The frozen Boundaries names EXPERIENCE.md as the verbatim source for items + visibility; its inline list groups the items by visibility and is a lossy summary — EXPERIENCE.md's row order governs. (Frozen inline list left untouched; flagged for the human at step-05.)
  - **Decode hardening:** a bare-string `roles` claim is read as a one-element list; a payload that parses to an array returns `null` (both degrade safely today, flagged twice across reviews).
  - **A11y / motion:** menu container gets `aria-label` + `id`, avatar gets `aria-controls`; Perfil / Configurações menu items get `aria-disabled="true"` (honouring "inert" while staying valid focusable menu items); dropdown also closes on focus leaving it; `@media (prefers-reduced-motion: reduce)` suppresses the fade. Full APG menu keyboard model and a skip-link are deferred to Story 14.9 (`deferred-work.md`).
  - **Polish:** menu-item hover uses `var(--uc-color-orange-tint)` (visible, consistent with nav items); `:host` sets `line-height: var(--uc-line-height-body)`.
  - **Rejected:** the `canActivate` + `canActivateChild` pair running `authGuard` twice on `/feed` (the guard is side-effect-free and frozen-pinned); `calc(var(--uc-space-unit) * N)` spacing called a hardcode (sanctioned composition pattern, reproduces the mockup's `8px 10px`); `/` -> `/login` for authenticated users (self-heals via `login.ts`). A `**` wildcard/404 route and a shell index redirect were deferred to `deferred-work.md`.

## Design Notes

- Shell as a child-route layout (parent `path: ''` holding `<router-outlet />`) keeps `App` untouched and public screens outside the chrome, extending how the app already separates the guarded `feed` route from public ones. The parent carries both `canActivate` and `canActivateChild` so a direct hit on the parent path is also gated.
- The role gate reads the claim named by `JWT_ROLES_CLAIM` = `roles`, because the backend's `Jwt.claims().groups(...)` builder honours `smallrye.jwt.path.groups=roles` and emits the perfil there. Centralising the name in one constant keeps `AuthService` and its spec from drifting apart (which is exactly how the first iteration passed while being wrong). Decode is payload-only, no signature check: the guard and backend already enforce auth, and any parse failure degrades to "no roles", so a tampered token can only ever hide privileged items, never reveal them. The decode is UTF-8-safe so a multibyte claim value never makes `JSON.parse` throw.
- Routeless items are inert rather than hidden so the full IA is visible from day one (product decision); each becomes an `<a routerLink>` as its epic lands.
- Active-item colours follow `mockups/home-comunidade.html` (`orange-tint` background + `orange` text); maroon stays a stroke/accent only.
- Focus returns to the avatar button whenever the dropdown closes; full arrow-key menu navigation is left to Story 14.9 so this story does not half-implement the ARIA menu pattern.

## Verification

**Commands:**
- `cd frontend && npm run build` -- expected: completes, no Sass or template errors.
- `cd frontend && npm test` -- expected: `shell.spec.ts`, `app.routes.spec.ts`, updated `auth.service.spec.ts` and `feed.spec.ts` pass; `tokens.spec.ts`, `ui-styles.spec.ts`, `app`, `login`, `cadastro`, `confirmar-email` still green.
- `cd frontend && grep -rEn "#[0-9a-fA-F]{3,8}|font-size:\s*[0-9]" src/app/layout --include=*.scss` -- expected: no match.
- `cd frontend && grep -rn "'groups'\|\"groups\"" src/app` -- expected: no match (the role claim is referenced only via `JWT_ROLES_CLAIM`).
- `cd frontend && git status --porcelain` -- expected: only paths under `src/app/layout/`, `src/app/core/auth/`, `src/app/app.routes.ts`, `src/app/app.routes.spec.ts`, `src/app/features/feed/`.

**Manual checks:**
- Seed a token whose `roles` claim is `["ALUNO"]`, then one with `["MODERADOR"]`, load `/feed`, confirm the sidebar item set changes and the avatar dropdown opens and closes by mouse and keyboard, with focus landing back on the avatar after Escape and a visible focus ring on every interactive element.

## Suggested Review Order

**Role gating (the reason for the iteration-1 loopback)**

- Entry point: the claim name is one exported constant, so `AuthService` and its spec cannot silently drift again.
  [`auth.service.ts:25`](../../frontend/src/app/core/auth/auth.service.ts#L25)
- `perfis()` reads that claim; a bare-string claim becomes a one-item list, anything else degrades to `[]`.
  [`auth.service.ts:65`](../../frontend/src/app/core/auth/auth.service.ts#L65)
- Payload-only decode, UTF-8-safe, `null` on any failure (array payloads included) — a tampered token can only hide privileged items.
  [`auth.service.ts:85`](../../frontend/src/app/core/auth/auth.service.ts#L85)
- The sidebar gate: derived once via `computed()`, both privileged roles.
  [`shell.ts:56`](../../frontend/src/app/layout/shell/shell.ts#L56)

**Route wiring / auth boundary**

- Shell mounted as a guarded parent `path: ''` wrapping the authenticated routes; public routes and the leading redirect stay outside.
  [`app.routes.ts:22`](../../frontend/src/app/app.routes.ts#L22)
- Guard behaviour proven over the real `routes` table: no token on `/feed` redirects to `/login`.
  [`app.routes.spec.ts:36`](../../frontend/src/app/app.routes.spec.ts#L36)

**Shell component & template**

- Sidebar is the `<nav>` landmark itself; items come from `NAV_ITENS` in EXPERIENCE.md order, moderation items gated.
  [`shell.ts:20`](../../frontend/src/app/layout/shell/shell.ts#L20)
- "Início" is the only real link (`aria-current="page"` when active); every routeless item is an inert non-focusable `<span>`.
  [`shell.html:27`](../../frontend/src/app/layout/shell/shell.html#L27)
- Dropdown: `aria-controls`/`id`/`aria-label` wired, Perfil/Configurações `aria-disabled`, closes on Escape / outside click / activation / focus-out.
  [`shell.html:56`](../../frontend/src/app/layout/shell/shell.html#L56)
- Close paths: `fecharMenu()` restores focus to the avatar; `aoSairFoco()` closes on Tab-out without stealing focus.
  [`shell.ts:72`](../../frontend/src/app/layout/shell/shell.ts#L72)

**Styling (token-only, sticky, motion)**

- Sticky sidebar/topbar with `100dvh` fallback; every value is a `var(--uc-*)` token or a sanctioned commented literal.
  [`shell.scss:30`](../../frontend/src/app/layout/shell/shell.scss#L30)
- Active-item treatment (`orange-tint` bg + `orange` text) and the `prefers-reduced-motion` guard on the fade.
  [`shell.scss:154`](../../frontend/src/app/layout/shell/shell.scss#L154)

**Peripherals**

- Feed stub reduced to a bare component; logout now lives only in the shell dropdown.
  [`feed.ts:6`](../../frontend/src/app/features/feed/feed.ts#L6)
- Claim-name regression guard: pins `JWT_ROLES_CLAIM` to `'roles'` and decodes a token whose payload uses the literal key.
  [`auth.service.spec.ts:84`](../../frontend/src/app/core/auth/auth.service.spec.ts#L84)
- Shell behaviour specs cover every I/O-matrix row.
  [`shell.spec.ts:47`](../../frontend/src/app/layout/shell/shell.spec.ts#L47)
- `shell.scss` style-contract block: no hex, no numeric `font-size`, `color` only via tokens.
  [`shell.spec.ts:300`](../../frontend/src/app/layout/shell/shell.spec.ts#L300)
