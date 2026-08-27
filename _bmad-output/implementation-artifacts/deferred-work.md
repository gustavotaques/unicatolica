- source_spec: `_bmad-output/implementation-artifacts/spec-1-1-fundação-do-projeto-scaffold-e-infraestrutura.md`
  status: resolvido (Stories 1.2/1.3)
  summary: ~~Criar um `ExceptionMapper` global que traduza validação (400/422), 404, 409 e erros não tratados (500) para o envelope `ErroResponse`~~ — feito em `infraestrutura.web`: `ApiExceptionMapper` (409/404/403/422 via `ApiException`), `ValidacaoBeanExceptionMapper` (422 para `ConstraintViolationException`) e `ErroInternoExceptionMapper` (500, rede de segurança final).
  evidence: exercitado por `/auth/registro` e `/auth/confirmacao-email/*` (Stories 1.2/1.3) — ver `ApiExceptionMapperTest`.

- source_spec: `_bmad-output/implementation-artifacts/spec-1-1-fundação-do-projeto-scaffold-e-infraestrutura.md`
  summary: Adicionar teste de contrato em runtime (corpo real da resposta vs. schema de `openapi.yaml`, ex. rest-assured + validador JSON Schema) na esteira de CI, conforme AD-4.
  evidence: agora há endpoints reais (`/auth/registro`, `/auth/confirmacao-email/*`, Stories 1.2/1.3) para exercitar o teste, mas ele ainda não foi escrito — os testes desta leva são unitários com Mockito (sem Docker disponível neste ambiente, mesma limitação já registrada abaixo para `JwtSecurityFilterTest`), não testes de contrato via REST real.

- source_spec: `_bmad-output/implementation-artifacts/spec-1-1-fundação-do-projeto-scaffold-e-infraestrutura.md`
  summary: Adicionar fail-fast em produção (`%prod`) quando `JWT_ISSUER`/segredos obrigatórios não estiverem configurados, em vez de silenciosamente validar contra o valor padrão de desenvolvimento.
  evidence: `application.properties` usa `${JWT_ISSUER:https://pacext.unicatolica.edu.br}` como fallback também em produção; deploy real está fora do escopo desta história (scaffold local), mas é um risco antes de qualquer deploy no Render.

- source_spec: `_bmad-output/implementation-artifacts/spec-1-1-fundação-do-projeto-scaffold-e-infraestrutura.md`
  summary: Adicionar rate limiting/proteção contra força bruta em `/auth/login` e `/auth/registro`, que ficam fora do filtro JWT global (allowlist `@PermitAll`).
  evidence: RNF04 (baseline OWASP ASVS 4.0.3) se aplica à autenticação; `/auth/registro` e `/auth/confirmacao-email/*` já existem (Stories 1.2/1.3) sem rate limiting — `/auth/login` chega na Story 1.4. Risco cresce a cada rota pública nova adicionada à allowlist sem essa proteção.

- source_spec: `_bmad-output/implementation-artifacts/spec-1-1-fundação-do-projeto-scaffold-e-infraestrutura.md`
  summary: Documentar explicitamente (comentário no código) que o par de chaves RSA commitado é só para dev/teste, e definir a estratégia de provisionamento/rotação de chave real de produção (localização via variável de ambiente, não classpath fixo).
  evidence: `mp.jwt.verify.publickey.location=publicKey.pem` aponta hoje para um recurso fixo no classpath sem indicar que é descartável; produção real está fora do escopo desta história (scaffold local).

- source_spec: `_bmad-output/implementation-artifacts/spec-1-1-fundação-do-projeto-scaffold-e-infraestrutura.md`
  summary: Substituir/complementar `ApplicationPropertiesJwtConfigTest` (comparação textual de `application.properties`) por um `@QuarkusTest`/`@QuarkusIntegrationTest` real que valide o `JwtSecurityFilter` através do runtime completo do Quarkus com CDI e datasource reais.
  evidence: Nem o ambiente de implementação nem o de revisão desta história tinham Docker disponível para os Dev Services de Postgres; o CI já provisiona um serviço Postgres real e poderia rodar esse teste de integração quando alguém tiver acesso a um ambiente com Docker para autorá-lo com segurança.

- source_spec: Story 1.2 (Cadastro de aluno com e-mail institucional)
  summary: Domínio institucional (RF01.3) e política de senha (RF04) foram implementados com valores de exemplo (`identidade.email.dominio-institucional=catolicasc.edu.br`, mínimo 8 caracteres com letra e dígito) por não estarem definidos em nenhum artefato de planejamento.
  evidence: ver `[DECISÃO A CONFIRMAR]` em `application.properties` e `CadastroService` — time precisa ratificar o domínio real de e-mail institucional e a política de senha antes de qualquer deploy/demo.

- source_spec: Story 1.2/2.3 (Cadastro / Auto-join à comunidade de curso)
  summary: `usuario.curso` é texto livre casado em runtime contra `comunidade.nome` (case-insensitive) — sem validação contra uma lista fechada de cursos da instituição no momento do cadastro.
  evidence: ver decisão "curso como texto solto em usuario" em `modelo-dados-semana-1.md`; um erro de digitação no cadastro (Story 1.2) resulta em auto-join silenciosamente sem efeito (Story 2.3 loga warning e segue). Alternativa mais rígida (dropdown de cursos válidos) sugerida no mesmo documento.

- source_spec: Story 2.3 (Auto-join à comunidade de curso)
  summary: O auto-join não bloqueia nem falha quando a comunidade de curso ainda não existe (Story 2.1, pré-criação pelo administrador, fora desta implementação) — só loga um warning e segue sem associar o aluno.
  evidence: `AutoJoinCursoServiceImpl` — até a Story 2.1 ser implementada e todas as comunidades de curso pré-criadas, cadastros com curso sem comunidade correspondente ficam sem associação. Revisitar quando 2.1 estiver pronta.

- source_spec: Story 2.3 (Auto-join à comunidade de curso)
  summary: O toast único "Você já faz parte de {comunidade} 🎓" na próxima visita ao Início (critério de aceite da Story 2.3) não foi implementado — depende da tela Início, que pertence ao Epic 14 (Design System)/telas ainda não construídas.
  evidence: esta leva cobriu só o mecanismo de persistência do auto-join no backend; a exibição do toast é responsabilidade do frontend, fora do escopo desta implementação.
