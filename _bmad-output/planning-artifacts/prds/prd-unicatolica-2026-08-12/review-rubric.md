# PRD Quality Review — UniCatólica — Rede Social Acadêmica (PACEXT)

## Overall verdict

This is a well-earned PRD, not a template exercise: both UJs trace forward into named FRs with testable consequences, the non-duplication of the 80 validated RF01–RF80 requirements is explicitly signaled and every cross-reference to `docs/unicatolica-pacext-contexto.md` checked out exactly, and the success metrics include real counter-metrics rather than vanity activity numbers. What's at risk is narrower than the PRD as a whole: UJ-2's protagonist is a role description, not a named person like Julia, and the admin role — which gains a brand-new module (Avisos Institucionais, §4.13) in this very conversation — has no journey at all, leaving two consumer-facing decision points (RF21.2, RF81/RF82) without a walked-through scenario for downstream UX/architecture to build against.

## Decision-readiness — strong

Decisions are stated as decisions, not softened into "considerations." Each "Novo" subsection in §4 is tagged with `(realiza UJ-1)` or `(realiza UJ-2)`, showing exactly which journey forced which choice — e.g., RF21.1's split between comunidade de curso and comunidade aberta is traced to "decisão confirmada em conversa, ver `.memlog.md`" (§4.3), and the memlog entry (line 14) gives the actual rationale ("Evita comunidade órfã criada por aluno que depois abandona o curso/instituição"). Trade-offs are named with what was given up: §5 Non-Goals doesn't just list omissions, it explains the cost of each — enquete segmentation is ruled out permanently "consequência direta do modelo de anonimato real," not a placeholder.

Open Questions (§10) are genuinely open, not rhetorical. Q1 (moderador neutro with a conflict, no second escalation tier) is left unresolved with a real trigger condition ("se o time de moderadores for pequeno o suficiente"). `[NOTE FOR PM]` callouts land on real tensions: §6.2 flags that course-community pre-creation (RF21.2) needs to be repeatable for future course rollouts "sem retrabalho," which is a genuine technical-debt risk, not a safe checkpoint.

### Findings
- **low** Untracked open item (title, line 9) — `*Working title — confirm.*` is a live open item that sits outside both the Open Questions (§10) and Assumptions Index (§11) tracking mechanisms used everywhere else in the document. *Fix:* either resolve the title or fold it into §10 as an explicit open question.

## Substance over theater — strong

No persona theater: exactly two UJs, each driving multiple concrete FRs rather than existing for coverage. UJ-1 (Julia) is cited by name as the rationale for five separate "Novo" requirements (RF01.2, RF01.3, RF20.2, RF21.1/21.2/24.1/27.1); UJ-2 similarly drives RF77.1, RF78.2, RF79.2, RF80.2. Nothing here reads as furniture.

No Vision theater: §1 is anchored to specifics that couldn't swap into another PRD unmodified — "3.000 alunos do Campus Joinville," WhatsApp fragmentation named as the status-quo failure mode, "enquetes que substituem a pesquisa de campo via WhatsApp." No NFR theater either: §7's category summaries cite concrete standards and numbers (OWASP ASVS 4.0.3, WCAG 2.2 AA, p95 ≤ 2s, ISO/IEC/IEEE 29148:2018) rather than "must be secure/scalable" boilerplate — and the PRD is explicit that granular per-requirement acceptance criteria for RNF09 are deferred to the epics/stories phase (§7, last bullet), not silently skipped. No differentiation/innovation section was forced in where Discovery didn't produce one.

## Strategic coherence — strong

The thesis is explicit and load-bearing: solve the "isolamento social e informacional do calouro" by giving every student a ready-made course community from first login (§1). Scope follows from it — MVP is scoped to a single course (Engenharia de Software) and campus (§6.1) specifically to validate that promise before expanding, and every "Novo" decision in §4 is tied back to one of the two UJs that embody the thesis.

Success metrics validate the thesis rather than just measuring activity: SM-1 (activation) is defined as completing signup *and* taking a first-session value action, which is exactly UJ-1's climax ("a promessa central da visão se cumpre na primeira sessão," line 49). The PRD explicitly guards against the DAU/MAU red flag the rubric warns about — SM-3 is kept as a secondary metric "com expectativa calibrada para cadência semanal (uso acadêmico), não diária" (§9), showing self-aware calibration rather than a copied metric. Counter-metrics (SM-C1/C2/C3) are named and each is mapped to the primary/secondary metric it counterbalances. MVP scope kind reads as an experience MVP (validate the isolation-solving experience for one course before expanding), and the scope logic (single course, no monetization, no official-system integration) matches that kind consistently.

## Done-ness clarity — strong

Every "Novo" FR in §4 (RF01.2, RF01.3, RF20.2, RF21.1, RF21.2, RF24.1, RF27.1, RF77.1, RF78.2, RF79.2, RF80.2, RF81, RF82) carries a "Consequências (testáveis)" block with verifiable conditions, not adjectives — e.g. RF82: "Aluno de Engenharia de Software não vê aviso escopado a outro curso" is a direct pass/fail test. No "handles gracefully" or "reasonable performance" language appears anywhere in these blocks.

For the 80 referenced-not-duplicated RF01–RF80, the PRD explicitly defers detail to `docs/unicatolica-pacext-contexto.md` (§0, §4 intro) — per the framing for this review, that's a structural choice, not a gap, and it holds up: spot-checking the source document (§3.1–§3.12) shows each RF is a single imperative, testable statement ("Deve impedir cadastro com e-mail já existente," "Deve invalidar sessão após logout") rather than vague prose. An engineer working only from this PRD does need to open the second document for the base 80 requirements, but that document doesn't degrade the testability once opened.

## Scope honesty — strong

§5 Non-Goals does real work — six items, each with a stated reason, not a bare list (e.g., "Contas não são anônimas nem pseudônimas... distinto do anonimato de *voto* em enquete," which pre-empts a plausible reader confusion). §6.2 restates MVP exclusions with a `[NOTE FOR PM]` where a silent assumption would otherwise creep in. All four inline `[ASSUMPTION]` tags map into the Índice de Suposições (§11) content-wise. De-scoping is argued, not asserted — enquete segmentation removal is justified via the anonymity model's mechanics (§5, §6.2), not just declared unwanted.

Open-items density (5 Open Questions, 4 assumptions, 2 NOTE FOR PM) is proportionate to the stated stakes — "entre 'interno formal' e 'lançamento real'" per the memlog (line 6) — for a document referencing ~93 total FR IDs. Nothing here reads as a green-light-to-build PRD papering over unresolved tensions.

### Findings
- **low** Assumptions Index includes a non-assumption entry (§11, "§6.2 — pré-criação de comunidades de curso deve ser repetível...") — this item is tagged inline as `[NOTE FOR PM: ...]` (§6.2), not `[ASSUMPTION: ...]`, so its presence in the "Índice de Suposições" conflates two distinct callout types. *Fix:* move this entry to a NOTE FOR PM index, or retag it consistently.

## Downstream usability — adequate

The PRD explicitly feeds architecture, UX, and epics/stories (§0: "a quem for dar continuidade técnica ao projeto (arquitetura, UX, épicos/histórias)"), so this dimension carries real weight. The Glossary (§3) is used consistently for the terms checked — "comunidade de curso," "comunidade aberta," "auto-join," "moderador neutro," "ocultar," "remover" all appear identically in the FRs that use them, and "ocultar" (RF78) vs. "excluir" (RF41, user deleting own content) are correctly kept distinct rather than conflated. ID continuity is clean: every new RF (RF01.2/.3, RF20.2, RF21.1/.2, RF24.1, RF27.1, RF77.1, RF78.2, RF79.2, RF80.2, RF81, RF82) extends an existing base ID or a genuinely new one, with no collisions against the source document's own sub-IDs. All module-range citations in §4 (e.g., "RF01–RF13, RF01.1" for §4.1, "RF75–RF80 e subitens" for §4.12) were checked against `docs/unicatolica-pacext-contexto.md` and match exactly.

The gap is UJ protagonist naming: UJ-1's Julia carries context inline (curso, momento, motivação) throughout her journey, but UJ-2's protagonist is introduced only as "representante de turma, moderador por compromisso diário assumido voluntariamente" (§2.3) — a role, not a named person. A downstream UX pass working from UJ-2 has no character to design around the way it does for Julia.

### Findings
- **medium** UJ-2 has no named protagonist (§2.3, UJ-2 header) — inconsistent with UJ-1 and with the rubric's own requirement that UJs carry a named protagonist inline. *Fix:* give the UJ-2 moderator a name and a touch of situational context (e.g., how long they've been a moderator, class size), mirroring Julia's treatment.
- **low** The email-confirmation assumption appears inline twice (§2.3 UJ-1 step 1, and again at RF01.2 in §4.1) but the Assumptions Index (§11) only cites the §2.3 location. *Fix:* either index both locations or note in §11 that RF01.2 restates the same assumption.

## Shape fit — adequate

This is squarely a multi-stakeholder consumer product (aluno, moderador, admin da plataforma are all named roles in the Glossário, §3) with meaningful UX, so per the rubric, UJs with named protagonists should be load-bearing. UJ-1 fits that bill well. But coverage is uneven across the three roles: two UJs exist, both centered on the student/moderator axis, and the admin role — who in this very PRD conversation gained a brand-new module (§4.13, "módulo novo — não coberto por RF01–RF80," RF81/RF82) plus a modified responsibility (RF21.2, pre-creating course communities instead of students) — has zero journey representation. Nothing in the PRD walks through how the admin actually receives content from "a coordenação do curso" (§4.13) and turns it into a published aviso, or how they pre-create a course community for a new cohort.

Given RF81/RF82 and RF21.2 are new-to-this-conversation decisions (the same category of decision that earned Julia's and the moderator's UJs a full write-up), the absence of an admin journey is a shape-fit gap rather than an appropriate omission for a thin back-office role.

### Findings
- **medium** No UJ for the admin persona (§2.3, §4.13) — admin gained a new module (Avisos Institucionais) and a new responsibility (pre-criação de comunidades de curso, RF21.2) in this PRD, the same class of decision that justified UJ-1 and UJ-2, but has no walked-through scenario. *Fix:* add a short UJ-3 for the admin publishing an aviso institucional or pre-creating a course community, or explicitly note in §2.3 why it was scoped out (e.g., low complexity, single admin role in MVP).

## Mechanical notes

- **Glossary drift**: §2.1 (JTBD) uses "informativos institucionais" ("Visualizar informativos institucionais... como feriados, treinamentos e atividades extra classe," line 27) while the Glossário (§3), Features (§4.13), and RF81/RF82 all consistently use "aviso institucional" / "avisos institucionais." The JTBD line should be reworded to the canonical term.
- **ID continuity**: clean. All new IDs (RF01.2/.3, RF20.2, RF21.1/.2, RF24.1, RF27.1, RF77.1, RF78.2, RF79.2, RF80.2, RF81, RF82) extend existing bases or are genuinely new top-level IDs, with no duplicates against the source document.
- **Cross-references**: every §-reference to `docs/unicatolica-pacext-contexto.md` checked (§3.1–§3.12 module ranges, §4 NFR categories/RNF01–09, §7 Interfaces/telas Figma, §9 Decisões de design validadas, R01 risk citation in §9 SM primárias) resolves exactly against the source document's actual section numbering and content.
- **Assumptions Index roundtrip**: 3 of 4 inline `[ASSUMPTION]` locations are cleanly indexed (§2.3/RF01.2 email-confirmation assumption counted once for two inline occurrences — see Downstream usability finding above); one Índice entry (§6.2) is sourced from a `[NOTE FOR PM]` tag rather than an `[ASSUMPTION]` tag (see Scope honesty finding above).
- **UJ protagonist naming**: UJ-1 (Julia) named and carries context inline; UJ-2 (moderador) does not (see Shape fit and Downstream usability findings above).
- **Required sections**: all sections the rubric and the project's own PRD template would expect for a chain-top, multi-stakeholder consumer product are present — Vision, JTBD, Non-Users, UJs, Glossary, Features, Non-Goals, MVP Scope, NFRs, Information Architecture, Success Metrics, Open Questions, Assumptions Index.
