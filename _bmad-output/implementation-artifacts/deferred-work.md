# Deferred Work

Surfaced during build but out of scope for the originating spec. Append-only.

- source_spec: `_bmad-output/implementation-artifacts/spec-14-1-tokens-de-design-e-tipografia.md`
  summary: Make the token drift guard parse DESIGN.md frontmatter directly instead of a hand-maintained EXPECTED table.
  evidence: Review loop 1 (blind-hunter) noted EXPECTED in tokens.spec.ts is a third hand-typed copy of the values (with _tokens.scss and the README table). An edit to DESIGN.md itself is not caught. The frozen I/O-matrix row is satisfied as written (it triggers on a --uc-* value edited to differ), but parsing the YAML would close the residual gap. Deferred as an enhancement, not a defect.

- source_spec: `_bmad-output/implementation-artifacts/spec-14-1-tokens-de-design-e-tipografia.md`
  summary: Revisit px vs rem for the type scale so font sizes honour the user's browser font-size preference.
  evidence: DESIGN.md defines the scale in px (down to 10.5px); Story 14.1 copies it verbatim as required. Unlike the --uc-shadow-overlay "provisional" caveat, no note flags the px-vs-rem accessibility tradeoff as revisitable. This is a DESIGN.md-level decision (or Story 14.9, accessibility floor), not a 14.1 change.

- source_spec: `_bmad-output/implementation-artifacts/spec-14-1-tokens-de-design-e-tipografia.md`
  summary: Link frontend/src/styles/README.md from a discoverable entry point (frontend/README.md and/or the AGENTS.md context block) once Story 14.2 starts consuming the tokens.
  evidence: Review loop 1 noted the "consume via var(--uc-*), never hardcode" rule lives in a file with no inbound link. Low value until a consumer exists; best handled as a line in the Story 14.2 spec.

- source_spec: `_bmad-output/implementation-artifacts/spec-14-2-componentes-visuais-base.md`
  summary: Give the frontend/src/app/ui/ specs a real-browser test target so component visual output is verified against computed styles, not SCSS file text.
  evidence: Story 14.2 review (verification-gap) showed every visual I/O-matrix row (pill, orange-tint bg, per-variant text colour, :focus-visible outline, [disabled] dim, card border/no-shadow) is pinned only to `*.scss` source-text assertions plus a styleUrl-wiring check, because jsdom does not compute styles from an external stylesheet. A styling regression in the template or encapsulation that leaves the .scss text intact would still pass. A browser-based unit-test target (or Playwright component tests) would let `getComputedStyle` assert the rendered box. Bigger than this story; the first real consuming screen (Story 14.7) is the natural point to add it.

- source_spec: `_bmad-output/implementation-artifacts/spec-14-2-componentes-visuais-base.md`
  summary: Add a forced-colors / Windows High Contrast Mode fallback for button[uc-button] (and re-check badge/member-indicator) so controls stay visible when system colours override the palette.
  evidence: Story 14.2 review (blind-hunter) noted the button conveys its affordance purely through `background`, which is dropped in `forced-colors: active`, leaving an invisible control. No `@media (forced-colors)` rule ships. Belongs to the deferred accessibility-floor story (14.9), which owns WCAG 2.2 AA for every screen in the cut.

- source_spec: `_bmad-output/implementation-artifacts/spec-14-2-componentes-visuais-base.md`
  summary: Audit the badge-open colour pair (orange text #EA6A2E on orange-tint #FDEEE6) for WCAG 1.4.3 contrast and decide whether badge state needs an icon or shape cue, not just text + colour.
  evidence: Story 14.2 review (blind-hunter) flagged low contrast for `badge-open` and that course/open differ only by text colour. The badge always carries a text label (course name / "Comunidade aberta"), so meaning is not colour-alone, and the colour pair is specified verbatim in DESIGN.md `components.badge-open`. Resolving it is a DESIGN.md-level change or a Story 14.9 (accessibility floor) finding, not a 14.2 change.

- source_spec: `_bmad-output/implementation-artifacts/spec-14-3-shell-de-navegacao-global.md`
  summary: Full ARIA menu-button keyboard model for the avatar dropdown (focus first item on open; ArrowUp/Down/Home/End/type-ahead; Tab semantics).
  evidence: Story 14.3 review (all three lenses, twice) flagged that `role="menu"` / `role="menuitem"` ships without the APG keyboard interaction model. The spec deliberately scoped this to "focus-return-to-trigger only", naming Story 14.9 (accessibility floor). The 3-item menu is usable today (buttons are Tab-reachable, Escape closes and restores focus); the full model belongs to 14.9.

- source_spec: `_bmad-output/implementation-artifacts/spec-14-3-shell-de-navegacao-global.md`
  summary: Add a "skip to content" link and a focusable `<main id tabindex="-1">` target to the navigation shell.
  evidence: Story 14.3 review (blind-hunter, both loops) noted a persistent-nav shell should let keyboard users bypass the sidebar. Out of scope for 14.3 (which does not touch the accessibility floor); Story 14.9 owns WCAG 2.2 AA for every cut screen, and the shell is where the skip link lives.

- source_spec: `_bmad-output/implementation-artifacts/spec-14-3-shell-de-navegacao-global.md`
  summary: Give app.routes.ts a `**` wildcard/404 route and a `{ path: '', pathMatch: 'full', redirectTo: 'feed' }` index under the shell parent.
  evidence: Story 14.3 review (blind-hunter + edge-case-hunter, both loops) noted the route restructure left no catch-all and no shell index. Harmless now (the only shell child is `/feed`, and `/` is claimed by the leading `redirectTo: 'login'`), but an unknown URL produces a router error rather than a friendly redirect, and a shell-prefixed URL with no matching child would render an empty outlet. Best added when the second authenticated screen lands.

- source_spec: `_bmad-output/implementation-artifacts/spec-14-5-sistema-de-toast-e-motion-minimo.md`
  summary: Decide how a future "critical confirmation" toast (e.g. voto registrado) satisfies WCAG 2.2.1 Timing Adjustable, since ToastService's fixed 4s auto-dismiss has no pause-on-hover/focus.
  evidence: Story 14.5 review (blind-hunter) flagged the fixed timer as a Timing Adjustable risk. EXPERIENCE.md's own mitigation is that critical confirmations must also persist in screen state, not rely on the toast alone (e.g. the poll's result bar, not just the toast) - that persistence is each future consumer story's (2.3/2.4/8.x) responsibility, not something toast.service.ts itself can guarantee.

- source_spec: `_bmad-output/implementation-artifacts/spec-14-5-sistema-de-toast-e-motion-minimo.md`
  summary: Extract a shared checkmark-icon component instead of duplicating the same inline `<svg>` in both member-indicator.html and toast-host.html.
  evidence: Story 14.5 review (blind-hunter) noted the toast's checkmark markup is hand-copied from Story 14.2's member-indicator with no single source of truth. Story 14.3 already flagged "a real icon set/SVG icon system" as an Ask-First item deferred at that time; worth revisiting once a third icon appears.
