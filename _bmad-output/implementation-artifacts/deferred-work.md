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
