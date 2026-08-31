---
title: 'Story 14.5: Toast system and minimal motion'
type: 'feature'
created: '2026-08-31'
status: 'done'
review_loop_iteration: 0
baseline_commit: '02b2bafe5815dc74aee5373e5d1503d10a169f18'
context:
  - '{project-root}/_bmad-output/implementation-artifacts/epic-14-context.md'
  - '{project-root}/_bmad-output/planning-artifacts/ux-designs/ux-unicatolica-2026-08-17/EXPERIENCE.md'
  - '{project-root}/_bmad-output/planning-artifacts/ux-designs/ux-unicatolica-2026-08-17/DESIGN.md'
---

<frozen-after-approval reason="human-owned intent — do not modify unless human renegotiates">

## Intent

**Problem:** There is no toast primitive. Must-have Communities stories (2.3 auto-join, 2.4 open-community join) and Enquetes voting cannot close their acceptance criteria without a non-blocking, self-dismissing confirmation surface (RF24, UX-DR12, UX-DR22), and the ~150–200ms fade/slide motion rule (UX-DR9, UX-DR26) exists nowhere as a reusable pattern beyond the one-off fade already inlined in `shell.scss`'s dropdown.

**Approach:** Add a `ToastService` (signal-backed queue, `providedIn: 'root'`) plus a `UcToastHost` presentational component under `src/app/ui/toast/`, mounted once in `app.html` so it is available on every route. `mostrar(mensagem)` pushes a toast that renders with a fade/slide entrance and self-removes after a fixed duration — no manual dismiss, no variants beyond the single success/confirmation style every current trigger needs. This story ships the reusable primitive only; wiring `mostrar()` into Comunidades/Enquetes screens happens when those stories are built.

## Boundaries & Constraints

**Always:**
- `ToastService` at `ui/toast/toast.service.ts`, `@Injectable({ providedIn: 'root' })`. Private `signal<ToastItem[]>([])`, public `readonly toasts = this.#itens.asReadonly()`. `mostrar(mensagem: string): void` — trims `mensagem`; a blank result is a no-op (no empty toast). Otherwise appends `{ id, mensagem }` (monotonic in-memory counter) and schedules removal via `setTimeout(..., TOAST_DURACAO_MS)`; no public dismiss method. Export `TOAST_DURACAO_MS = 4000` and `interface ToastItem { readonly id: number; readonly mensagem: string }`.
- `UcToastHost` at `ui/toast/toast-host.ts`, standalone, `selector: 'uc-toast-host'`, `inject(ToastService)`. Template (`toast-host.html`) is one `role="status" aria-live="polite" aria-atomic="false"` wrapper around `@for (toast of service.toasts(); track toast.id)`; each item repeats the exact checkmark `<svg>` markup from `member-indicator.html` (`stroke="currentColor"`, `aria-hidden="true"`) plus `<span>{{ toast.mensagem }}</span>`.
- `toast-host.scss`: `position: fixed`, anchored bottom-right via `var(--uc-space-page-margin)` (never `top`/left of the topbar dropdown, which already owns top-right). Card-like surface: `var(--uc-color-surface)` bg, `1px solid var(--uc-color-border)`, `var(--uc-radius-md)`, `var(--uc-shadow-overlay)`; icon colour `var(--uc-color-green-ok)` (DESIGN.md: the one green token for every positive confirmation). Entrance-only `@keyframes` fade + slide (opacity `0→1`, `transform: translateY(...) → translateY(0)` using a `calc(var(--uc-space-unit) * N)` distance), `150ms ease-out` — matching the duration `shell.scss`'s dropdown fade already established (`shell.scss:141`, comment `Story 14.5 owns motion`) so the app has one consistent motion value. `@media (prefers-reduced-motion: reduce)` disables the animation, mirroring `shell.scss` exactly. No exit animation — an item disappears immediately when removed from the array, same as the dropdown's own close behaviour.
- `ui/index.ts` — add `ToastService`, `UcToastHost`, `type ToastItem` to the barrel alongside the four Story 14.2 exports.
- `app.ts` / `app.html` — import `UcToastHost`, render `<uc-toast-host />` alongside `<router-outlet />` so toasts work on both public routes (login/cadastro) and the guarded shell.
- Every colour/spacing/radius/shadow value is `var(--uc-*)`; only the svg's own `viewBox`/`path`/`stroke-width` numbers and `1px` border are literal (same allowance every existing `ui/` component uses).
- `toast-host.spec.ts` + `toast.service.spec.ts` follow the existing per-file style: colocated, mirror `member-indicator.spec.ts` / `shell.spec.ts` structure (fixture-driven behaviour tests + an own style-contract `describe` block reading the raw `.scss`), and cover every I/O Matrix row. `toast.service.spec.ts` uses fake timers (`vi.useFakeTimers()`) for the auto-dismiss assertions.

**Ask First:**
- Any toast variant beyond the single success/confirmation style (e.g. error/warning) — no current trigger (join, auto-join, vote) needs one.
- A manual close control on the toast — EXPERIENCE.md specifies auto-dismiss only ("some sozinho").
- A max-simultaneous-toasts cap or queue-collapsing policy.

**Never:**
- Wiring `ToastService.mostrar()` into any feature screen (Comunidades 2.3/2.4, Enquetes vote) — none of those screens exist in this codebase yet; this story only builds the primitive.
- "Troca de aba" (tab-switch) motion from AC2 — no tab UI exists anywhere yet; nothing to attach it to.
- Touching `layout/shell/shell.scss`'s existing dropdown `@keyframes`/media query — it already satisfies the dropdown half of Story 14.3's AC2; read-only.
- Adding `@angular/animations` or any JS-driven exit transition.
- Any new `--uc-*` token or edit to `_tokens.scss` / `_typography.scss` / `tokens.spec.ts` (no duration or z-index token exists; the duration is a plain exported TS constant, not a design token).

## I/O & Edge-Case Matrix

| Scenario | Input / State | Expected Output / Behavior | Error Handling |
|----------|--------------|---------------------------|----------------|
| `mostrar('Você entrou em X')` called | non-blank message | new item appended to `toasts()`; host renders it with fade/slide-in | N/A |
| toast has been present `>= TOAST_DURACAO_MS` | timer elapses | item removed from `toasts()`; disappears, no exit animation | N/A |
| two `mostrar()` calls back to back | two distinct messages | both present simultaneously in `toasts()`, each with its own independent removal timer | N/A |
| `mostrar('')` / `mostrar('   ')` | blank or whitespace-only string | no-op: `toasts()` unchanged, nothing rendered | swallow silently |
| OS has `prefers-reduced-motion: reduce` | toast appears | still appears/disappears on the same schedule; `animation: none` (no fade/slide) | N/A |

</frozen-after-approval>

## Code Map

- `frontend/src/app/ui/toast/toast.service.ts` -- NEW. Signal-backed queue + `mostrar()` + auto-dismiss timer, per Boundaries.
- `frontend/src/app/ui/toast/toast.service.spec.ts` -- NEW. I/O Matrix rows for the service half; `vi.useFakeTimers()` for dismiss timing.
- `frontend/src/app/ui/toast/toast-host.ts` -- NEW. `uc-toast-host`, `inject(ToastService)`, `@for` template.
- `frontend/src/app/ui/toast/toast-host.html` -- NEW. Live-region wrapper + per-toast checkmark-icon + message markup, mirroring `frontend/src/app/ui/member-indicator/member-indicator.html`.
- `frontend/src/app/ui/toast/toast-host.scss` -- NEW. Fixed bottom-right position, surface/border/radius/shadow tokens, entrance keyframes + reduced-motion guard, per Boundaries. Automatically covered by the existing recursive glob in `frontend/src/app/ui/ui-styles.spec.ts` (no changes needed there).
- `frontend/src/app/ui/toast/toast-host.spec.ts` -- NEW. Renders queued toasts, asserts `role="status"`/`aria-live="polite"`, and its own style-contract block (token-only, `@keyframes` + `prefers-reduced-motion` present), mirroring the block pattern in e.g. `frontend/src/app/ui/badge/badge.spec.ts:77-95`.
- `frontend/src/app/ui/index.ts` -- MODIFY. Barrel export additions only.
- `frontend/src/app/app.ts` -- MODIFY. Add `UcToastHost` to `imports`.
- `frontend/src/app/app.html` -- MODIFY. Add `<uc-toast-host />` next to `<router-outlet />`.
- READ-ONLY refs: `frontend/src/app/ui/member-indicator/member-indicator.html` (checkmark svg markup to mirror), `frontend/src/app/layout/shell/shell.scss:131-158` (the 150ms fade + reduced-motion precedent to match), `frontend/src/styles/_tokens.scss` (token set — no additions), `frontend/src/app/ui/ui-styles.spec.ts` (recursive no-hardcode guard, applies automatically), `frontend/src/app/app.spec.ts` (must still pass unmodified).

## Tasks & Acceptance

**Execution:**
- [x] `frontend/src/app/ui/toast/toast.service.ts` -- add signal-backed `ToastItem` queue, `mostrar()`, `TOAST_DURACAO_MS` -- core state primitive.
- [x] `frontend/src/app/ui/toast/toast.service.spec.ts` -- cover all 5 I/O Matrix rows with fake timers -- proves auto-dismiss and the blank-message guard.
- [x] `frontend/src/app/ui/toast/toast-host.ts` + `toast-host.html` -- render the live queue with the checkmark markup and live-region wrapper -- the visible surface.
- [x] `frontend/src/app/ui/toast/toast-host.scss` -- token-only surface, fixed positioning, 150ms fade/slide entrance, reduced-motion guard -- visual + motion contract.
- [x] `frontend/src/app/ui/toast/toast-host.spec.ts` -- behaviour + style-contract block -- proves the a11y wrapper and no-hardcode styling.
- [x] `frontend/src/app/ui/index.ts` -- export the three new symbols -- keeps the barrel authoritative.
- [x] `frontend/src/app/app.ts` + `app.html` -- mount `<uc-toast-host />` app-wide -- makes the primitive actually reachable.

**Acceptance Criteria:**
- Given any code calls `ToastService.mostrar(mensagem)` with a non-blank message, when it executes, then a toast renders via `UcToastHost` without blocking the screen and disappears on its own after `TOAST_DURACAO_MS`, with no manual dismiss control anywhere.
- Given the toast entrance animation and the pre-existing `shell.scss` dropdown fade, when either renders, then both use a ~150–200ms fade/slide with no decorative animation and both honour `prefers-reduced-motion: reduce`.
- Given `cd frontend && npm run build && npm test`, when they run, then both pass, `tokens.spec.ts` / `ui-styles.spec.ts` / `app.spec.ts` / every pre-existing spec stays green, and no file outside `src/app/ui/toast/`, `src/app/ui/index.ts`, `src/app/app.ts` and `src/app/app.html` is modified.

## Spec Change Log

## Design Notes

- Green + checkmark reuses `member-indicator`'s "positive confirmation" language (DESIGN.md: one green token per positive confirmation) instead of a second idiom — every current trigger (join, auto-join, vote) is positive; a distinct error/warning style is Ask First until a story needs one.
- Bottom-right placement avoids the topbar dropdown (top-right) and the sidebar (left), the app's only other transient/overlay surfaces.
- `ToastService` is the first `signal()`-backed service here (existing services expose plain methods); `providedIn: 'root'` + a public `asReadonly()` signal means `UcToastHost` just reads `service.toasts()` reactively, no subscription/cleanup.
- Infrastructure-only by design (epic-14 context: 14.5 exists so 2.3/2.4 can close their ACs once built) — nothing to wire `mostrar()` into yet.

## Verification

**Commands:**
- `cd frontend && npm run build` -- expected: completes, no Sass or template errors.
- `cd frontend && npm test` -- expected: new `toast.service.spec.ts` / `toast-host.spec.ts` pass; `tokens.spec.ts`, `ui-styles.spec.ts`, `app.spec.ts`, `shell.spec.ts`, all pre-existing specs stay green.
- `cd frontend && grep -rEn "#[0-9a-fA-F]{3,8}" src/app/ui/toast --include=*.scss` -- expected: no match.
- `cd frontend && git status --porcelain` -- expected: only paths under `src/app/ui/toast/`, `src/app/ui/index.ts`, `src/app/app.ts`, `src/app/app.html`.

**Manual checks:**
- From devtools, call `ToastService.mostrar('Você entrou em Atlética')` on `/login` and `/feed`: confirm fade/slide-in bottom-right, no blocked clicks, auto-dismiss ~4s. Repeat with OS "reduce motion" on and confirm no animation.

## Suggested Review Order

**Toast queue engine (the reason for this story)**

- Entry point: signal-backed queue, blank-message guard, self-scheduling removal — no public dismiss method by design.
  [`toast.service.ts:31`](../../frontend/src/app/ui/toast/toast.service.ts#L31)
- Fixed duration is a plain TS constant, not a design token (none exists for timing).
  [`toast.service.ts:9`](../../frontend/src/app/ui/toast/toast.service.ts#L9)

**Visible surface & accessibility**

- `UcToastHost` reads the service's readonly signal reactively, no subscription/cleanup.
  [`toast-host.ts:17`](../../frontend/src/app/ui/toast/toast-host.ts#L17)
- One `aria-live="polite"` wrapper around the `@for`, so new toasts are announced without re-reading the stack.
  [`toast-host.html:1`](../../frontend/src/app/ui/toast/toast-host.html#L1)
- Checkmark icon markup mirrors `member-indicator.html` verbatim for a consistent "positive confirmation" language.
  [`toast-host.html:4`](../../frontend/src/app/ui/toast/toast-host.html#L4)

**Styling & motion (token-only, viewport-safe)**

- Fixed bottom-right position; `z-index: 1` comment now states plainly it's a placeholder, not backed by structural precedent (post-review fix).
  [`toast-host.scss:24`](../../frontend/src/app/ui/toast/toast-host.scss#L24)
- `max-height` + `overflow-y: auto` keeps a long queue scrolling internally instead of pushing older toasts off a short/mobile viewport (post-review fix).
  [`toast-host.scss:39`](../../frontend/src/app/ui/toast/toast-host.scss#L39)
- `max-width` + `overflow-wrap: anywhere` stops a long message overflowing the card horizontally (post-review fix).
  [`toast-host.scss:49`](../../frontend/src/app/ui/toast/toast-host.scss#L49)
- Entrance-only fade/slide keyframes at 150ms, matching `shell.scss`'s dropdown precedent, with a `prefers-reduced-motion` guard.
  [`toast-host.scss:61`](../../frontend/src/app/ui/toast/toast-host.scss#L61)

**App-wide wiring**

- `<uc-toast-host />` mounted as a sibling of `<router-outlet />` so toasts work on every route, public and guarded.
  [`app.html:2`](../../frontend/src/app/app.html#L2)
- `UcToastHost` imported into the root standalone component.
  [`app.ts:3`](../../frontend/src/app/app.ts#L3)
- Barrel export additions, no other change.
  [`index.ts:10`](../../frontend/src/app/ui/index.ts#L10)

**Peripherals**

- Service spec covers append, auto-dismiss via fake timers, independent-timer stacking, and the blank/whitespace no-op.
  [`toast.service.spec.ts:17`](../../frontend/src/app/ui/toast/toast.service.spec.ts#L17)
- Host spec covers rendering + the live-region attributes, plus its own SCSS style-contract block.
  [`toast-host.spec.ts:34`](../../frontend/src/app/ui/toast/toast-host.spec.ts#L34)
- New assertion proving `<uc-toast-host />` actually renders inside `App` at runtime (post-review fix, was previously only inspected by eye).
  [`app.spec.ts:24`](../../frontend/src/app/app.spec.ts#L24)

