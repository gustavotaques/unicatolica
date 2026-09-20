# Resumo dos testes E2E - Epic 14 (shell de navegação + toasts)

Data: 2026-09-07 (revisado em 2026-09-20 após merge das Stories 2.3/2.4/2.5 de Comunidades)
Escopo: shell de navegação global (Story 14.3) e sistema de toast (Story 14.5),
mais as telas públicas de auth do Epic 1. Só o que existe no código hoje.

**Nota da revisão de 2026-09-20:** um `git pull` trouxe 11 commits do Epic 2
(Comunidades: Stories 2.3/2.4/2.5 + correções de CI) que mudaram a Home
(`/feed`, antes um placeholder estático) pra uma tela que dispara `GET
/usuarios/me`, `GET /comunidades/minhas` e `GET /comunidades` ao montar, e
deram rota real a "Descobrir comunidades" na sidebar (antes inerte). Isso
quebrou 4 dos 32 testes desta suíte (assunções sobre "Início" ser o único link
e sobre um heading estático que não existe mais). Testes ajustados; conteúdo
de `/feed` continua fora de escopo (ver seção dedicada abaixo).

## Framework e setup

- Runner novo: `@playwright/test` (devDependency adicionada ao `frontend/`).
  O `ng test` (vitest + jsdom) continua sendo a suíte unitária, intocada.
- Config: `frontend/playwright.config.ts`. Sobe o `ng serve` sozinho via
  `webServer` (`npm start`, `http://localhost:4200`), projeto único `chromium`.
- Pré-requisito único (CI e primeira execução local):
  `cd frontend && npx playwright install chromium`.
- Comandos:
  - `cd frontend && npm run e2e` - roda a suíte headless.
  - `cd frontend && npm run e2e:ui` - modo interativo.
  - `E2E_BACKEND=1 npm run e2e` - inclui o teste de login contra o Quarkus real
    (exige `docker-compose up` no ar).

### Sem backend por padrão

- O guard só checa presença do token e `AuthService` decodifica **apenas o
  payload do JWT (sem verificar assinatura)**. Então os testes de papel plantam
  um JWT não assinado em `localStorage['pacext.token']` com a claim `roles`
  desejada (`e2e/support/jwt.ts` + `e2e/support/seed.ts`).
- As rotas `/auth/*` são mockadas com `page.route()` incluindo cabeçalhos CORS
  e o preflight `OPTIONS` (`e2e/support/mocks.ts`).
- `mockFeedOk(page)` (`e2e/support/mocks.ts`) mocka os 3 GETs que `/feed`
  dispara desde o Epic 2 (`usuarios/me`, `comunidades/minhas`, `comunidades`),
  usado em toda suíte que navega pra lá só pra atravessar o chrome da shell -
  sem isso a página gera ruído de `ERR_CONNECTION_REFUSED` no console (sem
  backend) ou fica sujeita a dados reais imprevisíveis (com backend/docker-compose
  no ar sem querer).

### Ponto de teste adicionado (toasts)

- `frontend/src/main.ts`: expõe `ToastService` em `window.__ucToast` **só em
  `isDevMode()`** (some da build de produção pelo guard). As Stories 2.3
  (auto-join) e 2.4 (join de comunidade aberta) já ligaram gatilhos reais de
  `mostrar()`, mas esses fluxos são conteúdo de Comunidades (Epic 2, fora de
  escopo aqui - ver seção "Fora de escopo"). O seam isola o primitivo de toast
  sem precisar mockar a lista de comunidades e clicar em "Participar".

## Testes gerados

Todos em `frontend/e2e/`.

### E2E - telas públicas de auth (`auth-publico.spec.ts`)

- [x] Login renderiza campos, labels associadas e botão Entrar
- [x] Login: submeter vazio não navega e não mostra erro de credencial
- [x] Login: foco de teclado percorre e-mail -> senha -> Entrar com indicador de foco visível
- [x] Cadastro renderiza os 5 campos com label associada e botão Cadastrar
- [x] Cadastro: submeter vazio mostra o erro de cada campo e não navega
- [x] Cadastro: e-mail inválido dispara a mensagem específica após blur
- [x] Confirmar e-mail sem token: mostra link inválido + caminho de volta ao cadastro
- [x] Confirmar e-mail com token válido (mock 204): confirma e leva ao login
- [x] Confirmar e-mail com token rejeitado (mock 422): mostra a mensagem da API

### E2E - fluxo de auth para a shell (`auth-fluxo.spec.ts`)

- [x] Login mockado válido: cai na shell em `/feed` com sidebar e avatar
- [x] Login mockado inválido (401): mostra o erro e mantém em `/login`
- [x] (gated `E2E_BACKEND=1`) Login com backend real, usuário semente `aluno.teste` (ALUNO): cai em `/feed`, sidebar sem itens de moderação

### E2E - sidebar por papel, Story 14.3 (`shell-sidebar.spec.ts`)

- [x] ALUNO vê exatamente os 7 itens comuns, na ordem da EXPERIENCE.md, sem os de moderação
- [x] MODERADOR vê os 9 itens, com o par de moderação entre "Criar enquete" e "Suas comunidades"
- [x] ADMINISTRADOR vê o mesmo conjunto do MODERADOR
- [x] Token malformado: shell renderiza, itens de moderação escondidos, sem erro de console
- [x] Item sem rota é `<span aria-disabled="true">`, `tabIndex -1`, e não navega no clique
- [x] "Início" carrega `aria-current="page"`; "Descobrir comunidades" (rota real desde o Epic 2) é link mas não fica ativo em `/feed`
- [x] Sem token: `/feed` redireciona para `/login` e a shell não renderiza

### E2E - dropdown do avatar, Story 14.3 (`shell-avatar.spec.ts`)

- [x] Clique no avatar abre o menu com Perfil, Configurações, Sair (`aria-expanded` alterna)
- [x] Perfil e Configurações são `menuitem` inertes (`aria-disabled="true"`)
- [x] Escape fecha o menu e devolve o foco ao avatar
- [x] Clique fora fecha o menu
- [x] Ativar um item fecha o menu e devolve o foco ao avatar
- [x] Sair chama o logout, limpa o token e navega para `/login`
- [x] Com `prefers-reduced-motion`: o menu ainda abre e fecha normalmente

### E2E - toasts e motion, Story 14.5 (`toast.spec.ts`)

- [x] `mostrar()` renderiza um toast com live-region (`role="status"`/`aria-live="polite"`), ícone de check e a mensagem
- [x] O toast fica ancorado no canto inferior direito da viewport
- [x] A animação de entrada dura ~150ms (`animation-duration: 0.15s`) - regra de motion mínimo
- [x] Duas chamadas seguidas empilham dois toasts
- [x] Mensagem em branco é no-op
- [x] O toast some sozinho após 4000ms, sem controle de fechar
- [x] Com `prefers-reduced-motion`: a entrada não anima (`animation-name: none`)

## Resultado da execução

`cd frontend && npm run e2e`

```
32 passed, 1 skipped (login com backend real, gated por E2E_BACKEND)
```

`cd frontend && npm test` (suíte unitária, regressão): `196 passed` (subiu de 180 com o merge do Epic 2).
`cd frontend && npm run build` (produção): OK.

## Cobertura

- Telas públicas de auth (Login, Cadastro, Confirmar e-mail): renderização,
  validação de formulário e foco de teclado - cobertas.
- Fluxo login -> guard -> shell -> `/feed`: coberto (mock sempre; backend real gated).
- Shell / sidebar (Story 14.3): todas as linhas da I/O matrix da spec - cobertas.
- Dropdown do avatar (Story 14.3): abrir/fechar por todos os caminhos, foco de
  retorno, itens inertes, Sair -> logout - cobertos.
- Toasts (Story 14.5): aparição, empilhamento, auto-dismiss, no-op de branco,
  contrato de motion (~150ms) e reduced-motion - cobertos via o seam de dev.

Testes de API dedicados: não gerados. O backend Quarkus tem suíte JUnit própria
(`backend/src/test/...`); esta tarefa é de E2E de frontend. Os contratos de
`/auth/*` consumidos pela SPA são exercitados indiretamente pelos mocks e pelo
teste gated de backend real.

## Fora de escopo (dependem de épicos no backlog)

- Restyle da Story 14.7 (Login/Cadastro/Verifique e-mail com tokens, criação de
  comunidade aberta, Artigo, painel admin) - rotas/estilos não existem.
- Fluxos "Participar -> member indicator + toast", auto-join, conteúdo dentro de
  `/feed` (Epic 2 em diante).
- Comportamento responsivo / colapso de navegação (Story 14.4, deferred).
- Stories 14.4 / 14.6 / 14.8 / 14.9 (deferred).

## Próximos passos

- Wire de CI: adicionar um job que roda `npx playwright install --with-deps
  chromium` + `npm run e2e` no workflow do frontend.
- Quando alguém cobrir Comunidades (Epic 2) em E2E, os fluxos reais de toast
  (join / auto-join) devem ganhar suíte própria ali; o seam de `window.__ucToast`
  em `main.ts` pode sair então, se nada mais depender dele.
- Quando existir seed de MODERADOR / ADMINISTRADOR no backend, adicionar um
  caminho `E2E_BACKEND=1` que valide o gating com token real emitido pelo login.
- Esta suíte assume que `/feed` sempre vai continuar chamando `usuarios/me` +
  `comunidades/minhas` + `comunidades` ao montar (por isso `mockFeedOk`
  existe); se esse contrato mudar, ajustar `e2e/support/mocks.ts` junto.
