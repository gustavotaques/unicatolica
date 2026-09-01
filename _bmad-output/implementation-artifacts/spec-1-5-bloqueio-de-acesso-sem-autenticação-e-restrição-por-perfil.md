---
title: 'Bloqueio de acesso sem autenticação e restrição por perfil'
type: 'feature'
created: '2026-08-27'
status: 'done'
baseline_commit: '8297226c76acf463823d80f1fc00be8654ed9164'
review_loop_iteration: 0
context: ['{project-root}/_bmad-output/implementation-artifacts/epic-1-context.md']
---

<frozen-after-approval reason="human-owned intent — do not modify unless human renegotiates">

## Intent

**Problem:** O `JwtSecurityFilter` já bloqueia com 401 requisições sem token válido (RF09), mas nenhum endpoint de negócio existe ainda para provar a segunda metade da história (RF12/RF13): nenhum módulo hoje recusa uma ação com 403/404 conforme o perfil do usuário autenticado, e não há mecanismo reutilizável para um recurso ler `sub`/`roles` do token.

**Approach:** Endurecer a cobertura de 401 do filtro (caso de token expirado, hoje não testado explicitamente) e criar em `identidade` os dois primeiros endpoints autenticados do sistema — `GET /usuarios/me` (qualquer perfil autenticado) e `GET /usuarios/{id}` (somente perfil `MODERADOR`) — introduzindo um bean CDI que lê `sub`/`roles` do `JsonWebToken` para uso por qualquer módulo, e o padrão de recusa 403 via exception mapper reaproveitando o envelope `ErroResponse` (AD-5).

## Boundaries & Constraints

**Always:**
- Autenticação continua 100% responsabilidade do `JwtSecurityFilter` (AD-2); os novos endpoints não adicionam checagem de token própria.
- Autorização fina por perfil é decidida no módulo (`identidade`), nunca no filtro — o filtro segue sem popular `SecurityContext`/`@RolesAllowed`.
- Todo erro de autorização usa `ErroResponse` (`{"error":{"code","message","details"}}`) — nunca uma exceção não tratada (500).
- `GET /usuarios/{id}` para perfil sem permissão retorna 403 (existência do usuário não precisa ficar oculta) — mapeamento fixo por AD-5, não escolha do endpoint.
- Campos JSON de resposta em camelCase português (`nomeCompleto`? não — usar os nomes já existentes na entidade: `nome`, `email`, `perfil`).
- Novo endpoint documentado em `openapi.yaml` antes/junto da implementação (AD-4).

**Ask First:** nenhuma decisão adicional além da já resolvida (escopo dos endpoints `/usuarios/me` e `/usuarios/{id}`).

**Never:** editar perfil (Épico 4), qualquer campo além de `id`/`nome`/`email`/`perfil` na resposta, `@PermitAll` real via anotação JAX-RS (mecanismo de allowlist do filtro não muda), autorização composta com papel de admin de comunidade (fora do escopo da semana 1).

## I/O & Edge-Case Matrix

| Scenario | Input / State | Expected Output / Behavior | Error Handling |
|----------|--------------|---------------------------|----------------|
| Token ausente/inválido | `GET /usuarios/me` sem header ou com token malformado | Rejeitado pelo filtro antes de chegar ao resource | 401 `NAO_AUTENTICADO` |
| Token expirado | `GET /usuarios/me` com JWT expirado | Rejeitado pelo filtro | 401 `NAO_AUTENTICADO` |
| Usuário vê o próprio perfil | Token válido, `sub` = id de um usuário existente | 200 com `id`, `nome`, `email`, `perfil` do próprio usuário | N/A |
| ALUNO tenta ver perfil de outro usuário | Token válido com `roles=["ALUNO"]`, `GET /usuarios/{id}` de outro id | Recusado — perfil não tem permissão, existência não precisa ficar oculta | 403 `ACESSO_NEGADO` |
| MODERADOR vê perfil de outro usuário | Token válido com `roles=["MODERADOR"]`, `GET /usuarios/{id}` de id existente | 200 com dados do usuário alvo | N/A |
| `id` inexistente (para MODERADOR) | Token de MODERADOR, id sem registro correspondente | Recurso não encontrado | 404 `RECURSO_NAO_ENCONTRADO` |

</frozen-after-approval>

## Code Map

- `backend/.../infraestrutura/seguranca/JwtSecurityFilter.java` -- já extrai `sub`/`roles` e grava em `ContainerRequestContext` properties (`pacext.usuarioId`, `pacext.roles`); não alterar a lógica de allowlist/401, só adicionar teste de token expirado (linhas ~83-100 tratam parse/expiração de forma genérica).
- `backend/.../identidade/dominio/Usuario.java` -- entidade já tem `id`, `nome`, `email`, `perfil` (String, ex. "ALUNO"/"MODERADOR"); reaproveitar, não alterar schema.
- `backend/.../identidade/dominio/UsuarioRepository.java` -- só tem `buscarPorEmail`; usar `findById` herdado do `PanacheRepository` para o novo lookup por id (não precisa de método novo).
- `backend/.../infraestrutura/web/ErroResponse.java` -- reusar `ErroResponse.of(code, message, details)` para os novos 403/404.
- `backend/.../identidade/web/AuthResource.java` -- padrão de resource existente (`@Path`, tratamento de exceção de domínio) a espelhar para o novo `UsuarioResource`.
- `openapi.yaml` -- só `/auth/login` documentado hoje; adicionar `/usuarios/me` e `/usuarios/{id}` com schema `Usuario` (novo) reaproveitando `Erro` existente.
- **Novo:** `backend/.../identidade/infraestrutura/UsuarioAutenticado.java` (ou local equivalente) -- bean `@RequestScoped` que lê `JsonWebToken` (`getSubject()`/`getGroups()`, já populado pelo SmallRye JWT independente do filtro) expondo `id()`/`possuiPerfil(String)`; ponto de reuso para autorização de qualquer módulo futuro.
- **Novo:** `backend/.../identidade/web/UsuarioResource.java`, `UsuarioResponse.java` -- os dois endpoints.
- **Novo:** exceção `AcessoNegadoException` + `ExceptionMapper` -- mapeia para 403 `ErroResponse` (padrão a reaproveitar por outros módulos).

## Tasks & Acceptance

**Execution:**
- [x] `backend/.../identidade/infraestrutura/UsuarioAutenticado.java` -- bean `@RequestScoped` injetando `JsonWebToken`, expondo `id()` (Long) e `possuiPerfil(String)` -- mecanismo reutilizável de leitura de identidade autenticada
- [x] `backend/.../identidade/dominio/AcessoNegadoException.java` + mapper -- exceção de domínio mapeada para 403 `ErroResponse` -- padrão de recusa por perfil (AD-5)
- [x] `backend/.../identidade/web/UsuarioResource.java` -- `GET /usuarios/me` (qualquer autenticado) e `GET /usuarios/{id}` (só `possuiPerfil("MODERADOR")`, senão `AcessoNegadoException`; se id não existe, 404) -- prova end-to-end de RF12/RF13
- [x] `backend/.../identidade/web/UsuarioResponse.java` -- DTO `id`, `nome`, `email`, `perfil`
- [x] `openapi.yaml` -- documentar `GET /usuarios/me`, `GET /usuarios/{id}` e schema `Usuario` -- contrato-fonte-de-verdade (AD-4)
- [x] `JwtSecurityFilterTest.java` -- adicionar caso de token expirado retornando 401 -- fecha gap de cobertura existente
- [x] `UsuarioResourceTest.java` -- cobrir os seis cenários da I/O Matrix -- prova as ACs

**Acceptance Criteria:**
- Given um endpoint fora da allowlist, when o token está ausente, expirado ou inválido, then o filtro rejeita com 401 no envelope padrão
- Given um usuário autenticado sem permissão para uma ação, when a requisição chega ao módulo `identidade`, then a resposta é 403 (recurso não precisa ficar oculto) ou 404 (id inexistente), nunca 401 nem 500

## Design Notes

`UsuarioAutenticado` não substitui o `JwtSecurityFilter` — ele só lê o que o SmallRye JWT já populou como bean CDI (`JsonWebToken`), independentemente das properties que o filtro grava no `ContainerRequestContext`. Isso evita acoplar módulos de negócio a `ContainerRequestContext`/nomes de property string, mantendo a decisão de 403 vs. 404 dentro do resource/service do módulo, como a AD-2 exige.

## Verification

**Commands:**
- `cd backend && ./mvnw test` -- build e testes passam, incluindo os novos casos de `UsuarioResourceTest` e o teste de token expirado
- `npx @redocly/cli lint openapi.yaml` -- sem erros de schema após adicionar `/usuarios/me`, `/usuarios/{id}` e `Usuario`

**Manual checks (if no CLI):**
- `docker-compose up` e, com um token de ALUNO e um de MODERADOR obtidos via `/auth/login`, chamar `GET /usuarios/me` e `GET /usuarios/{id}` via curl/Postman conferindo 200/403/404 conforme a matriz

## Suggested Review Order

**Autorização por perfil (RF13, ponto de entrada)**

- Perfil `MODERADOR` decide acesso ao recurso alheio; recusa lança `AcessoNegadoException` antes de tocar o repositório — 403 nunca esconde a existência do usuário (AD-5).
  [`UsuarioResource.java:47`](../../backend/src/main/java/br/edu/unicatolica/pacext/identidade/web/UsuarioResource.java#L47)

- Qualquer perfil autenticado vê o próprio perfil — nenhuma checagem de papel, só identidade.
  [`UsuarioResource.java:34`](../../backend/src/main/java/br/edu/unicatolica/pacext/identidade/web/UsuarioResource.java#L34)

**Leitura de identidade reutilizável**

- Bean CDI que lê `sub`/`roles` do `JsonWebToken` já populado pelo SmallRye JWT — caminho independente do `JwtSecurityFilter`, ponto de reuso para autorização de outros módulos.
  [`UsuarioAutenticado.java:30`](../../backend/src/main/java/br/edu/unicatolica/pacext/identidade/infraestrutura/UsuarioAutenticado.java#L30)

- Guarda defensiva contra `sub` ausente/não numérico — converte falha de leitura em 401 controlado em vez de deixar `NumberFormatException` virar 500 (fix da revisão).
  [`UsuarioAutenticado.java:30`](../../backend/src/main/java/br/edu/unicatolica/pacext/identidade/infraestrutura/UsuarioAutenticado.java#L33)

**Padrão de recusa por perfil (403)**

- Exceção de domínio dedicada, sem estado — só sinaliza o caso, mapeada fora do módulo de domínio.
  [`AcessoNegadoException.java:9`](../../backend/src/main/java/br/edu/unicatolica/pacext/identidade/dominio/AcessoNegadoException.java#L9)

- Tradutor para o envelope `ErroResponse` padrão (AD-5) — modelo a ser copiado por outros módulos quando precisarem de 403 por perfil.
  [`AcessoNegadoExceptionMapper.java:18`](../../backend/src/main/java/br/edu/unicatolica/pacext/identidade/web/AcessoNegadoExceptionMapper.java#L18)

**Guarda de autenticação (401, fix da revisão)**

- Mesmo padrão exceção+mapper acima, reaproveitado para o caso de drift entre o filtro e o `JsonWebToken` injetado.
  [`NaoAutenticadoException.java:11`](../../backend/src/main/java/br/edu/unicatolica/pacext/identidade/dominio/NaoAutenticadoException.java#L11)

- Devolve o mesmo código `NAO_AUTENTICADO` que o `JwtSecurityFilter` usa, para consistência de contrato.
  [`NaoAutenticadoExceptionMapper.java:19`](../../backend/src/main/java/br/edu/unicatolica/pacext/identidade/web/NaoAutenticadoExceptionMapper.java#L19)

**Contrato OpenAPI (AD-4)**

- Dois novos paths documentam 200/401/403/404 conforme o mapa fixo de cenário da AD-5.
  [`openapi.yaml:38`](../../openapi.yaml#L38)

- Schema `Usuario` com `perfil` restrito a enum — API não aceita valor livre para um campo que o código trata como conjunto fechado.
  [`openapi.yaml:104`](../../openapi.yaml#L104)

**Peripherals — testes**

- Prova que a leitura de `sub`/`roles` via `JsonWebToken` (caminho do `UsuarioAutenticado`) concorda com a config `smallrye.jwt.path.groups` contra um token real assinado/verificado — fecha o gap apontado pela revisão de que os dois caminhos de leitura de identidade nunca eram comparados.
  [`UsuarioAutenticadoTest.java`](../../backend/src/test/java/br/edu/unicatolica/pacext/identidade/infraestrutura/UsuarioAutenticadoTest.java)

- Cobre os quatro cenários de autorização por perfil da I/O Matrix (200 próprio, 403 ALUNO em outro, 200 MODERADOR em outro, 404 id inexistente) mais o mapper.
  [`UsuarioResourceTest.java`](../../backend/src/test/java/br/edu/unicatolica/pacext/identidade/web/UsuarioResourceTest.java#L44)

- Fecha o gap de cobertura citado na spec: token expirado também é 401, não só ausente/inválido.
  [`JwtSecurityFilterTest.java:145`](../../backend/src/test/java/br/edu/unicatolica/pacext/infraestrutura/seguranca/JwtSecurityFilterTest.java#L145)
