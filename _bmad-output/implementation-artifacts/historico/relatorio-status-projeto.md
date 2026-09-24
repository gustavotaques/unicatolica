# Relatório de Status do Projeto — UniCatólica (PAC Extensionista)

**Curso:** Engenharia de Software - 6º semestre
**Instituição:** Centro Universitário Católica de Santa Catarina (CatólicaSC), Campus Joinville
**Orientador:** Prof. Edson Vaz Lopes
**Equipe:** Gustavo Vinicius Taques, João Pedro Angélico, Luis Fernando Pereira, Vynicyus Cândido

---

## 1. Visão geral do projeto

A UniCatólica é uma rede social acadêmica voltada aos alunos do curso de Engenharia de Software do Campus Joinville da CatólicaSC. O produto resolve o isolamento hoje mitigado de forma improvisada por grupos de WhatsApp fragmentados: cada aluno é automaticamente associado à comunidade do seu curso ao se cadastrar, pode descobrir e participar de comunidades abertas por interesse, acompanhar publicações, comentários, enquetes e avisos institucionais — tudo centralizado em uma única plataforma.

O planejamento do produto está concluído e documentado: PRD, arquitetura, especificações de UX/design e o backlog de épicos e histórias já foram validados pela equipe e estão versionados no repositório. O trabalho atual da equipe é a fase de implementação, dividida em entregas incrementais.

---

## 2. Status atual do projeto

### 2.1 O que já está concluído

**Planejamento (100% concluído):**
- PRD com visão de produto, personas, jornadas de usuário e métricas de sucesso.
- Arquitetura de software (architecture spine): stack tecnológica, decisões estruturais, convenções de dados e diagramas.
- Especificação de UX/UI: sistema de design, fluxos de navegação e padrões de interação.
- Backlog completo: 14 épicos e mais de 60 histórias de usuário, cobrindo os 12 módulos funcionais do produto (identidade e acesso, perfil acadêmico, comunidades, publicações, discussões, filtro de conteúdo, materiais, enquetes, busca, notificações, mensagens e moderação), mais um módulo de avisos institucionais.

**Fundação técnica implementada:**
A primeira história do backlog — a fundação do projeto (scaffold e infraestrutura) — já foi implementada e está em fase final de revisão. Ela estabeleceu a base sobre a qual todas as funcionalidades seguintes serão construídas:
- Estrutura de monorepo com `frontend/` (Angular) e `backend/` (Java/Quarkus, um pacote por módulo de domínio).
- Ambiente de desenvolvimento local reproduzível via Docker Compose (banco de dados + backend + frontend), com variáveis de ambiente documentadas.
- Pipeline de integração contínua (GitHub Actions): build, testes automatizados e validação do contrato de API rodam a cada alteração; a branch principal está protegida e só aceita mudanças com a esteira de verificação totalmente aprovada.
- Contrato de API inicial (OpenAPI) definindo o formato padrão de resposta de erro e o componente compartilhado de paginação, usados por todos os módulos futuros.
- Filtro de segurança JWT registrado na aplicação (mecanismo de autenticação por token), pronto para validar as próximas funcionalidades de login e controle de acesso.
- Tabela e serviço de auditoria centralizados, para registrar eventos sensíveis (login, alterações administrativas, denúncias) de forma consistente em todos os módulos.
- Verificação de saúde da aplicação (health check), usada para monitoramento em produção.

Neste momento, os 12 módulos de domínio do backend existem apenas como estrutura de pacotes vazia (preparados para receber código); o frontend Angular está no estado padrão gerado pelo framework, ainda sem telas de produto. Ou seja: a fundação técnica está pronta, mas nenhuma funcionalidade de negócio foi implementada ainda.

### 2.2 O que está em andamento: escopo da entrega atual

A equipe trabalha agora na primeira entrega funcional do produto, cujo recorte de escopo foi definido para caber no prazo desta etapa. Esse recorte prioriza um fluxo ponta a ponta demonstrável em vez de cobrir todos os módulos parcialmente:

**Escopo obrigatório da entrega atual:**
- **Cadastro, login e controle de acesso** — cadastro com e-mail institucional, confirmação de e-mail antes do primeiro login, autenticação com emissão de sessão (JWT), bloqueio de acesso sem autenticação, restrição de ações por perfil de usuário, e logout com invalidação de sessão.
- **Comunidades** — administrador da plataforma pré-cria as comunidades de curso; aluno é automaticamente associado à comunidade do seu curso ao se cadastrar (auto-join); criação de comunidades abertas por qualquer aluno; entrada e saída voluntária de comunidades abertas; listagem, filtro e visualização do feed de uma comunidade mesmo sem ser membro; administração (edição, exclusão, remoção de membros) por quem criou a comunidade.
- **Publicações** — criação de postagens em uma comunidade da qual o aluno é membro, e listagem das postagens da comunidade.
- **Fundação visual e de experiência** — sistema de design ("Campus Clean": paleta de cores, tipografia, componentes reutilizáveis), casca de navegação global e comportamento responsivo, aplicados a todas as telas acima desde o primeiro deploy.

**Escopo desejável, se houver tempo (stretch):**
- **Perfil acadêmico** — edição de nome, curso, período e interesses; visualização do próprio perfil e do perfil público (somente leitura) de outros usuários.
- **Discussões** — comentários em postagens, respostas encadeadas, edição e exclusão do próprio conteúdo.

Ao final dessa etapa, o objetivo é ter um fluxo completo e publicado em produção: um aluno se cadastra com e-mail institucional, confirma o e-mail, faz login, é automaticamente inserido na comunidade do seu curso, descobre e entra em comunidades abertas, e publica conteúdo — tudo já com a identidade visual definitiva do produto.

**Fora do escopo desta etapa** (ficam para a próxima entrega, descrita na seção 3): filtro de conteúdo, materiais anexados, enquetes, busca, notificações, mensagens privadas, moderação e avisos institucionais.

---

## 3. Próximo MVP (segunda entrega)

A segunda entrega tem como objetivo completar o produto até o escopo total validado no PRD: os 12 módulos funcionais mais o módulo de avisos institucionais. Ela parte da fundação técnica e visual já construída na primeira entrega e do fluxo básico de identidade, comunidades e publicações já em produção, adicionando as camadas de interação social mais ricas, descoberta de conteúdo e governança da plataforma.

O escopo da segunda entrega se organiza em três frentes:

### 3.1 Consolidação do que ficou como "desejável" na primeira entrega

Caso perfil acadêmico e discussões não tenham sido concluídos como escopo desejável na etapa atual, eles se tornam obrigatórios nesta entrega:
- **Perfil acadêmico completo** — edição de nome, curso e período (com re-disparo automático da associação à comunidade de curso quando o curso é alterado), gerenciamento de interesses, notificação de onboarding progressivo convidando o aluno a completar o perfil, e visualização do perfil público de terceiros a partir de uma postagem ou comentário.
- **Discussões** — comentários em postagens, respostas encadeadas com hierarquia visual (limitada a três níveis de indentação), e edição/exclusão restrita ao próprio conteúdo.

### 3.2 Descoberta e enriquecimento de conteúdo

- **Filtro de conteúdo** — filtragem de postagens por curso, disciplina e tipo de conteúdo, com a filtragem aplicada persistindo durante a navegação do usuário.
- **Materiais** — anexação de arquivos (imagens e PDF) ou links externos às postagens, com validação de tipo de arquivo e acesso ao material por qualquer usuário com acesso à postagem.
- **Enquetes e pesquisas** — criação de enquetes dentro de uma comunidade (escolha única ou múltipla, de duas a cinco opções, com data de encerramento opcional), votação com anonimato real garantido (sem qualquer vínculo persistido entre o usuário e a opção escolhida), consolidação e exibição de resultados, solicitação de destaque da enquete na página inicial mediante aprovação de um moderador, e encerramento manual ou automático por data.
- **Busca** — busca textual unificada por usuários, comunidades e postagens, com suporte a filtros combinados e resultados paginados.

### 3.3 Engajamento, comunicação e governança

- **Notificações** — geração de notificações para resposta em postagem, menção e convites/enquetes; marcação de notificações como lidas e listagem centralizada.
- **Mensagens** — envio e recebimento de mensagens privadas entre usuários, listagem de conversas e suporte a grupos privados, com histórico persistido.
- **Moderação** — denúncia de conteúdo impróprio por qualquer usuário; triagem automática de primeiro nível por um agente (checagem contra lista de termos sensíveis) que notifica o moderador; visualização da denúncia pelo moderador sem exposição da identidade de quem denunciou; ocultação reversível ou remoção definitiva de conteúdo; restrição de usuários; escalonamento de uma denúncia a um moderador neutro; e notificação ao autor do conteúdo sobre a ação tomada, sempre preservando o anonimato do denunciante. Toda ação de moderação fica registrada no log de auditoria central.
- **Avisos institucionais** — publicação de avisos pelo administrador da plataforma, com escopo obrigatório (geral, para toda a universidade, ou específico de um curso), exibidos automaticamente no painel principal de cada usuário conforme o escopo definido.

### 3.4 Natureza incremental da entrega

Todas as funcionalidades acima reaproveitam a fundação já construída: o mesmo filtro de autenticação, o mesmo padrão de resposta de erro, o mesmo contrato de API, o mesmo log de auditoria e o mesmo sistema de design, de modo que o esforço desta etapa se concentra na lógica de negócio e nas telas de cada módulo, não na infraestrutura de base. Ao final desta entrega, o produto cobre a totalidade do escopo funcional validado com o orientador e a coordenação do curso, mantendo o recorte deliberado de público-alvo (alunos e docentes de Engenharia de Software do Campus Joinville) definido para o MVP.

