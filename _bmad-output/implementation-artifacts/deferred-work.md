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
