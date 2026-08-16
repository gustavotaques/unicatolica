---
title: UniCatólica — Rede Social Acadêmica (PACEXT) — PRD
status: final
created: 2026-08-12
updated: 2026-08-12
---

# PRD: UniCatólica — Rede Social Acadêmica (PACEXT)

## 0. Propósito do documento

Este PRD serve à equipe do projeto (Gustavo Vinicius Taques, João Pedro Angélico, Luis Fernando Pereira, Vynicyus Cândido), ao orientador (Prof. Edson Vaz Lopes) e a quem for dar continuidade técnica (arquitetura, UX, épicos/histórias). Apoia-se em `docs/unicatolica-pacext-contexto.md` — o relatório final do PAC Extensionista, com os 80 requisitos funcionais, 9 não funcionais, arquitetura C4, riscos e decisões validadas com o orientador — sem duplicar esse conteúdo: reorganiza-o em torno de visão, jornadas de usuário e métricas de sucesso, e formaliza decisões de produto tomadas após a validação acadêmica (registradas com data em `.memlog.md` e sinalizadas inline). Vocabulário-âncora no Glossário (§2). Requisitos funcionais mantêm a numeração RFxx original, em vez de um esquema FR-1...FR-N genérico, para preservar a rastreabilidade com o documento de contexto e o mapeamento de riscos; decisões novas desta conversa estendem essa numeração (ex.: RF21.1, RF81). Suposições marcadas com `[ASSUMPTION]` e indexadas em §10. Stack tecnológica e arquitetura C4 estão em `docs/unicatolica-pacext-contexto.md` §5–§6 — este PRD trata de capacidades, não de implementação.

## 1. Visão

A UniCatólica é uma rede social acadêmica que conecta os mais de 3.000 alunos do Campus Joinville da CatólicaSC em comunidades organizadas por curso e por interesse. Ela resolve o isolamento social e informacional do calouro — que hoje depende de grupos de WhatsApp fragmentados e da sorte de conhecer veteranos. Desde o primeiro login, cada aluno recebe uma comunidade de curso pronta (com colegas e conteúdo relevante) e o caminho para descobrir outras comunidades por afinidade, curso ou tema. Reúne em um único ambiente o que hoje está espalhado: dicas de disciplinas e professores vindas de veteranos, materiais de estudo, enquetes que substituem a pesquisa de campo via WhatsApp, e a rede de contatos entre cursos que amplia a percepção do aluno sobre a própria universidade.

## 2. Glossário

*Termos usados de forma verbatim no resto do documento — sem sinônimos.*

- **Comunidade de curso** — comunidade pré-criada pela administração da plataforma, associada a um curso da instituição (ex.: Engenharia de Software). Associação é automática (ver **auto-join**), não há botão "participar". Feed visível a qualquer usuário autenticado; interação (postar, comentar, votar) restrita a membros.
- **Comunidade aberta** — comunidade criada por qualquer aluno (ex.: Atlética de Engenharia), em torno de interesse ou tema livre. Ingresso voluntário via botão "participar" (RF24), aberto a qualquer usuário autenticado.
- **Auto-join** — associação automática de um aluno à comunidade de curso correspondente, disparada ao definir ou editar o curso no perfil acadêmico (RF14/RF16).
- **Enquete da universidade** — enquete fixada na homepage (aprovada por moderador), aceita voto de qualquer usuário autenticado, independentemente de comunidade.
- **Enquete de comunidade** — enquete publicada em uma comunidade específica, aceita voto apenas dos membros dessa comunidade (RF55.1).
- **Moderador** — usuário com permissões elevadas responsável por analisar denúncias e aplicar ações de moderação; inicialmente representantes de turma.
- **Moderador neutro** — moderador sem vínculo direto com os envolvidos em uma denúncia, para quem ela pode ser escalonada (RF80.1).
- **Ocultar** — ação de moderação reversível (RF78/RF78.1): conteúdo sai do feed e para de aceitar interações, mas é preservado e pode ser restaurado por qualquer moderador ou administrador.
- **Remover** — ação de moderação definitiva (RF79): encerra o conteúdo sem possibilidade de restauração; preserva registro de auditoria (RF79.1).
- **Administrador da plataforma** — perfil administrativo responsável por pré-criar comunidades de curso e publicar avisos institucionais, distinto do papel de moderador.
- **Aviso institucional** — comunicado publicado pelo administrador da plataforma (conteúdo recebido da coordenação por canal externo ao sistema), escopado como geral ou por curso, exibido no dashboard principal.

## 3. Público-alvo

### 3.1 Jobs To Be Done

- Adaptar-se socialmente e academicamente ao curso (motivação central do projeto — networking, dicas de veteranos sobre disciplinas/professores).
- Visualizar dicas de estudo compartilhadas por colegas e veteranos.
- Encontrar projetos interessantes para participar, criados por outros alunos (inclusive de outros cursos).
- Acompanhar publicações de amigos nas comunidades das quais participa.
- Visualizar avisos institucionais — gerais ou do próprio curso — como feriados, treinamentos e atividades extraclasse.
- Responder e acompanhar enquetes fixadas na comunidade ou na universidade, evitando a perda de informação típica de grupos de WhatsApp.

### 3.2 Não-usuários (v1)

- **Alunos e docentes de outros cursos que não Engenharia de Software** — fora do MVP; expansão prevista para os demais cursos do Campus Joinville logo em seguida ao lançamento.
- **Campus Jaraguá do Sul** — fase futura, não considerada nesta versão.
- **Público externo à universidade** — a plataforma exige e-mail acadêmico institucional; não há acesso público ou de visitantes.

### 3.3 Key User Journeys

- **UJ-1. Julia descobre que já pertence à comunidade do seu curso antes mesmo de procurar.**
  - **Persona + contexto:** Julia, caloura de Engenharia de Software, quer se integrar à comunidade acadêmica da instituição.
  - **Entrada:** não autenticada, primeiro acesso ao site (tela de login / Portal do Aluno).
  - **Caminho:**
    1. Cadastra-se com e-mail acadêmico institucional; confirma o e-mail antes do primeiro login (RF01.2; suposição sobre o mecanismo exato em §10).
    2. Faz login com usuário e senha, chega à página inicial.
    3. Explora rapidamente para se situar: vê enquetes fixadas na home e o feed geral da universidade.
    4. Ao ter definido o curso no cadastro do perfil, já está automaticamente associada à comunidade de Engenharia de Software (auto-join por curso — não precisa criar nem pedir para entrar).
    5. Dentro da comunidade, lê posts e vê membros; clica no perfil de um veterano que comentou em um post (reconhece por foto/nome).
    6. Por curiosidade, busca e entra na comunidade de Direito — vê o feed, mas não pode postar, comentar ou votar (não é membro do curso).
    7. Busca e encontra a comunidade da Atlética de Engenharia (criada por alunos, aberta) — usa o botão "participar" e entra.
  - **Clímax:** Julia percebe que já está inserida numa comunidade relevante sem nenhum esforço de configuração, e descobre que pode expandir sua rede entrando em comunidades abertas por interesse — a promessa central da visão se cumpre na primeira sessão.
  - **Resolução:** termina a sessão membro de duas comunidades (curso, via auto-join; Atlética, via join voluntário), tendo apenas visualizado o feed de uma comunidade de outro curso. Perfil ainda incompleto (interesses em aberto) — notificação de onboarding progressivo (RF20.1) chega depois.
  - **Edge case:** e-mail já cadastrado (RF02) ou fora do domínio institucional é rejeitado no cadastro; confirmação de e-mail pendente bloqueia o primeiro login.

- **UJ-2. Rafael, sem certeza sobre uma denúncia, delega a decisão a um colega neutro.**
  - **Persona + contexto:** Rafael, representante de turma de Engenharia de Software, é moderador há dois meses — um compromisso diário que assumiu voluntariamente para manter a comunidade saudável.
  - **Entrada:** autenticado como moderador, acessa a plataforma na rotina diária.
  - **Caminho:**
    1. Vê notificação de nova denúncia na navbar superior.
    2. Abre a denúncia: vê o conteúdo denunciado e o motivo — não vê quem denunciou.
    3. Sem certeza sobre a gravidade (situação envolve colega de turma), escalona para um moderador neutro (RF80.1) em vez de decidir.
    4. A denúncia sai da fila dele; ele não é notificado do desfecho — delegou a decisão.
    5. O moderador neutro recebe a denúncia em sua própria fila, com o mesmo conteúdo e motivo, e decide (ocultar, remover ou restringir o usuário).
    6. O autor do conteúdo é notificado do motivo da ação tomada, sem saber quem denunciou.
  - **Clímax:** a denúncia é tratada por alguém sem vínculo direto com os envolvidos, preservando a neutralidade do processo — e o autor do conteúdo entende por que algo aconteceu, mesmo sem saber quem o denunciou.
  - **Resolução:** conteúdo fica oculto/removido ou permanece, conforme decisão do moderador neutro; moderador original segue sua rotina sem a carga do julgamento que preferiu não fazer.
  - **Edge case:** se o próprio moderador neutro também tiver vínculo com os envolvidos (comunidade pequena), não há hoje um segundo nível de escalonamento definido — ver Open Questions.

*Sem UJ para o administrador da plataforma: escolha consciente para o MVP — papel de back-office de baixa complexidade (pré-criar comunidades de curso, publicar avisos institucionais recebidos da coordenação), sem os mesmos pontos de decisão de UX que justificaram UJ-1 e UJ-2. Revisitar se o volume de avisos/cursos crescer o suficiente para tornar esse fluxo não trivial.*

## 4. Features

*Os 12 módulos a seguir já foram validados com o orientador (seção 9 do documento de contexto) e têm seus requisitos completos em `docs/unicatolica-pacext-contexto.md` — referenciados aqui por ID (RFxx), não duplicados. As decisões novas desta conversa (2026-08-12) recebem descrição e critério de aceitação completos, com IDs que estendem a numeração existente.*

### 4.1 Identidade e Acesso
**Descrição:** Cadastro, autenticação, sessão (JWT) e controle de acesso por perfil. Requisitos completos: RF01–RF13, RF01.1 (`docs/unicatolica-pacext-contexto.md` §3.1).

**Novo — Confirmação de e-mail institucional** (realiza UJ-1)

#### RF01.2: Confirmação de e-mail antes do primeiro login
O sistema exige que o usuário confirme o e-mail cadastrado antes de permitir o primeiro login.
**Consequências (testáveis):**
- Cadastro concluído com sucesso (RF01) não gera sessão ativa nem permite login até a confirmação.
- Tentativa de login com e-mail não confirmado é rejeitada com mensagem específica (distinta de credencial inválida).
`[ASSUMPTION: mecanismo de confirmação — link por e-mail com token — fica para arquitetura]`

#### RF01.3: Restrição a domínio de e-mail institucional
Cadastro (RF01) só é aceito para e-mails do domínio institucional da Católica (o mesmo e-mail acadêmico que todo aluno já possui).
**Consequências (testáveis):**
- Cadastro com e-mail de domínio externo é rejeitado com mensagem explicando o motivo.

### 4.2 Perfil Acadêmico
**Descrição:** Criação e edição de nome, curso, período e interesses; interesses opcionais no cadastro inicial com notificação-gatilho posterior (RF20.1) — onboarding progressivo validado com o orientador. Requisitos completos: RF14–RF20, RF20.1 (§3.2).

**Novo — Visualização de perfil de terceiros** (realiza UJ-1)

#### RF20.2: Visualizar perfil público de outro usuário
Qualquer usuário autenticado pode visualizar o perfil acadêmico público de outro usuário, acessado a partir do nome/foto em uma postagem ou comentário.
**Consequências (testáveis):**
- Perfil de terceiros é somente leitura (sem opções de edição).
- Exibe os mesmos campos do próprio perfil (nome, curso, período, interesses), sem dados sensíveis adicionais.

### 4.3 Comunidades
**Descrição:** Criação, ingresso, saída, listagem, filtro e administração de comunidades. Requisitos completos: RF21–RF31 (§3.3).

`[NOTA: diverge da decisão validada com o orientador em docs/unicatolica-pacext-contexto.md §9.2 ("Coordenador ou professor pode gerenciar a comunidade e adicionar colaboradores"). Decisão desta conversa (2026-08-12): o docente é tratado como usuário acadêmico comum, sem privilégios de gestão de comunidade distintos de um aluno — mesma categoria das demais decisões pós-validação desta seção 4, ver .memlog.md.]`

**Novo — Dois tipos de comunidade, comunidades de curso pré-criadas e auto-join** (realiza UJ-1; decisão confirmada em conversa, ver `.memlog.md`)

#### RF21.1: Dois tipos de comunidade
O sistema distingue **comunidades de curso** (associação automática, criação restrita a administrador da plataforma) de **comunidades abertas** (criação livre por qualquer aluno, ingresso voluntário via RF24).
**Consequências (testáveis):**
- Toda comunidade tem um tipo definido no momento da criação, imutável depois.
- Comunidades de curso não exibem botão "participar"; comunidades abertas exibem.

#### RF21.2: Pré-criação de comunidades de curso
Administrador da plataforma pré-cria a comunidade de cada curso da instituição antes da entrada dos primeiros alunos, associando-a ao curso correspondente.
**Consequências (testáveis):**
- Não é possível a um aluno criar uma comunidade do tipo "comunidade de curso" (RF21 permanece válido apenas para comunidades abertas).

#### RF24.1: Auto-join por curso
Ao definir ou editar o curso no perfil acadêmico (RF14/RF16), o sistema associa automaticamente o aluno à comunidade de curso correspondente, sem exigir ação de ingresso (RF24) nem confirmação.
**Consequências (testáveis):**
- Alterar o curso no perfil remove a associação da comunidade do curso anterior e adiciona a do novo curso.
- Associação por auto-join não passa pela validação de "ingresso duplicado" (RF25) — é idempotente por definição.

#### RF27.1: Feed visível, interação restrita a membros (comunidades de curso)
Qualquer usuário autenticado pode visualizar o feed de uma comunidade de curso da qual não é membro, mas não pode postar, comentar ou votar em enquetes dela — apenas membros podem.
**Consequências (testáveis):**
- Tentativa de postar/comentar/votar em comunidade de curso da qual o usuário não é membro retorna erro de permissão.
- Generaliza RF55.1 (já existente para enquetes) para postagens e comentários.
- Não se aplica a comunidades abertas, onde a interação já depende de ingresso voluntário (RF24).

### 4.4 Publicações
**Descrição:** Criação, validação e listagem de postagens associadas a comunidades. Requisitos completos: RF32–RF36 (§3.4). Sujeito a RF27.1 (interação restrita a membros em comunidade de curso).

### 4.5 Discussões
**Descrição:** Comentários, respostas encadeadas (hierarquia similar ao modelo do YouTube) e edição/exclusão restrita ao próprio conteúdo. Requisitos completos: RF37–RF42 (§3.5).

### 4.6 Filtro de Conteúdo
**Descrição:** Filtragem de postagens por curso, disciplina e tipo de conteúdo. Requisitos completos: RF43–RF47 (§3.6).

### 4.7 Materiais
**Descrição:** Anexação de arquivos (PNG/PDF/JPG) e links a postagens. Requisitos completos: RF48–RF52 (§3.7).

### 4.8 Enquetes e Pesquisas
**Descrição:** Criação, resposta, consolidação e encerramento de enquetes, com anonimato real de voto (separação entre `enquete_participacao` e `enquete_voto`, sem vínculo persistido entre usuário e opção votada), mínimo de 5 votos para exibir resultado, e distinção entre enquete de comunidade e enquete da universidade (fixada na homepage, sujeita a aprovação de moderador). Especificação completa — incluindo regras de votação, encerramento, modelo de anonimato, auditoria e fluxo de criação — em `docs/unicatolica-pacext-contexto.md` §3.8. Requisitos: RF53–RF58 e subitens.

> Aviso de manutenção herdado da especificação-fonte: nenhuma coluna deve ser adicionada a `enquete_voto` (usuário, curso, período, timestamp) sob risco de anular o anonimato garantido por RF57.1 — ver §3.8 do documento de contexto para a justificativa completa.

### 4.9 Busca
**Descrição:** Busca textual de usuários, comunidades e postagens, com filtros combinados e resultados paginados. Requisitos completos: RF59–RF64 (§3.9).

### 4.10 Notificações
**Descrição:** Notificação de resposta, menção, convite/enquete; marcação como lida; listagem. Requisitos completos: RF65–RF69 (§3.10).

### 4.11 Mensagens
**Descrição:** Mensagens privadas, conversas, grupos privados, histórico persistido. Requisitos completos: RF70–RF74 (§3.11).

### 4.12 Moderação
**Descrição:** Denúncia, triagem por agente de IA, ocultação/remoção de conteúdo, restrição de usuário e escalonamento a moderador neutro. Requisitos completos: RF75–RF80 e subitens (§3.12).

**Novo — Anonimato do denunciante e transparência para o autor** (realiza UJ-2; decisões confirmadas em conversa)

#### RF77.1: Anonimato do denunciante para o moderador
O moderador que analisa uma denúncia vê o conteúdo denunciado e o motivo, mas nunca a identidade de quem denunciou.
**Consequências (testáveis):**
- Tela de análise de denúncia (RF77) não expõe nenhum campo com identidade do denunciante, em nenhum papel (moderador original ou neutro).

#### RF78.2: Notificação ao autor quando o conteúdo é ocultado
Ao ocultar um conteúdo (RF78), o sistema notifica o autor com o motivo da ação, sem revelar a identidade do denunciante.
**Consequências (testáveis):**
- Notificação é gerada no mesmo evento que a ocultação, sem atraso.
- Motivo exibido é o mesmo registrado pelo moderador na decisão.

#### RF79.2: Notificação ao autor quando o conteúdo é removido
Ao remover um conteúdo (RF79), o sistema notifica o autor com o motivo da ação, sem revelar a identidade do denunciante.
**Consequências (testáveis):**
- Mesma garantia de RF78.2, aplicada ao evento de remoção.

#### RF80.2: Sem retorno ao moderador original após escalonamento
Ao escalonar uma denúncia (RF80.1), o sistema não notifica o moderador original sobre o desfecho da denúncia escalonada.
**Consequências (testáveis):**
- Denúncia escalonada sai da fila de pendências do moderador original e não gera nenhuma notificação de fechamento a ele.
- Moderador neutro recebe a denúncia em sua própria fila, com o mesmo conteúdo e motivo já registrados — sem novo nível de escalonamento disponível a partir daí.

### 4.13 Avisos Institucionais *(módulo novo — não coberto por RF01–RF80)*
**Descrição:** Comunicados institucionais (feriados, treinamentos, atividades extraclasse) publicados pelo administrador da plataforma e exibidos no dashboard principal (já previsto na seção 7 do documento de contexto, mas sem RF formal). A coordenação do curso envia o conteúdo ao administrador por canal externo ao sistema — fora do escopo do software.

#### RF81: Publicação de avisos institucionais
Administrador da plataforma pode publicar um aviso institucional, escopado como geral (toda a universidade) ou específico de um curso.
**Consequências (testáveis):**
- Aviso tem escopo obrigatório definido na criação (geral ou curso específico).
- Apenas o papel de administrador tem acesso à criação de avisos (não moderadores).

#### RF82: Exibição de avisos institucionais no dashboard
O sistema exibe avisos institucionais no dashboard principal do usuário, filtrados por escopo (avisos gerais sempre aparecem; avisos de curso aparecem apenas a alunos daquele curso).
**Consequências (testáveis):**
- Aluno de Engenharia de Software não vê aviso escopado a outro curso.
- Avisos gerais aparecem para todos os usuários autenticados, independentemente do curso.

## 5. Non-Goals (Explícito)

- **Sem monetização no v1** — sem anúncios de terceiros, sem plano pago.
- **Sem integração com o sistema acadêmico oficial da Católica** (notas, matrícula, calendário oficial) — desacoplado por complexidade; avisos institucionais são publicados manualmente pelo administrador da plataforma (RF81/RF82), não sincronizados automaticamente.
- **Resultados de enquetes não são segmentáveis por atributo demográfico** (curso, período, membro/não-membro) — consequência direta do modelo de anonimato real (§3.8 do documento de contexto), não uma limitação temporária.
- **Moderação nunca é 100% automática** — o agente de IA faz triagem e aviso (RF75.1–RF75.3), mas nunca bloqueia ou remove conteúdo sozinho; a decisão final é sempre humana.
- **Contas não são anônimas nem pseudônimas** — identidade real vinculada ao e-mail acadêmico institucional (RF01.3); distinto do anonimato de *voto* em enquete (RF57.1), que é sobre a enquete, não sobre a conta.
- **Não é uma rede social genérica** — escopo estritamente acadêmico; não inclui stories, chat livre fora de comunidades/mensagens diretas, ou qualquer recurso voltado à vida social não relacionada à faculdade.

## 6. Escopo do MVP

Escopo deliberadamente contido para mitigar R02 (escopo superdimensionado) — ver mapeamento de riscos completo em `docs/unicatolica-pacext-contexto.md` §8.

### 6.1 Em escopo
- Os 12 módulos validados (RF01–RF80 e subitens): identidade e acesso, perfil acadêmico, comunidades, publicações, discussões, filtro de conteúdo, materiais, enquetes, busca, notificações, mensagens, moderação.
- Todas as decisões novas desta conversa (§4.1–§4.13): confirmação de e-mail institucional, dois tipos de comunidade com auto-join, visualização de perfil de terceiros, anonimato do denunciante, notificação ao autor em moderação, avisos institucionais.
- Público: alunos e docentes de **Engenharia de Software**, Campus Joinville.

### 6.2 Fora de escopo para o MVP
- **Demais cursos do Campus Joinville** — expansão prevista logo após o lançamento inicial em Engenharia de Software. `[NOTE FOR PM: pré-criação de comunidades de curso (RF21.2) deve ser repetível para novos cursos sem retrabalho — vale revisitar no design técnico.]`
- **Campus Jaraguá do Sul** — fase futura, sem data definida.
- **Integração com sistema acadêmico oficial e segmentação de resultados de enquete** — permanentes por design, não limitações temporárias; ver Non-Goals (§5).

## 7. Métricas de sucesso

**Primárias** — ligadas ao maior risco do projeto (R01, não adoção):
- **SM-1**: Taxa de ativação — % de alunos de Engenharia de Software cadastrados que completam o cadastro *e* realizam ao menos uma ação de valor na primeira sessão (entram na comunidade do curso, visualizam um post, votam em uma enquete). `[ASSUMPTION: meta numérica não definida — depende da base de alunos de Engenharia de Software do campus, a confirmar com coordenação/orientador]`.
- **SM-2**: Retenção D7 / D30 — % de usuários que retornam 7 e 30 dias após o cadastro.

**Secundárias**:
- **SM-3**: DAU/MAU (stickiness) — com expectativa calibrada para cadência semanal (uso acadêmico), não diária.
- **SM-4**: Taxa de participação em enquetes — % de membros de uma comunidade que votam nas enquetes fixadas nela. Valida RF de enquetes (RF53–RF58).
- **SM-5**: Comunidades ativas — nº de comunidades com ao menos N posts/semana `[ASSUMPTION: N a definir]`.
- **SM-6**: Materiais compartilhados por usuário ativo. Valida RF de materiais (RF48–RF52).

**Contra-métricas (não otimizar)**:
- **SM-C1**: Denúncias por usuário ativo — crescimento desproporcional sinaliza problema de convivência, não sucesso. Contrabalança SM-1/SM-3.
- **SM-C2**: Tempo de sessão sem ação concluída (nenhum post/comentário/voto) — sessão longa sem ação pode indicar UX ruim, não engajamento saudável. Contrabalança SM-3.
- **SM-C3**: Taxa de perfil incompleto após 30 dias (interesses nunca preenchidos) — sinal de onboarding progressivo (RF20.1) que não converteu. Contrabalança SM-2.

## 8. NFRs Cross-Cutting e Conformidade Regulatória

*Requisitos não funcionais completos (RNF01–RNF09) em `docs/unicatolica-pacext-contexto.md` §4 — não duplicados aqui. Resumo por categoria:*

- **Usabilidade e responsividade** (RNF01, RNF02) — realiza UJ-1: Julia completa login, ingresso em comunidade e primeira postagem sem treinamento prévio, em desktop e mobile browser.
- **Desempenho** (RNF03) — operações críticas (navegação, autenticação, feed, abertura de comunidade) com p95 ≤ 2s.
- **Segurança de aplicação** (RNF04) — baseline OWASP ASVS 4.0.3. Relevante em especial para RF01.2/RF01.3 (confirmação de e-mail, restrição de domínio) e para o filtro de segurança JWT (C4 nível 3).
- **Privacidade** (RNF05) — LGPD. Já atendido por design no modelo de anonimato de enquetes (§3.8 do contexto) e reforçado pelas decisões novas de anonimato do denunciante (RF77.1).
- **Acessibilidade** (RNF06) — WCAG 2.2 nível AA.
- **Auditabilidade** (RNF07) — login, denúncia, remoção de conteúdo, alteração administrativa. Estende-se às novas notificações de moderação (RF78.2/RF79.2) e à publicação de avisos institucionais (RF81).
- **Interoperabilidade de API** (RNF08) — OpenAPI 3.1.0. Relevante para a possibilidade de o projeto se tornar um produto real com integrações futuras.
- **Rastreabilidade de requisitos** (RNF09) — ISO/IEC/IEEE 29148:2018. Critério de aceitação granular por requisito (id, origem, prioridade, justificativa, vínculo com teste) é formalizado na fase de épicos/histórias (`bmad-create-epics-and-stories`), não neste PRD — ver §4 (Features) para a decisão sobre essa divisão.

## 9. Arquitetura de Informação (referência)

10 telas prototipadas no Figma (login, dashboard principal, dashboard de comunidades, perfil, busca, mensagens, notificações, criar enquete, solicitação de fixação, painel administrativo com URL própria) — diagrama de navegação e detalhes completos em `docs/unicatolica-pacext-contexto.md` §7. `[NOTE FOR PM: quando disponível, importar o System Design de alta fidelidade do Figma via MCP para validar esta seção contra o protótipo mais recente — ver `.memlog.md`.]`

## 10. Open Questions e Suposições

*Itens ainda não resolvidos, incluindo as suposições `[ASSUMPTION]` marcadas inline no documento.*

1. Se o moderador neutro designado também tiver vínculo com os envolvidos em uma denúncia (comunidade pequena), não há hoje um segundo nível de escalonamento definido (ver UJ-2, edge case). Precisa de resolução antes da fase de arquitetura/épicos se o time de moderadores for pequeno o suficiente para isso ser comum.
2. Meta numérica de SM-1 (taxa de ativação) e SM-2 (retenção D7/D30) dependem da base real de alunos de Engenharia de Software no Campus Joinville — não disponível nesta conversa.
3. N mínimo de posts/semana para uma comunidade contar como "ativa" (SM-5) não foi definido.
4. Mecanismo técnico exato de confirmação de e-mail (RF01.2, também citado em §3.3 UJ-1) — link com token, código, etc. — fica para a fase de arquitetura.
5. Cinco pontos do módulo de enquetes seguem indefinidos conforme nota final da seção 3.8 do documento de contexto (não reproduzidos aqui — conferir na fonte).
