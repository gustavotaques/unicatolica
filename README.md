# UniCatólica

Rede social acadêmica para o Campus Joinville da CatólicaSC. Conecta os 3.000+ alunos em comunidades por curso e por interesse, resolvendo o isolamento que hoje é tocado por grupos de WhatsApp fragmentados: cada aluno entra automaticamente na comunidade do seu curso (auto-join), descobre comunidades abertas por afinidade, acompanha enquetes, avisos institucionais e publicações de colegas — tudo em um único lugar.

Projeto do **PAC Extensionista** (5º semestre, Engenharia de Software) da CatólicaSC, orientado pelo Prof. Edson Vaz Lopes.

> **Status:** planejamento concluído (PRD, arquitetura, UX e épicos validados); fundação do projeto (scaffold, CI/CD, contrato OpenAPI base) implementada — Story 1.1. Entrega da fatia núcleo prevista para **2026-08-29**.

## Stack

| Camada | Tecnologia |
|---|---|
| Frontend | Angular ^22 |
| Backend | Java 21 + Quarkus 3.33 + Hibernate ORM |
| Banco de dados | PostgreSQL (Neon, gerenciado) |
| Migrações | Liquibase |
| Contrato de API | OpenAPI 3.1.0 (`openapi.yaml` na raiz — fonte de verdade) |
| CI/CD | GitHub Actions → deploy automático no Render |
| Deploy | Render Static Site (frontend) + Render Web Service via Docker (backend) |

Arquitetura: monólito multimodular (não microsserviços), um módulo por área funcional — Identidade, Perfil Acadêmico, Comunidades, Publicações, Discussões, Filtro de Conteúdo, Materiais, Enquetes, Busca, Notificações, Mensagens, Moderação. Detalhes e decisões em [`docs/unicatolica-architecture-spine.md`](docs/unicatolica-architecture-spine.md).

## Estrutura planejada do repositório

```
unicatolica/
  frontend/            # SPA Angular
  backend/              # Quarkus — um pacote por módulo de domínio
  openapi.yaml           # contrato REST
  docker-compose.yml     # ambiente local (Postgres + backend + frontend)
  .env.example
  docs/                   # PRD, arquitetura, UX, contexto do PAC Extensionista
  _bmad-output/            # artefatos de planejamento (PRD, épicos, UX, arquitetura)
```

## Como rodar

```bash
cp .env.example .env
docker-compose up
```

- Frontend em `http://localhost:4200`
- Backend em `http://localhost:8080` (health check em `/q/health`)
- Postgres local via container; produção usa Postgres gerenciado no Neon

Em produção, frontend e backend são publicados separadamente no Render, com deploy automático a cada merge em `main` com CI verde (build + testes + validação do contrato OpenAPI).

## Fluxo de contribuição

A `main` tem branch protection ativa (AD-8): os 3 checks do CI (`Frontend`, `Backend`, `Contrato — valida openapi.yaml`) precisam estar verdes antes de qualquer coisa entrar na `main` — **sem exceção, nem para admin**. Não existe mais push direto na `main`: um commit novo é sempre rejeitado até rodar CI em outro lugar primeiro. Fluxo:

```bash
git checkout -b minha-feature
# ... commits ...
git push -u origin minha-feature
gh pr create
# espera o CI verde, então:
gh pr merge --squash   # ou merge pela UI do GitHub
```

Revisão humana **não é obrigatória** para merge (decisão do time, AD-8) — só o CI precisa estar verde.

## Documentação

| Documento | Conteúdo |
|---|---|
| [`docs/unicatolica-pacext-contexto.md`](docs/unicatolica-pacext-contexto.md) | Relatório do PAC Extensionista — 80 requisitos funcionais, 9 não funcionais, riscos, C4 |
| [`docs/unicatolica-pacext-prd.md`](docs/unicatolica-pacext-prd.md) | PRD — visão, personas, jornadas de usuário, métricas de sucesso |
| [`docs/unicatolica-architecture-spine.md`](docs/unicatolica-architecture-spine.md) | Decisões de arquitetura, stack, convenções, diagramas |
| [`docs/unicatolica-experience.md`](docs/unicatolica-experience.md) / [`unicatolica-design.md`](docs/unicatolica-design.md) | Fluxos de UX e sistema de design (Campus Clean) |
| [`docs/unicatolica-artefatos.md`](docs/unicatolica-artefatos.md) | Links para os protótipos e decks interativos |
| [`_bmad-output/planning-artifacts/epics.md`](_bmad-output/planning-artifacts/epics.md) | Épicos e histórias, com corte de escopo da semana 1 |

## Escopo da entrega (semana 1)

- **Must-have:** Cadastro/Login/Acesso, Comunidades, Publicações (RF01–RF36) — ponta a ponta, deployado.
- **Stretch:** Perfil Acadêmico completo, Discussões com respostas encadeadas.
- **Fora do corte:** Filtro de Conteúdo, Materiais, Enquetes, Busca, Notificações, Mensagens, Moderação, Avisos Institucionais — desenhados no PRD, entram em fases seguintes.

## Equipe

- Gustavo Vinicius Taques
- João Pedro Angélico
- Luis Fernando Pereira
- Vynicyus Cândido

Orientador: Prof. Edson Vaz Lopes

## Instituição

Centro Universitário Católica de Santa Catarina (CatólicaSC) — Campus Joinville, curso de Engenharia de Software. Projeto acadêmico, sem licença de distribuição definida.
