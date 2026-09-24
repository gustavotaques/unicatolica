# Validação — Story 1.6 (Logout e invalidação de sessão)

Este documento registra o que foi implementado para a Story 1.6 (KAN-20) e como foi
validado, seguindo o mesmo formato de `docs/validacao-stories-1.2-1.3-1.4-2.3.md`.

## O que foi implementado

- **Story 1.6** — Logout e invalidação de sessão (`POST /auth/logout`)

## Decisão de arquitetura

O JWT emitido no login (Story 1.4) é stateless por natureza: não existe `jti`, tabela de
sessão ou qualquer mecanismo de revogação no backend antes desta história — o token só
"morre" quando expira naturalmente pelo claim `exp`. O critério de aceite exige que, após
o logout, o mesmo token pare de ser aceito em requisições subsequentes (401).

Duas abordagens foram cogitadas:

1. **Timestamp de sessão por usuário** (`sessao_valida_desde` em `usuario`) — o logout
   grava o instante atual; o filtro de segurança rejeita qualquer token cujo `iat` seja
   anterior a esse valor. Simples, sem tabela nova, sem dependência de cache externo
   (Redis). Efeito colateral aceito: invalida *todos* os tokens emitidos antes do logout
   para aquele usuário (não só o token da sessão atual) — não há problema, pois a
   plataforma não define múltiplas sessões simultâneas por usuário em nenhum artefato de
   planejamento.
2. Denylist de `jti` por token — mais granular, porém exige adicionar a claim `jti` na
   emissão, uma tabela nova e uma consulta extra por requisição.
3. Migrar para cookie de sessão com expiração — descartada: contraria a decisão
   arquitetural já documentada em `JwtSecurityFilter.java` (AD-2 — "Transporte
   exclusivamente via header `Authorization: Bearer` — nunca cookie") e exigiria mudanças
   bem maiores (filtro, frontend, CORS/CSRF) fora do escopo desta história.

**Escolhida a opção 1**, por ser a mais simples e consistente com o que já existe.

## O que foi implementado, arquivo a arquivo

- `db/changelog/modulos/identidade/identidade-004-add-sessao-valida-desde.xml` — novo
  changeset, coluna `sessao_valida_desde` (nullable) em `usuario`.
- `identidade/dominio/Usuario.java` — campo `sessaoValidaDesde`.
- `identidade/aplicacao/AuthService.java` — método `logout(Long usuarioId)`: grava
  `sessaoValidaDesde = Instant.now()` e registra auditoria (`"LOGOUT"`), mesmo padrão do
  `autenticar` para `"LOGIN"`.
- `identidade/web/AuthResource.java` — `POST /auth/logout`, autenticado (não entra na
  allowlist do `JwtSecurityFilter` — precisa saber quem é o usuário), reusa
  `UsuarioAutenticado.id()` (já usado por `UsuarioResource`, Story 1.5) em vez de ler a
  property JAX-RS diretamente. Retorna 204.
- `infraestrutura/seguranca/JwtSecurityFilter.java` — depois de validar assinatura/claims,
  consulta `UsuarioRepository.findById` e rejeita com o mesmo 401 `NAO_AUTENTICADO` se o
  `iat` do token for anterior a `sessaoValidaDesde` do usuário.
- `frontend/src/app/core/auth/auth.service.ts` — `logout()` continua limpando o
  `localStorage` de imediato (não espera rede), e dispara `POST /auth/logout` com o
  Bearer do token que acabou de ser removido, em best-effort (erro de rede não impede o
  logout local, que já aconteceu).

## Como foi testado

### 1. Testes unitários automatizados (backend)

```bash
cd backend
./mvnw test
```

**Resultado:** todos os testes passam (exit code 0), incluindo os novos:
- `AuthServiceTest.logoutGravaSessaoValidaDesdeERegistraAuditoria` /
  `logoutNaoFalhaParaUsuarioInexistente`
- `AuthResourceTest.retorna204NoContentAoDeslogarUsuarioAutenticado`
- `JwtSecurityFilterTest.rejeitaTokenEmitidoAntesDoLogout` (token com `iat` anterior a
  `sessaoValidaDesde` -> 401) e `aceitaTokenEmitidoAposLogoutDoProprioUsuario` (token
  emitido depois do logout de outro momento continua válido)
- Nenhuma regressão nos testes já existentes das Stories 1.2/1.3/1.4/1.5.

### 2. Frontend

`auth.service.spec.ts` ganhou 3 casos novos (POST para `/auth/logout` com header
`Authorization: Bearer <token>`, e ausência de chamada quando não há token armazenado).
**Não foi possível executar `ng test` neste ambiente** — o `node_modules` local tem o
binário nativo do `esbuild` instalado para Linux (`@esbuild/linux-x64`) rodando em
Windows, um problema de ambiente pré-existente e sem relação com esta mudança (mesmo
`ng build` falha com o mesmo erro, antes de qualquer teste rodar). Os testes foram
revisados manualmente linha a linha contra a implementação de `logout()`; recomenda-se
rodar `npm ci && ng test` num ambiente com o `node_modules` reinstalado nativamente antes
do merge.

### 3. Fluxo manual esperado (a confirmar com backend/Postgres reais, mesmo estilo do doc anterior)

```bash
# Login
curl -X POST http://localhost:8080/auth/login -H "Content-Type: application/json" \
  -d '{"email":"...","senha":"..."}'
# -> 200 {"token": "eyJ..."}

# Endpoint autenticado com o token acima
curl http://localhost:8080/usuarios/me -H "Authorization: Bearer <token>"
# -> 200

# Logout
curl -X POST http://localhost:8080/auth/logout -H "Authorization: Bearer <token>"
# -> 204

# Mesmo endpoint, mesmo token, depois do logout
curl http://localhost:8080/usuarios/me -H "Authorization: Bearer <token>"
# -> 401 {"error":{"code":"NAO_AUTENTICADO", ...}}
```

Este fluxo não pôde ser executado neste ambiente (sem Docker/Postgres disponíveis, mesma
limitação já registrada no doc de validação anterior) — coberto pelos testes unitários
isolados (`JwtSecurityFilterTest`, sem runtime completo do Quarkus, mesmo padrão da Story
1.5).

## Pendências / decisões a confirmar

- `identidade.email.dominio-institucional` e política de senha continuam com
  `[DECISÃO A CONFIRMAR]` desde a Story 1.2 (não afetado por esta história).
- Reinstalar `node_modules` nativamente (`npm ci`) num ambiente compatível antes do
  próximo merge, para validar o frontend de ponta a ponta (build + testes) antes do CI.
