package br.edu.unicatolica.pacext.infraestrutura.seguranca;

import br.edu.unicatolica.pacext.identidade.dominio.Usuario;
import br.edu.unicatolica.pacext.identidade.dominio.UsuarioRepository;
import br.edu.unicatolica.pacext.infraestrutura.web.ErroResponse;
import io.smallrye.common.annotation.Blocking;
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
 * Segunda etapa da autenticação (Story 1.6, RF10/RF11) — invalida token emitido antes do
 * último logout do usuário. Separada de {@link JwtSecurityFilter} só por restrição
 * técnica: esta checagem consulta o banco (Hibernate ORM/Panache, bloqueante), e um filtro
 * {@code @PreMatching} roda sempre na thread de I/O do Vert.x, onde {@code @Blocking} não
 * é honrado (defeito D1, comprovado por {@code AutenticacaoFluxoTest}) — um filtro comum
 * (pós-roteamento) como este já suporta {@code @Blocking} normalmente.
 *
 * <p>Roda só quando {@link JwtSecurityFilter} já validou o token e populou {@link
 * JwtSecurityFilter#REQUEST_PROPERTY_USUARIO_ID}; ausência dessa propriedade significa rota
 * pública (allowlist) ou requisição já abortada pelo filtro anterior — nada a fazer aqui.</p>
 */
@Provider
@Priority(Priorities.AUTHENTICATION + 1)
@Blocking
public class SessaoInvalidadaFilter implements ContainerRequestFilter {

    @Inject
    UsuarioRepository usuarioRepository;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        Object usuarioIdProperty = requestContext.getProperty(JwtSecurityFilter.REQUEST_PROPERTY_USUARIO_ID);
        if (usuarioIdProperty == null) {
            return;
        }

        Instant emitidoEm = (Instant) requestContext.getProperty(JwtSecurityFilter.REQUEST_PROPERTY_EMITIDO_EM);
        Usuario usuario = usuarioRepository.findById(Long.valueOf((String) usuarioIdProperty));
        if (usuario != null && usuario.sessaoValidaDesde != null && emitidoEm.isBefore(usuario.sessaoValidaDesde)) {
            ErroResponse erro = ErroResponse.of("NAO_AUTENTICADO", "Autenticação necessária.", "Token JWT inválido ou expirado.");
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(erro)
                    .build());
        }
    }
}
