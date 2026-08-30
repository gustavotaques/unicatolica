# Review Lens: Adversarial — Two Compliant Units That Still Clash

**File reviewed:** `ARCHITECTURE-SPINE.md`
**Reviewer lens:** Construct two units one level down that each obey every AD to the letter yet still build incompatibly — clashing shared-data shapes, two owners of one entity, conflicting state-mutation paths. Every pair found is a hole to close with a new or tightened AD.
**Date of review:** 2026-08-22

## Verdict

The spine's ADs are strong on *what module owns which table* but weak on *the shapes and protocols that cross module and frontend/backend boundaries* — pagination, JSON field casing, JWT claim schema, error-code-to-scenario mapping, CORS configuration locus, cross-module mutation contracts, and "CI validates the contract" are all left concrete enough to sound settled but loose enough for two AD-compliant builders to diverge. 14 concrete incompatible-pair findings below (14 ≥ required 10); most cluster around AD-3 (module boundary read/write gap), AD-4/AD-8 (contract enforcement vagueness), and AD-5 (error envelope under-specification).

## Findings

### 1. AD-3's "leitura via JPA association" doesn't forbid a write disguised as a read
**Severity: Critical**
**Pair:** Backend Dev A owns **Comunidades** (entity `Comunidade`, field `total_membros` denormalized counter). Backend Dev B owns **Publicações**, whose `Publicacao` entity holds `@ManyToOne Comunidade comunidade` per the spine's own diagram (`c4 -.->|"leitura via JPA association"| c3`).
**Clash:** Dev B, inside `PublicacaoService.criar()`, does `publicacao.getComunidade().setTotalMembros(comunidade.getTotalMembros() + 1)` to keep a denormalized count in sync while "just reading the association for context." Because the `Comunidade` instance is a managed JPA entity within the same transaction (single deploy unit, one `EntityManager`), Hibernate's dirty-checking flushes that mutation to the `comunidade` table on commit — with zero call into `ComunidadeService`. Dev A never authorized this write path, never validated it, and it bypasses whatever invariants `ComunidadeService` enforces. AD-3's text ("leitura... via associação JPA direta é permitida... escrita... só através do Service do módulo dono") never defines "escrita" operationally — a mutation on a JPA-managed object *reached via a read* is exactly the loophole. Both devs can honestly claim compliance.
**Fix:** Tighten AD-3 to require that cross-module JPA reads use a read-only/detached projection (e.g. `@Immutable` entity, DTO projection, or explicit `entityManager.detach()`/read-only transaction) so an accidental mutation cannot flush. State explicitly: "toda leitura cross-módulo é somente-leitura ao nível de transação — sessão marcada `FlushMode.MANUAL` ou entidade projetada, nunca a entidade gerenciável completa do módulo dono."

### 2. No defined mutation contract for a module that needs to act on another module's entity
**Severity: High**
**Pair:** Backend Dev B (**Publicações**, owns `PUBLICACAO`) vs. a future Backend Dev C (**Moderação**, deferred but same AD-3 applies per the spine's own "Deferred" note).
**Clash:** Moderação needs to remove/hide a publicação (RF-adjacent to denúncia handling). AD-3 says writes go "através do Service do módulo dono" but specifies neither the method signature nor the integration style. Dev C could reasonably build either (a) a synchronous call `PublicacaoService.moderar(id, motivo)` that Moderação's Resource invokes directly, or (b) an event/outbox pattern where Moderação publishes a "remover" intent and Publicações' Service consumes it asynchronously. Both satisfy "escrita só via Service do dono." If Dev B ships `PublicacaoService` with no `moderar()` method at all (not needed for week-1 scope) and Dev C later builds against an assumed interface, the two are incompatible by construction, and nothing in the spine forces the interface to be negotiated ahead of time the way AD-4 forces REST contracts to be negotiated.
**Fix:** Extend AD-3 (or add AD-3b) requiring that any cross-module *write* capability be declared as a Java interface (not just "the Service class") published in the owning module's package, with the same "agree before implementing" discipline AD-4 already applies to REST contracts.

### 3. "CI valida a implementação contra o contrato" doesn't specify what class of drift is actually caught
**Severity: Critical**
**Pair:** Backend Dev B (**Publicações**) implements OpenAPI validation via compile-time generated JAX-RS interfaces (`openapi-generator` produces `PublicacoesApi` interface; `PublicacaoResource implements PublicacoesApi` — mismatched method signatures fail the build). Frontend Dev 1 (**Angular / Publicações UI**) generates an HTTP client from the same `openapi.yaml` and assumes response *payloads*, not just signatures, are enforced.
**Clash:** The generated-interface approach only fails compilation on path/verb/parameter-type mismatches — it does not verify that the actual JSON returned at runtime matches the schema (extra fields, wrong enum casing, a field silently made nullable, wrong array vs. object at top level). Dev B's CI is green. Frontend's generated client then deserializes a response that technically diverges from `openapi.yaml` in ways compile-time codegen never checked (e.g., backend renames `dataPublicacao` to `criadaEm` in the DTO but never updates `openapi.yaml`'s example/schema tightly enough to fail codegen). Since there's no mandatory human review (AD-8), this ships to `main` and breaks the frontend at runtime. Both sides can each honestly say "I did what AD-4 requires."
**Fix:** AD-4 should name the *mechanism*, not just the intent — e.g. mandate a runtime contract test (schema validation against actual HTTP responses using a tool like `openapi-core`/`Dredd`/Quarkus's own `@Test` + JSON-schema assertion) as the CI gate, not merely generated-interface compilation. "CI valida contra o contrato" as currently written is a slogan, not a spec.

### 4. Pagination shape is entirely unspecified — every list endpoint can diverge
**Severity: High**
**Pair:** Backend Dev A (**Comunidades**, `GET /comunidades/{id}/membros`) and Backend Dev B (**Publicações**, `GET /comunidades/{id}/publicacoes` — the feed).
**Clash:** Nothing in AD-4/AD-5/Consistency Conventions defines a shared pagination envelope. Dev A ships Quarkus/Panache's natural default (`{content: [...], totalElements, totalPages, number}` Spring-Data-esque idiom) with `page`/`size` query params, 0-indexed. Dev B, working independently, ships `{items: [...], total, cursor}` with a `pageToken` cursor param. Both are documented faithfully in their slice of `openapi.yaml` (AD-4 only requires the *documented* contract match the *implemented* one — it says nothing about cross-endpoint consistency), and CI happily validates each in isolation. The Angular frontend now needs two entirely different list-rendering abstractions for what the UX spec likely treats as one interaction pattern (infinite scroll / paginated list).
**Fix:** Add a Consistency Convention row (or new AD) fixing one pagination envelope and param naming for all list endpoints across all 12 modules, referenced by a shared OpenAPI schema component (`PageResponse`) that every module's paths `$ref`.

### 5. AD-5's error envelope doesn't map scenarios to codes — same logical error, different HTTP status across modules
**Severity: High**
**Pair:** Backend Dev A (**Comunidades**) handles "usuário tenta acessar comunidade privada da qual não é membro." Backend Dev B (**Publicações**) handles "usuário tenta editar publicação de outro autor."
**Clash:** AD-5 lists valid codes (400/401/403/404/409/422/500) but gives no decision rule for *which* applies to "authenticated but not authorized for this specific resource." Dev A returns `404` (hide the resource's existence — a defensible security posture). Dev B returns `403` (resource existence is not sensitive, but action is forbidden). Both are "semanticamente corretos" in isolation and both match the envelope shape byte-for-byte — AD-5 is satisfied to the letter by both, yet the frontend's shared HTTP-error interceptor (built once, per AD-1's single Angular SPA) now needs per-module special-casing to render "not found" vs. "forbidden" UX for what the PRD probably treats as the same class of authorization failure.
**Fix:** Add a decision table to AD-5 (or a new AD) mapping specific scenario classes (not-authenticated / authenticated-not-authorized-hide-existence / authenticated-not-authorized-show-existence / not-found / conflict / validation) to codes, so authorization-driven 403-vs-404 choices aren't left to individual module owners.

### 6. JWT claims schema is unspecified — issuer and consumers can disagree on shape
**Severity: Critical**
**Pair:** Backend Dev A (**Identidade e Acesso**, the token *issuer*) vs. Backend Dev B (**Comunidades**, a token *consumer* enforcing RF13 authorization, which AD-2 explicitly delegates to "cada módulo, não o filtro").
**Clash:** AD-2 pins the *transport* (`Authorization: Bearer`, never cookie) and *that* a single filter validates the token, but never pins the *claims schema* — claim names for user id, role list, or community-scoped roles. Dev A issues tokens with `{"sub": "123", "groups": ["ALUNO"]}`. Dev B's authorization code, written independently against RF13 ("perfil de acesso"), expects `{"userId": "123", "roles": ["ALUNO"]}`. Both comply with AD-2's literal text (the filter still validates signature/expiry centrally; only the *authorization*, correctly left to each module, reads claims) — but Dev B's authorization silently fails open or closed depending on how missing-claim handling is written, and nothing in AD-2 would have caught it since the filter's job (per AD-2) is validation, not claims-contract enforcement.
**Fix:** Add to AD-2 a fixed JWT claims contract (claim names for subject/user id, role list, and how community-scoped roles are represented, if at all) — treat it with the same rigor as the error envelope in AD-5.

### 7. CORS configuration locus is unspecified — module-by-module CORS headers can collide
**Severity: Medium**
**Pair:** Backend Dev A (**Comunidades**) and Backend Dev B (**Publicações**), both reading AD-6 ("Backend libera explicitamente a origem do Static Site via CORS") as a per-module responsibility since AD-6 is stated as an outcome, not a mechanism, and AD-2 (the only AD that mandates centralization) covers *auth*, not CORS.
**Clash:** Dev A adds `@Provider` `ContainerResponseFilter` setting `Access-Control-Allow-Origin` scoped to Comunidades' `/comunidades/*` paths. Dev B, unaware, does the same for `/publicacoes/*` with a slightly different allowed-origin list (e.g. one includes the Render preview-deploy wildcard, the other doesn't). Since JAX-RS filters can double-apply or one can override the other depending on registration order (which the spine never assigns, since there's no single point of CORS ownership analogous to AD-2's "um único filtro JAX-RS"), browsers may reject responses with duplicate `Access-Control-Allow-Origin` headers, and behavior differs by which module's endpoint is hit — both devs followed AD-6's text exactly.
**Fix:** Extend AD-2 (or AD-6) to state CORS is configured exactly once, centrally (e.g. `quarkus.http.cors` properties, not per-resource filters), the same way JWT validation is centralized.

### 8. AD-2 says the JWT filter intercepts "toda requisição" literally — no carve-out for public endpoints
**Severity: High**
**Pair:** Backend Dev A (**Identidade e Acesso**, builds `POST /auth/login` and `POST /auth/registro` — RF01/RF02, necessarily unauthenticated) and whoever implements the shared JWT filter (could be Dev A or a rotating owner, per AD-2's "um único filtro").
**Clash:** AD-2's rule text is unconditional: "um único filtro JAX-RS intercepta toda requisição, valida o token JWT... antes de encaminhar a qualquer componente de módulo." Read literally, `/auth/login` itself would be blocked by its own precondition (no token exists yet to log in). One compliant implementation adds a hardcoded `@PermitAll`/path-exclusion list inside the filter (works, but is now a second, undocumented source of truth for "which endpoints are public" that every new module owner must remember to update). Another compliant-by-the-letter implementation team could instead literally apply the filter everywhere and issue a special pre-auth "anonymous" system JWT for login/registration flows to satisfy "toda requisição" without exception — a fundamentally different, heavier auth model. Both readings satisfy AD-2's text; they are not interoperable designs.
**Fix:** Amend AD-2 to explicitly acknowledge and name the public-endpoint exclusion mechanism (e.g. `@PermitAll` allowlist maintained where and by whom), rather than leaving "toda requisição" as an absolute that every implementer must silently soften.

### 9. JSON field casing/language is unspecified at the field level — only paths and DB are pinned
**Severity: Medium**
**Pair:** Backend Dev A (**Identidade e Acesso**) and Backend Dev B (**Comunidades**).
**Clash:** The Consistency Conventions table pins DB/entity naming to Portuguese and REST *paths* to English, but says nothing about JSON *field* names in request/response bodies. Dev A, whose entity is `Usuario{nomeCompleto, email}`, lets Jackson serialize field names as-is: `{"nomeCompleto": ..., "email": ...}`. Dev B, treating the "paths em inglês por convenção Java/JAX-RS" note as extending to the whole API surface, hand-writes DTOs with English fields: `{"name": ..., "description": ...}`. Both are compliant with the letter of the conventions table (neither rule technically covers JSON field casing/language), producing an API surface where Identidade endpoints use Portuguese field names and Comunidades endpoints use English ones — for one Angular SPA (AD-1) consuming both.
**Fix:** Add an explicit row to Consistency Conventions pinning JSON body field naming (language + case convention, e.g. camelCase English or camelCase Portuguese) independent of the DB and path conventions.

### 10. Cross-module Liquibase changelog merge is not addressed — CI-green-per-PR doesn't guarantee CI-green-post-merge
**Severity: Critical**
**Pair:** Backend Dev A (**Comunidades**) and Backend Dev B (**Publicações**), each adding a migration in parallel feature branches around the same time.
**Clash:** AD-9 mandates "Liquibase changelogs versionados no repo" but specifies no per-module file/id namespacing scheme, no master-changelog append discipline, and no cross-PR migration-ordering rule. Dev A's branch adds changeset id `002-comunidade-add-campo` appended to the master `db.changelog-master.xml`; Dev B's branch, cut from the same base before Dev A merged, appends its own changeset also intending to be "002" (or inserts at the same include position). Each PR's CI runs migrations against a fresh DB and passes independently (AD-8's gate is per-PR). Because AD-8 requires no human review, both merge to `main` in succession — the second merge's migration set was never tested combined with the first's actual final state (only against `main` at the moment its branch was cut, if even rebased), and Liquibable is checksum/order-sensitive. This is exactly the blind spot CI-green-only + no-human-review creates, and AD-9 does nothing to close it.
**Fix:** Add to AD-9 a concrete collision-avoidance scheme (e.g. changeset ids prefixed by module + timestamp, one changelog file per module auto-included by a stable master file that never needs a shared line edited) and require CI to re-run the full migration set against `main`'s HEAD (not just the PR branch's base) before merge — or explicitly flag this as a known gap given no mandatory review.

### 11. Audit log ("mecanismo transversal") has no assigned owner — two independent implementations likely
**Severity: Medium**
**Pair:** Backend Dev A (**Identidade e Acesso**, logging login events per RNF07) and Backend Dev B (**Comunidades**, logging "alteração administrativa de comunidade" per the same RNF07 reference in the Consistency Conventions table).
**Clash:** The spine states the audit log "existe desde a semana 1" and is "transversal," but assigns it to no module and defines no shared table/service. Each dev, reading AD-3 ("cada módulo é dono das próprias tabelas"), reasonably concludes their own module owns its own audit trail and creates `identidade_log_auditoria` and `comunidade_log_auditoria` independently — both technically satisfying "log de auditoria existe," neither producing the single queryable audit trail RNF07 (and eventual Moderação-module consumption) implies. Alternatively, if both instead assume a *shared* table is intended and both write a Liquibase changeset creating `log_auditoria` with different column sets, that's the AD-9 collision from Finding 10 again, now guaranteed by ambiguity rather than bad luck.
**Fix:** Add an AD (or extend AD-3) explicitly assigning audit-log ownership to a named cross-cutting component (e.g. its own tiny "Auditoria" module with a shared table and injectable service every module calls), so it isn't independently reinvented per module.

### 12. Authorization composition between global role and community-scoped role is unspecified
**Severity: High**
**Pair:** Backend Dev A (**Identidade e Acesso**, owns the global role concept — RF13, "perfil de acesso") and Backend Dev B (**Comunidades**, owns `COMUNIDADE_MEMBRO` and any community-local role like "admin da comunidade").
**Clash:** AD-2 explicitly delegates fine-grained authorization to "cada módulo, não o filtro." Dev A's global roles (e.g. `ALUNO`, `PROFESSOR`, `COORDENADOR`) and Dev B's community-scoped roles (e.g. `MEMBRO`, `ADMIN_COMUNIDADE`) are two independent authorization axes. A Publicações endpoint ("only a community admin or a global coordenador can pin a post") needs both axes combined, but the spine gives no precedence/composition rule. Backend Dev C (**Publicações**) building that check could OR the two role sets, AND them, or check only one — depending on which module's authorization pattern they copied from. Since RF13 enforcement is explicitly "responsabilidade de cada módulo" (AD text, Consistency Conventions row), three different modules can each build a self-consistent but mutually incompatible authorization model.
**Fix:** Add a Consistency Convention (or AD) defining how global and scoped roles compose for authorization checks, or centralize the composition logic in a shared library function every module calls rather than reimplementing per module.

### 13. Date-only vs instant fields: the "Instant, ISO-8601 UTC" convention doesn't cover date-only domain values
**Severity: Low**
**Pair:** Backend Dev A (**Identidade e Acesso**, might need "data de nascimento" or similar date-only field on `Perfil`) and Backend Dev B (**Publicações**, timestamps like `dataPublicacao`, inherently instant-like).
**Clash:** The Consistency Conventions row pins "Datas em ISO-8601 UTC (`Instant`, padrão Quarkus/Jackson)" without distinguishing date-only values (no meaningful time-of-day or timezone, e.g. birth date) from true instants (creation timestamps). A literal reading forces Dev A to store birth date as `Instant` (midnight UTC on some day), which then renders as the *previous* local day for any Brazil-timezone (UTC-3) frontend user displaying it naively — a real, silent off-by-one-day bug — while Dev B's genuine timestamps are correctly modeled as `Instant`. Both comply with the convention's literal text.
**Fix:** Add a carve-out: date-only domain values use `LocalDate` (no timezone conversion), true instants use `Instant` — state both explicitly rather than one blanket rule.

### 14. "CI-only gate" interacts with AD-4 to create a race on `openapi.yaml` itself
**Severity: Medium**
**Pair:** Frontend Dev 1 and Backend Dev A, both working the **Comunidades** slice in parallel per AD-4 ("acordado entre frontend e backend antes de qualquer lado implementar").
**Clash:** AD-4 requires agreement on `openapi.yaml` *before* implementation, but the enforcement mechanism (AD-8: CI-green, no human review) only checks a given PR's implementation against whatever `openapi.yaml` looks like *at that PR's branch point*. If Frontend Dev 1 and Backend Dev A both branch from the same commit, each independently edits `openapi.yaml` in incompatible ways to reflect their own (differently-remembered) version of the "agreement" — e.g. Frontend adds an optional `bio` field to the member-profile schema, Backend adds a required `curso` field to the same schema in a separate PR — both PRs are individually CI-green (each validates its own implementation against its own edited copy of the contract), and whichever merges second wins the `openapi.yaml` diff, silently discarding the other's schema change with no human review to catch the semantic loss. The "agreement" AD-4 requires has no artifact or enforcement beyond "they talked about it."
**Fix:** Require `openapi.yaml` changes to go through a lighter-weight but *mandatory* async review step (even a bot-enforced "both module owners' GitHub approval on files matching `openapi.yaml`" branch-protection rule) as a narrow, deliberate exception to AD-8's no-human-review policy — scoped only to the contract file, not full PR review — since AD-4 already treats the contract as requiring human agreement and AD-8 currently provides no mechanism to enforce that agreement actually landed.

## Summary Table

| # | Finding | Severity | AD(s) implicated |
| --- | --- | --- | --- |
| 1 | JPA-managed read enables undetected write | Critical | AD-3 |
| 2 | No cross-module mutation contract discipline | High | AD-3 |
| 3 | "CI validates contract" doesn't name the mechanism | Critical | AD-4, AD-8 |
| 4 | Pagination shape unspecified | High | AD-4, Conventions |
| 5 | Error code-to-scenario mapping unspecified | High | AD-5 |
| 6 | JWT claims schema unspecified | Critical | AD-2 |
| 7 | CORS configuration locus unspecified | Medium | AD-6, AD-2 |
| 8 | "Toda requisição" has no public-endpoint carve-out | High | AD-2 |
| 9 | JSON field naming language/case unspecified | Medium | Conventions |
| 10 | Cross-module Liquibase changelog collisions | Critical | AD-9, AD-8 |
| 11 | Audit log has no assigned owner | Medium | AD-3, Conventions |
| 12 | Global vs. scoped role composition unspecified | High | AD-2, RF13 |
| 13 | Date-only vs. instant fields conflated | Low | Conventions |
| 14 | `openapi.yaml` itself has no enforced-agreement mechanism | Medium | AD-4, AD-8 |
