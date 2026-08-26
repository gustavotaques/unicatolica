---
title: 'Fundação do projeto (scaffold e infraestrutura)'
type: 'chore'
created: '2026-08-26'
status: 'done'
baseline_commit: '02afaef96132ba4771a135b7c2254373fc5dec23'
review_loop_iteration: 0
context: ['{project-root}/_bmad-output/planning-artifacts/architecture/architecture-unicatolica-2026-08-22/ARCHITECTURE-SPINE.md']
---

<frozen-after-approval reason="human-owned intent — do not modify unless human renegotiates">

## Intent

**Problem:** O repositório não tem código de aplicação — falta a fundação técnica (monorepo, ambiente local, CI/CD, contrato REST, filtro de segurança, migrations, log de auditoria) sobre a qual as próximas histórias e épicos serão construídos.

**Approach:** Criar o monorepo (`frontend/` Angular, `backend/` Quarkus com pacote por módulo), inicializar `openapi.yaml` com envelope de erro e `PageResponse`, registrar o filtro JWT scaffold com allowlist `@PermitAll`, criar a infraestrutura transversal de auditoria, e configurar `docker-compose.yml`, Liquibase e o pipeline de CI/CD.

## Boundaries & Constraints

**Always:**
- Seguir a Architecture Spine AD-1 a AD-11 à risca — contrato técnico da fundação.
- Pastas vazias/placeholder são aceitáveis para módulos sem história ainda (identidade, perfil, comunidades, publicacoes, discussoes, filtro, materiais, enquetes, busca, notificacoes, mensagens, moderacao).
- Filtro JWT e `AuditoriaService`/`log_auditoria` são infraestrutura transversal — não pertencem a nenhum módulo de domínio (AD-2, AD-11).
- Convenções de dados: entidade/tabela/coluna em português; classes técnicas e paths REST em inglês; IDs `bigint`/identity; `Instant` para timestamps, `LocalDate` para datas; JSON em camelCase português.
- Changelog Liquibase mestre com `<includeAll>`; changesets futuros prefixados por módulo — nesta história só o mestre precisa existir.
- Segredos só via `.env`/variáveis de ambiente — nunca commitados.

**Ask First:** Branch protection no GitHub (exigir CI verde, AD-8) requer admin do repositório — se `gh` CLI não conseguir aplicar, perguntar ao humano antes de tentar, ou deixar como pendência manual no PR.

**Never:** Lógica de negócio de qualquer módulo (próximas histórias); ambiente de staging; object storage; deploy real no Render/Neon.

</frozen-after-approval>

## Code Map

Repositório vazio — não há código existente a mapear. Os alvos a criar estão listados em Tasks abaixo.

## Tasks & Acceptance

**Execution:**
- [x] `frontend/` -- `ng new` (Angular ^22, standalone) -- base da SPA (AD-1)
- [x] `backend/` -- projeto Quarkus 3.33/Java 21 com extensões `resteasy-reactive`, `hibernate-orm-panache`, `jdbc-postgresql`, `liquibase`, `smallrye-jwt`, `smallrye-health`, e um pacote por módulo (identidade, perfil, comunidades, publicacoes, discussoes, filtro, materiais, enquetes, busca, notificacoes, mensagens, moderacao), mesmo vazios -- base do monólito e limites de módulo (AD-1, AD-3)
- [x] `openapi.yaml` -- raiz, OpenAPI 3.1.0, `info`, schema `Erro` (`{"error":{"code","message","details"}}`), componente `PageResponse`, sem endpoints ainda -- contrato-fonte-de-verdade (AD-4, AD-5)
- [x] `backend/.../infraestrutura/seguranca/JwtSecurityFilter.java` -- filtro JAX-RS `@Provider`, valida `Authorization: Bearer`, claims `sub`+`roles`, allowlist `@PermitAll` (`/auth/login`, `/auth/registro`, `/q/health`) -- scaffold de segurança (AD-2)
- [x] `backend/.../infraestrutura/auditoria/` -- entidade `LogAuditoria` (tabela `log_auditoria`) + `AuditoriaService` injetável com método de registro genérico -- infraestrutura transversal (AD-11)
- [x] `db/changelog/db.changelog-master.xml` -- changelog mestre com `<includeAll path="db/changelog"/>` -- base de migrations (AD-9)
- [x] `backend` -- `/q/health` responde 200 via `smallrye-health` -- observabilidade mínima (AD-10)
- [x] `docker-compose.yml`, `.env.example` -- raiz, Postgres + Quarkus dev mode + Angular dev server -- ambiente local reprodutível (AD-7)
- [x] `.github/workflows/ci.yml` -- build+teste frontend, build+teste backend, validação de `openapi.yaml` -- gate de merge (AD-8)

**Acceptance Criteria:**
- Given o repositório vazio, when o scaffold é aplicado, then `frontend/` e `backend/` existem na raiz e `docker-compose up` sobe Postgres + Quarkus dev mode + Angular dev server usando `.env.example`
- Given o scaffold aplicado, when o contrato é inicializado, then `openapi.yaml` existe na raiz com `PageResponse` e o schema do envelope de erro definidos
- Given o scaffold aplicado, when a segurança é inicializada, then o filtro JWT está registrado com allowlist `@PermitAll` e claims `sub`+`roles`, e `log_auditoria`/`AuditoriaService` existem prontos para uso pelos módulos
- Given um PR aberto, when o pipeline GitHub Actions roda, then build+testes+validação de `openapi.yaml` executam, o changelog mestre do Liquibase existe com `<includeAll>`, e `/q/health` responde 200

## Spec Change Log

- **Pós-aprovação (Docker ficou disponível após o step-04):** `docker-compose up` do backend falhava 100% das vezes — `eclipse-temurin:21-jdk` não tem `unzip`, então o `mvnw` baixava silenciosamente o `.tar.gz` do Maven em vez do `.zip`, mas o checksum fixado em `maven-wrapper.properties` era o do `.zip`, então a validação sempre falhava. Corrigido instalando `unzip` antes de delegar pro `mvnw` em `docker-compose.yml` (commit `a25870c`). Após o fix, os três serviços (`db`, `backend`, `frontend`) foram subidos de verdade via `docker-compose up`, com `/q/health` respondendo 200, o filtro JWT retornando 401/404 corretamente, a migração Liquibase criando `log_auditoria`, e o frontend servindo a SPA — nenhum desses três serviços tinha sido executado de fato antes (nem pelo implementador nem pela revisão), só validados por sintaxe.

## Verification

**Commands:**
- `cd backend && ./mvnw test` -- build e testes passam
- `cd backend && ./mvnw quarkus:dev` + `curl localhost:8080/q/health` -- HTTP 200
- `cd frontend && ng build` -- build de produção sem erros
- `docker-compose up` -- os três serviços sobem sem erro
- `npx @redocly/cli lint openapi.yaml` -- sem erros de schema

**Manual checks (if no CLI):**
- Branch protection exigindo CI verde é configuração do GitHub, fora do repo — se `gh` CLI não conseguir aplicá-la, registrar como pendência manual no PR

## Suggested Review Order

**Segurança JWT (AD-2)**

- Ponto de entrada: filtro único que autentica toda requisição antes de qualquer módulo.
  [`JwtSecurityFilter.java:39`](../../backend/src/main/java/br/edu/unicatolica/pacext/infraestrutura/seguranca/JwtSecurityFilter.java#L39)

- Allowlist com match exato de segmento — evita que rotas futuras como `/auth/login-x` escapem da autenticação por coincidência de prefixo.
  [`JwtSecurityFilter.java:108`](../../backend/src/main/java/br/edu/unicatolica/pacext/infraestrutura/seguranca/JwtSecurityFilter.java#L108)

- Qualquer falha de parsing do token (não só `ParseException`) cai em 401 controlado, nunca em 500 não tratado.
  [`JwtSecurityFilter.java:98`](../../backend/src/main/java/br/edu/unicatolica/pacext/infraestrutura/seguranca/JwtSecurityFilter.java#L98)

- Envelope de erro padrão (AD-5) reutilizado pelo filtro e espelhado no contrato.
  [`ErroResponse.java:9`](../../backend/src/main/java/br/edu/unicatolica/pacext/infraestrutura/web/ErroResponse.java#L9)

**Auditoria transversal (AD-11)**

- Serviço injetável único de escrita em `log_auditoria`, com validação de tamanho de campo antes de persistir.
  [`AuditoriaService.java:40`](../../backend/src/main/java/br/edu/unicatolica/pacext/infraestrutura/auditoria/AuditoriaService.java#L40)

- Entidade da tabela compartilhada — nenhum módulo de domínio a possui.
  [`LogAuditoria.java:19`](../../backend/src/main/java/br/edu/unicatolica/pacext/infraestrutura/auditoria/LogAuditoria.java#L19)

- Changeset Liquibase que cria `log_auditoria`, isolado sob `db/changelog/modulos/infraestrutura`.
  [`infraestrutura-001-create-log-auditoria.xml:12`](../../backend/src/main/resources/db/changelog/modulos/infraestrutura/infraestrutura-001-create-log-auditoria.xml#L12)

- Changelog mestre estável — `<includeAll>` aponta para a subpasta `modulos`, não para si mesmo (evita referência circular).
  [`db.changelog-master.xml:24`](../../backend/src/main/resources/db/changelog/db.changelog-master.xml#L24)

**Contrato REST (AD-4/AD-5)**

- Schema `Erro` — fonte de verdade do envelope de erro consumido pelo filtro JWT.
  [`openapi.yaml:19`](../../openapi.yaml#L19)

- `PageResponse` — envelope de paginação compartilhado que todo endpoint de listagem futuro deve referenciar.
  [`openapi.yaml:49`](../../openapi.yaml#L49)

**Backend — base do monólito (AD-1/AD-3)**

- Extensões Quarkus e limites de dependência do módulo (Panache já traz Hibernate ORM transitivamente).
  [`pom.xml:16`](../../backend/pom.xml#L16)

- Datasource e Liquibase apontando para o changelog mestre.
  [`application.properties:14`](../../backend/src/main/resources/application.properties#L14)

- Mapeamento da claim `roles` do token para `JsonWebToken#getGroups()`.
  [`application.properties:22`](../../backend/src/main/resources/application.properties#L22)

**Ambiente local e CI/CD (AD-6/AD-7/AD-8)**

- `docker-compose up` sobe Postgres + Quarkus dev + Angular dev server; backend só fica "pronto" para o frontend via healthcheck real de porta.
  [`docker-compose.yml:47`](../../docker-compose.yml#L47)

- Pipeline de gate de merge: build+teste frontend, build+teste backend (com Postgres real), lint do contrato.
  [`ci.yml:13`](../../.github/workflows/ci.yml#L13)

**Frontend (AD-1)**

- Ponto de entrada da SPA Angular gerada — ainda sem telas de negócio.
  [`app.ts:10`](../../frontend/src/app/app.ts#L10)

**Peripherals — testes**

- Regressão do bug real encontrado em teste manual: `UriInfo#getPath()` do RESTEasy Reactive já retorna path com barra inicial.
  [`JwtSecurityFilterTest.java:89`](../../backend/src/test/java/br/edu/unicatolica/pacext/infraestrutura/seguranca/JwtSecurityFilterTest.java#L89)

- Fecha o gap de verificação apontado na revisão: liga `application.properties` real aos valores que os testes do filtro assumem manualmente.
  [`ApplicationPropertiesJwtConfigTest.java:20`](../../backend/src/test/java/br/edu/unicatolica/pacext/infraestrutura/seguranca/ApplicationPropertiesJwtConfigTest.java#L20)

- Cobre a nova validação de tamanho de campo do serviço de auditoria.
  [`AuditoriaServiceTest.java:61`](../../backend/src/test/java/br/edu/unicatolica/pacext/infraestrutura/auditoria/AuditoriaServiceTest.java#L61)
