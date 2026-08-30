# Reconciliação: input original vs. PRD derivado

**Input original:** `docs/unicatolica-pacext-contexto.md`
**PRD analisado:** `_bmad-output/planning-artifacts/prds/prd-unicatolica-2026-08-12/prd.md`

Escopo desta análise: apenas lacunas reais (conteúdo do input que o PRD não menciona nem referencia, e contradições entre o PRD e o input). Não é reportado como lacuna o conteúdo das tabelas RF/RNF, que o PRD referencia deliberadamente por número em vez de duplicar — essa estrutura está correta e foi confirmada.

---

## Gap 1 — Papel de "coordenador ou professor" na gestão de comunidades de curso não aparece no PRD

**O que está no input e falta no PRD:** a seção 9.2 do input (Decisões por módulo → Comunidades), validada com o orientador, diz textualmente: *"Comunidades segmentadas por curso... **Coordenador ou professor pode gerenciar a comunidade e adicionar colaboradores.** Administradores removem membros e editam configurações."* O PRD, ao formalizar o novo modelo de comunidades de curso (RF21.1, RF21.2, RF24.1 — seção 4.3) e ao definir "Admin da plataforma" no Glossário (seção 3), atribui a pré-criação e gestão de comunidades de curso exclusivamente ao "administrador da plataforma". O papel de coordenador/professor como gestor de comunidade e responsável por adicionar colaboradores não é mencionado em nenhum lugar do PRD — nem no Glossário, nem nas Features, nem nas personas/JTBD.

**Por que isso importa:** é uma decisão de módulo explicitamente validada com o orientador (não uma decisão nova de equipe pós-validação, como as de 2026-08-12), então tem o mesmo peso de "fonte de verdade" que RF21/RF23. Se o PRD silenciosamente restringe a gestão de comunidade a um único ator (admin da plataforma) sem reconciliar com o papel de coordenador/professor, isso é uma contradição de fato entre os dois documentos, não apenas uma omissão de detalhe — e pode levar a uma arquitetura de permissões incompleta (falta um papel "gestor de comunidade de curso" distinto de admin/moderador).

**Sugestão objetiva:** adicionar uma frase ao RF21.2 ou ao Glossário do PRD reconciliando os dois atores — por exemplo: *"Coordenador/professor do curso pode gerenciar a comunidade de curso correspondente e adicionar colaboradores (decisão validada, §9.2 do documento de contexto); papel a formalizar como RF adicional na fase de arquitetura/épicos."* — ou registrar a lacuna explicitamente na seção 10 (Open Questions) caso a equipe ainda não tenha decidido como esse papel se relaciona com "Admin da plataforma".

---

## Gap 2 — Nenhuma ponte para Stack Tecnológica (§5) e Arquitetura C4 (§6) do input

**O que está no input e falta no PRD:** a seção 5 do input define a stack completa (Angular; Java/Quarkus/Hibernate; PostgreSQL; JWT; Git/Figma/Postman/Confluence) e a seção 6 descreve a arquitetura (monólito multimodular, TDD/SOLID, DDD, três diagramas C4). O PRD não faz nenhuma referência a essas seções — a única menção correlata é "filtro de segurança JWT (C4 nível 3)" na seção 7 (NFRs), um detalhe pontual, não uma ponte para a arquitetura como um todo. Diferente das seções de RF/RNF (que o PRD referencia explicitamente com "`docs/unicatolica-pacext-contexto.md` §3.x/§4"), não há nenhum "ver §5/§6" no PRD.

**Por que isso importa:** o PRD se define na seção 0 como documento de apoio para "quem for dar continuidade técnica ao projeto (arquitetura, UX, épicos/histórias)". Um leitor que chegue só ao PRD não tem indicação de que a stack e a arquitetura C4 já estão decididas e documentadas — nem um ponteiro para onde encontrá-las. Isso é inconsistente com o cuidado que o PRD tem em referenciar §3/§4/§7 do input por número.

**Sugestão objetiva:** adicionar uma frase curta na seção 0 (Propósito) ou criar uma seção "Referência técnica" apontando: *"Stack tecnológica e arquitetura C4 (monólito multimodular, DDD/TDD) já definidas — ver `docs/unicatolica-pacext-contexto.md` §5–6."*

---

## Gap 3 — Mapeamento de riscos (§8, 13 riscos) referenciado só parcialmente (apenas R01)

**O que está no input e falta no PRD:** a seção 8 do input mapeia 13 riscos (R01–R13) com categoria, probabilidade/impacto, prioridade, mitigação e contingência. O PRD cita apenas R01 ("não adoção") como justificativa das métricas primárias (seção 9). Riscos igualmente relevantes ao conteúdo do PRD — como R02 (escopo superdimensionado, que se conecta diretamente à seção 6 "Escopo do MVP" do PRD) ou R09 (arquitetura/performance, relacionado a RNF03) — não são mencionados, e não há nenhuma ponte geral para "ver §8 para o mapa de riscos completo".

**Por que isso importa:** riscos como R02 têm relação direta com decisões que o PRD toma (controle de escopo do MVP, non-goals). Sem uma referência, fica menos claro para quem lê o PRD que essas decisões de escopo já são resposta a um risco mapeado e priorizado — enfraquece a rastreabilidade que o próprio PRD diz preservar (RNF09, seção 7).

**Sugestão objetiva:** adicionar uma frase na seção 6 (Escopo do MVP) ou 9 (Métricas): *"Ver `docs/unicatolica-pacext-contexto.md` §8 para o mapeamento completo de riscos (13 riscos); R01 e R02 são os mais diretamente endereçados pelas decisões de escopo e métricas deste PRD."*

---

## Gap 4 — "Docentes" aparece como público em escopo do MVP sem base no input nem desenvolvimento no restante do PRD

**O que está no input e falta no PRD:** o input define o público-alvo como "Estudantes universitários (+3.000 alunos, Campus Joinville)" (frontmatter) e todas as personas/jornadas de uso giram em torno de alunos. O PRD, na seção 6.1 (Escopo do MVP → Em escopo), declara: *"Público: alunos **e docentes** de Engenharia de Software, Campus Joinville."* Docentes não aparecem em nenhuma JTBD, jornada (UJ-1/UJ-2), persona, ou RF do PRD — a única conexão possível é o papel de "coordenador ou professor" citado no input §9.2 (ver Gap 1), que o próprio PRD não desenvolve.

**Por que isso importa:** é uma afirmação de escopo (quem é usuário do MVP) sem lastro nem no input nem no resto do próprio PRD — se não é intencional, é uma inconsistência interna; se é intencional (conectada ao papel de coordenador/professor do Gap 1), falta desenvolver o que docente pode fazer no sistema.

**Sugestão objetiva:** ou remover "e docentes" da seção 6.1 se for imprecisão de redação, ou — se intencional — adicionar uma frase conectando ao papel de coordenador/professor (§9.2 do input, ver Gap 1) e indicar que o RF desse papel ainda não existe, registrando como Open Question.

---

## Notas (não tratadas como gaps)

- O fator "financeiro" descartado do escopo (input §1, item 1) não aparece no PRD nem como Non-Goal — é uma omissão muito menor (não é um requisito, é um fator de causa-raiz já descartado no próprio input) e não chega a comprometer a rastreabilidade do documento.
- "Foco inicial nos calouros" (input §9.2, decisão de Lançamento) é apenas parcialmente refletido no PRD — a persona Julia é caloura, mas a seção 6.1 define escopo como "alunos... de Engenharia de Software" sem diferenciar calouros de veteranos. Achado marginal, não elevado a gap formal porque a persona já comunica a intenção implicitamente.
