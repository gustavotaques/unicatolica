# Review Lens: Web-Verification of Committed Decisions

**File reviewed:** `ARCHITECTURE-SPINE.md`
**Reviewer lens:** Verify every committed decision was web-researched or reality-checked rather than asserted from training data.
**Date of review:** 2026-08-22

## Verdict

All version/EOL/pricing claims spot-checked against the web this session are accurate as of 2026-08-22; the two gaps worth flagging are (1) a newer Java LTS (25) and a newer OpenAPI version (3.2.0) exist and weren't acknowledged even though the spine pins to older-but-still-valid choices, and (2) no failure-mode evidence (e.g. an actual GitHub issue/community report) was gathered for the Neon "never expires" and Render "native GitHub auto-deploy" claims beyond vendor marketing/docs pages — vendor self-reporting is the weakest form of confirmation for exactly the kind of claim this lens exists to catch.

## Item-by-Item Findings

### 1. Angular ^22 — CONFIRMED
Angular v22 shipped June 3, 2026 (six-month cadence from v21). Spine's "estável, jun/2026" annotation is accurate.
Source: [Angular Blog — Announcing Angular v22](https://blog.angular.dev/announcing-angular-v22-c52bb83a4664), [HeroDevs Angular version history](https://www.herodevs.com/blog-posts/angular-version-history-every-release-date-support-window-and-end-of-life-date-from-angularjs-to-angular-22)

### 2. Java 21 LTS — CONFIRMED, but MINOR STALENESS RISK not flagged in spine
Java 21 is a valid LTS with Oracle premier support through Sept 2028 (extended through Sept 2031); free commercial use of Oracle JDK 21 specifically ends Sept 2026 (open-source/other-vendor builds like Temurin are unaffected). However, **JDK 25 LTS has been GA since September 2025** — a newer LTS was available at spine authoring time and the spine doesn't mention having considered/rejected it. Choosing 21 isn't wrong (very common enterprise choice, wider ecosystem/tooling maturity, Quarkus 3.33 baseline likely targets it), but the spine presents "Java 21 LTS" as a bare fact without acknowledging a newer LTS exists — reads as asserted, not reality-checked against current options.
**Severity: Low.** Recommend adding one clause noting 21 was chosen over 25 for ecosystem maturity/tooling, so the choice reads as deliberate rather than default.
Source: [Oracle Java SE Support Roadmap](https://www.oracle.com/java/technologies/java-se-support-roadmap.html), [Azul — Java 21 EOFCU FAQ](https://www.azul.com/products/core/java-21-end-of-free-commercial-use-faq/)

### 3. Quarkus 3.33 LTS — CONFIRMED, claim is precisely accurate
Released March 25, 2026; LTS support window runs 12 months to March 25, 2027. Matches the spine's "mar/2026 — suporte até mar/2027" annotation almost exactly (spine rounds to month, actual date is the 25th — immaterial).
Source: [Quarkus Blog — 3.33 LTS released](https://quarkus.io/blog/quarkus-3-33-released/), [quarkusio/quarkus Wiki — 3.33 LTS Release Planning](https://github.com/quarkusio/quarkus/wiki/3.33-LTS-Release-Planning), [Red Hat Developer — RHBQ 3.33](https://developers.redhat.com/articles/2026/07/03/red-hat-build-quarkus-3-33-lts-release)

### 4. Hibernate ORM via Quarkus BOM — REASONABLE, not independently version-checked
This is standard, well-documented Quarkus practice (Hibernate ORM version is pinned transitively by whichever `quarkus-bom` version is imported) rather than a claim with an expiration date. No specific Hibernate version number is asserted in the spine, so there's nothing stale to verify — this entry is a mechanism description, not a fact claim. No issue.

### 5. `quarkus-liquibase` extension exists and is current — CONFIRMED
Extension is live at `io.quarkus:quarkus-liquibase`, documented at quarkus.io/extensions and Maven Central, actively maintained as part of core Quarkus (not a community/3rd-party add-on). The spine's choice of Liquibase over Flyway is a preference call, not a factual risk — both extensions exist and are current; Liquibase's existence claim checks out.
Source: [Quarkus Extensions — Liquibase](https://quarkus.io/extensions/io.quarkus/quarkus-liquibase/), [Maven Repository — io.quarkus:quarkus-liquibase](https://mvnrepository.com/artifact/io.quarkus/quarkus-liquibase)

### 6. Render Static Site + Web Service, native GitHub auto-deploy — CONFIRMED, docs-level only
Both product names are current and correctly used: "Static Sites" (free CDN, auto-deploy from Git) and "Web Services" (supports Docker deploys) are Render's actual product names per current docs. GitHub integration auto-deploys on push to the configured branch, configurable to gate on CI checks passing — matches spine's AD-8 description of "deploy automático via integração nativa GitHub↔Render."
**Caveat:** confirmed via Render's own documentation pages, not an independent/community source. Reasonable for product-naming facts (low volatility) but weaker for behavioral claims — see Finding 8 below.
Source: [Render Docs — Static Sites](https://render.com/docs/static-sites), [Render Docs — Web Services](https://render.com/docs/web-services), [Render Docs — Connect GitHub](https://render.com/docs/github)

### 7. Render free Postgres expiration (30 days + 14-day grace) — CONFIRMED, exact match
Render's own changelog confirms free Postgres instances expire after 30 days (reduced from 90 days as of a 2024 change), followed by a 14-day grace period to upgrade before permanent deletion. Spine's "expira em 30 dias + 14 dias de carência antes de apagar os dados" is precisely correct — this is the single most safety-critical claim in the stack table (data-loss risk) and it checks out exactly.
Source: [Render Changelog — Free PostgreSQL instances now expire after 30 days](https://render.com/changelog/free-postgresql-instances-now-expire-after-30-days-previously-90)

### 8. Neon free tier "persistente" / no expiration — CONFIRMED, but same single-source caveat as #6
Neon's free tier is a permanent (non-trial) offering: no credit card required, doesn't expire, 0.5GB storage, 100 compute-hours/month, compute scales to zero after 5 min idle (which is itself a latency/cold-start property AD-6/Deferred section doesn't mention for Neon specifically — only Render's cold start is called out in Deferred). This is the load-bearing decision in AD-6 (why Neon over Render's own Postgres) and it holds up.
**Minor gap:** the spine's Deferred section flags Render free-tier cold start (~1 min after 15 min idle) as a documented risk but says nothing about Neon's own compute-scale-to-zero behavior (5 min idle → compute suspends, next query pays a cold-start-like reconnect latency). Worth a one-line addition to Deferred for symmetry, since it's the same category of "free tier has to reawaken" risk.
Source: [Neon FAQ — managed Postgres free tier](https://neon.com/faqs/managed-postgres-databases-free-tier), [neondatabase/website FAQ source](https://github.com/neondatabase/website/blob/main/content/faqs/managed-postgres-databases-free-tier.md)

### 9. OpenAPI 3.1.0 — CONFIRMED as a valid, current spec version, but NEWER VERSION EXISTS unacknowledged
OpenAPI 3.1.0 is real, current, and JSON Schema 2020-12 aligned. However, **OpenAPI 3.2.0 has been out since September 19, 2025** (hierarchical tags, first-class streaming, custom HTTP methods, zero breaking changes from 3.1) — a newer minor version was available well before this spine's authoring date and isn't mentioned. The spine cites 3.1.0 as "per RNF08," i.e., inherited from the PRD rather than chosen fresh here, so this may be a pre-existing constraint outside the architecture's discretion — but the spine doesn't say that explicitly, so a reader can't tell whether 3.1.0 was reality-checked against 3.2.0 and rejected, or just inherited without a check.
**Severity: Low-Medium.** Recommend one clause: "3.1.0 per RNF08 (PRD constraint); 3.2.0 exists but wasn't evaluated / doesn't materially change tooling choice at this scope."
Source: [OpenAPI Specification v3.2.0](https://spec.openapis.org/oas/v3.2.0.html), [Nordic APIs — What's New in OpenAPI 3.2.0](https://nordicapis.com/whats-new-in-openapi-specification-v3-2-0/)

### 10. Docker, GitHub Actions — NOT INDEPENDENTLY FLAGGED
Neither claim carries a version/EOL assertion in the spine (no pinned Docker or Actions runner version stated), so there's nothing time-sensitive to verify here — both are described generically ("build/deploy do backend," "CI"). No issue.

## Summary Table

| Claim | Status | Severity if issue |
| --- | --- | --- |
| Angular ^22, jun/2026 | Confirmed | — |
| Java 21 LTS | Confirmed, but newer LTS (25) unacknowledged | Low |
| Quarkus 3.33 LTS, mar/2026–mar/2027 | Confirmed, exact | — |
| Hibernate ORM via Quarkus BOM | Not a checkable fact claim | — |
| `quarkus-liquibase` extension | Confirmed exists/current | — |
| Render Static Site + Web Service + GitHub auto-deploy | Confirmed (vendor docs only) | Low (source diversity) |
| Render free Postgres: 30d + 14d grace | Confirmed, exact | — |
| Neon free tier persistent/no expiration | Confirmed (vendor docs only); compute-idle suspend not mentioned in Deferred | Low |
| OpenAPI 3.1.0 | Confirmed valid, but 3.2.0 exists and is unacknowledged | Low-Medium |
| Docker / GitHub Actions | No version claim to check | — |

## Recommended Actions (non-blocking, all Low/Low-Medium severity)

1. Add one clause to the Stack table or AD-1 rationale noting Java 21 was chosen over the newer JDK 25 LTS deliberately (ecosystem/tooling maturity), not by default.
2. Add one clause to AD-4 or the Stack table noting OpenAPI 3.2.0 exists and was not adopted because 3.1.0 is an inherited PRD constraint (RNF08), not a fresh choice — or note it was evaluated and found unnecessary at this scope.
3. Add a one-line Deferred entry for Neon's compute-scale-to-zero cold-start behavior (5 min idle), mirroring the existing Render cold-start entry, for symmetry and completeness.
