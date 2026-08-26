# Epic 1 Context: Cadastro, Login e Controle de Acesso

<!-- Compiled from planning artifacts. Edit freely. Regenerate with compile-epic-context if planning docs change. -->

## Goal

Este épico entrega a fundação de identidade e acesso da UniCatólica: um aluno se cadastra com e-mail institucional, confirma o e-mail antes do primeiro login, autentica com sessão JWT, faz logout, e o sistema restringe ações com base no perfil do usuário. É também o épico que estabelece o scaffold técnico do projeto (monorepo, ambiente local, CI/CD, migrations, contrato OpenAPI, envelope de erro, filtro de segurança e log de auditoria) sobre o qual todos os demais épicos são construídos. É classificado como must-have da semana 1 — precisa estar ponta a ponta e deployado.

## Stories

- Story 1.1: Fundação do projeto (scaffold e infraestrutura)
- Story 1.2: Cadastro de aluno com e-mail institucional
- Story 1.3: Confirmação de e-mail antes do primeiro login
- Story 1.4: Login e emissão de sessão JWT
- Story 1.5: Bloqueio de acesso sem autenticação e restrição por perfil
- Story 1.6: Logout e invalidação de sessão

## Requirements & Constraints

- Cadastro exige nome, e-mail institucional, senha e curso; usuário é persistido com perfil padrão e status "e-mail não confirmado" — nenhuma sessão ativa é gerada até a confirmação.
- Cadastro só é aceito para e-mails do domínio institucional; domínio externo, e-mail já existente, formato inválido, senha fora de política, ou idade menor de 18 anos são todos rejeitados com mensagem de validação específica ao campo/motivo (nunca uma mensagem genérica). A política de senha em si e o mecanismo técnico exato de confirmação de e-mail (link com token, código, etc.) não estão definidos nos artefatos de planejamento — são decisões de implementação.
- Confirmação de e-mail é obrigatória antes do primeiro login; tentativa de login com e-mail não confirmado retorna mensagem distinta de credencial inválida (com opção de reenvio).
- Login com credenciais inválidas retorna mensagem genérica, sem indicar qual campo está incorreto — evita enumeração de contas.
- Autenticação bem-sucedida emite um JWT com claims fixos `sub` e `roles`; o token é transportado exclusivamente via header `Authorization: Bearer`, nunca via cookie.
- Todo endpoint autenticável passa por um único filtro de segurança JWT antes de qualquer módulo, exceto os listados numa allowlist `@PermitAll`; ausência/expiração/invalidade do token resulta em 401. Autorização fina por perfil (RF12/RF13) é responsabilidade de cada módulo, não do filtro.
- Mapa de erro fixo: 401 sem autenticação; 403 quando autenticado mas sem permissão e o recurso não precisa ficar oculto; 404 quando a existência do recurso deve ficar oculta a quem não tem acesso. Nenhum módulo escolhe 403 vs. 404 por conta própria. Toda resposta de erro segue o envelope padrão `{"error":{"code","message","details"}}`.
- Logout invalida a sessão; requisições subsequentes com o mesmo token devem ser rejeitadas com 401.
- Login deve ser registrado no log de auditoria centralizado desde esta história (RNF07); baseline OWASP ASVS 4.0.3 aplica-se a autenticação, controle de acesso, validação de entrada e gestão de sessão (RNF04).
- Usuário novo deve concluir login sem treinamento prévio, com integridade funcional preservada em desktop e mobile browser (RNF01, RNF02); operações críticas de autenticação devem atender p95 ≤ 2s (RNF03) — risco de cold-start em hosting de tier gratuito pode comprometer isso em demonstração ao vivo.
- Cada requisito funcional deste épico deve ser rastreável a critério de aceitação e teste (RNF09).

## Technical Decisions

- Stack: monólito multimodular Quarkus (1 deploy unit, 1 módulo por área funcional, Resource → Service → Repository), atrás de um único filtro de segurança JWT; frontend SPA Angular com deploy separado.
- `openapi.yaml` (3.1.0) na raiz do repo é a fonte de verdade do contrato, definido antes de qualquer lado implementar um endpoint; inclui o componente compartilhado `PageResponse` e o schema do envelope de erro. Validação de contrato roda em CI, incluindo teste de contrato em runtime (corpo real vs. schema). CORS é configurado centralmente (`quarkus.http.cors`), nunca por módulo.
- Hosting: Angular como Render Static Site; Quarkus como Render Web Service via Docker; PostgreSQL gerenciado no Neon (free tier persistente) — nunca o Postgres gratuito do Render.
- Ambiente local via `docker-compose.yml` na raiz (Postgres + Quarkus dev mode + Angular dev server) com `.env.example` compartilhado; nenhum dev instala Postgres localmente fora do compose.
- CI/CD via GitHub Actions: build + testes + validação de `openapi.yaml` em todo PR; merge em `main` exige CI verde; merge dispara deploy automático via integração GitHub↔Render.
- Migrations via Liquibase, changelogs versionados por módulo (`db/changelog/{modulo}/*.xml`) incluídos por changelog mestre via `<includeAll>`; changeset id prefixado pelo módulo (nunca contador global), para evitar colisão entre PRs paralelos.
- Observabilidade: health check via `quarkus-smallrye-health` em `/q/health`; logs estruturados JSON para stdout. Sem ambiente de staging na semana 1.
- Log de auditoria transversal: tabela única `log_auditoria`, gravada apenas via `AuditoriaService` injetável (infraestrutura transversal, não pertence a nenhum módulo de domínio); ativo desde esta história para eventos de login.
- Convenções de dados: nomes de entidade/tabela/coluna em português; classes técnicas (`Resource`/`Service`/`Repository`) e paths REST em inglês; IDs `bigint`/identity (não UUID); instantes em `Instant` (ISO-8601 UTC), datas em `LocalDate`; campos JSON em camelCase português (ex.: `nomeCompleto`).
- Estrutura de repositório: monorepo com `frontend/`, `backend/` (pacote por módulo), `openapi.yaml`, `docker-compose.yml`, `.env.example`, `.github/workflows/` na raiz.
- Cadastro (Story 1.2) captura curso do aluno como mínimo necessário para disparar o auto-join (RF24.1) sem depender do módulo completo de Perfil Acadêmico (Epic 4, stretch); edição de nome/curso/período/interesses pós-cadastro pertence ao Epic 4.

## UX & Interaction Patterns

- Tela "Verifique seu e-mail" pós-cadastro: confirma que o cadastro foi concluído, orienta a checar o e-mail e oferece opção de reenviar a confirmação. É uma das telas novas sem mockup visual (spine-only) a construir neste épico.
- Estado "e-mail não confirmado" no login: bloqueia o login com mensagem específica e visualmente distinta da mensagem de credencial inválida.
- Voz e tom: comunicação direta, em segunda pessoa ("você"); mensagens de erro explicam o que houve e o que fazer, sem culpar o usuário (aplica-se a todas as mensagens de rejeição de cadastro/login desta história).
- Piso de acessibilidade WCAG 2.2 AA: foco de teclado visível em todo elemento interativo dos formulários de cadastro/login; erros de validação anunciados, não apenas indicados por cor.
- Estilização visual das telas de cadastro/login (identidade "Campus Clean", tokens, componentes) é entregue pelo Epic 14 (Fundação Visual e de Experiência) — esta história cobre o comportamento e os dados, não o design visual das telas.

## Cross-Story Dependencies

- Story 1.1 (scaffold) é pré-requisito técnico de todas as demais histórias deste épico e de todos os épicos seguintes.
- Story 1.2 (cadastro) captura o curso do aluno, que dispara o auto-join à comunidade de curso implementado no Epic 2 (Story 2.3) — este épico não implementa o auto-join em si, apenas fornece o dado que o aciona.
- Story 1.3 depende da persistência de status "e-mail não confirmado" criada em Story 1.2.
- Story 1.4 (login/JWT) depende do e-mail confirmado (Story 1.3) e é pré-requisito para Story 1.5 (filtro de segurança) e Story 1.6 (logout).
- O filtro de segurança JWT e a allowlist `@PermitAll` inicializados em Story 1.1 são a base sobre a qual Story 1.5 implementa a rejeição por autenticação/perfil.
- Todos os épicos posteriores (Comunidades, Publicações, etc.) dependem do filtro JWT, do envelope de erro e do log de auditoria estabelecidos neste épico.
