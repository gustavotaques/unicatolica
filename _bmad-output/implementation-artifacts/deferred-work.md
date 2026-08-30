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
