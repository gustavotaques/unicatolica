# Modelo de dados — fatia núcleo (semana 1)

<!-- Complementa o ERD conceitual (sem colunas) de
     architecture/architecture-unicatolica-2026-08-22/ARCHITECTURE-SPINE.md, seção "Structural Seed".
     Cobre só Identidade, Comunidades e Publicações (must-have semana 1) — mesmo recorte do ERD original.
     `log_auditoria` já está implementada (db/changelog/modulos/infraestrutura/) e não é redefinida aqui. -->

## Status

Nenhuma destas tabelas tem changeset Liquibase ainda — este documento é a proposta de schema para as Stories 1.2 (Cadastro), 2.1/2.2 (Comunidades) e 3.1 (Publicações) a implementar. **Pontos marcados `[DECISÃO A CONFIRMAR]` são escolhas de implementação feitas aqui por não estarem travadas em nenhum artefato de planejamento — revisar com o time antes de codificar o changelog.**

## ERD

```mermaid
erDiagram
    USUARIO ||--o{ USUARIO_PAPEL : "tem"
    USUARIO ||--o{ COMUNIDADE_MEMBRO : "participa"
    USUARIO ||--o{ PUBLICACAO : "publica"
    COMUNIDADE ||--o{ COMUNIDADE_MEMBRO : "tem"
    COMUNIDADE ||--o{ PUBLICACAO : "recebe"

    USUARIO {
        bigint id PK
        varchar nome
        varchar email UK
        varchar senha_hash
        date data_nascimento
        varchar curso
        boolean email_confirmado
        varchar token_confirmacao_email
        timestamptz token_confirmacao_expira_em
        timestamptz criado_em
        timestamptz atualizado_em
    }
    USUARIO_PAPEL {
        bigint usuario_id PK_FK
        varchar papel PK
    }
    COMUNIDADE {
        bigint id PK
        varchar nome UK
        text descricao
        varchar tipo
        bigint criado_por_usuario_id
        timestamptz criado_em
        timestamptz atualizado_em
    }
    COMUNIDADE_MEMBRO {
        bigint id PK
        bigint comunidade_id FK
        bigint usuario_id
        varchar papel_na_comunidade
        timestamptz entrou_em
    }
    PUBLICACAO {
        bigint id PK
        bigint comunidade_id FK
        bigint usuario_id
        text conteudo
        timestamptz criado_em
        timestamptz atualizado_em
    }
```

---

## `usuario` — módulo Identidade

Changelog: `db/changelog/modulos/identidade/identidade-001-create-usuario.xml`

| Coluna | Tipo | Constraints | Origem |
|---|---|---|---|
| `id` | `BIGINT` | PK, identity | — |
| `nome` | `VARCHAR(150)` | NOT NULL | Story 1.2 |
| `email` | `VARCHAR(255)` | NOT NULL, UNIQUE | RF02, RF03 |
| `senha_hash` | `VARCHAR(255)` | NOT NULL | RF04 (nunca a senha em texto puro) |
| `data_nascimento` | `DATE` | NOT NULL | RF01.1 (validação de idade ≥ 18 no cadastro) |
| `curso` | `VARCHAR(150)` | NOT NULL | RF24.1 — ver nota de auto-join abaixo |
| `email_confirmado` | `BOOLEAN` | NOT NULL, DEFAULT `false` | RF01.2 |
| `token_confirmacao_email` | `VARCHAR(255)` | NULL | RF01.2 — token de uso único do link de confirmação |
| `token_confirmacao_expira_em` | `TIMESTAMP WITH TIME ZONE` | NULL | RF01.2 |
| `criado_em` | `TIMESTAMP WITH TIME ZONE` | NOT NULL | RF05 |
| `atualizado_em` | `TIMESTAMP WITH TIME ZONE` | NULL | — |

**Índices:** único em `email` (também serve à checagem de RF02); índice em `token_confirmacao_email` (lookup no clique do link, Story 1.3).

`[DECISÃO A CONFIRMAR]` **Validação de domínio institucional (RF01.3)** não precisa de coluna — é uma regra de validação no `Service` (sufixo de e-mail aceito), não um dado persistido.

`[DECISÃO A CONFIRMAR]` **Roles (RF12/RF13)** ficam em tabela separada `usuario_papel` (abaixo) em vez de coluna única, porque o claim `roles` do JWT (AD-2) é plural/`Set<String>` — um usuário pode acumular papéis (ex.: aluno que também é administrador de plataforma). Se o time preferir um único papel por usuário nesta fase, colapsar em uma coluna `papel VARCHAR(30)` é uma simplificação válida.

`[DECISÃO A CONFIRMAR]` **`curso` como texto solto em `usuario`** (não FK para `comunidade`): a Story 1.2 só precisa do mínimo pra disparar o auto-join (RF24.1) sem depender do módulo de Perfil Acadêmico completo (Epic 4). Guardar o nome do curso como texto — casado em runtime, pelo serviço de auto-join do módulo Comunidades, contra `comunidade.nome` onde `tipo='CURSO'` — evita uma FK cruzando a fronteira de módulo (AD-3 proíbe módulo lendo/escrevendo direto na tabela de outro; a checagem de existência do curso deve passar pela interface pública do módulo Comunidades, não por uma constraint de banco). Alternativa mais rígida: expor um endpoint/enum de cursos válidos que o formulário de cadastro consome como dropdown, evitando texto livre digitado errado.

### `usuario_papel` — módulo Identidade

Changelog: mesmo arquivo `identidade-001-create-usuario.xml` (tabela auxiliar da mesma história).

| Coluna | Tipo | Constraints |
|---|---|---|
| `usuario_id` | `BIGINT` | PK (composta), FK → `usuario.id` |
| `papel` | `VARCHAR(30)` | PK (composta), NOT NULL |

Valores esperados de `papel` nesta fase: `ALUNO`, `ADMINISTRADOR` (quem pode pré-criar comunidade de curso, RF21.2). `MODERADOR` só passa a ser emitido quando o Epic 12 (Moderação) for construído — não é bloqueado pelo schema, é decisão de quem recebe o papel.

---

## `comunidade` — módulo Comunidades

Changelog: `db/changelog/modulos/comunidades/comunidades-001-create-comunidade.xml`

| Coluna | Tipo | Constraints | Origem |
|---|---|---|---|
| `id` | `BIGINT` | PK, identity | — |
| `nome` | `VARCHAR(150)` | NOT NULL, UNIQUE | RF22 |
| `descricao` | `TEXT` | NULL | RF22 |
| `tipo` | `VARCHAR(10)` | NOT NULL, CHECK IN (`CURSO`, `ABERTA`) | RF21.1 — **imutável após criação**, aplicado no `Service` (não existe `UPDATE` de `tipo` em nenhum fluxo) |
| `criado_por_usuario_id` | `BIGINT` | NOT NULL | RF23 — id de `usuario`, sem FK física (leitura cross-módulo, AD-3) |
| `criado_em` | `TIMESTAMP WITH TIME ZONE` | NOT NULL | — |
| `atualizado_em` | `TIMESTAMP WITH TIME ZONE` | NULL | RF30 |

**Índices:** índice em `tipo` (RF27/RF28 — listagem e filtro por tipo).

`[DECISÃO A CONFIRMAR]` `criado_por_usuario_id` não determina sozinho quem administra a comunidade — RF29 permite remoção de membros, o que exige saber *todos* os administradores, não só o criador original. Por isso o papel de administração vive em `comunidade_membro.papel_na_comunidade`, não aqui; `criado_por_usuario_id` é só rastreabilidade/auditoria de quem criou.

## `comunidade_membro` — módulo Comunidades

Changelog: mesmo arquivo `comunidades-001-create-comunidade.xml`.

| Coluna | Tipo | Constraints | Origem |
|---|---|---|---|
| `id` | `BIGINT` | PK, identity | — |
| `comunidade_id` | `BIGINT` | NOT NULL, FK → `comunidade.id` | — |
| `usuario_id` | `BIGINT` | NOT NULL | RF24 — id de `usuario`, sem FK física (AD-3) |
| `papel_na_comunidade` | `VARCHAR(20)` | NOT NULL, DEFAULT `MEMBRO`, CHECK IN (`ADMINISTRADOR`, `MEMBRO`) | RF23, RF29 |
| `entrou_em` | `TIMESTAMP WITH TIME ZONE` | NOT NULL | — |

**Constraints:** único em (`comunidade_id`, `usuario_id`) — é o mecanismo que garante RF25 (impedir ingresso duplicado) no nível do banco, além da checagem no `Service`.
**Índices:** em `comunidade_id` (RF27 listar comunidade) e em `usuario_id` (achar comunidades do usuário).

Ao criar uma comunidade (RF23), insere-se aqui uma linha com `papel_na_comunidade='ADMINISTRADOR'` para o criador. O auto-join (RF24.1) insere `papel_na_comunidade='MEMBRO'` sem passar pela constraint de duplicidade quando é idempotente (upsert/`ON CONFLICT DO NOTHING`, não uma segunda tentativa de `INSERT` simples).

---

## `publicacao` — módulo Publicações

Changelog: `db/changelog/modulos/publicacoes/publicacoes-001-create-publicacao.xml`

| Coluna | Tipo | Constraints | Origem |
|---|---|---|---|
| `id` | `BIGINT` | PK, identity | — |
| `comunidade_id` | `BIGINT` | NOT NULL | RF35 — id de `comunidade`, sem FK física (AD-3) |
| `usuario_id` | `BIGINT` | NOT NULL | RF34 — id de `usuario`, sem FK física (AD-3) |
| `conteudo` | `TEXT` | NOT NULL | RF32, RF33 |
| `criado_em` | `TIMESTAMP WITH TIME ZONE` | NOT NULL | — |
| `atualizado_em` | `TIMESTAMP WITH TIME ZONE` | NULL | reservado — edição de postagem não está nas Stories 3.1/3.2 |

**Índices:** em `comunidade_id` (RF36 listagem do feed) e em `criado_em` (ordenação cronológica do feed).

---

## Nota transversal — fronteiras de módulo (AD-3)

`comunidade_id` em `publicacao`/`comunidade_membro` e `usuario_id` em `comunidade`/`comunidade_membro`/`publicacao` são **ids simples (`BIGINT`), sem `@ManyToOne`/`@JoinColumn` no mapeamento JPA** cruzando pacote de módulo — isso é o que o teste de arquitetura (ArchUnit, AD-3) provavelmente vai checar. Se o módulo precisar exibir dado do outro lado (ex.: nome do autor da postagem), a leitura passa por uma projeção/DTO exposta pela interface pública do módulo dono (`identidade`/`comunidades`), nunca por navegação de entidade JPA direta. Constraint de `FOREIGN KEY` a nível de banco (não de entidade Java) é opcional — ajuda a integridade referencial e não viola a regra, que é sobre acesso a repositório/tabela, não sobre o schema físico; decisão de incluí-la ou não fica a critério de quem escrever o changelog.

## Convenções aplicadas (já travadas na Architecture Spine)

- Nomes de tabela/coluna em português; PKs `BIGINT` identity (nunca UUID).
- Instantes em `TIMESTAMP WITH TIME ZONE` (mapeia `Instant`); datas-only em `DATE` (mapeia `LocalDate`) — só `usuario.data_nascimento` usa isso aqui.
- Changeset id prefixado por módulo (`identidade-001-...`, `comunidades-001-...`, `publicacoes-001-...`), nunca contador global (AD-9).
- Cada arquivo de changelog aqui listado é novo — nenhum edita `infraestrutura-001-create-log-auditoria.xml`.
