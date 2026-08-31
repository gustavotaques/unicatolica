---
title: 'Story 14.2: Base visual components'
type: 'feature'
created: '2026-08-30'
status: 'done'
review_loop_iteration: 0
baseline_commit: 'a4e8f3b6be190accf44988174ce961ea6a61309b'
context:
  - '{project-root}/_bmad-output/implementation-artifacts/epic-14-context.md'
  - '{project-root}/_bmad-output/planning-artifacts/ux-designs/ux-unicatolica-2026-08-17/DESIGN.md'
  - '{project-root}/frontend/src/styles/README.md'
---

<frozen-after-approval reason="human-owned intent — do not modify unless human renegotiates">

## Intent

**Problem:** Story 14.1 shipped the token layer, but no feature epic (Comunidades, Publicações, Enquetes, ...) has anything to render a community badge, a strong-action button, a "you are a member" indicator, or a generic content card. Each epic will otherwise re-invent them and drift from "Campus Clean".

**Approach:** Add four standalone Angular components under `frontend/src/app/ui/` whose SCSS consumes only `var(--uc-*)` tokens from 14.1. Typed inputs encode the invariants. No feature screen is touched (that is Story 14.7).

## Boundaries & Constraints

**Always:**
- Components live in `frontend/src/app/ui/<name>/`, one folder each, standalone, selector prefix `uc-`. Each has a `*.spec.ts` (Vitest + `TestBed`, mirroring `src/app/cadastro/cadastro.spec.ts`).
- Every colour / font-size / padding / radius / gap value is a `var(--uc-*)` token from `_tokens.scss`. No such literal in any `src/app/ui/**/*.scss`. Allowed literals: `1px` / `2px` border & outline widths, `0`, unitless `opacity`.
- No new `--uc-*` token and no edit to `_tokens.scss` / `_typography.scss` (14.1 frozen set, asserted by `tokens.spec.ts`).
- The four components, per `DESIGN.md` `components` + "Components"/"Shapes"/"Elevation" prose:
  - **`<uc-badge>`** — element selector. Required input `variant: 'course' | 'open'`; projects label text. Pill (`--uc-radius-full`), `background: var(--uc-color-orange-tint)`, `font-size: var(--uc-font-size-meta)`, `font-weight: var(--uc-font-weight-semibold)`. Text `var(--uc-color-maroon)` for `course`, `var(--uc-color-orange)` for `open`, via one of two mutually-exclusive host modifier classes — the two colour treatments never coexist on one element.
  - **`button[uc-button]`** — attribute selector on a native `<button>`. `background: var(--uc-color-orange)`, text `var(--uc-color-surface)`, pill, `font-size: var(--uc-font-size-body)` / `font-weight: var(--uc-font-weight-semibold)`, `border: 0`, `cursor: pointer`. `:focus-visible` → `outline: 2px solid var(--uc-color-maroon); outline-offset: 2px`. `[disabled]` → `opacity: .5; cursor: not-allowed`. No secondary/ghost variant. No inputs — native `type` / `disabled` used directly.
  - **`<uc-member-indicator>`** — element selector. Input `label` (default `Membro`). Inline check `<svg stroke="currentColor">` + the label text. `color: var(--uc-color-green-ok)`, no `background`. Meaning carried by the text label, never colour alone.
  - **`<uc-card>`** — element selector, projects content. `display: block`, `background: var(--uc-color-surface)`, `border: 1px solid var(--uc-color-border)`, `border-radius: var(--uc-radius-md)`, `padding: var(--uc-space-card-padding)`. Border, never shadow.
- Icons are inline `<svg>` with `currentColor` — no icon library, no hardcoded colour. Paddings/gaps compose from `var(--uc-space-unit)` (with `calc()` when needed).

**Ask First:**
- Adding hover / active / loading states, or size / tone variants beyond what `DESIGN.md` lists.
- Adding a `--uc-*` token, an icon component / SVG sprite system, or a runtime UI framework / `@angular/material`.

**Never:**
- Editing any file under `frontend/src/app/**` outside `frontend/src/app/ui/` (Story 14.7).
- A demo / showcase route or page — consumers are the feature epics.
- `maroon` as a fill/background larger than an icon or badge text.

## I/O & Edge-Case Matrix

| Scenario | Input / State | Expected Output / Behavior |
|----------|--------------|---------------------------|
| Course badge | `<uc-badge variant="course">Engenharia</uc-badge>` | Pill, `orange-tint` bg, `maroon` text, label visible; host has the course modifier class only |
| Open badge | `<uc-badge variant="open">Comunidade aberta</uc-badge>` | Pill, `orange-tint` bg, `orange` text; host has the open modifier class only |
| Badge variant missing | `<uc-badge>x</uc-badge>` | Angular required-input error at dev time |
| Primary button | `<button uc-button>Participar</button>` | Native `<button>`, `orange` bg, `surface` text, pill, keyboard-focusable |
| Button focused via keyboard | Tab to button | `outline: 2px solid var(--uc-color-maroon)`, `2px` offset |
| Button disabled | `<button uc-button disabled>` | `opacity: .5`, `cursor: not-allowed`; native `disabled` blocks click |
| Member indicator | `<uc-member-indicator />` / `label="Você participa"` | Check icon + `Membro` (or the given label), `green-ok`, no background |
| Generic card | `<uc-card><p>…</p></uc-card>` | `surface` bg, 1px `border`, `--uc-radius-md`, card padding, no shadow; content rendered |
| Hardcoded value in a ui SCSS | `color: #EA6A2E` or `font-size: 12px` | `ui-styles.spec.ts` fails, naming the file and declaration |

</frozen-after-approval>

## Code Map

- `frontend/src/app/ui/card/card.ts` (+ `card.scss`, `card.spec.ts`) -- NEW. `selector: 'uc-card'`, inline template `<ng-content>`. Simplest, no inputs. Spec: projects content, host renders.
- `frontend/src/app/ui/badge/badge.ts` (+ `badge.scss`, `badge.spec.ts`) -- NEW. `selector: 'uc-badge'`, `variant = input.required<'course' | 'open'>()`, host binds `uc-badge` plus `uc-badge--course` xor `uc-badge--open`, inline template `<ng-content>`. Spec: required input; exactly one modifier class; label renders; changing `variant` swaps the class.
- `frontend/src/app/ui/button/button.ts` (+ `button.scss`, `button.spec.ts`) -- NEW. `selector: 'button[uc-button]'`, host class `uc-button`, inline template `<ng-content>`, no inputs. Spec: host is a `<button>`; `uc-button` class present; native `disabled` reflected; component sets no `type`.
- `frontend/src/app/ui/member-indicator/member-indicator.ts` (+ `.html`, `.scss`, `.spec.ts`) -- NEW. `selector: 'uc-member-indicator'`, `label = input('Membro')`, template file = check `<svg>` + `{{ label() }}`. Spec: default label; custom `label`; an `<svg>` present.
- `frontend/src/app/ui/ui-styles.spec.ts` -- NEW. Reads every `src/app/ui/**/*.scss`; fails on a hex colour literal, on `px` in `font-size`/`padding`/`border-radius`/`gap`, or on a `color`/`background`/`background-color`/`border-color` value that is not `var(--uc-*)`. Allows `1px`/`2px` in `border`/`outline` and `0`. Asserts each file uses `var(--uc-` at least once.
- `frontend/src/app/ui/index.ts` -- NEW. Barrel re-exporting the four components.
- `frontend/src/styles/README.md` -- MODIFY. In "`.uc-text-*` vs classe de componente", replace the "classes de componente (badge, botao, card)" line with the standalone components `<uc-badge>`, `button[uc-button]`, `<uc-member-indicator>`, `<uc-card>` in `src/app/ui/`.
- `frontend/src/styles/_tokens.scss`, `_typography.scss` -- READ-ONLY. Token source (frozen) and the global `.uc-text-*` utilities.
- `frontend/src/app/cadastro/cadastro.ts` / `cadastro.spec.ts` -- READ-ONLY. Standalone-component and `TestBed` patterns to mirror (`input()`, `inject()`, signals; `provideHttpClient` not needed here).
- `frontend/angular.json` -- READ-ONLY. `prefix: "app"` is a schematics default only, no eslint gate; test builder `@angular/build:unit-test`.
- `_bmad-output/planning-artifacts/ux-designs/ux-unicatolica-2026-08-17/DESIGN.md` -- READ-ONLY. `components` frontmatter + "Components"/"Shapes"/"Elevation" prose = the visual contract.

## Tasks & Acceptance

**Execution:**
- [x] `frontend/src/app/ui/card/card.ts` + `card.scss` -- generic card, no inputs.
- [x] `frontend/src/app/ui/badge/badge.ts` + `badge.scss` -- `<uc-badge>` with required `variant` and mutually-exclusive host modifier classes.
- [x] `frontend/src/app/ui/button/button.ts` + `button.scss` -- `button[uc-button]`: strong-action fill, pill, `:focus-visible` maroon outline, `[disabled]` dim + not-allowed.
- [x] `frontend/src/app/ui/member-indicator/member-indicator.ts` + `.html` + `.scss` -- inline check SVG (`currentColor`) + `label` input, `green-ok`, no background.
- [x] `frontend/src/app/ui/index.ts` -- barrel export.
- [x] `frontend/src/app/ui/{badge,button,member-indicator,card}/*.spec.ts` -- one Vitest + `TestBed` spec per component covering that component's I/O & Edge-Case Matrix rows (behavioral rows + an SCSS style-contract block for the rows jsdom cannot compute).
- [x] `frontend/src/app/ui/ui-styles.spec.ts` -- the no-hardcode SCSS guard.
- [x] `frontend/src/styles/README.md` -- update the one line naming the 14.2 deliverables.

**Acceptance Criteria:**
- Given a feature epic needs a community badge, strong-action button, member indicator or content card, when it imports from `frontend/src/app/ui`, then a `uc-*` standalone component renders it per `DESIGN.md` with no new hardcoded design value.
- Given `button[uc-button]`, when reviewed it is the only strong-action button style in the repo, sits on a native `<button>`, shows a visible `--uc-color-maroon` keyboard-focus outline, and dims with `cursor: not-allowed` when `disabled`.
- Given `<uc-member-indicator>`, when rendered its state is conveyed by the text label plus a check icon (not colour alone) and it has no background.
- Given the story, when `cd frontend && npm run build && npm test` runs, then both pass; `ui-styles.spec.ts` fails the build on any hardcoded colour/size in `src/app/ui/**/*.scss` where a `--uc-*` token exists; `tokens.spec.ts` and all pre-existing specs still pass; nothing under `frontend/src/app/` outside `ui/` is modified.

## Design Notes

- `uc-` prefix marks a design-system primitive, distinct from feature components' `app-`. `angular.json` `prefix` is only a schematics default and there is no eslint gate, so no config change.
- `button[uc-button]` (attribute on the real `<button>`, not a wrapper element) keeps native `type` / `disabled` / form participation / focus — what the accessibility floor needs. The other three are element selectors with `<ng-content>`.
- "badge-course / badge-open never together" is structural: `variant` is a typed-union `input.required` and the template binds exactly one of two host classes — no code path sets both.
- Button text is `var(--uc-color-surface)` (`#FFFFFF`) rather than a new `--uc-color-on-accent`, so 14.1's `tokens.spec.ts` stays green. Focus outline and disabled dim are values `DESIGN.md` omits, composed from `--uc-color-maroon` + structural literals (`2px`, `.5`) with no new token; both are provisional, revisited in Story 14.9.
- Default (Emulated) view encapsulation is fine: CSS custom properties pierce it, so `:host` + `var(--uc-*)` resolves against the global `:root`.

## Verification

**Commands:**
- `cd frontend && npm run build` -- expected: completes, no Sass or template errors.
- `cd frontend && npm test` -- expected: the four component specs and `ui-styles.spec.ts` pass; `tokens.spec.ts`, `app`, `cadastro`, `confirmar-email`, `feed`, `login`, `auth.*` still pass unchanged.
- `cd frontend && grep -rEn "#[0-9a-fA-F]{3,8}|font-size:\s*[0-9]|padding:\s*[0-9].*px|border-radius:\s*[0-9]" src/app/ui --include=*.scss` -- expected: no match.
- `cd frontend && git status --porcelain` -- expected: only files under `frontend/src/app/ui/` plus `frontend/src/styles/README.md`.

**Manual checks:**
- In a scratch (uncommitted) template, render: badge in both variants, button enabled + disabled + keyboard-focused, member-indicator default + custom label, card with projected content — each matches the `DESIGN.md` "Components" description.

## Suggested Review Order

**The invariant, encoded in types**

- Entry point: the whole story in one file — `variant` is a required typed union, so "course + open colour never on one badge" cannot be expressed.
  [`badge.ts:27`](../../frontend/src/app/ui/badge/badge.ts#L27)
- The union is exported as a named type so feature epics can annotate their own bindings.
  [`badge.ts:4`](../../frontend/src/app/ui/badge/badge.ts#L4)
- Template binds exactly one of two mutually-exclusive host modifier classes; the SCSS colours hang off those.
  [`badge.scss:17`](../../frontend/src/app/ui/badge/badge.scss#L17)

**The one strong-action button, on a real `<button>`**

- Attribute selector on the native element (not a wrapper) keeps native `type` / `disabled` / form participation / focus.
  [`button.ts:17`](../../frontend/src/app/ui/button/button.ts#L17)
- `:focus-visible` maroon outline — the accessibility-floor keyboard affordance, composed from an existing token, no new `--uc-*`.
  [`button.scss:23`](../../frontend/src/app/ui/button/button.scss#L23)
- `:host(:disabled)` (pseudo-class, not `[disabled]`) so an ancestor `<fieldset disabled>` is covered too.
  [`button.scss:30`](../../frontend/src/app/ui/button/button.scss#L30)

**Presentational primitives**

- Member indicator: `label` input with an empty/whitespace guard, so state is never conveyed by the green colour alone.
  [`member-indicator.ts:22`](../../frontend/src/app/ui/member-indicator/member-indicator.ts#L22)
- Generic card: `surface` + 1px `border` + `--uc-radius-md` + card padding — border, never shadow.
  [`card.scss:5`](../../frontend/src/app/ui/card/card.scss#L5)
- Barrel: the four components plus the variant type, the single import surface for feature epics.
  [`index.ts:6`](../../frontend/src/app/ui/index.ts#L6)

**Machine-checked boundaries (peripheral)**

- No-hardcode guard: any hex, any length unit in a size prop, or a non-token colour in a `border` / `outline` shorthand fails the build.
  [`ui-styles.spec.ts:67`](../../frontend/src/app/ui/ui-styles.spec.ts#L67)
- Token-existence guard: every `var(--uc-*)` referenced under `ui/` must resolve against `_tokens.scss`.
  [`ui-styles.spec.ts:164`](../../frontend/src/app/ui/ui-styles.spec.ts#L164)
- Per-component `*.scss style contract` blocks pin the visual matrix rows jsdom cannot compute, and assert the `styleUrl` wiring.
  [`button.spec.ts:1`](../../frontend/src/app/ui/button/button.spec.ts#L1)
- README: `.uc-text-*` vs component guidance, plus the note that `button[uc-button]` does not override native `type`.
  [`README.md:47`](../../frontend/src/styles/README.md#L47)
