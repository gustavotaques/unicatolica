---
title: 'Documentar POST /auth/logout no openapi.yaml'
type: 'chore'
created: '2026-08-31'
status: 'done'
route: 'one-shot'
baseline_commit: '9ce3d491428ff9dcec67e49e2843a39ea5a59d20'
review_loop_iteration: 0
context: []
---

<frozen-after-approval reason="human-owned intent — do not modify unless human renegotiates">

## Intent

**Problem:** `POST /auth/logout` (Story 1.6, RF10/RF11) está implementado em `AuthResource.java` e consumido pelo frontend, mas nunca foi adicionado ao `openapi.yaml` — viola AD-4 ("openapi.yaml é a fonte de verdade, acordada entre frontend e backend antes de qualquer lado implementar um endpoint novo"). Rastreado como defeito F-02 / action item A-02 em `sprint-status.yaml` e `epic-1-retro-2026-08-29.md`.

**Approach:** Adicionar o path `/auth/logout` ao `openapi.yaml`, seguindo exatamente o padrão dos endpoints autenticados já documentados (`/usuarios/me`): sem `security: []` explícito (herda `bearerAuth` global), resposta `204` sem corpo e `401` via `$ref` para o response `NaoAutenticado` compartilhado. Descrição do endpoint esclarece a semântica de invalidação por `iat` (imediata, mesmo antes do `exp` natural) e que chamadas repetidas são idempotentes.

## Boundaries & Constraints

**Always:** documentação apenas — nenhuma mudança de comportamento em `AuthResource.java`/`AuthService.java`/`JwtSecurityFilter.java`; contrato deve validar com `npx @redocly/cli lint openapi.yaml` (mesmo comando do CI).

**Ask First:** nenhuma.

**Never:** alterar o bug F-01 (truncamento de `iat`, action item A-01) nesta mudança — escopo isolado por item de ação.

</frozen-after-approval>

## Code Map

- `openapi.yaml` -- único arquivo alterado; adiciona o path `/auth/logout`.

## Tasks & Acceptance

**Execution:**
- [x] `openapi.yaml` -- adicionar path `/auth/logout` (POST) com respostas 204/401 -- fecha o gap do defeito F-02 (AD-4)

**Acceptance Criteria:**
- Given o contrato `openapi.yaml`, when consultado, then `/auth/logout` aparece documentado com operationId `logout`, resposta `204` e `401`.
- Given `npx @redocly/cli lint openapi.yaml` (comando usado pelo CI), when executado, then retorna válido, sem novos warnings/erros introduzidos por esta mudança.

## Verification

**Commands:**
- `npx --yes @redocly/cli lint openapi.yaml` -- expected: "Woohoo! Your API description is valid." com os mesmos 5 warnings pré-existentes (nenhum novo).

## Suggested Review Order

- Novo path, mesmo padrão dos endpoints autenticados existentes (`/usuarios/me`) — sem `security` explícito, herda `bearerAuth` global.
  [`openapi.yaml:108`](../../openapi.yaml#L108)

- Descrição esclarece invalidação imediata por `iat` (antes do `exp` natural) e idempotência em chamadas repetidas.
  [`openapi.yaml:113`](../../openapi.yaml#L113)
