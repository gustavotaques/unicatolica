---
title: UniCatólica — Rede Social Acadêmica (PACEXT) — Experience
status: final
created: 2026-08-17
updated: 2026-08-22
sources:
  - docs/unicatolica-pacext-prd.md
  - docs/unicatolica-pacext-contexto.md
---

# UniCatólica — Experience Spine

> Web only (desktop/responsivo). Consumo acadêmico, tom leve e clean. Par com `DESIGN.md` (Campus Clean). Personas de referência: Julia (UJ-1, calouro) e Rafael (UJ-2, moderador), do PRD §3.3.

## Foundation

Superfície única: web, desktop e navegador mobile (RNF02 do PRD exige integridade funcional nas duas). Nenhum UI system nomeado foi adotado — componentes são customizados a partir de `DESIGN.md`. Modo escuro fora do MVP. Motion mínimo e funcional: transições simples (fade/slide rápido) em toasts, dropdowns e troca de aba — sem animação decorativa ou microinteração elaborada.

## Information Architecture

**Navegação global (sidebar, sempre visível para usuário autenticado):**

| Item | Visível para | Leva a |
|---|---|---|
| Início | Todos | Feed principal, avisos, enquete da universidade |
| Buscar | Todos | Busca (lista única mista) |
| Mensagens | Todos | Conversas privadas |
| Notificações | Todos | Lista de notificações agrupadas |
| Criar enquete | Todos (membro de ao menos uma comunidade) | Formulário dedicado de criação de enquete |
| Denúncias | Moderador / administrador | Fila de denúncias pendentes |
| Solicitações de fixação | Moderador / administrador | Fila separada de pedidos de fixação na home |
| Suas comunidades (lista) + Descobrir comunidades | Todos | Comunidades das quais é membro / descoberta de novas |

**Topbar:** avatar no canto superior direito → dropdown (Perfil, Configurações, Sair). Perfil e configurações não têm entrada própria na sidebar.

**Mapa de superfícies** (12 módulos do PRD + avisos institucionais → tela):

| Superfície | Módulo(s) do PRD | Status |
|---|---|---|
| Login | Identidade e Acesso (RF01–RF13) | Existe no Figma — restilizar em Campus Clean |
| Verifique seu e-mail | RF01.2 | Nova — sem mockup visual (spine only) |
| Cadastro | RF01–RF05 | Existe no Figma (`tela-cadastro-usuario`) — restilizar |
| Início (feed) | Publicações, Avisos institucionais, Enquete da universidade | Mockada — [`mockups/home-comunidade.html`](mockups/home-comunidade.html) |
| Comunidade (curso / aberta) | Comunidades (RF21–RF31), Publicações, Discussões, Filtro de Conteúdo | Mockada (tela secundária, mesmo arquivo) — feed de comunidade real ainda a construir |
| Criação de comunidade | Comunidades — só comunidade **aberta** (RF21.2 tira curso desse fluxo) | Existe no Figma (`tela-criacao-comunidade`) — restilizar e revisar copy |
| Artigo (post + comentários) | Publicações, Discussões (RF32–RF42) | Existe no Figma (`Artigo - Protótipo`) — restilizar; mockup comportamental próprio: [`mockups/key-artigo.html`](mockups/key-artigo.html) |
| Perfil (próprio) | Perfil Acadêmico (RF14–RF20) | Existe no Figma (`tela-perfil`) — restilizar |
| Perfil (de terceiros, somente leitura) | RF20.2 | Mesma tela do perfil próprio, sem controles de edição — não é superfície nova |
| Busca | Busca (RF59–RF64) | Nova — sem mockup visual (spine only; padrão: `Component Patterns.Resultado de busca`) |
| Mensagens | Mensagens (RF70–RF74) | Nova — sem mockup visual (spine only) |
| Notificações | Notificações (RF65–RF69) | Mockada — [`mockups/key-notificacoes.html`](mockups/key-notificacoes.html) |
| Criar enquete | Enquetes (RF53–RF58) | Mockada — [`mockups/key-criar-enquete.html`](mockups/key-criar-enquete.html); fluxo de criação já especificado em contexto §3.8 |
| Denúncias (fila do moderador) | Moderação (RF75–RF80) | Mockada — [`mockups/key-denuncias.html`](mockups/key-denuncias.html) |
| Solicitações de fixação (fila do moderador) | RF53.4–RF53.6 | Nova — sem mockup visual (spine only), fila própria, separada de Denúncias (mesmo padrão de `Item de fila de moderação`) |
| Painel admin (pré-criar comunidade de curso, publicar aviso) | Avisos Institucionais (RF81–RF82), RF21.2 | Existe no Figma (`tela-adm-dashboard`) — escopo reduzido, **relatórios fora de escopo**, reuso ainda não verificado visualmente (ver pendência abaixo) |

Filtro de Conteúdo e Materiais não são superfícies próprias — vivem dentro de Feed/Busca (filtro) e de Publicações (anexo), respectivamente.

**Resolvido:** `TelaOficial - Modelo` (Figma, nó 33:18) não é uma tela do produto — é a referência de que o UniCatólica é acessado a partir do menu "Já sou Aluno" do site institucional (catolicasc.org.br), ao lado de AVA, Portal do Aluno, Portal Pós-Graduação e Webmail. Não entra no mapa de superfícies acima porque o ponto de entrada é o site institucional existente, não uma tela própria do produto. Ver `reconcile-figma-prototype.md` e `imports/figma-prototype/tela-oficial-modelo.png`.

**Pendência para o time:** as telas de admin (`adm-login`, `adm-dashboard`, `adm-moderacao`) nunca foram capturadas do Figma nesta sessão — a reutilização do padrão visual do painel admin (linha "Painel admin" acima) é intenção registrada, não uma decisão visualmente verificada. Confirmar com screenshot antes de restilizar.

→ Composição de referência: [`mockups/home-comunidade.html`](mockups/home-comunidade.html) (Início + comunidade). Spine vence em caso de conflito.

## Voice and Tone

Microcopy. Voz e identidade estética vivem em `DESIGN.md.Brand & Style`. Direto, conversa de igual pra igual ("você"), sem jargão corporativo. Erros explicam o que houve e o que fazer — nunca culpam o usuário. Estados vazios convidam a agir, sem forçar humor.

| Momento | Do | Don't |
|---|---|---|
| Cadastro — e-mail já existe | "Esse e-mail já tem uma conta. Esqueceu a senha?" | "Erro: e-mail duplicado." |
| Cadastro — domínio errado | "Use seu e-mail institucional para se cadastrar." | "E-mail inválido." |
| Login — e-mail não confirmado | "Confirme seu e-mail antes de entrar. Reenviar confirmação" | Mesma mensagem genérica de senha incorreta |
| Comunidade nova, sem posts | "Ainda não rolou nada por aqui. Seja a primeira pessoa a postar." | "Nenhum conteúdo encontrado." |
| Busca sem resultado | 'Nada encontrado para "{termo}". Tenta outro termo ou dá uma olhada nas comunidades em alta.' | "0 resultados." |
| Entrar em comunidade aberta | Toast: "Você entrou em {nome da comunidade}" | "Ops! Você entrou com sucesso!" |
| Votar em enquete | Barra atualiza na hora + "Seu voto foi registrado." | Modal de confirmação separado |
| Conteúdo oculto (para o autor) | "Sua postagem foi ocultada. Motivo: {motivo}" | Notificação sem motivo, ou revelando o denunciante |
| Conteúdo removido (para o autor) | "Sua postagem foi removida. Motivo: {motivo}" | Mesma mensagem de ocultação (são ações distintas) |
| Onboarding progressivo | "Complete seu perfil e apareça mais nas buscas — adicione seus interesses." | "Seu perfil está incompleto!" |
| Auto-join (toast, 1ª vez no Início) | "Você já faz parte de {comunidade do curso} 🎓" | Nenhum aviso — usuário descobre sozinho |

## Component Patterns

Comportamental. Specs visuais vivem em `DESIGN.md.Components`.

| Componente | Uso | Regras comportamentais |
|---|---|---|
| Card de post | Feed do Início, feed de comunidade | Avatar, nome, `{components.badge-course}` ou `{components.badge-open}`, horário relativo, corpo, ações (curtir · comentar · compartilhar). Anexo de material quando houver. |
| Comentário em árvore | Artigo | Hierarquia trava em ~3 níveis de indentação; respostas além do 3º nível ficam no mesmo recuo (não empilha mais). Edição/exclusão restrita ao próprio conteúdo (RF39/RF40). |
| Badge de comunidade | Card de post, header de comunidade | "Comunidade de curso" (sem botão) ou "Comunidade aberta" (com botão Participar → vira indicador de membro). Nunca as duas juntas. |
| Botão Participar → Indicador de membro | Comunidade aberta | Clique muda estado imediatamente (sem confirmação modal) + toast "Você entrou em {comunidade}". |
| Formulário de criação de enquete | Criar enquete | Pergunta, tipo (única/múltipla — RF54.1), limite de seleção se múltipla (RF54.2), 2–5 opções (RF54.3), data de encerramento opcional, checkbox "fixar na homepage" (RF53.4), lista de comunidades do usuário com as que já atingiram limite de 2 enquetes ativas sinalizadas como desabilitadas (RF53.3). |
| Enquete (exibição) | Início, comunidade | Ver State Patterns abaixo — 4 estados distintos. |
| Notificação agrupada | Notificações | Eventos parecidos no mesmo alvo agrupam numa linha (ex.: "Rafael e mais 4 curtiram sua postagem"), não uma notificação por evento. Não lida = destacada; clique leva à origem. |
| Material anexado | Card de post, Artigo | PDF/imagem: ícone + nome do arquivo, clique abre/baixa. Link externo: preview simples (título + domínio). |
| Resultado de busca | Busca | Lista única mista (usuários, comunidades, postagens), diferenciados por ícone/tipo — não abas separadas. |
| Item de fila de moderação | Denúncias, Solicitações de fixação | Conteúdo + motivo visíveis; identidade de quem denunciou **nunca** exibida (RF77.1). Ações: Ocultar, Remover, Restringir usuário, Escalonar para moderador neutro. |

## State Patterns

| Estado | Superfície | Tratamento |
|---|---|---|
| Enquete — antes de votar | Início, comunidade | Opções como botões clicáveis, sem resultado visível. |
| Enquete — < 5 votos, já votou | Início, comunidade | "X de 5 necessários" em vez de esconder tudo (RF58.1). |
| Enquete — resultado liberado | Início, comunidade | Barras de resultado após o próprio voto (se ≥5 votos totais) ou após encerramento. |
| Enquete — encerrada | Início, comunidade | Resultado sempre visível a qualquer autenticado, sem opção de votar (RF58.5). |
| Comunidade de curso, não-membro | Comunidade | Feed visível; no lugar da caixa de postar/comentar/votar, aviso explicando que o usuário não é membro (RF27.1) — nunca omitir o controle silenciosamente. |
| Conteúdo oculto | Feed, Artigo | Esmaecido/cinza, some das interações normais, botão "Restaurar" visível a qualquer moderador/admin (RF78.1). Reversível. |
| Conteúdo removido | Feed, Artigo | Desaparece da lista; sobrevive só no log de auditoria (RF79.1). Definitivo. |
| Auto-join | Início | Toast único na primeira visita pós-associação: "Você já faz parte de {comunidade} 🎓". |
| E-mail não confirmado | Login | Bloqueia login com mensagem específica, distinta de credencial inválida (RF01.2). |

**Deferido para implementação:** estado de carregamento/erro de rede genérico (skeleton, timeout) não foi discutido em conversa; segue o tom de voz acima ("Não conseguimos carregar..." em vez de "Erro 500") quando construído — não bloqueia este spine.

## Interaction Primitives

- Clique para agir; sem gestos customizados (é web, não app nativo).
- Toast para confirmações leves (entrar em comunidade, auto-join, voto registrado) — não bloqueia a tela, some sozinho.
- Dropdown para o menu de avatar (Perfil, Configurações, Sair) — sem página própria de "menu".
- Fila de moderação usa lista + painel de detalhe (não modal) — decisão de moderação é uma ação com peso, merece tela própria, não popup.
- Motion mínimo: fade/slide rápido — ~150–200ms é o valor decidido de partida (`DESIGN.md` não fixa um token dedicado de duração; ajustável em implementação se render lento/brusco no navegador real).

## Accessibility Floor

Comportamental. Contraste visual vive em `DESIGN.md`. Piso mínimo herdado de RNF06 do PRD (WCAG 2.2 nível AA):

- Todo elemento interativo (botão, link, item de fila de moderação) tem foco de teclado visível.
- Badges de comunidade e indicadores de estado (membro, oculto, removido) não dependem só de cor — carregam texto/label, para não excluir quem não distingue `{colors.orange}` de `{colors.maroon}`.
- Toasts não são o único canal de uma confirmação crítica (ex.: voto registrado também reflete na barra da enquete, não só no toast) — quem usa leitor de tela precisa da confirmação persistente, não só transiente.
- Formulário de criação de enquete: cada campo tem label associado; erros de validação (menos de 2 opções, mais de 5) são anunciados, não só coloridos em vermelho.
- **Deferido para implementação:** comportamento detalhado de leitor de tela (anúncios ARIA live para toast, navegação por teclado na árvore de comentários) não foi especificado em conversa — fica para a fase de implementação, dentro do piso AA já travado pelo PRD.

## Key Flows

### Flow 1 — UJ-1: Julia descobre que já pertence à comunidade do curso

*Persona: Julia, caloura de Engenharia de Software, primeiro acesso.*

1. **Cadastro.** Nome, e-mail institucional, senha. Domínio errado ou e-mail já cadastrado → erro inline específico (ver Voice and Tone).
2. **Verifique seu e-mail.** Tela intermediária confirma o cadastro e pede confirmação por e-mail antes do primeiro login (RF01.2). Tentativa de login antes da confirmação → mensagem distinta de senha incorreta.
3. **Login → Início.** Primeiro acesso: topbar com avatar, aviso institucional no topo, enquete da universidade (estado "aguardando votos" se abaixo do mínimo). Sidebar ainda sem comunidades.
4. **Auto-join silencioso + toast.** Ao definir o curso no perfil, "Engenharia de Software" aparece em "Suas comunidades" sem nenhuma ação dela — toast de boas-vindas na primeira visita ao Início após isso.
5. **Explora a comunidade do curso.** Clica na comunidade na sidebar → feed, badge "Comunidade de curso", sem botão participar.
6. **Perfil de terceiros.** Vê post de Rafael (veterano), clica no nome/avatar → perfil dele, somente leitura, mesmos campos do próprio perfil, sem controles de edição.
7. **Curiosidade: entra em Direito (curso, não-membro).** Busca "Direito" → feed visível, mas no lugar da caixa de postar/comentar/votar, aviso de que ela não é membro dessa comunidade.
8. **Entra na Atlética (comunidade aberta).** Busca "Atlética" → badge "Comunidade aberta" + botão Participar → clica → confirma, vira membro, toast de entrada.

**Clímax:** Julia percebe que já está inserida numa comunidade relevante sem esforço nenhum, e descobre que pode expandir a rede entrando em comunidades abertas por interesse — a promessa central da visão se cumpre na primeira sessão (PRD §3.3).

**Resolução:** termina membro de duas comunidades; perfil com interesses em aberto — notificação de onboarding progressivo chega depois (RF20.1).

### Flow 2 — UJ-2: Rafael, sem certeza sobre uma denúncia, escalona para um moderador neutro

*Persona: Rafael, representante de turma, moderador há dois meses.*

1. **Badge na navbar.** "Denúncias" mostra contador de pendências.
2. **Abre a fila → análise.** Clica num item pendente → conteúdo denunciado + motivo, **sem** identidade do denunciante (RF77.1).
3. **Escalona.** Sem certeza (envolve colega de turma), clica "Escalonar para moderador neutro" → confirmação → item some da fila dele, contador cai.
4. **Moderador neutro decide.** Recebe o mesmo item na própria fila de Denúncias → escolhe Ocultar, Remover ou Restringir usuário.
5. **Estado visual reflete a decisão.** Ocultar = esmaecido no feed + botão Restaurar visível a qualquer moderador/admin. Remover = some da lista, só sobrevive na auditoria.
6. **Autor notificado.** Recebe o motivo, sem saber quem denunciou (RF78.2/RF79.2). Rafael (original) não recebe nada — a denúncia simplesmente não volta pra fila dele (RF80.2).

**Clímax:** a denúncia é tratada por alguém sem vínculo direto com os envolvidos, preservando a neutralidade — e o autor entende por que algo aconteceu, mesmo sem saber quem o denunciou.

**Resolução:** conteúdo fica oculto/removido ou permanece, conforme decisão do moderador neutro; Rafael segue sua rotina sem a carga do julgamento que preferiu não fazer.

**Edge case aberto (não bloqueia esta UX):** se o moderador neutro designado também tiver vínculo com os envolvidos (comunidade pequena), não há hoje um segundo nível de escalonamento — mesma Open Question do PRD §10.1.
