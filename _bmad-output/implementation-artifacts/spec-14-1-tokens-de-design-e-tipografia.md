---
title: 'Story 14.1: Design tokens and typography'
type: 'feature'
created: '2026-08-30'
status: 'done'
review_loop_iteration: 1
baseline_commit: '46b549d9596f7fd33899161b9a50aba743468f03'
context:
  - '{project-root}/_bmad-output/implementation-artifacts/epic-14-context.md'
  - '{project-root}/_bmad-output/planning-artifacts/ux-designs/ux-unicatolica-2026-08-17/DESIGN.md'
---

<frozen-after-approval reason="human-owned intent — do not modify unless human renegotiates">

## Intent

**Problem:** The Angular frontend has no design system. `src/styles.scss` is empty and existing screens use per-component SCSS with hardcoded hex, px and font values. Every later Epic 14 story and every feature epic needs one shared, non-hardcoded source for the "Campus Clean" palette, type scale, spacing and radius.

**Approach:** Add a global token layer as CSS custom properties on a single bare `:root` (the source of truth), values copied verbatim from `DESIGN.md`, plus type-role utility classes (`.uc-text-*`) composed only from those tokens, and a developer usage doc. No global element styles, `body` restyle or CSS reset ship here, and no existing screen is restyled: both are Story 14.7.

## Boundaries & Constraints

**Always:**
- Token values copied verbatim from `DESIGN.md` frontmatter (`colors`, `typography`, `rounded`, `spacing`). Names mirror `DESIGN.md` roles (e.g. `--uc-radius-md` = `rounded.md` = 12px) with no translation layer.
- All custom properties namespaced `--uc-*`, declared once under a bare `:root` in `frontend/src/styles/_tokens.scss`.
- Utility classes reference `var(--uc-*)` only: no hardcoded design value outside the `:root` declarations in `_tokens.scss`.
- `--uc-shadow-overlay` is the one token sourced from `DESIGN.md` prose (section "Elevation & Depth": `0 8px 24px rgba(0,0,0,.08)`) rather than frontmatter; 14.1 canonicalizes the value to `0 8px 24px rgba(0, 0, 0, 0.08)`.
- `styles.scss` stays the only build entry point; new partials live in `frontend/src/styles/` and load via `@use`.

**Ask First:**
- Adding a runtime CSS framework or `@angular/material`.
- Adding a second parallel token source (Sass `$` variables or maps).
- Adding a numeric `--uc-space-{n}` step ramp (`DESIGN.md` `spacing` defines only `unit` plus three named steps, no scale).
- Adding dark-mode / `prefers-color-scheme` overrides (dark mode is out of the MVP per `DESIGN.md`).

**Never:**
- Editing any file under `frontend/src/app/**` (screen migration to tokens is Story 14.7).
- Implementing component tokens/classes for badge, primary button, member indicator or card (Story 14.2).
- Using `maroon` as a screen background or default surface token.

## I/O & Edge-Case Matrix

| Scenario | Input / State | Expected Output / Behavior | Error Handling |
|----------|--------------|---------------------------|----------------|
| Runtime token lookup | Element reads `var(--uc-color-orange)` | Resolves to `#EA6A2E` (the `DESIGN.md` value) | N/A |
| Every `DESIGN.md` value present | Compiled `styles.scss` | One `--uc-*` custom property per colour (10: bg, surface, border, ink, ink-soft, ink-faint, maroon, orange, orange-tint, green-ok), per type role (greeting/question/body/meta/label-caps: font-size always, plus `--uc-line-height-body` and letter-spacing for greeting + label-caps), per spacing entry (4: unit, card-padding, section-gap, page-margin) and per radius (5: sm, default, md, lg, full), each carrying the `DESIGN.md` value; plus `--uc-shadow-overlay` | N/A |
| Token drifts from `DESIGN.md` | A `--uc-*` value edited to differ, or the `README.md` table out of sync with `_tokens.scss` | `tokens.spec.ts` fails, naming the token (value drift) or reporting the README mismatch (doc drift) | Test asserts name+value against a `DESIGN.md`-mirroring table and against the README table |
| Consumer needs a value absent from `DESIGN.md` | Story 14.2 / 14.7 needs e.g. focus-ring, overlay scrim, disabled state | No `--uc-*` token exists; the consuming story raises it as "Ask First" | Escalation, not a silent literal |
| Sass compile error | `_tokens.scss` / `_typography.scss` malformed | `npm run build` and `tokens.spec.ts` both fail | Sass error surfaced by build |

</frozen-after-approval>

## Code Map

- `frontend/src/styles.scss` -- MODIFY. Placeholder comment today. Becomes the aggregator: `@use './styles/tokens';` + `@use './styles/typography';`.
- `frontend/package.json` -- MODIFY. Add `sass` (`~1.101.0`, tilde not caret) and `@types/node` (`~24`, matching CI Node 24) to `devDependencies`. `sass` resolves only transitively via `@angular/build` today; the spec compiles `styles.scss` with it directly. `@types/node` lets `tokens.spec.ts` use `fs` / `process` with real types (see below).
- `frontend/tsconfig.spec.json` -- MODIFY. Add `"types": ["node"]` (merged with the existing `"vitest/globals"`) so the spec reads files without obfuscated `import('node:'+...)` / `globalThis as any` accessors.
- `frontend/src/styles/_tokens.scss` -- NEW. Single `:root` block; every `--uc-*` custom property (colour, typography, spacing, radius, shadow). Source of truth, values verbatim from `DESIGN.md`.
- `frontend/src/styles/_typography.scss` -- NEW. One utility class per type role (`.uc-text-greeting|question|body|meta|label-caps`), each composed only from `var(--uc-*)`. No global element selectors, no `body` restyle, no `box-sizing` / margin reset (those are Story 14.7).
- `frontend/src/styles/README.md` -- NEW. Token table (name -> value -> `DESIGN.md` role), the "consume via `var(--uc-*)`, never hardcode" rule, "dark mode out of scope", a minimal application example, the `.uc-text-*` vs component-class guidance, and the `DESIGN.md` note that the system font stack is a pending team decision (a brand font is a possible later revision). The table is guarded against drift by `tokens.spec.ts`.
- `frontend/src/styles/tokens.spec.ts` -- NEW. Compiles `src/styles.scss` with the `sass` package (`{ style: 'expanded' }`), locating the styles dir via `import.meta.url`. Asserts, from the compiled CSS: each `--uc-*` name+value against a `DESIGN.md`-mirroring `EXPECTED` table; the token set is exactly `EXPECTED` (no extra, none missing); the `README.md` token table matches; the five `.uc-text-*` rules exist and every declaration value in them is a `var(--uc-*)` reference (no literal); the compiled output has no bare-element / `body` / `box-sizing` rule. No source-text fallback: any `sass` import failure, compile throw, or empty `:root` is a test failure.
- `frontend/src/app/**/*.scss` -- READ-ONLY. `cadastro.scss` / `confirmar-email.scss` hold pre-epic hardcoded values; untouched here, handed to Story 14.7.
- `frontend/src/app/app.spec.ts` -- READ-ONLY. Vitest pattern reference (`vitest/globals`, `describe`/`it`/`expect`).
- `.github/workflows/ci.yml` -- READ-ONLY. Runs `ng build` + `ng test` (`--watch=false`); the new spec runs there with no config change.
- `_bmad-output/planning-artifacts/ux-designs/ux-unicatolica-2026-08-17/DESIGN.md` -- READ-ONLY. Canonical values in frontmatter `colors` / `typography` / `rounded` / `spacing`; `--uc-shadow-overlay` comes from the prose section "Elevation & Depth".

## Tasks & Acceptance

**Execution:**
- [x] `frontend/package.json` -- add `sass` `~1.101.0` and `@types/node` `~24` to `devDependencies`.
- [x] `frontend/tsconfig.spec.json` -- set `compilerOptions.types` to `["vitest/globals", "node"]`.
- [x] `frontend/src/styles/_tokens.scss` -- create the `:root` token block:
  - 10 palette colours, hex verbatim from `DESIGN.md` `colors` in uppercase: `--uc-color-bg` #FAFAF8, `--uc-color-surface` #FFFFFF, `--uc-color-border` #EAEAE6, `--uc-color-ink` #1C1C1A, `--uc-color-ink-soft` #6B6B66, `--uc-color-ink-faint` #A2A29C, `--uc-color-maroon` #7A1F2B, `--uc-color-orange` #EA6A2E, `--uc-color-orange-tint` #FDEEE6, `--uc-color-green-ok` #3A7D5C.
  - Type scale: `--uc-font-family-base` (system stack, verbatim); `--uc-font-size-greeting` 22px / `-question` 15px / `-body` 13.5px / `-meta` 12px / `-label-caps` 10.5px; `--uc-font-weight-regular` 400 / `-semibold` 600 / `-caps` 700; `--uc-line-height-body` 1.5 (only `body` has one in `DESIGN.md`); `--uc-letter-spacing-greeting` -0.01em / `-label-caps` 0.05em (only these two have one).
  - Spacing, verbatim from `DESIGN.md` `spacing` (no numeric ramp): `--uc-space-unit` 4px, `--uc-space-card-padding` 18px, `--uc-space-section-gap` 20px, `--uc-space-page-margin` 32px.
  - Radius: `--uc-radius-sm` 6px / `-default` 8px (`DESIGN.md` key `DEFAULT`) / `-md` 12px / `-lg` 14px / `-full` 9999px.
  - `--uc-shadow-overlay: 0 8px 24px rgba(0, 0, 0, 0.08)` (canonicalized from `DESIGN.md` prose `rgba(0,0,0,.08)`).
- [x] `frontend/src/styles/_typography.scss` -- `.uc-text-{greeting,question,body,meta,label-caps}` utility classes, `var(--uc-*)` only. No global element selectors, no `body` restyle, no reset (Story 14.7). Each class sets exactly the properties `DESIGN.md` defines for that role: font-size + font-weight for every role, plus line-height for `body` and letter-spacing for `greeting` and `label-caps`. Do not add `text-transform`, `font-family`, or any property `DESIGN.md` does not list for the role.
- [x] `frontend/src/styles.scss` -- replace the placeholder comment with `@use './styles/tokens';` + `@use './styles/typography';`.
- [x] `frontend/src/styles/README.md` -- token reference (name -> value -> `DESIGN.md` role), "consume via `var(--uc-*)`, never hardcode" rule, "value absent from `DESIGN.md` = Ask First in the consuming story" rule, dark-mode-out-of-scope note, a minimal application snippet (e.g. `<h1 class="uc-text-greeting">`), `.uc-text-*` vs component-class guidance, pointer to Stories 14.2 / 14.7, and a one-line note that `DESIGN.md` marks the system font stack as a pending team decision.
- [x] `frontend/src/styles/tokens.spec.ts` -- Vitest spec, compiled-Sass path only (no source-text fallback):
  - In `beforeAll`: resolve the styles dir from `import.meta.url`; `sass.compile('<dir>/../styles.scss', { style: 'expanded' })`; parse `--uc-*` declarations out of the `:root` block (brace-balanced, not a non-greedy match). A thrown `sass` import, a compile throw, or zero `--uc-*` found is an unhandled failure — do not catch-and-degrade.
  - `EXPECTED`: a `Record<group, Record<name, value>>` mirroring `DESIGN.md` (`colors` / `typography` / `rounded` / `spacing`) plus `--uc-shadow-overlay`. `describe` per group, `it` per token asserting name present + value equal (whitespace-normalized; hex compared in canonical uppercase).
  - `it`: the compiled `--uc-*` set equals `EXPECTED`'s key set exactly (dedupe-safe).
  - `it`: the `README.md` token table (`| \`--uc-...\` | \`value\` |` rows) has exactly `EXPECTED`'s keys and each row's value equals the compiled value.
  - `it`: the compiled CSS contains rules for all five `.uc-text-*` classes, and every declaration inside them has a value matching `var(--uc-[a-z0-9-]+)` (no literal colour/length/number).
  - `it`: the compiled CSS contains no `body{...}`, no `* {...}`, no `box-sizing` declaration, and no bare element-type selector rule (guards the "no reset / no `body` restyle" boundary against future regressions in either partial).

**Acceptance Criteria:**
- Given a component SCSS or template anywhere in `frontend/src/app`, when it needs a colour/font/spacing/radius value defined in `DESIGN.md`, then a matching `--uc-*` token exists and is documented in `frontend/src/styles/README.md`, with no new hardcoded value required. A value absent from `DESIGN.md` (focus-ring, overlay scrim, disabled state) is an "Ask First" escalation in the consuming story, not a hardcode.
- Given the token and typography layer, when reviewed, then it adds no hardcoded colour/font/spacing value outside the `:root` declarations in `_tokens.scss`, ships no global element style / `body` restyle / reset, and modifies no file under `frontend/src/app/`. `tokens.spec.ts` verifies the `.uc-text-*` classes reference only `var(--uc-*)` and that the compiled output carries no reset / `body` rule, so this criterion is machine-checked, not manual.
- Given `npm run build` and `npm test` are run in `frontend/`, when they finish, then both pass; `tokens.spec.ts` runs on the compiled-Sass path (there is no source-text fallback) and fails if `styles.scss` compiles to zero `--uc-*` custom properties or if either style partial is malformed.

## Spec Change Log

- 2026-08-30 (advanced-elicitation pass, pre-approval): spacing ramp `--uc-space-1..8` removed (not in `DESIGN.md`), replaced by `--uc-space-unit` plus the three named steps; `_typography.scss` scoped to `.uc-text-*` utilities only, with the global reset / `body` restyle moved to Story 14.7; `sass` added as an explicit `devDependency`; `tokens.spec.ts` gains a README-drift assertion, whitespace normalization, canonical-uppercase hex and assertion-path logging; all `--uc-*` names and values enumerated in Tasks; AC1 narrowed to values defined in `DESIGN.md`; `--uc-shadow-overlay` provenance (`DESIGN.md` prose) documented.
- 2026-08-30 (review loop 1, bad_spec): **Trigger** -- review found `tokens.spec.ts` caught every `sass.compile` error (not just an import failure) and silently degraded to a source-text scan of `_tokens.scss`, so a malformed partial or a `styles.scss` that stops exposing the tokens still passed `npm test` -- contradicting the frozen I/O-matrix row "Sass compile error -> `tokens.spec.ts` fails". Review also found `_typography.scss` and the `styles.scss` `@use` wiring had zero automated coverage, leaving AC2 ("utility classes reference `var(--uc-*)` only", "no reset / `body` restyle") verifiable only by manual `grep`. **Amended** (non-frozen sections): the source-text fallback is removed -- `tokens.spec.ts` is compiled-Sass only and a compile throw / empty `:root` is a hard failure; `sass.compile` is pinned to `{ style: 'expanded' }`; the styles dir is resolved via `import.meta.url`; new assertions cover the five `.uc-text-*` classes (must be `var(--uc-*)`-only) and the absence of any reset / `body` / `box-sizing` / bare-element rule in the compiled output; `@types/node` + a `tsconfig.spec.json` `types` entry replace the obfuscated Node accessors; `sass` range tightened `^` -> `~`; README gains an application example and the `DESIGN.md` "system stack is a pending decision" caveat; the `_typography.scss` task's self-contradiction (`.uc-text-meta` "font-size only" vs the Design Notes example showing size+weight) is resolved in favour of `DESIGN.md`, which defines a weight for every role. **Known-bad state avoided:** a regression that drops the token layer from the production bundle, or malforms a style partial, passing CI green. **KEEP:** the `_tokens.scss` `:root` block exactly as built in loop 0 -- 32 tokens, values verbatim from `DESIGN.md` in canonical uppercase, grouped with comments, single bare `:root`, `// prettier-ignore` (all 35 loop-0 token assertions passed); the five `.uc-text-*` classes as built (each already `var(--uc-*)`-only and limited to `DESIGN.md` properties); `styles.scss` two-line `@use` aggregator + header comment; `README.md` token tables, the never-hardcode / Ask-First rules, the dark-mode note and the `.uc-text-*`-vs-component guidance; `tokens.spec.ts`'s `EXPECTED` table, per-token `it`s, exact-set check, README-drift `it`, and the whitespace-normalize + canonical-uppercase-hex helpers.

- 2026-08-30 (review loop 1, patches -- no code-reverting loopback): review found no `bad_spec`/`intent_gap`; the following were auto-fixed in `tokens.spec.ts` and the spec's non-frozen `## Verification` section. **`tokens.spec.ts`**: the `.uc-text-*` test now asserts the *exact* declaration map per role (`ROLE_DECLS`) -- a dropped `font-weight`, a stray `letter-spacing`, or an added `font-family` / `text-transform` now fails; every `var(--uc-*)` referenced by a utility class must resolve against the compiled `:root` set (catches a token-name typo); a new assertion pins exactly one `:root` block and forbids any `--uc-*` declaration outside it; a new assertion forbids `@media` / `prefers-color-scheme` in the compiled output. **`README.md`**: added a note that `.uc-text-*` carry no `font-family` until Story 14.7. **`package.json`**: `@types/node` `~22` -> `~24` to match CI's Node 24. **`## Verification`**: the `maroon` / reset `grep` commands are scoped to `src/styles/*.scss` (they were matching `tokens.spec.ts` assertion regexes and README prose). Deferred (see `deferred-work.md`): parse `DESIGN.md` YAML in the drift guard; px-vs-rem type scale; link the README from a discoverable entry point. Test count 64 -> 66, all green; `npm run build` clean.

- 2026-08-30 (review loop 1, re-run of the two layers that had failed on a rate limit): verification-gap ran 9 mutation tests and reported **no verification gaps** (the loop-0 silent-degrade bug confirmed gone). edge-case-hunter raised four `tokens.spec.ts` robustness nits; three patched: strip a leading `@charset` before locating `:root`; the no-reset guard now rejects `@font-face` / `@keyframes` / `@supports` / `@import` too, not just `@media`; the README-row regex tolerates 0-3 leading spaces. Also fixed spec-internal staleness left by the prior patch round (`@types/node` `~22` -> `~24` in `## Verification` and Code Map). One nit rejected (a token value containing a literal `;` -- pathological). Test count unchanged at 66, all green.

## Design Notes

- Naming mirrors `DESIGN.md` to avoid a translation layer (an AGENTS.md principle). `DESIGN.md` `rounded.md` is 12px: keep it as `--uc-radius-md` so Story 14.2's card (`rounded.md`) reads that token. The `DESIGN.md` `rounded` key `DEFAULT` (Tailwind casing) is lowercased to `--uc-radius-default` (value 8px); `full` is `9999px`.
- Once merged, every `--uc-*` name is an API consumed by Stories 14.2 / 14.7 and later feature epics; renaming one later is a cross-story break. The name list is the primary deliverable of 14.1, ahead of the values. It also freezes the `DESIGN.md` role names as a contract: a future `DESIGN.md` restructure forces a rename here.
- `DESIGN.md` `spacing` defines only `unit` (4px) plus `card-padding` / `section-gap` / `page-margin`. No numeric `--uc-space-{n}` ramp ships in 14.1; consumers compose off `--uc-space-unit` with `calc()`. Add a ramp later via "Ask First" only if a consumer needs it. `--uc-space-card-padding: 18px` is intentionally off the 4px grid, per `DESIGN.md`.
- `DESIGN.md` gives a line-height only for `body` (1.5) and letter-spacing only for `greeting` (-0.01em) and `label-caps` (0.05em); it gives a font-weight for every role. `.uc-text-*` classes therefore always set font-size + font-weight, and add line-height / letter-spacing only for the roles above. Do not invent values `DESIGN.md` omits, and do not add `text-transform` to `label-caps` (not in the frontmatter) -- 14.2 / 14.7 decide that.
- `--uc-shadow-overlay` is the one token from `DESIGN.md` prose (section "Elevation & Depth"), not frontmatter: `0 8px 24px rgba(0,0,0,.08)`, canonicalized to `0 8px 24px rgba(0, 0, 0, 0.08)`. `DESIGN.md` flags it as adjustable in implementation, so treat the value as provisional.
- `_typography.scss` ships only `.uc-text-*` utility classes. The global `body` background/colour, `box-sizing` and margin reset belong to Story 14.7, where their effect on existing screens is reviewed alongside the screens. This keeps 14.1 zero-visual-impact. `tokens.spec.ts` asserts the compiled output has no such rule, so a later edit that regresses Story 14.7 scope into either partial fails the build.
- `tokens.spec.ts` has no source-text fallback. `sass` is a committed `devDependency`, so the compiled-Sass path is the only path that actually exercises `styles.scss`; a failure to import `sass`, a compile error, or a `:root` with zero `--uc-*` is a real defect and must fail the test (the frozen I/O-matrix row demands it). This also deletes ~50 lines of otherwise-untested fallback code and its brittle "which path ran" logging.
- Node access in the spec uses real `@types/node` types via a `tsconfig.spec.json` `types` entry, not `import('node:'+x)` / `globalThis as any` tricks. This is the one extra changed file beyond `src/styles/**` + `styles.scss` + `package.json`; it is test-only config.
- `.uc-text-*` vs component class: use `.uc-text-*` for a bare type role in a template; Story 14.2 introduces component classes (badge, button, card) that compose the same tokens. The README carries the rule so 14.2 / 14.7 do not diverge.
- Type roles are discrete tokens + utility classes; no Sass mixin yet (add in 14.2 only if a consumer needs it).
- Single bare `:root`, no `@media (prefers-color-scheme)` block: dark mode is out of the MVP.

```scss
.uc-text-greeting {
  font-size: var(--uc-font-size-greeting);
  font-weight: var(--uc-font-weight-semibold);
  letter-spacing: var(--uc-letter-spacing-greeting);
}

.uc-text-meta {
  /* DESIGN.md defines size + weight for this role, nothing else */
  font-size: var(--uc-font-size-meta);
  font-weight: var(--uc-font-weight-regular);
}
```

## Verification

**Commands:**
- `cd frontend && npm ls sass @types/node` -- expected: `sass@1.101.x` and `@types/node@24.x` resolve as direct `devDependencies`.
- `cd frontend && npm run build` -- expected: completes, no Sass errors. `styles.scss` lands in the `initial` bundle (500 kB warning budget), not the `anyComponentStyle` 4 kB budget; the token layer is < 2 kB, so no new budget-warning line versus the pre-change build.
- `cd frontend && npm test` -- expected: `tokens.spec.ts` passes on the compiled-Sass path (token values, exact set, README table, `.uc-text-*` `var()`-only, no-reset assertions all green); all existing specs (`app`, `cadastro`, `confirmar-email`, `feed`, `login`, `auth.*`) still pass unchanged.
- `cd frontend && node -e "const s=require('sass');const c=s.compile('src/styles.scss',{style:'expanded'}).css;process.exit(/:root\s*\{[^}]*--uc-color-orange:\s*#EA6A2E/i.test(c)?0:1)"` -- expected: exit 0 (the compiled global stylesheet really carries the `:root` tokens).
- `cd frontend && grep -rn "7A1F2B\|maroon" src/styles/*.scss` -- expected (SCSS partials only, not the `.spec.ts` / `.md`): `#7A1F2B` only in the `--uc-color-maroon` declaration; `maroon` never as a `background`.
- `cd frontend && grep -rEn "box-sizing|(^|[ }])body[ ]*\{|(^|[ }])\*[ ]*\{" src/styles/*.scss` -- expected: no match (SCSS partials only; the `(^|[ }])` anchor avoids matching the `.uc-text-body` class; `tokens.spec.ts` carries these tokens inside assertion regexes by design).

**Manual checks:**
- `frontend/src/styles/README.md` lists every token in `_tokens.scss` with value and `DESIGN.md` role, states the never-hardcode + Ask-First rules, shows an application example, and carries the system-stack "pending decision" note.
- `git status`: only `frontend/src/styles/**`, `frontend/src/styles.scss`, `frontend/package.json`, `frontend/package-lock.json` and `frontend/tsconfig.spec.json` changed; nothing under `frontend/src/app/`.

## Suggested Review Order

**Design intent (start here)**

- The whole story in one screen: the single bare `:root`, every `--uc-*` value verbatim from `DESIGN.md`, grouped by concern.
  [`_tokens.scss:19`](../../frontend/src/styles/_tokens.scss#L19)

- The one token not from frontmatter: `--uc-shadow-overlay`, canonicalized from `DESIGN.md` prose, flagged provisional.
  [`_tokens.scss:60`](../../frontend/src/styles/_tokens.scss#L60)

- How the layer reaches the bundle: `styles.scss` (the single `angular.json` entry) aggregates the partials.
  [`styles.scss:5`](../../frontend/src/styles.scss#L5)

**Typography surface**

- One utility class per `DESIGN.md` role; each sets only the properties that role defines, values are `var(--uc-*)` only, no `font-family` / reset.
  [`_typography.scss:13`](../../frontend/src/styles/_typography.scss#L13)

**Consumption contract**

- The rule every later story follows: consume via `var(--uc-*)`, never hardcode; a value absent from `DESIGN.md` is an "Ask First" escalation.
  [`README.md:17`](../../frontend/src/styles/README.md#L17)

**Drift guard (peripheral)**

- Compiled-Sass only, no source-text fallback: a compile throw or an empty `:root` fails every test in the file (satisfies the frozen I/O-matrix row).
  [`tokens.spec.ts:148`](../../frontend/src/styles/tokens.spec.ts#L148)

- Per-role exact declaration map + token-name resolution + single-`:root` + no-`@media` assertions (added in review loop 1).
  [`tokens.spec.ts:211`](../../frontend/src/styles/tokens.spec.ts#L211)

- README table is asserted equal to the compiled token set, so doc drift is a red test.
  [`tokens.spec.ts:183`](../../frontend/src/styles/tokens.spec.ts#L183)

**Config (peripheral)**

- `sass` promoted to a direct `~1.101.0` devDependency (the spec compiles with it); `@types/node` `~24` for the spec's `fs` / `import.meta` use.
  [`package.json:30`](../../frontend/package.json#L30)

- `tsconfig.spec.json` gains `"node"` in `types` so the spec uses real Node types instead of obfuscated accessors.
  [`tsconfig.spec.json:8`](../../frontend/tsconfig.spec.json#L8)
