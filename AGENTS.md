<!-- bmad:context -->
<!-- Verified 2026-08-26 against 02afaef. Managed by bmad-project-context; edits inside this block are replaced on refresh. Keep anything you want preserved outside the markers. -->

## UniCatólica (PACEXT)

Rede social acadêmica do Campus Joinville da CatólicaSC — projeto de PAC Extensionista. Monólito multimodular: backend Java 21 + Quarkus 3.33 + Hibernate (pacote por módulo), frontend Angular ^22 SPA, Postgres via Neon. Nenhum código de aplicação existe ainda — implementação começa após Sprint Planning. Arquitetura completa em `_bmad-output/planning-artifacts/architecture/architecture-unicatolica-2026-08-22/ARCHITECTURE-SPINE.md`; requisitos em `docs/unicatolica-pacext-contexto.md` e `docs/unicatolica-pacext-prd.md`; épicos/histórias em `_bmad-output/planning-artifacts/epics.md`.

## Policy

- Nunca implementar um endpoint novo (front ou back) sem `openapi.yaml` acordado primeiro entre os dois lados — contrato é fonte de verdade (AD-4).
- Nunca importar o `Repository` de outro módulo — leitura entre módulos só via associação JPA somente-leitura (DTO/projeção); escrita só pela interface Java publicada pelo módulo dono (AD-3).
- Nunca commitar segredos/config — só variáveis de ambiente (Render env vars / `.env` local).
- Merge em `main` exige apenas CI verde (build + testes + validação OpenAPI) — sem revisão humana obrigatória (AD-8, decisão do time).

## Where things are

- Planejamento completo (PRD, UX, arquitetura, épicos): `_bmad-output/planning-artifacts/`
- Requisitos funcionais/não funcionais originais: `docs/unicatolica-pacext-contexto.md`
- Contrato REST (quando existir): `openapi.yaml` na raiz

## Running and verifying

- TODO: sem `docker-compose.yml`, `frontend/`, `backend/` ainda — ambiente local planejado é Postgres + Quarkus dev mode + Angular dev server via compose (AD-7); verificar comandos reais no primeiro refresh após o código existir.
- TODO: TDD é reforçado por gate de CI (sem suite verde, sem merge) — GitHub Actions ainda não criado (AD-8).

## Conventions that differ from defaults

- Nomes de entidade/tabela/coluna em português (linguagem ubíqua do domínio); classes técnicas (`Resource`/`Service`/`Repository`) e paths REST em inglês.
- Campos JSON de request/response em camelCase português (`nomeCompleto`, não `name`) — sem camada de tradução.
- IDs são `bigint`/identity do Postgres, nunca UUID.
- `Instant` só para timestamps (ISO-8601 UTC); campos só-data usam `LocalDate`, nunca `Instant`.
- Erros seguem envelope fixo `{"error": {"code","message","details"}}` com status HTTP por cenário (401/403/404/400/422/409/500) — 403 vs. 404 decide se a existência do recurso deve ficar oculta (AD-5).
- Toda listagem pagina com o componente compartilhado `PageResponse` do `openapi.yaml` — nenhum endpoint inventa a própria forma.
- JWT só via header `Authorization: Bearer` — nunca cookie.
- Changelog Liquibase: um arquivo por módulo, changeset id prefixado pelo nome do módulo (ex.: `comunidades-002-...`), nunca contador global (AD-9).
- Frontend segue WCAG 2.2 nível AA (RNF06) — não verificado automaticamente em CI na semana 1.

<!-- /bmad:context -->
