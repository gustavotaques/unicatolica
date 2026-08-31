# Epic 14 Context: Fundação Visual e de Experiência (Design System)

<!-- Compiled from planning artifacts. Edit freely. Regenerate with compile-epic-context if planning docs change. -->

## Goal

Epic 14 establishes the shared visual and experiential foundation ("Campus Clean") every other epic builds its screens on: design tokens, base UI components, the global navigation shell, the toast/motion system, a voice-and-tone standard, plus restyling existing screens and building a few new ones. These concerns are cross-cutting (they belong to no single feature module), yet the week-1 must-have cut (Identity, Communities, Publications) cannot ship without styled, consistent screens. The direction deliberately drops the dense "news portal" feel of the original Figma prototype (which used maroon as a dominant background), keeping the institution's identity through generous whitespace, low-contrast cards, and one restrained accent color. Scope was triaged on 2026-08-30: stories 14.1, 14.2, 14.3, 14.5, 14.7 are in the week-1 cut; 14.4, 14.6, 14.8, 14.9 are deferred. Deferring a story does not suspend the rules it systematizes: responsiveness, voice-and-tone, and the accessibility floor still apply as acceptance criteria to every screen built in the cut.

## Stories

- Story 14.1: Design tokens and typography
- Story 14.2: Base visual components (badge, primary button, member indicator, generic card)
- Story 14.3: Global navigation shell (sidebar + avatar dropdown)
- Story 14.4: Responsive navigation behavior (deferred)
- Story 14.5: Toast system and minimal motion
- Story 14.6: Voice and tone system / microcopy (deferred)
- Story 14.7: Restyle the must-have cut screens
- Story 14.8: Build the new spine-only screens (deferred)
- Story 14.9: Accessibility floor, WCAG 2.2 AA (deferred)

## Requirements & Constraints

- All color, font, spacing, and radius values come from reusable tokens. No hardcoded design values anywhere, including screens (Login, Cadastro, Verifique seu e-mail) built earlier with per-component SCSS and no tokens: that SCSS must be replaced with token consumption.
- Fixed palette roles: near-white backgrounds (never pure white on the general canvas); one subtle border color used only as a divider; three ink levels for text; maroon as a minimal accent only (brand stroke, active icon, course-community titles), never a dominant screen background; orange as the single strong-action accent, at most one orange action per screen; orange-tint only as a soft background for badges/active nav, never text; one green success token for every positive confirmation.
- Typography scale: greeting 22px/600, question 15px/600, body 13.5px/400 lh 1.5, meta 12px/400, label-caps 10.5px/700 tracking 0.05em. Weight 400 or 600 only; 700 reserved for label-caps; never 800+. System font stack throughout. No dark mode in the MVP.
- Shapes and elevation: moderate rounded corners (6px small controls, 8 to 12px cards/inputs, 14px outer container); pill radius only for community badges and action buttons, never content cards; cards use a 1px border, not shadow; dropdowns and toasts use one light diffuse shadow (start value 0 8px 24px rgba(0,0,0,.08)).
- A badge never shows the course color and the open color at once. State badges and indicators never rely on color alone; they carry text/label.
- Motion is minimal: ~150 to 200ms fade/slide only on toasts, dropdowns, tab switches. No decorative animation.
- Toasts never block the screen and dismiss themselves; a toast is never the sole channel for a critical confirmation (persistent screen state must also reflect it).
- Admin panel screen is scoped only to pre-creating course communities; institutional notices and a reports screen are out of this UX's scope.
- Exact responsive breakpoints are deferred to implementation, but a functional mobile browser is required.

## Technical Decisions

- Frontend is an Angular SPA (Angular ^22), a single deploy unit, deployed as a Render Static Site, talking to the backend over REST/JSON with a JWT Bearer header. Implement tokens as reusable primitives (CSS custom properties or an Angular theme).
- The existing frontend/ project already has src/styles.scss and per-component SCSS predating this epic; Story 14.7 converts those to tokens.
- Navigation shell: fixed sidebar (~220px) with Início, Buscar, Mensagens, Notificações, Criar enquete, Suas comunidades + Descobrir; moderator/admin roles additionally see Denúncias and Solicitações de fixação. A right-hand discovery panel (~260px) shows on wide screens. Topbar avatar opens a dropdown (Perfil, Configurações, Sair) with no sidebar entry. Item visibility is driven by the user's global role (JWT roles claim).
- Voice and tone: direct, second person ("você"); errors explain what happened and what to do without blaming the user; empty states invite action. Approved strings exist per moment (signup email-exists / wrong domain, unconfirmed-email login, empty community, empty search, community join, vote registered, hidden/removed content, progressive onboarding, auto-join).

## UX & Interaction Patterns

- Base components consumed by later epics: community badge (course/open), primary button (the only strong-action style), member indicator (green text, no background, replaces the action button once the user is a member), generic card (surface + 1px border + md radius + card padding), post card, tree comment (indentation caps at ~3 levels), grouped notification, attached material, search result (single mixed list, not tabs), moderation queue item (content + reason visible, reporter identity never shown).
- "Participar" switches to the member indicator immediately on click, no confirmation modal, plus a toast "Você entrou em {comunidade}". Auto-join shows a single toast on the first visit to Início after association: "Você já faz parte de {comunidade} 🎓".
- Course-community non-member state: feed visible, but the post/comment/vote box is replaced by an explanatory notice, never silently omitted.
- Accessibility floor: visible keyboard focus on every interactive element; state meaning carried by text, not color alone; critical confirmations persist in screen state, not only a toast; poll-creation form has associated labels and announced validation errors.
- Must-have screens to restyle: Login, Cadastro, Verifique seu e-mail, open-community creation, Artigo (post only, without the comments layer), and the admin panel limited to course-community pre-creation. Reconcile divergences recorded in the Figma reconciliation notes (the prototype's dense news-portal layout and blurred organic background shapes are deliberately dropped).
- Deferred spine-only screens (no mockup): Busca, Mensagens, Solicitações de fixação (its own queue, same "moderation queue item" pattern as Denúncias).

## Cross-Story Dependencies

- 14.2 depends on 14.1 (components consume tokens); 14.7 depends on 14.1 and 14.2; 14.4 depends on 14.3; 14.8 depends on 14.1 and 14.2.
- 14.5 stays in the week-1 cut because must-have Communities stories (auto-join 2.3, open-community join 2.4) need a toast to close their acceptance criteria.
- 14.3 (sidebar role visibility) depends on the JWT role model from Epic 1.
- 14.7 restyles screens delivered by Epic 1 (Login, Cadastro, Verifique seu e-mail) and the admin pre-creation flow from Epic 2 Story 2.1; Perfil (Epic 4), the Artigo comments layer (Epic 5), and institutional notices in the admin panel (Epic 13) are explicitly out of this story.
- 14.9's poll-form criteria target Epic 8 Story 8.1.
