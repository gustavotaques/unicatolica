---
stepsCompleted: [1, 2, 3, 4]
inputDocuments:
  - _bmad-output/planning-artifacts/prds/prd-unicatolica-2026-08-12/prd.md
  - _bmad-output/planning-artifacts/architecture/architecture-unicatolica-2026-08-22/ARCHITECTURE-SPINE.md
  - _bmad-output/planning-artifacts/ux-designs/ux-unicatolica-2026-08-17/DESIGN.md
  - _bmad-output/planning-artifacts/ux-designs/ux-unicatolica-2026-08-17/EXPERIENCE.md
  - docs/unicatolica-pacext-contexto.md
---

# UniCatólica (PACEXT) - Epic Breakdown

## Overview

Este documento decompõe os requisitos do PRD (`prd-unicatolica-2026-08-12/prd.md`), da UX Spine (`DESIGN.md` + `EXPERIENCE.md`) e da Architecture Spine (`ARCHITECTURE-SPINE.md`) em épicos e histórias implementáveis para a UniCatólica — rede social acadêmica do PACEXT.

**Nota de escopo crítica:** a Architecture Spine (AD, seção Deferred) corta o trabalho em duas fatias por causa do prazo de uma semana (entrega até 2026-08-30):
- **Must-have semana 1:** RF01–RF13 (Identidade/Acesso) + RF21–RF31 (Comunidades) + RF32–RF36 (Publicações), ponta a ponta, deployado.
- **Stretch semana 1** (só se sobrar tempo): RF14–RF20 (Perfil Acadêmico) + RF37–RF42 (Discussões).
- **Fora do corte da semana 1:** Filtro de Conteúdo, Materiais, Enquetes, Busca, Notificações, Mensagens, Moderação, Avisos Institucionais (RF43–RF82).

O PRD, por outro lado, escopa todos os 12 módulos + Avisos Institucionais como "em escopo do MVP" (§6.1) — sem o corte semanal. Este documento mantém o inventário completo de requisitos (para rastreabilidade — RNF09) e organiza os épicos por módulo/valor de usuário, mas a **priorização/sequenciamento de épicos deve refletir o corte da Architecture Spine**, com os épicos fora da semana 1 marcados como tal.

## Requirements Inventory

### Functional Requirements

#### 4.1 Identidade e Acesso (RF01–RF13, RF01.1–RF01.3)

RF01: Deve permitir cadastro com e-mail válido e senha
RF01.1: Deve impedir o cadastro de usuários menores de 18 anos
RF01.2: O sistema exige que o usuário confirme o e-mail cadastrado antes de permitir o primeiro login. Cadastro concluído (RF01) não gera sessão ativa nem permite login até a confirmação; tentativa de login com e-mail não confirmado é rejeitada com mensagem específica, distinta de credencial inválida. `[ASSUMPTION: mecanismo de confirmação — link por e-mail com token — fica para a fase de arquitetura/build]`
RF01.3: Cadastro (RF01) só é aceito para e-mails do domínio institucional da Católica. Cadastro com e-mail de domínio externo é rejeitado com mensagem explicando o motivo.
RF02: Deve impedir cadastro com e-mail já existente
RF03: Deve validar formato de e-mail
RF04: Deve validar política de senha
RF05: Deve persistir usuário com perfil padrão
RF06: Deve autenticar com credenciais válidas
RF07: Deve rejeitar credenciais inválidas
RF08: Deve gerar token/sessão ao autenticar
RF09: Deve impedir acesso sem autenticação
RF10: Deve permitir logout
RF11: Deve invalidar sessão após logout
RF12: Deve associar usuário a um perfil
RF13: Deve restringir ações com base no perfil

#### 4.2 Perfil Acadêmico (RF14–RF20, RF20.1, RF20.2) — stretch semana 1

RF14: Deve permitir criar perfil acadêmico
RF15: Deve permitir editar nome
RF16: Deve permitir editar curso
RF17: Deve permitir editar período
RF18: Deve permitir adicionar interesses
RF19: Deve persistir alterações do perfil
RF20: Deve retornar perfil corretamente
RF20.1: Deve emitir notificação para atualização de perfil do usuário (onboarding progressivo — interesses opcionais no cadastro, notificação-gatilho posterior)
RF20.2: Qualquer usuário autenticado pode visualizar o perfil acadêmico público de outro usuário, acessado a partir do nome/foto em uma postagem ou comentário. Perfil de terceiros é somente leitura (sem opções de edição); exibe os mesmos campos do próprio perfil, sem dados sensíveis adicionais.

#### 4.3 Comunidades (RF21–RF31, RF21.1, RF21.2, RF24.1, RF27.1) — must-have semana 1

RF21: Deve permitir criar comunidade (válido apenas para comunidades **abertas** — ver RF21.2)
RF21.1: O sistema distingue **comunidades de curso** (associação automática, criação restrita a administrador da plataforma) de **comunidades abertas** (criação livre por qualquer aluno, ingresso voluntário via RF24). Toda comunidade tem um tipo definido na criação, imutável depois; comunidades de curso não exibem botão "participar", comunidades abertas exibem.
RF21.2: Administrador da plataforma pré-cria a comunidade de cada curso da instituição antes da entrada dos primeiros alunos, associando-a ao curso correspondente. Não é possível a um aluno criar uma comunidade do tipo "comunidade de curso".
RF22: Deve validar campos obrigatórios da comunidade
RF23: Deve associar criador como administrador
RF24: Deve permitir ingresso em comunidade (comunidades abertas)
RF24.1: Ao definir ou editar o curso no perfil acadêmico (RF14/RF16), o sistema associa automaticamente o aluno à comunidade de curso correspondente, sem exigir ação de ingresso nem confirmação. Alterar o curso remove a associação da comunidade anterior e adiciona a do novo curso; é idempotente por definição (não passa pela validação de RF25).
RF25: Deve impedir ingresso duplicado
RF26: Deve permitir saída da comunidade
RF27: Deve listar comunidades
RF27.1: Qualquer usuário autenticado pode visualizar o feed de uma comunidade de curso da qual não é membro, mas não pode postar, comentar ou votar em enquetes dela — apenas membros podem. Tentativa de interação sem ser membro retorna erro de permissão. Generaliza RF55.1 para postagens e comentários; não se aplica a comunidades abertas.
RF28: Deve filtrar comunidades
RF29: Deve permitir que o admin remova membros
RF30: Deve permitir que o admin edite comunidades
RF31: Deve permitir que o admin exclua comunidades

`[NOTA: diverge da decisão original de contexto (§9.2 — "Coordenador ou professor pode gerenciar a comunidade") — decisão de 2026-08-12: docente é usuário acadêmico comum, sem privilégios de gestão de comunidade distintos de um aluno.]`

#### 4.4 Publicações (RF32–RF36) — must-have semana 1

RF32: Deve permitir criar postagem
RF33: Deve validar conteúdo obrigatório da postagem (papel de moderador)
RF34: Deve associar postagem ao usuário
RF35: Deve associar postagem à comunidade
RF36: Deve listar postagens da comunidade

Sujeito a RF27.1 (interação restrita a membros em comunidade de curso).

#### 4.5 Discussões (RF37–RF42) — stretch semana 1

RF37: Deve permitir comentar em postagem
RF38: Deve permitir responder comentário
RF39: Deve permitir editar o próprio conteúdo
RF40: Deve impedir edição de conteúdo de terceiros
RF41: Deve permitir excluir conteúdo próprio
RF42: Deve manter encadeamento de respostas (hierarquia estilo YouTube — trava em ~3 níveis de indentação por UX)

#### 4.6 Filtro de Conteúdo (RF43–RF47) — fora do corte semana 1

RF43: Deve permitir filtrar postagem por curso
RF44: Deve permitir filtrar por disciplina
RF45: Deve permitir filtrar por tipo de conteúdo
RF46: Deve persistir filtragem
RF47: Deve permitir filtrar por conteúdos

#### 4.7 Materiais (RF48–RF52) — fora do corte semana 1

RF48: Deve permitir anexar arquivos (PNG/PDF/JPG)
RF49: Deve permitir anexar link
RF50: Deve validar tipo de arquivo
RF51: Deve associar material à postagem
RF52: Deve permitir acesso ao material

`[DEFERRED — Architecture Spine: storage de arquivos (object storage) não escolhido; disco do Render free tier é efêmero.]`

#### 4.8 Enquetes e Pesquisas (RF53–RF58 + subitens) — fora do corte semana 1

RF53: Deve permitir criar e fixar enquete na homepage
RF53.1: Deve permitir que usuário autenticado crie enquete em comunidade da qual seja membro
RF53.2: Deve restringir a fixação de enquete na homepage ao papel de moderador
RF53.3: Deve impedir que um mesmo usuário mantenha mais de duas enquetes ativas por comunidade
RF53.4: Deve permitir que o criador solicite a fixação da enquete na homepage
RF53.5: Deve notificar o moderador ao receber solicitação de fixação
RF53.6: Deve notificar o autor sobre o deferimento ou indeferimento da solicitação de fixação
RF54: Deve permitir múltiplas opções
RF54.1: Deve permitir que o criador defina, na criação, se a enquete é de escolha única ou múltipla
RF54.2: Deve permitir que o criador defina o limite de opções selecionáveis em enquetes de escolha múltipla
RF54.3: Deve exigir no mínimo duas e no máximo cinco opções por enquete
RF55: Deve permitir responder enquete
RF55.1: Deve restringir a votação em enquete não fixada aos membros da comunidade de origem
RF55.2: Deve permitir a votação em enquete fixada a qualquer usuário autenticado
RF55.3: Deve impedir a votação em enquete ocultada por moderação
RF56: Deve impedir seleção múltipla em enquetes de escolha única
RF56.1: Deve impedir que o mesmo usuário vote mais de uma vez na mesma enquete
RF57: Deve consolidar resultados
RF57.1: Deve consolidar resultados sem manter vínculo entre usuário e opção votada (anonimato real — split `enquete_participacao`/`enquete_voto`; **nunca adicionar coluna de identidade/timestamp a `enquete_voto`**)
RF58: Deve exibir resultados
RF58.1: Deve suprimir a exibição de resultados enquanto a enquete aberta tiver menos de 5 votos
RF58.2: Deve permitir o encerramento manual da enquete
RF58.3: Deve encerrar a enquete ao atingir a data de encerramento, quando definida
RF58.4: Deve desafixar a enquete da homepage ao encerrá-la
RF58.5: Deve impedir a reabertura de enquete encerrada

`[DEFERRED — Architecture Spine: RNF05/LGPD para este módulo segue o padrão de anonimização já travado; módulo inteiro fora do corte semana 1.]`

#### 4.9 Busca (RF59–RF64) — fora do corte semana 1

RF59: Deve buscar por texto
RF60: Deve buscar usuários
RF61: Deve buscar comunidades
RF62: Deve buscar postagens
RF63: Deve aplicar filtros combinados
RF64: Deve retornar resultados paginados (usa `PageResponse` compartilhado, AD-4)

#### 4.10 Notificações (RF65–RF69) — fora do corte semana 1

RF65: Deve notificar resposta em postagem
RF66: Deve notificar menção
RF67: Deve notificar convite/enquete
RF68: Deve permitir marcar notificação como lida
RF69: Deve listar notificações do usuário

#### 4.11 Mensagens (RF70–RF74) — fora do corte semana 1

RF70: Deve permitir enviar mensagem privada
RF71: Deve receber mensagem
RF72: Deve listar conversas
RF73: Deve suportar grupos privados
RF74: Deve persistir histórico de mensagens

#### 4.12 Moderação (RF75–RF80 + subitens, RF77.1, RF78.2, RF79.2, RF80.2) — fora do corte semana 1

RF75: Deve permitir denunciar conteúdo
RF75.1: Deve alocar um agente inteligente para triagem de primeiro nível (blacklist de palavras)
RF75.2: O agente deve notificar o moderador
RF75.3: O agente deve analisar título e opções de enquete antes da publicação (avisa o autor antes de publicar, sem bloqueio automático)
RF76: Deve registrar denúncia
RF77: Moderador deve visualizar denúncias
RF77.1: O moderador que analisa uma denúncia vê o conteúdo denunciado e o motivo, mas nunca a identidade de quem denunciou — em nenhum papel (moderador original ou neutro).
RF78: Moderador deve ocultar conteúdo (reversível — estado de análise)
RF78.1: Deve permitir que qualquer moderador ou administrador restaure conteúdo ocultado
RF78.2: Ao ocultar um conteúdo, o sistema notifica o autor com o motivo, sem revelar a identidade do denunciante, no mesmo evento da ocultação, sem atraso.
RF79: Moderador deve remover conteúdo (definitivo)
RF79.1: Deve preservar registro de auditoria de conteúdo denunciado ou removido
RF79.2: Ao remover um conteúdo, o sistema notifica o autor com o motivo, sem revelar a identidade do denunciante (mesma garantia de RF78.2).
RF80: Moderador deve restringir usuário
RF80.1: Deve permitir o escalonamento de denúncia a moderador neutro
RF80.2: Ao escalonar uma denúncia, o sistema não notifica o moderador original sobre o desfecho. Denúncia sai da fila do original sem gerar notificação de fechamento; moderador neutro recebe na própria fila, sem novo nível de escalonamento a partir daí.

`[DEFERRED — Architecture Spine: integração do agente de IA de triagem (RF75.1–75.3) — onde roda, síncrono/assíncrono, fonte da blacklist — precisa de desenho próprio antes de Moderação ser construído. Open Question do PRD §10.1: sem segundo nível de escalonamento se o moderador neutro também tiver vínculo com os envolvidos.]`

#### 4.13 Avisos Institucionais (RF81, RF82) — módulo novo, fora do corte semana 1

RF81: Administrador da plataforma pode publicar um aviso institucional, escopado como geral (toda a universidade) ou específico de um curso. Aviso tem escopo obrigatório definido na criação; apenas administrador tem acesso (não moderadores).
RF82: O sistema exibe avisos institucionais no dashboard principal do usuário, filtrados por escopo — avisos gerais sempre aparecem; avisos de curso aparecem apenas a alunos daquele curso.

### NonFunctional Requirements

RNF01 (Usabilidade): usuário novo deve concluir login, entrada em comunidade e criação de postagem sem treinamento prévio.
RNF02 (Responsividade): interface deve operar corretamente em desktop e mobile browser, preservando integridade funcional nas jornadas principais.
RNF03 (Desempenho): operações críticas (navegação, autenticação, feed, abertura de comunidade) com p95 ≤ 2s em carga acadêmica esperada. `[RISCO — Architecture Spine Deferred: cold-start em free tier (Render hiberna após 15min; Neon escala a zero após 5min) ameaça este NFR em demo ao vivo.]`
RNF04 (Segurança de aplicação): baseline OWASP ASVS 4.0.3 — autenticação segura, controle de acesso, validação de entrada, gestão de sessão, proteção contra falhas comuns.
RNF05 (Privacidade): LGPD — finalidade, necessidade, transparência, controle de acesso, proteção do ciclo de vida dos dados. Atendido por design no modelo de anonimato de enquetes (RF57.1) e reforçado por RF77.1.
RNF06 (Acessibilidade): WCAG 2.2 nível AA.
RNF07 (Auditabilidade): login, denúncia, remoção de conteúdo, alteração administrativa — via `log_auditoria` centralizado (AD-11). Estende-se a RF78.2/RF79.2 e RF81.
RNF08 (Interoperabilidade de API): OpenAPI 3.1.0 — `openapi.yaml` como fonte de verdade (AD-4).
RNF09 (Rastreabilidade de requisitos): ISO/IEC/IEEE 29148:2018 — id único, origem, prioridade, racional, critério de aceitação e vínculo com teste por requisito. Critério de aceitação granular formalizado nesta fase (épicos/histórias).

### Additional Requirements

**Paradigma e stack (AD-1):** monólito multimodular — 1 deploy unit Quarkus, 1 módulo por área funcional, cada um JAX-RS Resource → Service → Repository, atrás de 1 filtro de segurança JWT. Frontend SPA Angular, deploy separado. TDD+SOLID+DDD (núcleo: interação de comunidade). TDD reforçado mecanicamente pelo gate de CI (AD-8); SOLID/DDD são disciplina de design, não verificados automaticamente.

**Segurança/JWT (AD-2):** filtro JAX-RS único intercepta toda requisição autenticável e valida o JWT antes de qualquer módulo, exceto endpoints `@PermitAll` (allowlist única no próprio filtro). Transporte só via header `Authorization: Bearer`, nunca cookie. Claims fixos: `sub` + `roles`. Autorização fina por perfil é responsabilidade de cada módulo. CORS configurado centralmente (`quarkus.http.cors`), nunca por módulo.

**Limites de módulo (AD-3):** cada módulo é dono das próprias tabelas. Leitura entre módulos via JPA association é permitida, mas somente-leitura ao nível de transação (DTO/projeção ou `FlushMode.MANUAL`). Escrita em dado de outro módulo só através de interface Java publicada pelo módulo dono. Acesso direto a repositório/tabela alheios é proibido — verificado por teste de arquitetura automatizado (ex.: ArchUnit) no CI.

**Contrato OpenAPI-first (AD-4):** `openapi.yaml` (3.1.0) na raiz do repo, fonte de verdade acordada antes de qualquer lado implementar endpoint novo. Validação de contrato em CI inclui teste de contrato em runtime por endpoint (corpo real vs. schema). Toda listagem usa o componente compartilhado `PageResponse` (`$ref`), nenhum endpoint inventa a própria paginação.

**Envelope de erro padrão (AD-5):** `{"error":{"code","message","details"}}` + status HTTP mapeado por cenário (401 sem auth; 403 autenticado sem permissão, recurso não precisa ficar oculto; 404 quando a existência do recurso deve ficar oculta a quem não tem acesso; 400/422 validação; 409 conflito; 500 não tratado) — mapa de cenário fixo, nenhum módulo escolhe 403 vs. 404 por conta própria.

**Hosting e CORS (AD-6):** Angular como Render Static Site; Quarkus como Render Web Service via Docker; PostgreSQL gerenciado no Neon (free tier persistente) — nunca o Postgres gratuito do Render (expira em 30+14 dias). CORS liberado exatamente para a origem do Static Site.

**Ambiente local (AD-7):** `docker-compose.yml` na raiz sobe Postgres + Quarkus dev mode + Angular dev server, com `.env.example` compartilhado. Nenhum dev instala Postgres localmente fora do compose.

**CI/CD (AD-8):** GitHub Actions roda build + testes + validação OpenAPI em todo PR. Merge em `main` exige CI verde (sem revisão humana obrigatória — decisão do time). Merge dispara deploy automático via integração nativa GitHub↔Render.

**Migrations (AD-9):** Liquibase, changelogs versionados por módulo (`db/changelog/{modulo}/*.xml`), incluídos por changelog mestre estável via `<includeAll>`. Changeset id prefixado pelo módulo (ex.: `comunidades-002-add-campo`), nunca contador global — evita colisão entre PRs paralelos por construção.

**Observabilidade (AD-10):** health check via `quarkus-smallrye-health` (`/q/health`). Logs estruturados JSON para stdout (logs nativos do Render). Sem staging na semana 1 — só local (docker-compose) e produção (Render).

**Log de auditoria transversal (AD-11):** tabela `log_auditoria` única, gravada só via `AuditoriaService` injetável (infraestrutura transversal, não pertence a nenhum dos 12 módulos). Ativo desde a semana 1 (login via Identidade, alteração administrativa via Comunidades).

**Convenções de dados:** nomes de entidade/tabela/coluna em português (linguagem ubíqua do domínio); classes técnicas (`Resource`/`Service`/`Repository`) e paths REST em inglês. IDs `bigint`/identity (não UUID). Instantes em `Instant` (ISO-8601 UTC); valores só-data em `LocalDate`. Campos JSON de request/response em camelCase português (`nomeCompleto`).

**Estrutura do repo:** monorepo (`frontend/`, `backend/` por pacote de módulo, `openapi.yaml`, `docker-compose.yml`, `.env.example`, `.github/workflows/`) — `[ASSUMPTION]` não decidido explicitamente pelo time, recomendação da arquitetura.

**Corte de escopo semana 1 (crítico — ver Overview acima):** must-have RF01–RF13 + RF21–RF31 + RF32–RF36 ponta a ponta deployado; stretch RF14–RF20 + RF37–RF42; todo o resto (RF43–RF82) fora do corte.

**Item deferido de risco aceito:** colisão de edição concorrente em `openapi.yaml` — time optou por manter CI-only, sem exceção de aprovação obrigatória para esse arquivo.

### UX Design Requirements

UX-DR1: Sistema de design tokens "Campus Clean" — paleta de cor (bg, surface, border, ink/ink-soft/ink-faint, maroon, orange, orange-tint, green-ok) implementada como tokens/variáveis CSS reutilizáveis, consumida por todos os componentes.
UX-DR2: Escala tipográfica (greeting 22px/600, question 15px/600, body 13.5px/400 lh1.5, meta 12px/400, label-caps 10.5px/700 tracking 0.05em) com pilha de fontes de sistema.
UX-DR3: Componente Badge de comunidade (`badge-course` maroon/`badge-open` orange) — pill, fundo `orange-tint`, nunca as duas cores no mesmo badge.
UX-DR4: Componente Botão primário (`button-primary`) — fundo orange, texto branco, pill; único estilo de ação forte do sistema (Participar, CTA de enquete, confirmar formulário).
UX-DR5: Componente Indicador de membro (`member-indicator`) — texto green-ok sem fundo, com ícone de check, substitui o botão de ação quando já é membro.
UX-DR6: Componente Card genérico (surface + border 1px + rounded.md + card-padding) — base de post, notícia, item de enquete.
UX-DR7: Shell de navegação global — sidebar fixa ~220px (Início, Buscar, Mensagens, Notificações, Criar enquete, Denúncias/Solicitações de fixação para moderador/admin, Suas comunidades + Descobrir) + conteúdo fluido + painel de descoberta ~260px em telas largas; topbar com avatar → dropdown (Perfil, Configurações, Sair).
UX-DR8: Comportamento responsivo de colapso da sidebar/painel direito abaixo de desktop — breakpoints exatos deferidos para a implementação, mas exigido por RNF02 (mobile browser funcional).
UX-DR9: Sistema de toast para confirmações leves (entrar em comunidade, auto-join, voto registrado) — não bloqueia a tela, some sozinho; nunca único canal de confirmação crítica (ver UX-DR25).
UX-DR10: Componente Card de post — avatar, nome, badge de comunidade, horário relativo, corpo, ações (curtir/comentar/compartilhar), anexo de material quando houver.
UX-DR11: Componente Comentário em árvore — indentação trava em ~3 níveis (respostas além ficam no mesmo recuo); edição/exclusão restrita ao próprio conteúdo (RF39/RF40).
UX-DR12: Transição Botão Participar → Indicador de membro — muda de estado imediatamente ao clicar (sem modal de confirmação) + toast "Você entrou em {comunidade}".
UX-DR13: Formulário de criação de enquete — pergunta, tipo (única/múltipla, RF54.1), limite de seleção se múltipla (RF54.2), 2–5 opções (RF54.3), data de encerramento opcional, checkbox "fixar na homepage" (RF53.4), lista de comunidades do usuário com as que atingiram limite de 2 enquetes ativas desabilitadas (RF53.3).
UX-DR14: Componente Enquete (exibição) com 4 estados distintos — antes de votar; <5 votos após votar ("X de 5 necessários", RF58.1); resultado liberado (barras); encerrada (resultado sempre visível, sem opção de votar).
UX-DR15: Componente Notificação agrupada — eventos parecidos no mesmo alvo agrupam numa linha (ex.: "Rafael e mais 4 curtiram sua postagem"); não lida destacada; clique leva à origem.
UX-DR16: Componente Material anexado — PDF/imagem: ícone + nome do arquivo, clique abre/baixa; link externo: preview simples (título + domínio).
UX-DR17: Resultado de busca — lista única mista (usuários, comunidades, postagens) diferenciados por ícone/tipo, não abas separadas.
UX-DR18: Componente Item de fila de moderação — conteúdo + motivo visíveis, identidade de quem denunciou nunca exibida (RF77.1); ações Ocultar/Remover/Restringir usuário/Escalonar para moderador neutro.
UX-DR19: Estado "comunidade de curso, não-membro" — feed visível; no lugar da caixa de postar/comentar/votar, aviso explicando que o usuário não é membro (RF27.1) — nunca omitir o controle silenciosamente.
UX-DR20: Estado "conteúdo oculto" — esmaecido/cinza, some das interações normais, botão "Restaurar" visível a qualquer moderador/admin (RF78.1), reversível.
UX-DR21: Estado "conteúdo removido" — desaparece da lista, sobrevive só no log de auditoria (RF79.1), definitivo.
UX-DR22: Toast de auto-join — único, na primeira visita ao Início pós-associação: "Você já faz parte de {comunidade} 🎓".
UX-DR23: Estado "e-mail não confirmado" no login — bloqueia login com mensagem específica, distinta de credencial inválida (RF01.2).
UX-DR24: Sistema de voz e tom (microcopy) — direto, segunda pessoa ("você"), erros explicam o que houve e o que fazer sem culpar o usuário, estados vazios convidam a agir sem forçar humor — conforme tabela Do/Don't de `EXPERIENCE.md`.
UX-DR25: Piso de acessibilidade WCAG 2.2 AA — foco de teclado visível em todo elemento interativo; badges/indicadores de estado carregam texto/label (não dependem só de cor); confirmação crítica reflete em estado persistente, não só toast; formulário de enquete com labels associados e erros de validação anunciados (não só coloridos).
UX-DR26: Motion mínimo — fade/slide rápido (~150–200ms) só em toasts, dropdowns e troca de aba; sem animação decorativa ou microinteração elaborada.
UX-DR27: Restilização Campus Clean de 6 telas já existentes no Figma (Login, Cadastro, Criação de comunidade — só comunidade aberta, Artigo/post+comentários, Perfil, Painel admin) — reconciliar contra o protótipo original (ver `reconcile-figma-prototype.md`).
UX-DR28: Construção de 4 telas novas sem mockup visual, spine-only (Verifique seu e-mail, Busca, Mensagens, Solicitações de fixação — fila separada de Denúncias, mesmo padrão de "Item de fila de moderação").

### FR Coverage Map

RF01–RF13, RF01.1–RF01.3: Epic 1 — Cadastro, Login e Controle de Acesso
RF21–RF31, RF21.1, RF21.2, RF24.1, RF27.1: Epic 2 — Comunidades
RF32–RF36: Epic 3 — Publicações
RF14–RF20, RF20.1, RF20.2: Epic 4 — Perfil Acadêmico
RF37–RF42: Epic 5 — Discussões
RF43–RF47: Epic 6 — Filtro de Conteúdo
RF48–RF52: Epic 7 — Materiais
RF53–RF58, RF53.1–RF53.6, RF54.1–RF54.3, RF55.1–RF55.3, RF56.1, RF57.1, RF58.1–RF58.5: Epic 8 — Enquetes e Pesquisas
RF59–RF64: Epic 9 — Busca
RF65–RF69: Epic 10 — Notificações
RF70–RF74: Epic 11 — Mensagens
RF75–RF80, RF75.1–RF75.3, RF77.1, RF78.1, RF78.2, RF79.1, RF79.2, RF80.1, RF80.2: Epic 12 — Moderação
RF81, RF82: Epic 13 — Avisos Institucionais

**NFRs, ADs (Architecture) e UX-DRs são transversais** — não mapeiam a um único épico; cada história, em qualquer épico, deve satisfazer as regras relevantes (envelope de erro AD-5, contrato OpenAPI AD-4, filtro JWT AD-2, limites de módulo AD-3, log de auditoria AD-11 quando aplicável, piso de acessibilidade UX-DR25, voz/tom UX-DR24, etc.) como critério de aceitação, não como épico próprio. RNF09 (rastreabilidade) é atendido por este mapa + pelos critérios de aceitação granulares nas histórias (Step 3+).

## Epic List

### Epic 1: Cadastro, Login e Controle de Acesso `[Semana 1 · Must-have]`
Aluno se cadastra com e-mail institucional, confirma o e-mail antes do primeiro login, autentica com sessão JWT, faz logout, e o sistema restringe ações por perfil. Inclui o scaffold do projeto (repo monorepo, docker-compose, pipeline CI/CD, migrations Liquibase, contrato `openapi.yaml` base, envelope de erro padrão, filtro de segurança JWT, health check, log de auditoria transversal) como primeira história — fundação sobre a qual todos os épicos seguintes constroem.
**FRs covered:** RF01, RF01.1, RF01.2, RF01.3, RF02, RF03, RF04, RF05, RF06, RF07, RF08, RF09, RF10, RF11, RF12, RF13

### Epic 2: Comunidades `[Semana 1 · Must-have]`
Aluno é auto-associado à comunidade do seu curso ao definir o curso, descobre e entra voluntariamente em comunidades abertas, sai de comunidades, visualiza o feed de uma comunidade mesmo sem ser membro (com interação bloqueada e explicada); administrador da plataforma pré-cria comunidades de curso; administrador de comunidade edita, exclui e remove membros.
**FRs covered:** RF21, RF21.1, RF21.2, RF22, RF23, RF24, RF24.1, RF25, RF26, RF27, RF27.1, RF28, RF29, RF30, RF31

### Epic 3: Publicações `[Semana 1 · Must-have]`
Aluno cria postagens em uma comunidade da qual é membro e visualiza a listagem de postagens da comunidade, sujeito à restrição de interação por membro (RF27.1).
**FRs covered:** RF32, RF33, RF34, RF35, RF36

### Epic 4: Perfil Acadêmico `[Semana 1 · Stretch]`
Aluno cria e edita nome, curso, período e interesses do próprio perfil; recebe notificação de onboarding progressivo para completar interesses; visualiza o perfil público (somente leitura) de outro usuário a partir de uma postagem/comentário.
**FRs covered:** RF14, RF15, RF16, RF17, RF18, RF19, RF20, RF20.1, RF20.2

### Epic 5: Discussões `[Semana 1 · Stretch]`
Aluno comenta em uma postagem, responde a um comentário formando encadeamento (trava em ~3 níveis de indentação por UX), edita e exclui apenas o próprio conteúdo.
**FRs covered:** RF37, RF38, RF39, RF40, RF41, RF42

### Epic 6: Filtro de Conteúdo `[Pós-semana-1]`
Aluno filtra postagens por curso, disciplina e tipo de conteúdo, com a filtragem persistida entre sessões/navegação.
**FRs covered:** RF43, RF44, RF45, RF46, RF47

### Epic 7: Materiais `[Pós-semana-1]`
Aluno anexa arquivos (PNG/PDF/JPG) ou links a uma postagem; qualquer usuário com acesso à postagem acessa o material anexado.
**FRs covered:** RF48, RF49, RF50, RF51, RF52

### Epic 8: Enquetes e Pesquisas `[Pós-semana-1]`
Aluno membro de uma comunidade cria uma enquete (escolha única/múltipla, 2–5 opções, encerramento opcional), vota com anonimato real garantido (sem vínculo persistido entre usuário e opção), acompanha resultados consolidados após votar ou após o encerramento; solicita fixação na homepage, que o moderador aprova ou recusa; qualquer usuário autenticado vota em enquete fixada da universidade.
**FRs covered:** RF53, RF53.1, RF53.2, RF53.3, RF53.4, RF53.5, RF53.6, RF54, RF54.1, RF54.2, RF54.3, RF55, RF55.1, RF55.2, RF55.3, RF56, RF56.1, RF57, RF57.1, RF58, RF58.1, RF58.2, RF58.3, RF58.4, RF58.5

### Epic 9: Busca `[Pós-semana-1]`
Aluno busca usuários, comunidades e postagens por texto livre, combina filtros e navega por resultados paginados (usando o envelope `PageResponse` compartilhado).
**FRs covered:** RF59, RF60, RF61, RF62, RF63, RF64

### Epic 10: Notificações `[Pós-semana-1]`
Aluno recebe notificação de resposta em postagem, menção e convite/enquete; marca notificações como lidas; lista todas as suas notificações.
**FRs covered:** RF65, RF66, RF67, RF68, RF69

### Epic 11: Mensagens `[Pós-semana-1]`
Aluno envia e recebe mensagens privadas, lista suas conversas, participa de grupos privados, com histórico persistido.
**FRs covered:** RF70, RF71, RF72, RF73, RF74

### Epic 12: Moderação `[Pós-semana-1]`
Usuário denuncia conteúdo impróprio; agente de triagem (blacklist) notifica o moderador; moderador visualiza a denúncia sem ver a identidade do denunciante, oculta (reversível) ou remove (definitivo) o conteúdo, ou restringe o usuário autor; qualquer moderador/admin restaura conteúdo ocultado; autor é notificado do motivo sem saber quem denunciou; moderador escalona a um moderador neutro sem retorno ao moderador original; toda ação preserva registro de auditoria.
**FRs covered:** RF75, RF75.1, RF75.2, RF75.3, RF76, RF77, RF77.1, RF78, RF78.1, RF78.2, RF79, RF79.1, RF79.2, RF80, RF80.1, RF80.2

### Epic 13: Avisos Institucionais `[Pós-semana-1]`
Administrador da plataforma publica um aviso institucional com escopo obrigatório (geral ou curso específico); aluno vê no dashboard os avisos gerais e os avisos do próprio curso, sem ver avisos de outros cursos.
**FRs covered:** RF81, RF82

### Epic 14: Fundação Visual e de Experiência (Design System) `[Semana 1 · Must-have parcial]`
*Épico adicionado no Step 3 para cobrir os UX-DRs transversais (tokens, componentes base, shell de navegação, toast/motion, voz e tom, restilização de telas) que não pertencem a um único módulo de feature — necessário para o corte da semana 1, já que Identidade/Comunidades/Publicações precisam de telas estilizadas para ir ao ar.*
*Triado em 2026-08-30: 5 histórias no corte da semana 1 (14.1, 14.2, 14.3, 14.5, 14.7) e 4 diferidas (14.4, 14.6, 14.8, 14.9). Ver a nota de triagem na seção do épico — diferir a história não suspende os NFRs/UX-DRs que ela sistematiza.*
Aplica a identidade "Campus Clean" (tokens de cor/tipografia/espaçamento/forma), a casca de navegação global e os componentes/padrões de comportamento reutilizáveis, para que toda tela construída nos demais épicos herde uma experiência visual e textual consistente desde o primeiro deploy.
**UX-DRs covered:** UX-DR1, UX-DR2, UX-DR3, UX-DR4, UX-DR5, UX-DR6, UX-DR7, UX-DR8, UX-DR9, UX-DR24, UX-DR26, UX-DR27, UX-DR28

---

## Epic 1: Cadastro, Login e Controle de Acesso

Aluno se cadastra com e-mail institucional, confirma o e-mail antes do primeiro login, autentica com sessão JWT, faz logout, e o sistema restringe ações por perfil. Inclui o scaffold do projeto como primeira história — fundação sobre a qual todos os épicos seguintes constroem.

**FRs covered:** RF01, RF01.1, RF01.2, RF01.3, RF02, RF03, RF04, RF05, RF06, RF07, RF08, RF09, RF10, RF11, RF12, RF13

### Story 1.1: Fundação do projeto (scaffold e infraestrutura)

As a integrante do time de desenvolvimento,
I want um projeto com repositório monorepo, ambiente local reproduzível, pipeline de CI/CD, migrations versionadas, contrato OpenAPI base, envelope de erro padrão, filtro de segurança JWT (scaffold) e log de auditoria transversal já configurados,
So that toda funcionalidade das próximas histórias seja construída sobre uma fundação consistente e testável, alinhada à Architecture Spine (AD-1 a AD-11), desde o primeiro PR.

**Acceptance Criteria:**

**Given** o repositório está vazio
**When** o scaffold é aplicado
**Then** existe estrutura `frontend/` (Angular) e `backend/` (Quarkus, pacote por módulo) na raiz do monorepo
**And** `docker-compose.yml` sobe Postgres + Quarkus dev mode + Angular dev server com `.env.example` compartilhado (AD-7)

**Given** o scaffold é aplicado
**When** o contrato é inicializado
**Then** `openapi.yaml` existe na raiz com o componente compartilhado `PageResponse` e o schema do envelope de erro `{"error":{"code","message","details"}}` definidos (AD-4, AD-5)

**Given** o scaffold é aplicado
**When** a segurança é inicializada
**Then** o filtro de segurança JWT está registrado como JAX-RS filter, com allowlist `@PermitAll` inicial e claims `sub`+`roles` definidos no contrato do token (AD-2)
**And** a tabela `log_auditoria` e o `AuditoriaService` injetável existem e estão prontos para uso pelos módulos (AD-11)

**Given** um PR é aberto no repositório
**When** o pipeline GitHub Actions roda
**Then** build + testes + validação de `openapi.yaml` executam, e merge em `main` é bloqueado sem CI verde (AD-8)
**And** o changelog mestre do Liquibase existe com `<includeAll>` configurado, pronto para receber changelogs por módulo (AD-9)
**And** o endpoint `/q/health` responde 200 quando a aplicação está saudável (AD-10)

### Story 1.2: Cadastro de aluno com e-mail institucional

As a aluno sem conta,
I want me cadastrar informando nome, e-mail institucional, senha e meu curso,
So that eu possa criar minha conta na UniCatólica, já ser associado à comunidade do meu curso, e depois confirmar meu e-mail para acessar a plataforma.

**Acceptance Criteria:**

**Given** eu não tenho conta e informo nome, e-mail do domínio institucional, senha válida, curso e sou maior de 18 anos
**When** eu envio o cadastro
**Then** o sistema persiste o usuário com um perfil padrão contendo nome e curso (RF05) e status "e-mail não confirmado"
**And** nenhuma sessão ativa é gerada
**And** o curso informado dispara o auto-join à comunidade de curso correspondente (RF24.1, Epic 2 Story 2.3)

`Nota: a captura de curso no cadastro é o mínimo necessário para o auto-join (RF24.1, must-have semana 1) funcionar sem depender do módulo completo de Perfil Acadêmico (RF14–RF20, Epic 4, stretch). Edição de nome/curso/período/interesses depois do cadastro é escopo do Epic 4, não desta história.`

**Given** eu informo um e-mail que já possui conta (RF02)
**When** eu envio o cadastro
**Then** o sistema rejeita com a mensagem "Esse e-mail já tem uma conta. Esqueceu a senha?"

**Given** eu informo um e-mail de domínio externo ao institucional (RF01.3)
**When** eu envio o cadastro
**Then** o sistema rejeita com a mensagem "Use seu e-mail institucional para se cadastrar."

**Given** eu informo um e-mail em formato inválido (RF03) ou uma senha que não atende à política (RF04)
**When** eu envio o cadastro
**Then** o sistema rejeita com mensagem de validação específica ao campo

**Given** eu informo uma data de nascimento que resulta em idade menor que 18 anos (RF01.1)
**When** eu envio o cadastro
**Then** o sistema rejeita o cadastro
**And** toda resposta de erro desta história segue o envelope `{"error":{"code","message","details"}}` com status HTTP mapeado por AD-5

### Story 1.3: Confirmação de e-mail antes do primeiro login

As a aluno recém-cadastrado,
I want confirmar meu e-mail institucional,
So that eu possa fazer login pela primeira vez na plataforma.

**Acceptance Criteria:**

**Given** meu cadastro foi concluído e meu e-mail está pendente de confirmação
**When** eu clico no link de confirmação enviado ao meu e-mail
**Then** meu status muda para "e-mail confirmado" e eu posso fazer login normalmente

**Given** meu e-mail ainda não foi confirmado
**When** eu tento fazer login com credenciais corretas
**Then** o sistema rejeita com a mensagem "Confirme seu e-mail antes de entrar. Reenviar confirmação" — distinta da mensagem de credencial inválida (RF01.2)

**Given** estou na tela "Verifique seu e-mail" pós-cadastro
**When** a tela carrega
**Then** ela confirma o cadastro e orienta a checar o e-mail, com opção de reenviar a confirmação

### Story 1.4: Login e emissão de sessão JWT

As a aluno com e-mail confirmado,
I want fazer login com meu e-mail e senha,
So that eu receba uma sessão autenticada e possa acessar a plataforma.

**Acceptance Criteria:**

**Given** eu informo credenciais válidas e meu e-mail está confirmado
**When** eu envio o login (RF06)
**Then** o sistema gera um token JWT com claims `sub` e `roles` (RF08), transportado apenas via header `Authorization: Bearer` (AD-2)

**Given** eu informo credenciais inválidas (e-mail ou senha incorretos)
**When** eu envio o login (RF07)
**Then** o sistema rejeita com mensagem genérica de credencial inválida, sem indicar qual campo está errado

### Story 1.5: Bloqueio de acesso sem autenticação e restrição por perfil

As a aluno ou moderador com dados na plataforma,
I want que qualquer ação exija autenticação válida e respeite meu perfil,
So that meus dados e os de outros usuários fiquem protegidos contra acesso não autorizado ou fora do meu papel (RF09, RF12, RF13).

**Acceptance Criteria:**

**Given** uma requisição chega a um endpoint que não está na allowlist `@PermitAll`
**When** o token JWT está ausente, expirado ou inválido
**Then** o filtro de segurança JWT rejeita a requisição com 401, no envelope de erro padrão (AD-2, AD-5)

**Given** um usuário autenticado tenta executar uma ação para a qual seu perfil (RF12) não tem permissão (RF13)
**When** a requisição chega ao módulo responsável
**Then** o módulo rejeita com 403 (quando o recurso não precisa ficar oculto) ou 404 (quando a existência do recurso deve ficar oculta), conforme o mapa de cenário da AD-5

### Story 1.6: Logout e invalidação de sessão

As a aluno autenticado,
I want fazer logout,
So that minha sessão seja encerrada e não possa mais ser usada para autenticar requisições.

**Acceptance Criteria:**

**Given** estou autenticado com uma sessão válida
**When** eu solicito logout (RF10)
**Then** a sessão é invalidada (RF11)
**And** requisições subsequentes com o mesmo token são rejeitadas com 401

---

## Epic 2: Comunidades

Aluno é auto-associado à comunidade do seu curso, descobre e entra voluntariamente em comunidades abertas, sai de comunidades, visualiza o feed de uma comunidade mesmo sem ser membro (com interação bloqueada e explicada); administrador da plataforma pré-cria comunidades de curso; administrador de comunidade edita, exclui e remove membros.

**FRs covered:** RF21, RF21.1, RF21.2, RF22, RF23, RF24, RF24.1, RF25, RF26, RF27, RF27.1, RF28, RF29, RF30, RF31

### Story 2.1: Pré-criação de comunidades de curso pelo administrador

As a administrador da plataforma,
I want pré-criar a comunidade de cada curso da instituição,
So that toda comunidade de curso já exista antes da entrada dos primeiros alunos (RF21.2, RF21.1).

**Acceptance Criteria:**

**Given** sou administrador da plataforma
**When** eu crio uma comunidade do tipo "comunidade de curso" associada a um curso da instituição
**Then** a comunidade é persistida com tipo imutável "curso" e sem botão "participar" exposto (RF21.1)

**Given** não sou administrador da plataforma
**When** eu tento criar uma comunidade do tipo "curso"
**Then** o sistema rejeita — apenas comunidades abertas podem ser criadas por alunos (RF21.2)

**Given** os campos obrigatórios da comunidade não são preenchidos (RF22)
**When** eu tento salvar
**Then** o sistema rejeita com validação por campo

### Story 2.2: Criação de comunidade aberta por qualquer aluno

As a aluno autenticado,
I want criar uma comunidade aberta em torno de um interesse ou tema,
So that eu possa reunir outros alunos com o mesmo interesse (RF21, RF23).

**Acceptance Criteria:**

**Given** informo os campos obrigatórios de uma comunidade aberta (RF22)
**When** eu envio a criação
**Then** a comunidade é persistida com tipo imutável "aberta", com botão "participar" exposto (RF21.1)
**And** eu sou associado automaticamente como administrador dela (RF23)

### Story 2.3: Auto-join à comunidade de curso

As a aluno que define ou altera o curso no meu perfil,
I want ser automaticamente associado à comunidade daquele curso,
So that eu já esteja inserido numa comunidade relevante sem esforço de configuração (RF24.1).

**Acceptance Criteria:**

**Given** eu defino meu curso pela primeira vez — no cadastro (Story 1.2) ou, quando o Epic 4 existir, na edição de perfil (Story 4.1)
**When** a informação é salva
**Then** sou automaticamente associado à comunidade de curso correspondente, sem passar pela validação de ingresso duplicado (RF25) — a operação é idempotente
**And** na minha próxima visita ao Início, vejo um toast único "Você já faz parte de {comunidade do curso} 🎓"

**Given** eu já estava associado à comunidade de um curso anterior e altero meu curso
**When** a alteração é salva
**Then** a associação à comunidade do curso anterior é removida e a do novo curso é adicionada

`Nota: esta história cobre o mecanismo de auto-join em si (reagir a um curso definido/alterado). O gatilho "definir no cadastro" já está disponível via Story 1.2 — não depende do Epic 4 para o corte must-have da semana 1.`

### Story 2.4: Ingresso e saída de comunidade aberta

As a aluno autenticado,
I want entrar e sair de comunidades abertas,
So that eu controle minha participação em comunidades por interesse (RF24, RF25, RF26).

**Acceptance Criteria:**

**Given** encontro uma comunidade aberta da qual não sou membro
**When** eu clico em "Participar"
**Then** me torno membro imediatamente, sem modal de confirmação, com o botão trocando para indicador de membro e um toast "Você entrou em {comunidade}" (RF24)

**Given** já sou membro de uma comunidade aberta
**When** eu tento entrar novamente
**Then** o sistema impede o ingresso duplicado (RF25)

**Given** sou membro de uma comunidade
**When** eu solicito sair
**Then** minha associação é removida (RF26)

### Story 2.5: Listagem, filtro e visualização de comunidade não-membro

As a aluno autenticado,
I want listar e filtrar comunidades, e visualizar o feed de uma comunidade de curso mesmo sem ser membro,
So that eu descubra comunidades relevantes e entenda por que não posso interagir em uma da qual não sou membro (RF27, RF27.1, RF28).

**Acceptance Criteria:**

**Given** estou autenticado
**When** eu listo comunidades (RF27) ou aplico um filtro (RF28)
**Then** vejo a lista/lista filtrada correspondente

**Given** visualizo uma comunidade de curso da qual não sou membro
**When** a tela carrega
**Then** vejo o feed normalmente, mas no lugar da caixa de postar/comentar/votar vejo um aviso explicando que não sou membro — nunca omitido silenciosamente (RF27.1)

**Given** tento postar, comentar ou votar em uma comunidade de curso da qual não sou membro
**When** a requisição chega ao backend
**Then** o sistema retorna erro de permissão (RF27.1)

### Story 2.6: Administração de comunidade

As a administrador de uma comunidade,
I want remover membros, editar e excluir a comunidade,
So that eu mantenha a comunidade organizada e saudável (RF29, RF30, RF31).

**Acceptance Criteria:**

**Given** sou administrador de uma comunidade
**When** eu removo um membro (RF29)
**Then** o membro perde a associação e as permissões de interação correspondentes

**Given** sou administrador de uma comunidade
**When** eu edito os campos da comunidade (RF30)
**Then** as alterações são persistidas, respeitando a imutabilidade do tipo (curso/aberta) definida na criação

**Given** sou administrador de uma comunidade
**When** eu excluo a comunidade (RF31)
**Then** a comunidade deixa de aparecer nas listagens e não aceita mais interações

---

## Epic 3: Publicações

Aluno cria postagens em uma comunidade da qual é membro e visualiza a listagem de postagens da comunidade, sujeito à restrição de interação por membro (RF27.1).

**FRs covered:** RF32, RF33, RF34, RF35, RF36

### Story 3.1: Criar postagem em comunidade

As a aluno membro de uma comunidade,
I want criar uma postagem nessa comunidade,
So that eu compartilhe conteúdo com os demais membros (RF32, RF34, RF35).

**Acceptance Criteria:**

**Given** sou membro da comunidade e preencho o conteúdo obrigatório da postagem (RF33)
**When** eu publico
**Then** a postagem é persistida associada a mim (RF34) e à comunidade (RF35)

**Given** tento publicar sem o conteúdo obrigatório
**When** eu envio
**Then** o sistema rejeita com validação específica (RF33)

**Given** não sou membro de uma comunidade de curso e tento publicar nela (RF27.1)
**When** eu envio
**Then** o sistema rejeita com erro de permissão

### Story 3.2: Listar postagens da comunidade

As a usuário autenticado,
I want visualizar a listagem de postagens de uma comunidade,
So that eu acompanhe o conteúdo publicado (RF36).

**Acceptance Criteria:**

**Given** acesso o feed de uma comunidade
**When** a tela carrega
**Then** vejo as postagens da comunidade, cada uma com avatar do autor, nome, badge de comunidade, horário relativo e corpo do texto (RF36)

---

## Epic 4: Perfil Acadêmico

Aluno cria e edita nome, curso, período e interesses do próprio perfil; recebe notificação de onboarding progressivo para completar interesses; visualiza o perfil público (somente leitura) de outro usuário a partir de uma postagem/comentário.

**FRs covered:** RF14, RF15, RF16, RF17, RF18, RF19, RF20, RF20.1, RF20.2

### Story 4.1: Criar e editar perfil acadêmico

As a aluno autenticado,
I want criar e editar meu nome, curso, período e interesses,
So that meu perfil reflita minha situação acadêmica atual (RF14, RF15, RF16, RF17, RF18, RF19).

**Acceptance Criteria:**

**Given** ainda não tenho perfil acadêmico completo
**When** eu preencho nome, curso e período
**Then** o perfil é criado e persistido (RF14, RF19)

**Given** já tenho um perfil
**When** eu edito nome (RF15), curso (RF16), período (RF17) ou adiciono interesses (RF18)
**Then** as alterações são persistidas (RF19)
**And** se eu alterei o curso, o auto-join da comunidade de curso (Epic 2, Story 2.3) é disparado

### Story 4.2: Consultar o próprio perfil

As a aluno autenticado,
I want consultar meu perfil acadêmico,
So that eu confirme os dados salvos (RF20).

**Acceptance Criteria:**

**Given** tenho um perfil salvo
**When** eu acesso a tela de perfil
**Then** vejo nome, curso, período e interesses corretamente retornados (RF20)

### Story 4.3: Notificação de onboarding progressivo

As a aluno com perfil incompleto (interesses vazios),
I want receber uma notificação convidando a completar meu perfil,
So that eu seja incentivado a preencher interesses depois de já estar engajado (RF20.1).

**Acceptance Criteria:**

**Given** meu perfil tem interesses vazios após um período de uso definido pelo sistema
**When** a condição-gatilho é atingida
**Then** recebo uma notificação com o texto "Complete seu perfil e apareça mais nas buscas — adicione seus interesses." (RF20.1)

### Story 4.4: Visualizar perfil público de outro usuário

As a usuário autenticado,
I want visualizar o perfil acadêmico público de outro usuário a partir do nome/foto em uma postagem ou comentário,
So that eu conheça quem publicou ou comentou (RF20.2).

**Acceptance Criteria:**

**Given** clico no nome/avatar de outro usuário em uma postagem ou comentário
**When** a tela de perfil de terceiros carrega
**Then** vejo os mesmos campos do próprio perfil (nome, curso, período, interesses), somente leitura, sem controles de edição e sem dados sensíveis adicionais (RF20.2)

---

## Epic 5: Discussões

Aluno comenta em uma postagem, responde a um comentário formando encadeamento (trava em ~3 níveis de indentação), edita e exclui apenas o próprio conteúdo.

**FRs covered:** RF37, RF38, RF39, RF40, RF41, RF42

### Story 5.1: Comentar em postagem

As a aluno membro da comunidade da postagem,
I want comentar em uma postagem,
So that eu participe da discussão (RF37).

**Acceptance Criteria:**

**Given** sou membro da comunidade da postagem
**When** eu envio um comentário
**Then** o comentário é persistido associado à postagem e a mim

### Story 5.2: Responder comentário com encadeamento

As a aluno,
I want responder a um comentário existente,
So that a conversa mantenha o encadeamento visual da discussão (RF38, RF42).

**Acceptance Criteria:**

**Given** existe um comentário em uma postagem
**When** eu respondo a esse comentário
**Then** a resposta é persistida mantendo a relação hierárquica com o comentário pai (RF42)
**And** a indentação visual trava em ~3 níveis — respostas além do 3º nível ficam no mesmo recuo, sem empilhar mais

### Story 5.3: Editar e excluir o próprio conteúdo

As a autor de um comentário ou resposta,
I want editar ou excluir apenas o meu próprio conteúdo,
So that eu corrija ou remova algo que escrevi, sem poder alterar o conteúdo de terceiros (RF39, RF40, RF41).

**Acceptance Criteria:**

**Given** sou o autor de um comentário
**When** eu edito (RF39) ou excluo (RF41) esse comentário
**Then** a alteração/exclusão é aplicada

**Given** não sou o autor de um comentário
**When** eu tento editá-lo ou excluí-lo
**Then** o sistema impede a ação (RF40)

---

## Epic 6: Filtro de Conteúdo

Aluno filtra postagens por curso, disciplina e tipo de conteúdo, com a filtragem persistida durante a navegação.

**FRs covered:** RF43, RF44, RF45, RF46, RF47

### Story 6.1: Filtrar postagens por curso, disciplina e tipo

As a usuário autenticado,
I want filtrar postagens por curso, disciplina e tipo de conteúdo,
So that eu encontre conteúdo relevante mais rápido (RF43, RF44, RF45, RF47).

**Acceptance Criteria:**

**Given** estou no feed
**When** eu aplico um filtro por curso (RF43), disciplina (RF44) ou tipo de conteúdo (RF45)
**Then** a listagem exibida reflete apenas as postagens que atendem ao(s) filtro(s) selecionado(s) (RF47)

### Story 6.2: Persistência da filtragem aplicada

As a usuário autenticado,
I want que o filtro que apliquei permaneça ativo enquanto eu navego,
So that eu não precise reaplicá-lo a cada tela (RF46).

**Acceptance Criteria:**

**Given** apliquei um ou mais filtros no feed
**When** eu navego para outra tela e retorno ao feed dentro da mesma sessão
**Then** os filtros aplicados anteriormente continuam ativos (RF46)

---

## Epic 7: Materiais

Aluno anexa arquivos (PNG/PDF/JPG) ou links a uma postagem; qualquer usuário com acesso à postagem acessa o material anexado.

**FRs covered:** RF48, RF49, RF50, RF51, RF52

### Story 7.1: Anexar arquivo a uma postagem

As a autor de uma postagem,
I want anexar um arquivo (PNG, PDF ou JPG),
So that eu compartilhe material de apoio junto ao conteúdo (RF48, RF50, RF51).

**Acceptance Criteria:**

**Given** crio ou edito uma postagem e anexo um arquivo PNG, PDF ou JPG
**When** eu envio
**Then** o sistema valida o tipo do arquivo (RF50) e associa o material à postagem (RF51)

**Given** tento anexar um arquivo de tipo não suportado
**When** eu envio
**Then** o sistema rejeita com mensagem explicando os formatos aceitos (RF50)

### Story 7.2: Anexar link a uma postagem

As a autor de uma postagem,
I want anexar um link externo,
So that eu compartilhe uma referência externa junto ao conteúdo (RF49, RF51).

**Acceptance Criteria:**

**Given** crio ou edito uma postagem e informo uma URL válida
**When** eu envio
**Then** o link é associado à postagem (RF51) e exibido com preview simples (título + domínio)

### Story 7.3: Acessar material anexado

As a usuário com acesso à postagem,
I want abrir ou baixar um material anexado,
So that eu consuma o conteúdo compartilhado (RF52).

**Acceptance Criteria:**

**Given** uma postagem tem um arquivo anexado
**When** eu clico no ícone do material
**Then** o arquivo abre ou é baixado (RF52)

**Given** uma postagem tem um link anexado
**When** eu clico no preview do link
**Then** sou levado ao destino externo

---

## Epic 8: Enquetes e Pesquisas

Aluno membro de uma comunidade cria uma enquete, vota com anonimato real garantido, acompanha resultados consolidados após votar ou após o encerramento; solicita fixação na homepage, que o moderador aprova ou recusa; qualquer usuário autenticado vota em enquete fixada da universidade.

**FRs covered:** RF53, RF53.1–RF53.6, RF54, RF54.1–RF54.3, RF55, RF55.1–RF55.3, RF56, RF56.1, RF57, RF57.1, RF58, RF58.1–RF58.5

### Story 8.1: Criar enquete em comunidade

As a aluno membro de uma comunidade,
I want criar uma enquete nessa comunidade,
So that eu colete a opinião dos membros sobre um tema (RF53.1, RF54, RF54.1, RF54.2, RF54.3).

**Acceptance Criteria:**

**Given** sou membro de ao menos uma comunidade e ainda não tenho duas enquetes ativas nela
**When** eu preencho pergunta, tipo (única ou múltipla — RF54.1), limite de seleção se múltipla (RF54.2), e entre 2 e 5 opções (RF54.3)
**Then** a enquete é criada e publicada na comunidade escolhida (RF53.1)

**Given** já tenho duas enquetes ativas na comunidade selecionada (RF53.3)
**When** eu tento selecionar essa comunidade no formulário
**Then** a comunidade aparece desabilitada na lista

**Given** informo menos de 2 ou mais de 5 opções (RF54.3)
**When** eu envio
**Then** o sistema rejeita com erro de validação anunciado, não só colorido (UX-DR25)

**Given** não sou membro de nenhuma comunidade
**When** eu acesso a página de criar enquete
**Then** vejo um botão para conhecer comunidades em vez do formulário

### Story 8.2: Solicitar e decidir fixação na homepage

As a autor de uma enquete,
I want solicitar a fixação da minha enquete na homepage,
So that mais usuários da universidade possam vê-la e votar (RF53.4, RF53.5, RF53.6, RF53.2).

**Acceptance Criteria:**

**Given** marco "fixar na homepage" ao criar a enquete
**When** eu envio
**Then** a enquete é publicada normalmente na comunidade, independentemente da decisão de fixação, e uma solicitação de fixação é enviada ao moderador (RF53.4)
**And** o moderador é notificado da nova solicitação (RF53.5)

**Given** sou moderador e recebo uma solicitação de fixação
**When** eu defiro ou indefiro
**Then** apenas moderadores podem tomar essa decisão (RF53.2)
**And** o autor é notificado do deferimento/indeferimento (RF53.6)

### Story 8.3: Votar em enquete de comunidade

As a membro de uma comunidade,
I want votar em uma enquete publicada nela,
So that eu expresse minha opinião de forma anônima (RF55, RF55.1, RF56, RF56.1).

**Acceptance Criteria:**

**Given** a enquete não está fixada na homepage e sou membro da comunidade de origem
**When** eu registro meu voto
**Then** meu voto é persistido em `enquete_voto` sem vínculo com minha identidade, e minha participação é registrada em `enquete_participacao` (RF57.1, RF55.1)

**Given** a enquete é de escolha única
**When** eu tento selecionar mais de uma opção
**Then** o sistema impede a seleção múltipla (RF56)

**Given** já votei nessa enquete
**When** eu tento votar novamente
**Then** o sistema impede o segundo voto (RF56.1)

**Given** não sou membro da comunidade de origem e a enquete não está fixada
**When** eu tento votar
**Then** o sistema impede a votação (RF55.1)

**Given** a enquete foi ocultada por um moderador (Epic 12, Story 12.5)
**When** eu tento votar nela, fixada ou não
**Then** o sistema impede o voto, sem afetar os votos já registrados antes da ocultação (RF55.3)

### Story 8.4: Votar em enquete fixada da universidade

As a qualquer usuário autenticado,
I want votar em uma enquete fixada na homepage,
So that eu participe mesmo sem ser membro da comunidade de origem (RF55.2).

**Acceptance Criteria:**

**Given** a enquete está fixada na homepage
**When** qualquer usuário autenticado registra o voto
**Then** o voto é aceito independentemente de associação a comunidade (RF55.2), seguindo as mesmas regras de RF56/RF56.1

### Story 8.5: Consultar resultados da enquete

As a usuário autenticado,
I want ver os resultados consolidados de uma enquete,
So that eu acompanhe a opinião do grupo sem comprometer o anonimato de ninguém (RF57, RF58, RF58.1).

**Acceptance Criteria:**

**Given** a enquete está aberta, tem 5 votos ou mais, e eu já votei
**When** eu acesso a enquete
**Then** vejo as barras de resultado consolidado (RF57, RF58)

**Given** a enquete está aberta, tem menos de 5 votos, e eu já votei
**When** eu acesso a enquete
**Then** vejo "X de 5 necessários" em vez do resultado (RF58.1)

**Given** ainda não votei em uma enquete aberta
**When** eu acesso a enquete
**Then** vejo a tela normal de votação, sem resultado, independentemente do total de votos

### Story 8.6: Encerrar enquete

As a criador da enquete ou administrador,
I want encerrar a enquete manualmente ou tê-la encerrada automaticamente na data definida,
So that a coleta de votos pare no momento certo (RF58.2, RF58.3, RF58.4, RF58.5).

**Acceptance Criteria:**

**Given** sou o criador da enquete ou administrador
**When** eu encerro manualmente a enquete
**Then** ela passa a estado "encerrada" (RF58.2), é desafixada da homepage se estava fixada (RF58.4), e o resultado completo fica visível a todos os usuários autenticados, inclusive quem não votou

**Given** a enquete tem data de encerramento definida e essa data já passou
**When** qualquer leitura ou tentativa de voto ocorre
**Then** o estado "encerrada" é derivado em tempo de leitura (RF58.3), sem job agendado

**Given** a enquete já está encerrada
**When** alguém tenta reabri-la
**Then** o sistema impede a reabertura (RF58.5)

---

## Epic 9: Busca

Usuário autenticado busca usuários, comunidades e postagens por texto, combinando filtros e navegando por páginas de resultado.

**FRs covered:** RF59, RF60, RF61, RF62, RF63, RF64

### Story 9.1: Buscar por texto com filtros combinados e paginação

As a usuário autenticado,
I want buscar usuários, comunidades e postagens por texto, combinando filtros e navegando por páginas de resultado,
So that eu encontre rapidamente o que procuro na plataforma (RF59, RF60, RF61, RF62, RF63, RF64).

**Acceptance Criteria:**

**Given** informo um termo de busca
**When** eu envio
**Then** vejo uma lista única mista de usuários, comunidades e postagens correspondentes, diferenciados por ícone/tipo — não em abas separadas (RF59, RF60, RF61, RF62)

**Given** combino filtros (ex.: tipo + curso)
**When** eu envio a busca
**Then** apenas resultados que atendem a todos os filtros combinados aparecem (RF63)

**Given** a busca retorna mais resultados do que cabem em uma página
**When** eu navego
**Then** os resultados são paginados usando o envelope compartilhado `PageResponse` (RF64, AD-4)

**Given** a busca não encontra nenhum resultado
**When** a tela carrega
**Then** vejo a mensagem 'Nada encontrado para "{termo}". Tenta outro termo ou dá uma olhada nas comunidades em alta.'

---

## Epic 10: Notificações

Aluno recebe notificação de resposta, menção e convite/enquete; marca notificações como lidas; lista todas as suas notificações.

**FRs covered:** RF65, RF66, RF67, RF68, RF69

### Story 10.1: Gerar notificações de resposta, menção e convite/enquete

As a usuário autenticado,
I want receber uma notificação quando alguém responde à minha postagem, me menciona, ou há um evento relevante de convite/enquete,
So that eu saiba de interações relevantes sem precisar checar manualmente cada comunidade (RF65, RF66, RF67).

**Acceptance Criteria:**

**Given** alguém comenta na minha postagem
**When** o comentário é publicado
**Then** recebo uma notificação de resposta (RF65)

**Given** alguém me menciona em um post ou comentário
**When** a menção é publicada
**Then** recebo uma notificação de menção (RF66)

**Given** recebo um convite ou um evento relevante de enquete (ex.: deferimento de fixação — RF53.6)
**When** o evento ocorre
**Then** recebo uma notificação correspondente (RF67)

**Given** eventos parecidos ocorrem no mesmo alvo
**When** são exibidos na minha lista
**Then** eles agrupam numa linha única (ex.: "Rafael e mais 4 curtiram sua postagem") em vez de uma notificação por evento

### Story 10.2: Marcar como lida e listar notificações

As a usuário autenticado,
I want marcar notificações como lidas e listar todas as minhas notificações,
So that eu gerencie o que já vi e o que ainda preciso conferir (RF68, RF69).

**Acceptance Criteria:**

**Given** tenho notificações não lidas
**When** eu clico em uma notificação
**Then** ela é marcada como lida e eu sou levado à origem do evento (RF68)

**Given** acesso a tela de notificações
**When** ela carrega
**Then** vejo a lista completa das minhas notificações, com as não lidas destacadas (RF69)

---

## Epic 11: Mensagens

Aluno envia e recebe mensagens privadas, lista suas conversas, participa de grupos privados, com histórico persistido.

**FRs covered:** RF70, RF71, RF72, RF73, RF74

### Story 11.1: Enviar e receber mensagens privadas

As a usuário autenticado,
I want enviar e receber mensagens privadas com outro usuário,
So that eu converse diretamente sem expor a conversa às comunidades (RF70, RF71, RF74).

**Acceptance Criteria:**

**Given** informo um destinatário e o conteúdo da mensagem
**When** eu envio
**Then** a mensagem é entregue ao destinatário (RF70, RF71)
**And** o histórico da conversa é persistido (RF74)

### Story 11.2: Listar conversas

As a usuário autenticado,
I want listar minhas conversas privadas,
So that eu retome rapidamente uma conversa existente (RF72).

**Acceptance Criteria:**

**Given** tenho uma ou mais conversas privadas
**When** eu acesso a tela de Mensagens
**Then** vejo a lista de conversas, ordenada por atividade recente (RF72)

### Story 11.3: Grupos privados

As a usuário autenticado,
I want criar e participar de um grupo privado de mensagens,
So that eu converse com múltiplas pessoas ao mesmo tempo, fora das comunidades públicas (RF73).

**Acceptance Criteria:**

**Given** seleciono múltiplos destinatários para uma nova conversa
**When** eu confirmo a criação
**Then** um grupo privado é criado, e mensagens enviadas nele chegam a todos os participantes (RF73, RF74)

---

## Epic 12: Moderação

Usuário denuncia conteúdo impróprio; agente de triagem (blacklist) notifica o moderador; moderador visualiza a denúncia sem ver a identidade do denunciante, oculta (reversível) ou remove (definitivo) o conteúdo, ou restringe o usuário autor; qualquer moderador/admin restaura conteúdo ocultado; autor é notificado do motivo sem saber quem denunciou; moderador escalona a um moderador neutro sem retorno ao moderador original; toda ação preserva registro de auditoria.

**FRs covered:** RF75, RF75.1–RF75.3, RF76, RF77, RF77.1, RF78, RF78.1, RF78.2, RF79, RF79.1, RF79.2, RF80, RF80.1, RF80.2

### Story 12.1: Denunciar conteúdo

As a usuário autenticado,
I want denunciar um conteúdo impróprio,
So that a equipe de moderação possa avaliá-lo (RF75, RF76).

**Acceptance Criteria:**

**Given** encontro um conteúdo que considero impróprio
**When** eu denuncio, informando o motivo
**Then** a denúncia é registrada (RF76) associada ao conteúdo e ao motivo — sem expor minha identidade a quem for analisá-la depois (RF77.1)

### Story 12.2: Triagem automática por agente de IA

As a moderador,
I want que um agente de triagem baseado em blacklist de palavras analise automaticamente todo conteúdo denunciado antes de eu vê-lo,
So that eu seja notificado com prioridade sobre denúncias com maior indício de violação, em vez de analisar tudo do zero (RF75.1, RF75.2).

**Acceptance Criteria:**

**Given** uma denúncia é registrada
**When** o agente de triagem processa o conteúdo denunciado
**Then** o moderador é notificado da nova denúncia pendente (RF75.1, RF75.2)

`Nota de implementação: mecanismo de execução do agente (síncrono/assíncrono, fonte da blacklist) é item Deferred na Architecture Spine — precisa de desenho próprio antes desta história ser implementada.`

### Story 12.3: Triagem prévia de enquete pelo agente

As a autor de uma enquete,
I want ser avisado antes da publicação se o título ou as opções contêm termo da blacklist,
So that eu possa revisar ou confirmar a publicação com conhecimento de causa (RF75.3).

**Acceptance Criteria:**

**Given** o título ou uma opção da enquete contém termo da blacklist
**When** eu tento publicar
**Then** o agente me avisa antes da publicação, permitindo revisar ou confirmar
**And** se eu confirmar, a enquete é publicada e sinalizada ao moderador (RF75.2), sem bloqueio automático

### Story 12.4: Moderador visualiza denúncias sem identidade do denunciante

As a moderador,
I want visualizar o conteúdo denunciado e o motivo,
So that eu possa avaliar a denúncia sem viés introduzido por saber quem denunciou (RF77, RF77.1).

**Acceptance Criteria:**

**Given** há uma denúncia pendente na minha fila
**When** eu abro a tela de análise
**Then** vejo o conteúdo denunciado e o motivo, mas nenhum campo com a identidade do denunciante, em nenhum papel — moderador original ou neutro (RF77.1)

### Story 12.5: Ocultar e restaurar conteúdo

As a moderador,
I want ocultar um conteúdo denunciado, e qualquer moderador ou administrador poder restaurá-lo,
So that a análise não fique bloqueada pela ausência de uma pessoa específica (RF78, RF78.1).

**Acceptance Criteria:**

**Given** decido ocultar o conteúdo denunciado
**When** eu confirmo a ação
**Then** o conteúdo sai do feed e para de aceitar interações, mas é preservado (RF78)
**And** o autor é notificado com o motivo, sem revelar a identidade do denunciante, no mesmo evento da ocultação (RF78.2)

**Given** um conteúdo está oculto
**When** qualquer moderador ou administrador (não necessariamente quem ocultou) clica em "Restaurar"
**Then** o conteúdo volta a aparecer normalmente no feed (RF78.1)

### Story 12.6: Remover conteúdo definitivamente

As a moderador,
I want remover um conteúdo denunciado,
So that conteúdo grave seja encerrado sem possibilidade de restauração, preservando o registro de auditoria (RF79, RF79.1).

**Acceptance Criteria:**

**Given** decido remover o conteúdo denunciado
**When** eu confirmo a ação
**Then** o conteúdo desaparece da listagem, sem possibilidade de restauração (RF79), preservando enunciado/conteúdo/autor/ações de moderação no log de auditoria (RF79.1)
**And** o autor é notificado com o motivo, sem revelar a identidade do denunciante (RF79.2)

### Story 12.7: Restringir usuário

As a moderador,
I want restringir um usuário responsável por conteúdo impróprio,
So that reincidências sejam contidas (RF80).

**Acceptance Criteria:**

**Given** decido restringir o usuário autor do conteúdo denunciado
**When** eu confirmo a ação
**Then** a restrição é aplicada ao usuário e registrada no log de auditoria

### Story 12.8: Escalonar denúncia a moderador neutro

As a moderador sem certeza sobre uma denúncia que envolve alguém com quem tenho vínculo,
I want escalonar a denúncia a um moderador neutro,
So that a decisão seja tomada por alguém sem vínculo direto com os envolvidos (RF80.1, RF80.2).

**Acceptance Criteria:**

**Given** estou analisando uma denúncia e não tenho certeza da decisão
**When** eu escalono para um moderador neutro
**Then** a denúncia sai da minha fila e não sou notificado do desfecho (RF80.2)
**And** o moderador neutro recebe a mesma denúncia (mesmo conteúdo e motivo) na própria fila, sem identidade do denunciante (RF80.1, RF77.1), sem novo nível de escalonamento disponível a partir daí

---

## Epic 13: Avisos Institucionais

Administrador da plataforma publica um aviso institucional com escopo obrigatório (geral ou curso específico); aluno vê no dashboard os avisos gerais e os avisos do próprio curso, sem ver avisos de outros cursos.

**FRs covered:** RF81, RF82

### Story 13.1: Publicar aviso institucional

As a administrador da plataforma,
I want publicar um aviso institucional com escopo geral ou de um curso específico,
So that eu comunique informações relevantes (feriados, treinamentos, atividades extraclasse) recebidas da coordenação (RF81).

**Acceptance Criteria:**

**Given** tenho o conteúdo do aviso e defino o escopo (geral ou curso específico)
**When** eu publico
**Then** o aviso é persistido com escopo obrigatório (RF81)

**Given** não sou administrador (ex.: sou moderador)
**When** eu tento publicar um aviso
**Then** o sistema impede a ação — apenas administrador tem acesso à criação de avisos (RF81)

### Story 13.2: Exibir avisos institucionais no dashboard

As a aluno autenticado,
I want ver no meu dashboard os avisos institucionais relevantes a mim,
So that eu me mantenha informado sem ver avisos de outros cursos (RF82).

**Acceptance Criteria:**

**Given** existem avisos gerais publicados
**When** eu acesso o dashboard
**Then** vejo todos os avisos gerais, independentemente do meu curso (RF82)

**Given** existe um aviso escopado a um curso diferente do meu
**When** eu acesso o dashboard
**Then** não vejo esse aviso (RF82)

**Given** existe um aviso escopado ao meu curso
**When** eu acesso o dashboard
**Then** eu o vejo (RF82)

---

## Epic 14: Fundação Visual e de Experiência (Design System)

Aplica a identidade "Campus Clean" (tokens de cor/tipografia/espaçamento/forma), a casca de navegação global e os componentes/padrões de comportamento reutilizáveis, para que toda tela construída nos demais épicos herde uma experiência visual e textual consistente desde o primeiro deploy.

**UX-DRs covered:** UX-DR1, UX-DR2, UX-DR3, UX-DR4, UX-DR5, UX-DR6, UX-DR7, UX-DR8, UX-DR9, UX-DR24, UX-DR25, UX-DR26, UX-DR27, UX-DR28

**Triagem de escopo (2026-08-30).** O épico inteiro estava marcado `[Semana 1 · Must-have]`, mas parte dele constrói telas de módulos que a própria Architecture Spine colocou fora do corte. As histórias abaixo foram reclassificadas:

- **No corte da semana 1 (5):** 14.1, 14.2, 14.3, 14.5, 14.7. São as que sustentam as jornadas obrigatórias de Identidade, Comunidades e Publicações.
- **Diferidas para pós-semana-1 (4):** 14.4, 14.6, 14.8, 14.9.

**A diferir não apaga a obrigação.** Este documento (ver `## FR Coverage Map`) trata NFRs e UX-DRs como critério de aceitação de toda história, em qualquer épico. Portanto RNF02 (responsividade, 14.4), UX-DR24 (voz e tom, 14.6) e RNF06/UX-DR25 (acessibilidade, 14.9) continuam valendo como critério de aceite de cada tela construída no corte da semana 1 — o que foi diferido é a história que os trata de forma sistemática e transversal, não o piso que cada tela precisa cumprir.

**Por que a 14.5 permanece no corte:** os critérios de aceite das Stories 2.3 (auto-join) e 2.4 (ingresso em comunidade aberta), ambas must-have, exigem toast explicitamente (RF24, UX-DR12, UX-DR22). Sem o sistema de toast essas histórias não fecham.

### Story 14.1: Tokens de design e tipografia

**Prioridade:** Semana 1 · Must-have

As a integrante do time de frontend,
I want implementar os tokens de cor, tipografia, espaçamento e forma da direção "Campus Clean",
So that toda tela e componente subsequente consuma os mesmos valores, sem hardcode (UX-DR1, UX-DR2).

**Acceptance Criteria:**

**Given** o DESIGN.md define a paleta (bg, surface, border, ink/ink-soft/ink-faint, maroon, orange, orange-tint, green-ok), a escala tipográfica (greeting/question/body/meta/label-caps) e a escala de espaçamento/raio
**When** os tokens são implementados como variáveis reutilizáveis (ex.: CSS custom properties ou tema Angular)
**Then** nenhum componente subsequente usa valor de cor/fonte/espaçamento hardcoded fora dos tokens

### Story 14.2: Componentes visuais base

**Prioridade:** Semana 1 · Must-have

As a integrante do time de frontend,
I want implementar os componentes Badge de comunidade, Botão primário, Indicador de membro e Card genérico,
So that os épicos de feature (Comunidades, Publicações, Enquetes etc.) reutilizem esses componentes em vez de recriá-los (UX-DR3, UX-DR4, UX-DR5, UX-DR6).

**Acceptance Criteria:**

**Given** os tokens da Story 14.1 existem
**When** os 4 componentes visuais base são implementados
**Then** `badge-course`/`badge-open` nunca aparecem juntos no mesmo badge, `button-primary` é o único estilo de ação forte do sistema, `member-indicator` substitui o botão de ação sem fundo, e o Card genérico segue `surface`+`border`+`rounded.md`+`card-padding`

### Story 14.3: Shell de navegação global

**Prioridade:** Semana 1 · Must-have

As a usuário autenticado,
I want uma sidebar persistente com os itens de navegação relevantes ao meu papel, e um dropdown de avatar no topo,
So that eu navegue pela plataforma de forma consistente em qualquer tela (UX-DR7).

**Acceptance Criteria:**

**Given** estou autenticado como aluno
**When** qualquer tela carrega
**Then** vejo a sidebar com Início, Buscar, Mensagens, Notificações, Criar enquete, Suas comunidades e Descobrir comunidades — e não vejo Denúncias/Solicitações de fixação
**And** o avatar no topo abre um dropdown com Perfil, Configurações, Sair

**Given** estou autenticado como moderador ou administrador
**When** qualquer tela carrega
**Then** vejo adicionalmente os itens Denúncias e Solicitações de fixação na sidebar

### Story 14.4: Comportamento responsivo da navegação

**Prioridade:** Pós-semana-1

As a usuário em um navegador mobile,
I want que a sidebar e o painel de descoberta se adaptem à tela menor,
So that eu use a plataforma corretamente em qualquer dispositivo (RNF02, UX-DR8).

**Acceptance Criteria:**

**Given** acesso a plataforma em um viewport abaixo do breakpoint de desktop
**When** a tela carrega
**Then** a sidebar e o painel de descoberta colapsam para um layout mobile-friendly, preservando o acesso a todos os itens de navegação

### Story 14.5: Sistema de toast e motion mínimo

**Prioridade:** Semana 1 · Must-have

As a usuário,
I want ver confirmações leves como toasts não-bloqueantes, com transições rápidas e sutis,
So that eu receba feedback sem interrupção do meu fluxo (UX-DR9, UX-DR26).

**Acceptance Criteria:**

**Given** uma ação gera uma confirmação leve (ex.: entrar em comunidade, auto-join, voto registrado)
**When** a ação é concluída
**Then** um toast aparece, não bloqueia a tela, e some sozinho após alguns segundos

**Given** um toast, dropdown ou troca de aba ocorre
**When** a transição é renderizada
**Then** usa fade/slide de ~150–200ms, sem animação decorativa

### Story 14.6: Sistema de voz e tom (microcopy)

**Prioridade:** Pós-semana-1

As a integrante do time de produto/frontend,
I want aplicar consistentemente o tom de voz definido (direto, segunda pessoa, erros que explicam sem culpar) em toda mensagem de sistema,
So that a experiência textual da plataforma seja coerente (UX-DR24).

**Acceptance Criteria:**

**Given** uma mensagem de erro, confirmação ou estado vazio precisa ser exibida em qualquer tela
**When** o texto é escrito
**Then** segue a tabela Do/Don't do EXPERIENCE.md (ex.: "Use seu e-mail institucional para se cadastrar." em vez de "E-mail inválido.")

### Story 14.7: Restilização das telas do corte must-have

**Prioridade:** Semana 1 · Must-have

As a integrante do time de frontend,
I want restilizar na direção Campus Clean as telas que as jornadas obrigatórias da semana 1 atravessam — Login, Cadastro, Verifique seu e-mail, Criação de comunidade aberta, Artigo (postagem, sem a camada de comentários) e Painel admin restrito à pré-criação de comunidades de curso,
So that a plataforma abandone a densidade "portal de notícias" do protótipo original sem perder a identidade institucional (UX-DR27).

**Acceptance Criteria:**

**Given** uma tela do corte must-have existe (no Figma original ou já construída em código)
**When** ela é restilizada
**Then** usa os tokens da Story 14.1 e os componentes da Story 14.2, nunca usa `maroon` como fundo dominante de tela, e reconcilia qualquer divergência registrada em `reconcile-figma-prototype.md`

**Given** as telas Login, Cadastro e Verifique seu e-mail já existem em código, construídas antes do Epic 14 com SCSS por componente e sem nenhum token
**When** elas são restilizadas
**Then** o SCSS local é substituído por consumo dos tokens, sem valor de cor, fonte ou espaçamento hardcoded

**Given** a tela Painel admin
**When** ela é restilizada
**Then** cobre apenas a pré-criação de comunidades de curso (Story 2.1) — publicação de avisos institucionais é Epic 13, fora do corte, e `tela-adm-relatorios` está fora do escopo desta UX por decisão registrada em `reconcile-figma-prototype.md`

**Fora desta história, diferidas para pós-semana-1:** Perfil (Epic 4, stretch), camada de comentários do Artigo (Epic 5, stretch) e a parte de avisos institucionais do Painel admin (Epic 13).

### Story 14.8: Construção das telas novas sem mockup visual

**Prioridade:** Pós-semana-1

As a integrante do time de frontend,
I want construir as telas "Busca", "Mensagens" e "Solicitações de fixação" seguindo apenas a spine (sem mockup pronto),
So that essas superfícies existam com a mesma consistência visual das demais (UX-DR28).

**Acceptance Criteria:**

**Given** uma dessas 3 telas ainda não tem mockup visual
**When** ela é construída
**Then** segue os tokens (Story 14.1) e os componentes base (Story 14.2)
**And**, no caso de Solicitações de fixação, segue o mesmo padrão de "Item de fila de moderação" usado em Denúncias

**Diferida porque as três telas pertencem a módulos fora do corte da semana 1:** Busca é Epic 9, Mensagens é Epic 11, Solicitações de fixação é Epic 8/12. Construí-las antes dos módulos que as alimentam produziria casca sem função.

**A tela "Verifique seu e-mail" saiu desta história** e passou para a 14.7: ela é must-have (Story 1.3, entregue) e já existe em código, portanto é restilização, não construção nova.

### Story 14.9: Piso de acessibilidade (WCAG 2.2 AA)

**Prioridade:** Pós-semana-1

As a usuário que depende de teclado ou leitor de tela,
I want que todo elemento interativo tenha foco visível, que estados não dependam só de cor, e que confirmações críticas fiquem persistentes na tela,
So that eu use a plataforma com a mesma eficácia de quem usa mouse e enxerga cores (RNF06, UX-DR25).

**Acceptance Criteria:**

**Given** navego por qualquer tela usando apenas o teclado
**When** eu percorro os elementos interativos (botão, link, item de fila de moderação)
**Then** cada um exibe foco de teclado visível

**Given** um badge de comunidade ou indicador de estado (membro, oculto, removido) é exibido
**When** a tela renderiza
**Then** o significado é comunicado por texto/label, não só pela cor do elemento

**Given** uma ação gera uma confirmação crítica (ex.: voto registrado)
**When** o toast correspondente desaparece
**Then** a confirmação continua refletida em um estado persistente na tela (ex.: barra da enquete atualizada) — o toast nunca é o único canal

**Given** preencho o formulário de criação de enquete (Epic 8, Story 8.1)
**When** um campo obrigatório falha na validação (menos de 2 ou mais de 5 opções)
**Then** cada campo tem label associado e o erro é anunciado para tecnologia assistiva, não apenas colorido em vermelho
