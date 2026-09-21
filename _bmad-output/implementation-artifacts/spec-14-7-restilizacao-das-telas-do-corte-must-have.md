---
title: 'Story 14.7: Restyle the must-have cut screens'
type: 'feature'
created: '2026-09-20'
status: 'done'
review_loop_iteration: 0
baseline_commit: '69671f6e739eaff95edce8ce9bc337c425115f38'
context:
  - '{project-root}/_bmad-output/implementation-artifacts/epic-14-context.md'
  - '{project-root}/_bmad-output/planning-artifacts/ux-designs/ux-unicatolica-2026-08-17/DESIGN.md'
  - '{project-root}/_bmad-output/planning-artifacts/ux-designs/ux-unicatolica-2026-08-17/EXPERIENCE.md'
  - '{project-root}/frontend/src/styles/README.md'
---

<frozen-after-approval reason="human-owned intent — do not modify unless human renegotiates">

## Intent

**Problem:** Stories 14.1/14.2/14.3/14.5 shipped tokens, base components, the shell and toasts, but the three public screens built before Epic 14 still ignore all of it: `login` has no stylesheet at all, `cadastro.scss` and `confirmar-email.scss` are pure hardcoded SCSS, and the global base layer (document reset, body surface, the token font stack) was explicitly deferred to this story by `styles.scss:3` and `_typography.scss:8-11`. Screens built after 14.1 also drifted — `comunidades-lista.scss:64` references an undeclared `var(--lista-ink)`, three stylesheets hardcode `#b3261e` for error text because DESIGN.md declares no error colour, and the Story 14.2 no-hardcode guard only globs `src/app/ui/**`, so nothing outside the component library is checked.

**Approach:** Ship the global base layer, add the one missing palette token (`--uc-color-error`), give the three public screens a shared token-built auth layout, and widen the no-hardcode guard from `src/app/ui/**` to every stylesheet in the app — converting `feed`, `comunidades-lista`, `comunidade-detalhe` and `shell` to token consumption so the widened guard passes. The three cut screens with no code (open-community creation, Artigo, admin panel) are recorded in `deferred-work.md` against the stories that own them, not built here.

## Boundaries & Constraints

**Always:**
- Exactly one new design token: `--uc-color-error: #B3261E` (the value already de-facto hardcoded in three stylesheets; ~5.9:1 on `--uc-color-surface`, AA for normal text). It is added in lockstep to `_tokens.scss`, the `README.md` palette table, `tokens.spec.ts`'s `EXPECTED.colors`, and DESIGN.md's `colors` frontmatter plus a Colors prose bullet marking it an implementation-sourced addition. `tokens.spec.ts` asserts the compiled `--uc-*` set equals `EXPECTED` exactly, so a partial addition is a red test.
- The global base layer lives in a new `src/styles/_base.scss` loaded by `styles.scss` after the token and typography partials. It carries: a `box-sizing: border-box` reset, `margin: 0` on `html, body`, `body` painted with `--uc-color-bg` / `--uc-color-ink` / `--uc-font-family-base` / `--uc-font-size-body` / `--uc-line-height-body`, `font: inherit` on form controls, and one global `:focus-visible` ring matching `button.scss:23-26` (`2px solid var(--uc-color-maroon)`, `outline-offset: 2px`). It declares **no** `:root` block and **no** `--uc-*` custom property — `tokens.spec.ts` extracts the first `:root` rule and compares the token set exactly.
- A shared `UcAuthShell` (`src/app/layout/auth-shell/`, selector `uc-auth-shell`, standalone) renders the `<main>` landmark, the brand header (maroon mark + "UniCatólica", same idiom as `shell.html:7-10`) and a centred card, projecting each screen's own `<h1>` and content. `login`, `cadastro` and `confirmar-email` consume it instead of each re-declaring a centred-card layout.
- Every strong action on the three public screens is `button[uc-button]` (Story 14.2) — it is the system's only strong-action style. Secondary actions ("Reenviar confirmação") are a bordered, transparent, token-only pill, never a second filled colour. At most one orange action per screen.
- Voice and tone follow the EXPERIENCE.md Do/Don't table (closes open Epic-1 action item A-10): drop `"(Story 1.2)"` from `cadastro.html:4`, and keep every error message explaining what to do. Existing API-supplied messages are passed through untouched.
- Accessibility floor on the touched screens (closes open action item A-11): `login` gains the `<main>` landmark and an `<h1>`; every `cadastro` field error is wired with `aria-invalid` and `aria-describedby` pointing at the `.campo-erro` element's `id`; focus stays visible everywhere via the global ring.
- The no-hardcode guard moves to `src/styles/scss-guard.spec.ts` and globs `src/app/**/*.scss` plus `src/styles/*.scss`. It keeps every Story 14.2 rule (hex literal; length literal in a sizing property; non-token `color`/`background`/`background-color`/`border-color`; non-token colour inside `border`/`outline`/`box-shadow`/`fill`/`stroke`; reference to an undeclared `--uc-*`) and adds `font-weight` / `letter-spacing` / `font-family` to the token-only set. It gains two allowances the wider glob requires: a gradient whose colour stops are all `var(--uc-*)` is a valid `background`, and a declaration is exempt when its own line or the line directly above carries `// layout-literal: <reason>` — with a non-empty reason. `src/app/ui/ui-styles.spec.ts` is deleted, superseded by the wider file; the new file's header states that.
- Every deliberate structural literal kept anywhere under `src/app/**` (sidebar `220px`, topbar `64px`, dropdown `200px`, content column max-widths, grid `minmax`) carries a `// layout-literal:` reason. Anything else becomes a token or a `calc()` over `--uc-space-unit`.
- Font weight is `--uc-font-weight-regular` or `--uc-font-weight-semibold`; `--uc-font-weight-caps` is used only with the `label-caps` size (DESIGN.md reserves 700 to that role). `comunidades-lista.scss:63`'s bare `font-weight: 700` becomes semibold.
- Existing test selectors are contract: `.campo-erro` must survive as a class, every `getByLabel` string in `e2e/auth-publico.spec.ts` must still resolve, the login tab order must stay e-mail → senha → Entrar (no focusable element inserted between them), and the `confirmar-email` headings / link texts must stay verbatim.

**Ask First:**
- Any second new `--uc-*` token beyond `--uc-color-error` (an error tint, a focus-ring colour, a disabled token). Error and success notices are built from `surface` background + a 1px token border + token text, so none is needed.
- Removing or recolouring the maroon community header band in `comunidade-detalhe.scss:26`. DESIGN.md's Do/Don't prose forbids maroon behind a block larger than an icon, but the approved mockup `home-comunidade.html:85` ships exactly that band. This story keeps the mockup's behaviour and records the tension.
- Any change to `openapi.yaml` or to backend code.

**Never:**
- Building the open-community creation screen (Story 2.2 owns it), the Artigo screen (Epic 3 — module `publicacoes` is empty and `openapi.yaml` has no endpoint) or the admin course-community pre-creation panel (Story 2.1 — `openapi.yaml:173-176` states the endpoint is out of the current slice). Building a UI against a non-existent contract violates AD-4. Each gets a `deferred-work.md` entry naming its owning story.
- Changing any component's public API: no new `@Input`/`@Output`, no selector rename, no touching `src/app/ui/index.ts` exports beyond adding `UcAuthShell` if it is exported at all.
- Editing `src/app/ui/toast/**`, `auth.service.ts`, `auth.guard.ts`, `comunidades.service.ts`, `usuario.service.ts`, `app.routes.ts`, or any component's TypeScript logic other than `styleUrl` / `imports` / template-bound error-id plumbing.
- Adding a dark-mode or `prefers-color-scheme` block, a CSS framework, an icon library, or `@angular/animations`.
- Responsive breakpoints beyond what already exists (Story 14.4), and systematic WCAG work beyond the two named action items (Story 14.9).

## I/O & Edge-Case Matrix

| Scenario | Input / State | Expected Output / Behavior | Error Handling |
|----------|--------------|---------------------------|----------------|
| Any route renders | app boots | `body` computes to `--uc-color-bg` background, `--uc-color-ink` text and the token font stack; the shell's own `:host` painting is unchanged | N/A |
| `/login` loads | unauthenticated | one `<main>`, one `<h1>`, labels "E-mail"/"Senha" still resolve, submit is `button[uc-button]`, tab order e-mail → senha → Entrar | N/A |
| `/login` submit rejected | API 401 | `role="alert"` message rendered in `--uc-color-error` on a surface card, not a filled red block | message text unchanged |
| `/cadastro` submitted empty | all 5 controls invalid + touched | 5 `.campo-erro` elements, each with an `id`; each input carries `aria-invalid="true"` and `aria-describedby` = that id | no navigation |
| `/cadastro` succeeds | API 201 | "Verifique seu e-mail" state: `role="status"` notice bordered in `--uc-color-green-ok`, the e-mail echoed, and the resend action as a secondary pill | N/A |
| `/confirmar-email` without token | no `token` query param | heading "Não foi possível confirmar" + "Voltar ao cadastro" link, token-styled | N/A |
| A stylesheet under `src/app/**` gains a hex, a raw sizing length, a non-token colour or an unknown `--uc-*` | `npm test` | `scss-guard.spec.ts` fails naming the file and the offending declaration | N/A |
| A declaration carries `// layout-literal: <reason>` | `npm test` | that one declaration is exempt; an empty or missing reason is still a failure | N/A |

</frozen-after-approval>

## Code Map

**Token + global base layer**

- `frontend/src/styles/_tokens.scss:30` -- MODIFY. Add `--uc-color-error: #B3261E` to the palette block, with the same comment idiom as its neighbours.
- `frontend/src/styles/_base.scss` -- NEW. Reset, `body` painting, `font: inherit` on controls, global `:focus-visible` ring. No `:root`, no custom property.
- `frontend/src/styles.scss:5-6` -- MODIFY. Add `@use './styles/base';` after the two existing partials; update the header comment that currently says the reset "is Story 14.7".
- `frontend/src/styles/README.md:84-93,53-54,68-72` -- MODIFY. Palette table row for `--uc-color-error`, a "Camada base" section, and correct the two paragraphs that describe the base layer and the font stack as still pending.
- `frontend/src/styles/tokens.spec.ts:30` -- MODIFY. Add `'--uc-color-error': '#B3261E'` to `EXPECTED.colors`; the exact-set assertion at `tokens.spec.ts` "token set" and the README drift guard then cover it automatically.
- `frontend/src/styles/base.spec.ts` -- NEW. Compiles `styles.scss` with `sass` (same pattern as `tokens.spec.ts:145-158`) and asserts the `body` rule exists with token-only values, the `:focus-visible` rule exists, and no hex appears outside the `:root` block.
- `_bmad-output/planning-artifacts/ux-designs/ux-unicatolica-2026-08-17/DESIGN.md:17,84` -- MODIFY. `colors.error: '#B3261E'` in frontmatter + a Colors bullet marking it added during Story 14.7 implementation (the "Ask First" escalation `README.md:36-41` prescribes).

**Guard**

- `frontend/src/styles/scss-guard.spec.ts` -- NEW. Supersedes and widens the Story 14.2 guard. Port the rules from `frontend/src/app/ui/ui-styles.spec.ts:66-109` verbatim, re-implement `parseDeclarations` line-aware (the current `split(/[;{}]/)` at line 59 discards line numbers, which the `// layout-literal:` exemption needs), add the font-property rule and the all-token gradient allowance.
- `frontend/src/app/ui/ui-styles.spec.ts` -- DELETE. Fully subsumed.

**The three cut screens**

- `frontend/src/app/layout/auth-shell/auth-shell.ts` / `.html` / `.scss` / `.spec.ts` -- NEW. `uc-auth-shell`: `<main>` + brand header + centred card. Brand mark mirrors `frontend/src/app/layout/shell/shell.scss:48-58`.
- `frontend/src/app/features/identidade/login/login.html` -- MODIFY. Wrap in `uc-auth-shell`, add `<h1>`, keep label text and control order, submit becomes `button uc-button`, "Criar conta" link placed after the submit so the e2e tab order at `e2e/auth-publico.spec.ts:39-51` still holds.
- `frontend/src/app/features/identidade/login/login.scss` -- NEW; `login.ts:6-10` -- MODIFY (add `styleUrl`, import `UcAuthShell`, `UcButton`, `RouterLink`).
- `frontend/src/app/features/identidade/login/login.spec.ts` -- MODIFY. Add `<main>` / `<h1>` / `uc-button` assertions; existing structural queries at lines 38-85 must keep passing untouched.
- `frontend/src/app/cadastro/cadastro.html` -- MODIFY. `uc-auth-shell`, drop `"(Story 1.2)"` at line 4, per-field `.campo-erro` ids + `aria-invalid` / `aria-describedby`, `uc-button` submit, secondary pill for resend.
- `frontend/src/app/cadastro/cadastro.scss` -- REWRITE to tokens (currently 139 lines, every value hardcoded).
- `frontend/src/app/cadastro/cadastro.ts:33-38` -- MODIFY. Component `imports` only.
- `frontend/src/app/cadastro/cadastro.spec.ts` -- MODIFY. Add the aria wiring assertions from the I/O matrix.
- `frontend/src/app/confirmar-email/confirmar-email.html` / `.scss` / `.ts:14-19` / `.spec.ts` -- MODIFY. Same treatment; headings and link texts asserted at `e2e/auth-publico.spec.ts:86-112` are verbatim contract.

**Token conversion so the wider guard passes**

- `frontend/src/app/features/feed/feed.scss:15,9,40,79,93,106,113,104-105,44-45` -- MODIFY. `#b3261e` → `var(--uc-color-error)`; raw `rem`/`px` in padding/gap/margin → `calc()` over `--uc-space-unit` or a spacing token; the `36px` avatar and the `minmax(200px, …)` grid keep their literals behind `// layout-literal:` reasons; the orange→maroon avatar gradient stays (all stops are tokens, allowed by the new gradient rule).
- `frontend/src/app/features/comunidades/comunidades-lista/comunidades-lista.scss:64,63,41,18,26,47,48,58,59,75,97,106` -- MODIFY. `var(--lista-ink)` (undeclared — a real defect the guard's unknown-token rule now catches) → `var(--uc-color-ink)`; `font-weight: 700` → semibold; `#b3261e` → error token; raw lengths → tokens or annotated literals.
- `frontend/src/app/features/comunidades/comunidade-detalhe/comunidade-detalhe.scss:13,41,57-58,72-73` -- MODIFY. `#b3261e` → error token; the two `rgba(255,255,255,…)` values on `.detalhe__botao-sair` → `1px solid var(--uc-color-surface)` on a transparent background; the `opacity: .85` subtitle and the negative-margin bleed keep their literals with reasons. The maroon header band at line 26 is kept as-is per Boundaries.
- `frontend/src/app/layout/shell/shell.scss:15,33-34,17-18,142,176` -- MODIFY, comments only. Convert the existing prose annotations ("~220px sidebar (DESIGN.md …, deferred)") into `// layout-literal:` markers the guard recognises. No visual change.
- `frontend/src/app/ui/**/*.scss` -- READ-ONLY apart from a `// layout-literal:` marker if the ported guard flags a literal the old one allowed. The ported rules already allow `1px`/`2px` borders/outlines, `0` and unitless `opacity`, so none is expected.

**e2e**

- `frontend/e2e/auth-publico.spec.ts:6` -- MODIFY. The header comment says "restyle 14.7 fora de escopo" and is now stale. Add one assertion per public route that `body`'s computed background is the `--uc-color-bg` value, proving the base layer reaches a real browser.

**Read-only references**

- `frontend/src/app/ui/card/card.scss`, `button/button.scss:23-33`, `badge/badge.scss`, `member-indicator/member-indicator.scss` -- the token idiom to copy.
- `_bmad-output/.../mockups/home-comunidade.html:37,85` -- brand mark and community header band precedent.
- `frontend/e2e/auth-fluxo.spec.ts:18-23,43` -- login selectors and the exact alert text that must survive.

## Tasks & Acceptance

**Execution:**
- [x] `frontend/src/styles/_tokens.scss` + `tokens.spec.ts` + `README.md` + `DESIGN.md` -- add `--uc-color-error` in lockstep across all four -- `tokens.spec.ts` compares the compiled token set and the README table exactly, so these four move together or the suite is red.
- [x] `frontend/src/styles/_base.scss` + `frontend/src/styles.scss` -- ship the global base layer and wire it in -- the piece 14.1 explicitly deferred here; without it the three public screens never get the token font or canvas.
- [x] `frontend/src/styles/base.spec.ts` -- compiled-CSS contract for the base layer -- proves `body` and the global focus ring are token-only and that no hex leaked outside `:root`.
- [x] `frontend/src/styles/scss-guard.spec.ts` + delete `frontend/src/app/ui/ui-styles.spec.ts` -- widen the no-hardcode guard to every app stylesheet, line-aware, with the `// layout-literal:` escape hatch -- this is what makes "no hardcoded value anywhere" enforceable instead of aspirational.
- [x] `frontend/src/app/layout/auth-shell/` (4 files) -- the shared `<main>` + brand + card layout for the public screens -- three copies of the same centred-card SCSS is the drift this story exists to stop.
- [x] `frontend/src/app/features/identidade/login/` (html, new scss, ts, spec) -- restyle the only screen with zero stylesheet; add `<main>` + `<h1>` (action item A-11).
- [x] `frontend/src/app/cadastro/` (html, scss, ts, spec) -- token rewrite, microcopy fix (A-10), aria wiring (A-11), including the post-signup "Verifique seu e-mail" state.
- [x] `frontend/src/app/confirmar-email/` (html, scss, ts, spec) -- token rewrite of the e-mail-link landing screen, headings and links verbatim.
- [x] `frontend/src/app/features/feed/feed.scss`, `comunidades-lista.scss`, `comunidade-detalhe.scss` -- convert to tokens, fixing the undeclared `var(--lista-ink)` and the three hardcoded error reds -- required for the widened guard to pass.
- [x] `frontend/src/app/layout/shell/shell.scss` -- convert the existing deferred-dimension comments into `// layout-literal:` markers -- comment-only, no visual change.
- [x] `frontend/e2e/auth-publico.spec.ts` -- drop the stale scope comment, assert the base layer's computed body background on each public route.
- [x] `_bmad-output/implementation-artifacts/deferred-work.md` -- append four entries: the three unbuilt cut screens against Stories 2.2 / Epic 3 / 2.1, and the maroon-header-band tension between DESIGN.md prose and the approved mockup.

**Acceptance Criteria:**
- Given `cd frontend && npm run build && npm test`, when they run, then both pass with `tokens.spec.ts`, `base.spec.ts`, `scss-guard.spec.ts` and every pre-existing spec green.
- Given `npx playwright test` against the dev server, when the public-route suites run, then every previously passing assertion in `auth-publico.spec.ts`, `auth-fluxo.spec.ts`, `shell-*.spec.ts` and `toast.spec.ts` still passes.
- Given any stylesheet under `frontend/src/app/**`, when it is inspected, then it contains no hex literal, no raw sizing length without a `// layout-literal:` reason, and no `var(--uc-*)` reference to a token `_tokens.scss` does not declare.
- Given the restyled Login, Cadastro and Verifique-seu-e-mail screens, when they render, then no screen uses `--uc-color-maroon` as a dominant background, each shows at most one `button[uc-button]` orange action, and every colour, font and spacing value resolves through a `--uc-*` token.
- Given the three cut screens that have no code, when this story closes, then none was built and each has a `deferred-work.md` entry naming the story that owns it.

## Spec Change Log

- **Matrix audit, post-implementation.** Three matrix rows were only partly covered and were closed before review:
  - The "raw sizing length" row was false for percentages. The guard ported Story 14.2's `LENGTH_LITERAL`, whose trailing `\b` can never match after `%` (both `%` and the following `;` are non-word characters), so `width: 100%` slipped past every rule. The trailing `\b` is now `(?![\w%-])`, and the five percentage declarations this exposed (`shell.scss` ×3, `comunidade-detalhe.scss` ×1, all structural) carry `// layout-literal:` reasons. Verified by removing one marker and watching the guard fail with the file and line.
  - The "guard fails naming the offender" row was verified only by absence. `scss-guard.spec.ts` gained five detector tests proving `HEX`, `LENGTH_LITERAL` (percentages included), the font rule, the unknown-token set and the shorthand-colour rule actually fire.
  - The `/login` 401 row asserted the message text but nothing about its treatment. `login.spec.ts` gained a `login.scss` style-contract block pinning error text + 1px error border on a `surface` fill (never a filled red block) and asserting the screen never paints maroon as a background.
- **Three assertions removed from `tokens.spec.ts`** (no `body{}`, no `*{}`, class-or-`:root` selectors only). They existed solely to guard that Story 14.1 had *not* shipped the base layer; 14.7 ships it, and `base.spec.ts` now owns that contract. The single-`:root`, no-token-outside-`:root` and no-dark-mode assertions survive.
- **`cadastro` now replaces the form with the success state** rather than showing a notice above a reset form. This realises the matrix row that names "Verifique seu e-mail" a state of its own, as the EXPERIENCE.md surface map does. Covered by a new unit test; no e2e mock for a successful signup exists, so that state has unit coverage only.
- **feed's 36px avatar became `calc(var(--uc-space-unit) * 9)`** rather than an annotated literal: the Boundaries enumerate which literals may stay, and an avatar is not among them. `shell.scss` already sizes its own avatar that way.
- **Review loop 1 (blind-hunter + edge-case-hunter + verification-gap): 14 patches, 12 deferrals, no intent or spec defect.** The patches, all re-verified by breaking the target and watching the named assertion fail:
  - *The escape hatch was too wide.* `// layout-literal:` exempted a declaration from **every** rule, so a hex under a marker passed. The AC scopes it to sizing lengths, so it now waives the SIZE_PROP rule only; a trailing marker covers just its own line instead of licensing the next; the glob picked up `src/styles.scss` (the build entry point, previously outside both globs); `declaredTokens` now blanks comments first; and the property/unit lists gained `border-*` shorthands, logical box properties, `outline-color`/`caret-color`/`accent-color`, the `font` shorthand, `flex-basis` and the `pt|pc|ch|ex|cm|mm|in|q` plus `dv*`/`sv*`/`lv*` units. `border-bottom: 1px solid red` was reachable before this and is not now.
  - *Two accessibility regressions this story introduced.* The new `::placeholder { color: var(--uc-color-ink-faint) }` gave 2.57:1 where the UA default had been compliant, on placeholders that carry real examples — now `--uc-color-ink-soft` (5.36:1). The new global maroon focus ring is 1.00:1 on `comunidade-detalhe`'s maroon header band — `.detalhe__botao-sair` now overrides it with `--uc-color-surface` (10.20:1), pinned by a style-contract block that also pins the button's restyle.
  - *Four tests that could not observe what they claimed to guard*, each demonstrated by deleting the target with the suite green: nothing asserted `angular.json` still lists `src/styles.scss` (the entire base layer could ship unwired); nothing bounded which element selectors the base layer may carry, so a global `a {}` restyled every link unnoticed; the `/login` 401 notice could lose `class="login__erro"`; and `cadastro`'s whole `@if (erro())` block could be deleted, since only the signal was asserted. All four now have assertions.
  - *Factual.* DESIGN.md's contrast figure for `--uc-color-error` was wrong (written as ~5.9:1; actually 6.54:1 on `surface`, 6.25:1 on `bg`).
- **Prettier scope corrected against the evidence.** The six pre-existing files flagged by `prettier --check` were verified to fail at `baseline_commit` too, so they are untouched repo-wide drift (deferred with the missing `format` script and CI gate). Only `scss-guard.spec.ts`, created by this story, was newly unformatted and was formatted.

## Design Notes

- **One token, not three.** An error needs a text colour; an error *notice* would normally also want a tint and a border colour. Building notices as `surface` background + 1px token border + token text (the same "border, never shadow" rule `card.scss` follows) keeps the addition to a single palette entry and avoids the filled-red block DESIGN.md's Do/Don't warns against.
- **`// layout-literal:` over a blanket exemption.** The Story 14.2 guard could forbid every raw length because `ui/` components are pure primitives. App screens legitimately need a `220px` sidebar and a `720px` reading column — DESIGN.md itself writes those as "~" and defers them. An annotated, reason-bearing opt-out keeps them visible and auditable instead of either failing the build or silently widening the rule.
- **Deleting `ui-styles.spec.ts` is not a coverage loss.** The new file applies the same rules to a strict superset of files. Keeping both would mean two copies of the parser drifting apart.
- **The maroon band stays.** `comunidade-detalhe` is not one of this story's six screens, and its band comes straight from the direction the user picked. Recolouring it is a DESIGN.md decision, so it is recorded rather than silently changed.

## Verification

**Commands:**
- `cd frontend && npm run build` -- expected: completes, no Sass or template error.
- `cd frontend && npm test` -- expected: all green, including the three token/style specs.
- `cd frontend && grep -rEn "#[0-9a-fA-F]{3,8}" src/app --include=*.scss` -- expected: no match.
- `cd frontend && grep -rn "lista-ink" src` -- expected: no match.
- `cd frontend && npx playwright test` -- expected: every suite passes.
- `cd /home/taques/Projetos/unicatolica && git status --porcelain` -- expected: no path under `backend/`, and `openapi.yaml` unchanged.

**Manual checks:**
- Load `/login`, `/cadastro` and `/confirmar-email` in a browser: near-white `#FAFAF8` canvas, a single centred card, the system font stack applied, one orange action per screen, no maroon block bigger than the brand mark.
- Tab through `/login` and `/cadastro`: every control shows the maroon focus ring.
- Submit `/cadastro` empty: five inline errors in the error red, each announced through `aria-describedby`.
