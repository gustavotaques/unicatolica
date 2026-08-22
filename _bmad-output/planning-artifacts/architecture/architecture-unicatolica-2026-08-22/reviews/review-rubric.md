# Rubric Review — ARCHITECTURE-SPINE.md (UniCatólica / PACEXT)

Reviewed: `_bmad-output/planning-artifacts/architecture/architecture-unicatolica-2026-08-22/ARCHITECTURE-SPINE.md`
Cross-checked against: `.memlog.md` (this run's decision log) and `docs/unicatolica-pacext-contexto.md` (adopted academic context/stack/C4).

## Verdict

Strong, mostly complete spine — it correctly formalizes the already-adopted stack/paradigm from the context doc and adds nine well-targeted ADs that cover the real parallel-work seams (contract, error format, auth, module boundaries, CI/deploy, migrations, local env). Two gaps keep it from a clean pass: the operational/observability dimension is entirely silent, and AD-3's module-boundary rule has no enforcement mechanism — which matters more than usual because AD-8 explicitly waives mandatory human code review.

## Checklist walk

### 1. Fixes the real divergence points for a 4-person team / 12 modules / monolith / FE-BE split

Covered well: stack (AD-1), single JWT filter (AD-2), module table ownership + read/write rules (AD-3), OpenAPI-first contract with CI gate (AD-4), uniform error envelope (AD-5), hosting/CORS (AD-6), reproducible local env (AD-7), CI/CD + merge gate (AD-8), migrations via Liquibase (AD-9). This is the right set of seams for a horizontally-split (FE vs BE) student team shipping in a week.

**Gap found — operational/environmental envelope is not fully decided.** See §6 below; this is the most material miss.

### 2. Every AD's Rule is enforceable and actually prevents its divergence

Most ADs are concretely enforceable (specific frameworks/tools, a CI check for AD-4, a fixed JSON shape for AD-5, specific hosting targets for AD-6). Two exceptions:

- **AD-1**: the clause "TDD+SOLID; DDD focado no núcleo" is aspirational language with no enforcement path stated (no coverage gate, no architecture test, no linter rule) — everything else in AD-1 (language/framework choice) is trivially checkable, but this clause isn't. Minor, since it's inherited verbatim from the already-adopted context doc rather than invented here, but the spine is the place that should have added the enforcement mechanism if one exists.
- **AD-3** (module boundaries — the rule most likely to actually get violated under deadline pressure): "escrita em tabela de outro módulo só através do Service... acesso direto a repositório/tabela alheios é proibido" is stated as a convention, not backed by any technical mechanism (e.g., package-private repository classes, a module/package structure that makes cross-module repository imports impossible, or an ArchUnit-style test in CI). **This combines badly with AD-8**, which explicitly removes mandatory human review from the merge gate ("sem revisão humana obrigatória"). With 4 people racing in parallel and nobody required to look at anyone else's diff, a convention-only rule is exactly the kind of AD most likely to be silently violated — and nothing in CI would catch it. Recommend either a lightweight enforcement mechanism (package-private JPA repositories per module, or a CI architecture test) or, at minimum, an explicit acknowledgment that this rule relies on discipline alone given AD-8.

### 3. Nothing under Deferred could let two units diverge before revisited

Deferred items were checked individually — the 9 out-of-scope modules extend the same ADs, the AI-moderation agent and file-storage choice are out of week-1 build scope, Render cold start and monorepo-vs-multirepo are explicitly flagged as revisit points. No divergence risk found here, **except one fidelity issue against the memlog**:

**Finding — the Deferred module list collapses a three-tier scope decision into two tiers.** The memlog records the week-1 must-have as RF01–13 + RF21–31 + RF32–36, with RF14–20 (Perfil) and RF37–42 (Discussões) explicitly marked *"stretch goal se sobrar tempo — não bloqueiam a entrega funcional"* — i.e., conditionally in scope, not deferred. The spine's Deferred bullet lists "Perfil além do default de cadastro, Discussões/threading" together with the genuinely out-of-scope modules (Filtro, Materiais, Enquetes, Busca, Notificações, Mensagens, Moderação) under one heading, "Os 9 módulos fora do corte da semana 1," with no distinction. A team reading only the spine would reasonably conclude Perfil/Discussões are off the table for week 1, when the actual team decision left the door open if time permits. Low-to-moderate severity — doesn't break the architecture, but is a loss of fidelity to the recorded decision that could cost the team a stretch goal they were allowed to take.

### 4. Named tech is verified-current / internally consistent

Angular ^22 (jun/2026), Java 21 LTS, Quarkus 3.33 LTS (mar/2026–mar/2027), Liquibase via `quarkus-liquibase`, Render, Neon — all plausible and internally consistent with each other and with the memlog's version note. One low-severity flag:

- **Java 21 LTS vs Java 25 LTS.** Java 25 is the newer LTS (released Sept 2025); as of the document's own date (2026-08-22) it's been available for ~11 months. Choosing 21 over 25 for a brand-new project isn't wrong (21 is still fully supported), but the spine doesn't say *why* — presumably Quarkus 3.33's Java-version support/recommendation at the time favors 21. Worth one line of justification so it doesn't read as an oversight.
- **Neon free-tier compute autosuspend not mentioned.** The spine correctly flags Render's free-tier Postgres *data-expiry* risk (30+14 days) as the reason to use Neon instead, and separately flags Render's own free-tier *cold start* under Deferred. But Neon's free tier also autosuspends idle compute (data persists, but the next query pays a cold-start cost) — this is a different mechanism than what's documented, and could compound with Render's own cold start on the very "demo" scenario the Deferred section already worries about. Low severity, but the Deferred entry on cold starts would be more complete if it accounted for both layers.

### 5. Ratifies rather than contradicts the context doc

Confirmed by direct comparison: stack table (§5), module set and C4 level 3 boundaries (§6.3), JWT-in-header transport, and the enquete `enquete_participacao`/`enquete_voto` split (§3.8) are all carried forward faithfully, not redesigned. The Structural Seed explicitly declines to redesign the ERD for deferred modules rather than inventing new shape. No contradictions found.

### 6. Every initiative-altitude dimension is decided/deferred/open — operational envelope check

Deployment (AD-6, AD-8), infra/provider strategy (Render + Neon, AD-6), local dev environment (AD-7), and CI/CD (AD-8) are all explicitly decided. **Operations/observability is not** — this is the clearest whole-dimension gap:

- No monitoring, error-tracking, or health-check decision anywhere (application-level, as opposed to the RNF07 security/moderation audit log, which *is* covered under Consistency Conventions). Given RNF03 sets a p95 ≤ 2s performance target, and the Deferred section itself flags Render cold-start as a real risk to "revisit before any high-risk demo," there's no stated mechanism for anyone to *notice* a performance or availability regression in the first place — not decided, not deferred, not even raised as an open question.
- No backup/disaster-recovery statement for the Neon-hosted data beyond "it doesn't expire like Render's does."
- Environments: only local dev (AD-7) and production (AD-6/AD-8, deploy triggers straight off `main`) are named; the absence of a staging/pre-prod tier is a reasonable call for a one-week build but is never stated as a decision — it's only inferable from the absence of any other environment.

Recommend adding at minimum one Deferred (or Open Question) line naming observability/monitoring explicitly, so it reads as a conscious scope cut rather than an omission.

## Summary of findings by severity

| # | Severity | Finding |
|---|---|---|
| 1 | Moderate | Operational/observability dimension (monitoring, error tracking, health checks, backup/DR) is entirely silent — not decided, deferred, or raised as an open question, despite RNF03 and the already-flagged cold-start risk. |
| 2 | Moderate | AD-3's module-boundary rule (no cross-module repository writes) has no enforcement mechanism, and AD-8 removes mandatory human review — nothing technical stops the rule from being silently violated under deadline pressure. |
| 3 | Minor | Deferred section flattens the memlog's three-tier scope (must-have / stretch-goal / deferred) into two tiers, losing the "Perfil and Discussões are stretch goals, not deferred" nuance. |
| 4 | Minor | AD-1's "TDD+SOLID; DDD focado no núcleo" clause is aspirational, not enforceable as written. |
| 5 | Low | No stated environments strategy (implicit local+prod only, no staging) — reasonable but never named as a decision. |
| 6 | Low | Java 21 LTS chosen over newer Java 25 LTS without justification; Neon free-tier compute autosuspend not mentioned alongside the Render cold-start risk it compounds with. |

No contradictions of the adopted context doc were found, and no stale/implausible tech pins were found beyond the low-severity Java-version note above.
