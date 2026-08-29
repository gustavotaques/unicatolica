# Validação — Stories 1.2, 1.3, 2.3 (e reconciliação com Story 1.4/1.5)

Este documento registra o que foi alterado/adaptado na branch
`feature/story-1.2-1.3-2.3-cadastro-confirmacao-autojoin` (PR #3) e como cada parte foi
testada, para deixar rastreável que o backend/frontend rodam sem erro antes do merge.

## O que foi implementado

- **Story 1.2** — Cadastro de aluno com e-mail institucional (`POST /auth/registro`)
- **Story 1.3** — Confirmação de e-mail antes do primeiro login
  (`POST /auth/confirmacao-email/{token}`, `POST /auth/confirmacao-email/reenvio`)
- **Story 2.3** — Auto-join à comunidade de curso (`AutoJoinCursoService`, interface
  pública do módulo Comunidades, AD-3)

## O que foi alterado/adaptado (reconciliação com o trabalho do time)

A branch `feature/init-project` (Vynicyus/Gustavo) implementou a Story 1.4 (Login) e,
depois, a Story 1.5 (bloqueio de acesso/restrição por perfil) em paralelo, com um modelo
de dados e uma estrutura de pacotes (`web`/`aplicacao`/`dominio`) diferentes do que esta
branch usava. Depois que esse trabalho foi consolidado na `main` (PR #5), esta branch
foi reconciliada por cima, sem alterar nada que já estava na `main`:

| Antes (nesta branch) | Depois (adaptado à main) |
|---|---|
| Pacotes achatados (`identidade.*`) | Estrutura em camadas `identidade.web` / `identidade.aplicacao` / `identidade.dominio` |
| `Usuario` próprio, com `usuario_papel` (múltiplos papéis) | Estende o `Usuario` da main (campo único `perfil`) com `curso`, `dataNascimento`, `tokenConfirmacaoEmail`, `tokenConfirmacaoExpiraEm` — via changeset novo `identidade-003-add-cadastro-fields.xml` (nunca edita `identidade-001`/`002`, já aplicados) |
| Hash de senha com `BcryptUtil` (Quarkus) | Hash de senha com `PasswordHasher` (`at.favre.lib`, já usado pelo login) — evita duas libs de bcrypt divergentes no mesmo módulo |
| `ApiException` genérica (infra própria) | Mantida só para Cadastro/Confirmação (arquivos novos, não conflita); `AuthService` usa o padrão de exceção específica já estabelecido (`CredenciaisInvalidasException`, `EmailNaoConfirmadoException` — nova, mesmo padrão) |
| Chave JWT lida de arquivo `.pem` commitado | Adaptado para o padrão da main: conteúdo da chave via variável de ambiente (`JWT_PRIVATE_KEY`/`JWT_PUBLIC_KEY`), nunca arquivo no repo |

## Bugs reais encontrados e corrigidos (só apareceram testando de verdade)

1. **`mvnw` com quebra de linha Windows (CRLF)** — quebrava o shebang dentro do
   container Linux (`exec: ./mvnw: not found`). Corrigido via `.gitattributes`.
2. **Tag `<addCheckConstraint>` do Liquibase** — não existe nessa versão; trocada por
   `<sql>` puro.
3. **Hash de senha incompatível** — o hash gerado por uma lib de bcrypt não era
   reconhecido pela outra (`ELY08003: Unknown crypt string algorithm`). Resolvido
   unificando em uma única lib (`PasswordHasher`) para todo o módulo.
4. **Mensagem de login não distinguia e-mail não confirmado** — a Story 1.3 exige
   mensagem distinta de credencial inválida; corrigido com `EmailNaoConfirmadoException`
   (checada só depois de validar a senha, para não permitir enumerar contas pendentes).
5. **`mvnw` sem bit de execução** — perdido numa mesclagem, CI do Linux recusava rodar
   (`Permission denied`, exit 126). Corrigido com `git update-index --chmod=+x`.
6. **`docker-compose.yml` não repassava `JWT_PRIVATE_KEY`/`JWT_PUBLIC_KEY`** para o
   container do backend — sem elas, o login quebrava com erro de chave inválida.

## Como foi testado

### 1. Testes unitários automatizados (backend)

```bash
cd backend
rm -rf target/surefire-reports   # garante que não há relatório de execução antiga
./mvnw test
```

**Resultado:** 56 testes, 0 falhas, 0 erros — cobrindo `CadastroService`,
`ConfirmacaoEmailService`, `AuthService` (incluindo os dois cenários de mensagem
distinta: e-mail não confirmado vs. credencial inválida), `AutoJoinCursoServiceImpl`,
`ApiExceptionMapper`, `JwtSecurityFilter`, `UsuarioAutenticado`, `UsuarioResource`, entre
outros.

### 2. Contrato da API

```bash
npx --yes @redocly/cli lint openapi.yaml
```

**Resultado:** `openapi.yaml` válido (só warnings de estilo, sem erro) — mesmo comando
que o job "Contrato" do CI roda.

### 3. Migrations do banco, do zero

```bash
docker compose down -v      # remove os volumes, banco 100% limpo
docker compose up db backend
```

**Resultado:** as 6 migrations (changesets Liquibase) aplicaram sem erro num Postgres
recém-criado — `identidade-001`, `identidade-002` (seed), `identidade-003` (esta
branch), `comunidades-001` (2 changesets), `infraestrutura-001`.

### 4. Fluxo de ponta a ponta, contra o backend e o Postgres reais

Com um par de chaves RSA gerado de verdade (`openssl genpkey`/`openssl rsa -pubout`,
conforme `.env.example`) e o backend/banco rodando via `docker compose up db backend`:

```bash
# Cadastro
curl -X POST http://localhost:8080/auth/registro -H "Content-Type: application/json" \
  -d '{"nome":"Beatriz Souza","email":"beatriz@catolicasc.edu.br","senha":"minhaSenha1",
       "curso":"Engenharia de Software","dataNascimento":"2003-05-10"}'
# -> 201, usuário criado com emailConfirmado=false

# Login antes de confirmar (credencial correta)
curl -X POST http://localhost:8080/auth/login -H "Content-Type: application/json" \
  -d '{"email":"beatriz@catolicasc.edu.br","senha":"minhaSenha1"}'
# -> 401 {"error":{"code":"EMAIL_NAO_CONFIRMADO","message":"Confirme seu e-mail antes de
#     entrar. Reenviar confirmação"}}

# Login com senha errada (não pode revelar que o e-mail existe/está pendente)
curl -X POST http://localhost:8080/auth/login -H "Content-Type: application/json" \
  -d '{"email":"beatriz@catolicasc.edu.br","senha":"errada"}'
# -> 401 {"error":{"code":"CREDENCIAL_INVALIDA","message":"E-mail ou senha inválidos."}}

# Token extraído direto do banco (e-mail é só simulado, não sai da rede em dev)
docker exec unicatolica-db-1 psql -U pacext -d pacext -t -c \
  "SELECT token_confirmacao_email FROM usuario WHERE email='beatriz@catolicasc.edu.br';"

# Confirmação
curl -X POST http://localhost:8080/auth/confirmacao-email/<token>
# -> 204

# Login depois de confirmar
curl -X POST http://localhost:8080/auth/login -H "Content-Type: application/json" \
  -d '{"email":"beatriz@catolicasc.edu.br","senha":"minhaSenha1"}'
# -> 200 {"token": "eyJ..."} — JWT com claims sub/roles corretos

# Endpoint autenticado (Story 1.5, da main) com o token acima
curl http://localhost:8080/usuarios/me -H "Authorization: Bearer <token>"
# -> 200 {"id":2,"nome":"Beatriz Souza","email":"beatriz@catolicasc.edu.br","perfil":"ALUNO"}
```

Também confirmado: login do usuário semente (`identidade-002-seed-usuario-teste.xml`)
funcionando, e o log do auto-join (`AutoJoinCursoServiceImpl`) registrando corretamente
que nenhuma comunidade de curso foi encontrada ainda (esperado — Story 2.1, pré-criação
pelo administrador, está fora do escopo desta leva).

### 5. Frontend

```bash
cd frontend
node -e "process.argv=['node','ng','build']; require('./node_modules/@angular/cli/bin/bootstrap.js');"
```

**Resultado:** build limpo, 4 chunks gerados (`login`, `cadastro`, `confirmar-email`,
`feed`). Servido localmente (`ng serve`) e confirmado por `curl` que `/login`,
`/cadastro` e `/confirmar-email` respondem 200.

### 6. CI (GitHub Actions, PR #3)

Após os pontos 1–6 acima e a correção do bit de execução do `mvnw` (item 5 da lista de
bugs), os 3 checks obrigatórios do repositório passaram a verde:
`Frontend — build e testes (Angular)`, `Backend — build e testes (Quarkus)`,
`Contrato — valida openapi.yaml`.
