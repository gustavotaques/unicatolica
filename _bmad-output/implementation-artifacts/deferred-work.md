- source_spec: `_bmad-output/implementation-artifacts/spec-1-1-fundação-do-projeto-scaffold-e-infraestrutura.md`
  summary: Criar um `ExceptionMapper` global que traduza validação (400/422), 404, 409 e erros não tratados (500) para o envelope `ErroResponse`, hoje só implementado para o caso 401 do filtro JWT.
  evidence: AD-5 exige o envelope `{"error":{"code","message","details"}}` para toda resposta de erro de todo endpoint REST, mas nenhum endpoint real existe ainda nesta história para exercitar os demais status HTTP — relevante a partir da Story 1.2.

- source_spec: `_bmad-output/implementation-artifacts/spec-1-1-fundação-do-projeto-scaffold-e-infraestrutura.md`
  summary: Adicionar teste de contrato em runtime (corpo real da resposta vs. schema de `openapi.yaml`, ex. rest-assured + validador JSON Schema) na esteira de CI, conforme AD-4.
  evidence: `openapi.yaml` ainda não tem nenhum path definido nesta história (scaffold apenas); o teste de contrato só faz sentido a partir do primeiro endpoint real implementado.

- source_spec: `_bmad-output/implementation-artifacts/spec-1-1-fundação-do-projeto-scaffold-e-infraestrutura.md`
  summary: Adicionar fail-fast em produção (`%prod`) quando `JWT_ISSUER`/segredos obrigatórios não estiverem configurados, em vez de silenciosamente validar contra o valor padrão de desenvolvimento.
  evidence: `application.properties` usa `${JWT_ISSUER:https://pacext.unicatolica.edu.br}` como fallback também em produção; deploy real está fora do escopo desta história (scaffold local), mas é um risco antes de qualquer deploy no Render.

- source_spec: `_bmad-output/implementation-artifacts/spec-1-1-fundação-do-projeto-scaffold-e-infraestrutura.md`
  summary: Adicionar rate limiting/proteção contra força bruta em `/auth/login` e `/auth/registro`, que ficam fora do filtro JWT global (allowlist `@PermitAll`).
  evidence: RNF04 (baseline OWASP ASVS 4.0.3) se aplica à autenticação; esses endpoints ainda não existem nesta história — implementação real chega na Story 1.4 (login).

- source_spec: `_bmad-output/implementation-artifacts/spec-1-1-fundação-do-projeto-scaffold-e-infraestrutura.md`
  summary: Documentar explicitamente (comentário no código) que o par de chaves RSA commitado é só para dev/teste, e definir a estratégia de provisionamento/rotação de chave real de produção (localização via variável de ambiente, não classpath fixo).
  evidence: `mp.jwt.verify.publickey.location=publicKey.pem` aponta hoje para um recurso fixo no classpath sem indicar que é descartável; produção real está fora do escopo desta história (scaffold local).

- source_spec: `_bmad-output/implementation-artifacts/spec-1-1-fundação-do-projeto-scaffold-e-infraestrutura.md`
  summary: Substituir/complementar `ApplicationPropertiesJwtConfigTest` (comparação textual de `application.properties`) por um `@QuarkusTest`/`@QuarkusIntegrationTest` real que valide o `JwtSecurityFilter` através do runtime completo do Quarkus com CDI e datasource reais.
  evidence: Nem o ambiente de implementação nem o de revisão desta história tinham Docker disponível para os Dev Services de Postgres; o CI já provisiona um serviço Postgres real e poderia rodar esse teste de integração quando alguém tiver acesso a um ambiente com Docker para autorá-lo com segurança.

- source_spec: `_bmad-output/implementation-artifacts/spec-1-5-bloqueio-de-acesso-sem-autenticação-e-restrição-por-perfil.md`
  summary: `JwtSecurityFilter` grava `sub`/`roles` em properties do `ContainerRequestContext` (`pacext.usuarioId`, `pacext.roles`) especificamente para leitura downstream, mas o novo `UsuarioAutenticado` (Story 1.5) as ignora e lê o `JsonWebToken` CDI diretamente — reconciliar os dois mecanismos (remover as properties não lidas, ou documentar por que dois caminhos de leitura de identidade convivem) antes que um terceiro módulo escolha um dos dois por acaso.
  evidence: Revisão adversarial (blind-hunter) apontou as properties como código morto após a Story 1.5; confirmado por busca — nenhum código em `src/main` lê `REQUEST_PROPERTY_USUARIO_ID`/`REQUEST_PROPERTY_ROLES` além do próprio filtro que as escreve.

- source_spec: `_bmad-output/implementation-artifacts/spec-1-5-bloqueio-de-acesso-sem-autenticação-e-restrição-por-perfil.md`
  summary: Registrar em `log_auditoria` (`AuditoriaService`, AD-11) quando um `MODERADOR` consulta o perfil de outro usuário via `GET /usuarios/{id}`, já que é a primeira ação real de um perfil sobre dados de outro usuário no sistema.
  evidence: AD-11 cobre "alteração administrativa" como caso de auditoria; leitura de PII por um perfil elevado sobre outro usuário é adjacente, mas a spec 1.5 não pediu esse registro explicitamente — revisão adversarial (blind-hunter) sinalizou a ausência.

- source_spec: `_bmad-output/implementation-artifacts/spec-1-5-bloqueio-de-acesso-sem-autenticação-e-restrição-por-perfil.md`
  summary: Adicionar um `ExceptionMapper`/`ParamConverterProvider` compartilhado para `@PathParam` `Long` malformado (ex. `GET /usuarios/abc`) que devolva 400 no envelope `ErroResponse` padrão, em vez do erro default do RESTEasy Reactive.
  evidence: `edge-case-hunter` apontou que `GET /usuarios/{id}` com `id` não numérico hoje escapa do envelope `ErroResponse`; é um gap cross-cutting (qualquer endpoint futuro com `Long`/`Integer` no path herda o mesmo problema), não específico desta história.
