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

- source_spec: `_bmad-output/implementation-artifacts/spec-14-7-restilizacao-das-telas-do-corte-must-have.md`
  summary: Build the open-community creation screen. Owned by Story 2.2, not by Epic 14.
  evidence: The screen is listed among the "must-have cut screens to restyle" of epic-14-context.md, but no code exists for it (no component, template or route under frontend/src/app). Story 14.7 restyles screens that exist; building a screen from scratch against the Comunidades contract belongs to the story that owns the flow (2.2, RF21.2 restricts creation to open communities). Nothing was built here.

- source_spec: `_bmad-output/implementation-artifacts/spec-14-7-restilizacao-das-telas-do-corte-must-have.md`
  summary: Build the Artigo screen (post only, without the comments layer). Owned by Epic 3 (Publicacoes).
  evidence: Listed as a cut screen to restyle, but the `publicacoes` module is empty and `openapi.yaml` declares no publication endpoint. Building a UI against a contract that does not exist violates AD-4 ("never implement an endpoint, front or back, without openapi.yaml agreed first"). Waits for Epic 3 to agree the contract; the Campus Clean primitives (card, badge, member-indicator, uc-button) and the widened SCSS guard are ready for it.

- source_spec: `_bmad-output/implementation-artifacts/spec-14-7-restilizacao-das-telas-do-corte-must-have.md`
  summary: Build the admin panel limited to pre-creating course communities. Owned by Story 2.1.
  evidence: Listed as a cut screen, but `openapi.yaml:173-176` states the pre-creation endpoint is out of the current slice, so there is no contract to build against (AD-4). EXPERIENCE.md also records an open team item on this surface: the admin Figma screens (`adm-login`, `adm-dashboard`, `adm-moderacao`) were never captured, so reusing the admin visual pattern is recorded intention, not a verified decision. Confirm the screenshots before restyling.

- source_spec: `_bmad-output/implementation-artifacts/spec-14-7-restilizacao-das-telas-do-corte-must-have.md`
  summary: Decide whether the maroon community header band in comunidade-detalhe.scss stays, since DESIGN.md prose and the approved mockup disagree.
  evidence: DESIGN.md "Do's and Don'ts" says maroon is a stroke/icon colour and must never back a block larger than an icon or badge; the approved mockup `mockups/home-comunidade.html:85` ships exactly such a band (`.comm-header`, a maroon gradient behind the whole community header). Story 14.7 kept the mockup's behaviour: `comunidade-detalhe` is not one of its six screens and the band comes straight from the direction the user picked. Recolouring it (or amending the prose) is a DESIGN.md-level decision, not a silent implementation change.

- source_spec: `_bmad-output/implementation-artifacts/spec-14-7-restilizacao-das-telas-do-corte-must-have.md`
  summary: Give the login screen the same inline field validation the same story gave cadastro (per-field message, aria-invalid, aria-describedby).
  evidence: Story 14.7 review (blind-hunter, edge-case-hunter). `login.ts` runs `Validators.required`/`Validators.email` and `enviar()` returns silently when invalid, while `login.html` carries `novalidate`, so an empty submit produces no visible or announced feedback. Action item A-11 scoped login to `<main>` + `<h1>` only and gave the field-error treatment to cadastro, and `e2e/auth-publico.spec.ts` codifies the current silence as intended ("submeter vazio não navega e não mostra erro de credencial"). Changing it means changing that e2e expectation too, which belongs to the accessibility-floor story (14.9) or to a deliberate UX decision.

- source_spec: `_bmad-output/implementation-artifacts/spec-14-7-restilizacao-das-telas-do-corte-must-have.md`
  summary: Audit `--uc-color-orange` as link/CTA text colour: #EA6A2E on #FFFFFF is 3.18:1, below WCAG AA for normal text.
  evidence: Story 14.7 review (blind-hunter). `confirmar-email.scss`'s "Ir para o login" / "Voltar ao cadastro" link and every `button[uc-button]` label rely on this pair. The value comes verbatim from DESIGN.md `colors.orange`, which names it the single strong-action accent, so fixing it is a DESIGN.md-level decision (darker orange, larger type, or an underline) and not a 14.7 change. Same family as the already-recorded `badge-open` contrast item from Story 14.2; Story 14.9 owns WCAG 2.2 AA for every cut screen.

- source_spec: `_bmad-output/implementation-artifacts/spec-14-7-restilizacao-das-telas-do-corte-must-have.md`
  summary: Add a CI job that runs `npx playwright test`, so the e2e suite becomes part of the merge gate.
  evidence: Story 14.7 review (verification-gap). `.github/workflows/ci.yml`'s frontend job runs `ng test` and `ng build` only; `package.json`'s `e2e` script is never invoked by CI. Every browser-level assertion the project owns - the computed canvas colour, the visible focus ring, the toast anchoring and motion timing from Story 14.5, the sidebar role gating from 14.3 - therefore sits outside the gate AGENTS.md calls authoritative ("Merge em `main` exige apenas CI verde"). Pre-existing since the suite was added in #18, surfaced here because 14.7's base layer is the first change whose only browser-level proof lives there.

- source_spec: `_bmad-output/implementation-artifacts/spec-14-7-restilizacao-das-telas-do-corte-must-have.md`
  summary: Give the public auth shell (`uc-auth-shell`) responsive behaviour below desktop width.
  evidence: Story 14.7 review (blind-hunter). `.auth-shell` applies `padding: var(--uc-space-page-margin)` (32px) at every viewport and the file has no `@media`; at 360px a 400px card plus 64px of gutter does not fit. Story 14.4 (responsive navigation) is scoped to the authenticated sidebar and discovery panel, so the public screens have no owner for RNF02. Decide whether 14.4 absorbs them or a separate story does.

- source_spec: `_bmad-output/implementation-artifacts/spec-14-7-restilizacao-das-telas-do-corte-must-have.md`
  summary: Define hover and active states for the system's interactive elements; the restyled screens ship base and `:disabled` only.
  evidence: Story 14.7 review (blind-hunter). `.botao-secundario`, `.login__link`, `button[uc-button]` and `.detalhe__botao-sair` define no hover or active treatment, while `.shell__nav-item` / `.shell__menu-item` / `.confirmacao__link` do - so the app is inconsistent about pointer feedback. DESIGN.md specifies no hover token or rule for any component, which makes this a DESIGN.md gap, not an implementation omission.

- source_spec: `_bmad-output/implementation-artifacts/spec-14-7-restilizacao-das-telas-do-corte-must-have.md`
  summary: Decide whether the global bundle may host a document-level `prefers-reduced-motion` reset, which `tokens.spec.ts`'s at-rule ban currently forbids.
  evidence: Story 14.7 review (blind-hunter). `tokens.spec.ts` asserts the compiled bundle matches no `@(media|font-face|keyframes|supports|import)`, so `_base.scss` can never carry a global reduced-motion safety net. Reduced motion is handled per component today (`toast-host.scss`, `shell.scss`), which works but means every future animated component must remember its own guard. Revisit with Story 14.9 (accessibility floor) or 14.4 (which will need `@media` in the global layer anyway).

- source_spec: `_bmad-output/implementation-artifacts/spec-14-7-restilizacao-das-telas-do-corte-must-have.md`
  summary: Rewrite the cadastro success state's `Nome` / `Curso` / `Status do e-mail` definition list, a data dump left under the "Verifique seu e-mail" heading.
  evidence: Story 14.7 review (blind-hunter). The story rewrote the copy on this screen (a test now asserts the "(Story 1.2)" label is gone), so leaving the summary dump is inconsistent with the voice-and-tone pass it just received. EXPERIENCE.md's Do/Don't table has no row for this moment, so the replacement copy is a Story 14.6 (voice and tone) decision rather than a restyle.

- source_spec: `_bmad-output/implementation-artifacts/spec-14-7-restilizacao-das-telas-do-corte-must-have.md`
  summary: Add a `format` / `lint` script and a CI gate for Prettier; the repo has pre-existing formatting drift across at least 21 files.
  evidence: Story 14.7 review (blind-hunter). `prettier --check` fails on files this story never touched (`auth.service.ts`, `comunidades.service.ts`, `feed.ts`, `index.html`, `shell.spec.ts`, the comunidades screens). `package.json` has no `format` or `lint` script and `.github/workflows/ci.yml` runs no formatting check, so drift compounds silently. Story 14.7 reformatted only the six files it edited; the repo-wide sweep needs its own change so the diff stays reviewable.

- source_spec: `_bmad-output/implementation-artifacts/spec-14-7-restilizacao-das-telas-do-corte-must-have.md`
  summary: Make `tokens.spec.ts` read DESIGN.md's `colors` frontmatter instead of mirroring it in a hand-maintained `EXPECTED` map.
  evidence: Story 14.7 review (verification-gap). The story required `--uc-color-error` to land in lockstep across `_tokens.scss`, the README table, `tokens.spec.ts` and DESIGN.md. Three of those four are drift-guarded against each other; the DESIGN.md edit is verified by nothing, so DESIGN.md and the token layer can silently diverge. Pre-existing since Story 14.1 built `EXPECTED` by hand.

- source_spec: `_bmad-output/implementation-artifacts/spec-14-7-restilizacao-das-telas-do-corte-must-have.md`
  summary: Extend the SCSS guard to at-rule preludes, so responsive breakpoint literals cannot bypass it.
  evidence: Story 14.7 review (edge-case-hunter). `parseDeclarations` only sees `prop: value` pairs; an `@media (min-width: 768px)` prelude is never parsed, so breakpoint literals escape every rule. Nothing is unchecked today (no `@media` with a length exists outside the two reduced-motion queries), but Story 14.4 introduces the project's first breakpoints and should land this guard with them.

- source_spec: `_bmad-output/implementation-artifacts/spec-14-7-restilizacao-das-telas-do-corte-must-have.md`
  summary: Extend the SCSS guard to inline component styles (`@Component({ styles: [...] })`) and `style=""` template attributes.
  evidence: Story 14.7 review (edge-case-hunter). The guard globs `.scss` files only, so hardcoded design values could re-enter through an inline style block. No component uses either today, which is why this is prevention rather than a fix; a simple assertion that no `@Component` carries `styles:` and no template carries a `style=` attribute would close it.

- source_spec: `_bmad-output/implementation-artifacts/spec-14-7-restilizacao-das-telas-do-corte-must-have.md`
  summary: Keep one persistent `aria-live` region on `confirmar-email` instead of creating and destroying it per `@switch` branch, and give the resend failure path a visible message.
  evidence: Story 14.7 review (edge-case-hunter). The `role="status"` node only exists during the `confirmando` branch and is replaced when the result arrives, so a screen reader may never announce the outcome; separately, `reenviarConfirmacao()`'s error path resets `reenviando` with no message (a pre-existing behaviour its own code comment documents). Both are screen-reader announcement semantics, which Story 14.9 owns for every cut screen.
