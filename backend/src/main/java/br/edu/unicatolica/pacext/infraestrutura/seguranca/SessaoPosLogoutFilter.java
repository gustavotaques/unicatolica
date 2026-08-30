package br.edu.unicatolica.pacext.infraestrutura.seguranca;

import br.edu.unicatolica.pacext.identidade.dominio.Usuario;
import br.edu.unicatolica.pacext.identidade.dominio.UsuarioRepository;
import br.edu.unicatolica.pacext.infraestrutura.web.ErroResponse;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import java.time.Instant;

/**
 * Logout (Story 1.6, RF10/RF11) — token continua criptograficamente válido até o
 * {@code exp} natural, mas deixa de ser aceito se foi emitido ({@code iat}) antes do
 * último logout do usuário ({@code sessaoValidaDesde}, gravado por
 * {@code AuthService.logout}). {@code null} nesse campo (usuário nunca deslogou, ou
 * deslogou antes deste token) não restringe nada.
 *
 * <p><b>Por que é um filtro separado, não faz parte de {@link JwtSecurityFilter}:</b> essa
 * checagem precisa ler {@code usuario} no banco (bloqueante, JDBC/Hibernate ORM
 * clássico). {@link JwtSecurityFilter} é {@code @PreMatching} e roda sempre na I/O thread
 * do Vert.x — uma consulta bloqueante ali derruba a requisição com
 * {@code BlockingOperationNotAllowedException} (visto ao vivo via Docker, nenhum teste
 * Mockito percebe isso). Este filtro, por não ser {@code @PreMatching}, roda depois do
 * roteamento e herda o modo de despacho do recurso alvo — como todo recurso deste backend
 * é bloqueante, ele já executa numa worker thread, onde a consulta é segura.</p>
 *
 * <p>Só age quando {@link JwtSecurityFilter#REQUEST_PROPERTY_USUARIO_ID} está presente —
 * ausente significa rota da allowlist (nunca chegou a autenticar) ou requisição já
 * abortada pelo filtro anterior (que nunca chega até aqui, o abort corta a cadeia).</p>
 */
@Provider
@Priority(Priorities.AUTHENTICATION + 1)
public class SessaoPosLogoutFilter implements ContainerRequestFilter {

    @Inject
    UsuarioRepository usuarioRepository;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        Object usuarioIdProperty = requestContext.getProperty(JwtSecurityFilter.REQUEST_PROPERTY_USUARIO_ID);
        if (usuarioIdProperty == null) {
            return;
        }

        Long usuarioId = Long.valueOf((String) usuarioIdProperty);
        Long emitidoEmEpoch = (Long) requestContext.getProperty(JwtSecurityFilter.REQUEST_PROPERTY_EMITIDO_EM);
        Usuario usuario = usuarioRepository.findById(usuarioId);
        if (usuario == null || usuario.sessaoValidaDesde == null) {
            return;
        }

        Instant emitidoEm = Instant.ofEpochSecond(emitidoEmEpoch);
        if (emitidoEm.isBefore(usuario.sessaoValidaDesde)) {
            ErroResponse erro = ErroResponse.of("NAO_AUTENTICADO", "Autenticação necessária.",
                    "Token JWT inválido ou expirado.");
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(erro)
                    .build());
        }
    }
}
