package br.edu.unicatolica.pacext.infraestrutura.seguranca;

import br.edu.unicatolica.pacext.identidade.dominio.Usuario;
import br.edu.unicatolica.pacext.identidade.dominio.UsuarioRepository;
import br.edu.unicatolica.pacext.infraestrutura.web.ErroResponse;
import io.smallrye.jwt.auth.principal.JWTParser;
import io.smallrye.jwt.auth.principal.ParseException;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;
import java.time.Instant;
import java.util.Set;
import org.eclipse.microprofile.jwt.JsonWebToken;

/**
 * Filtro de segurança JWT global (AD-2 da Architecture Spine).
 *
 * <p>É o único ponto do backend que valida autenticação — intercepta toda requisição
 * antes dela alcançar qualquer módulo de domínio, exceto os caminhos da allowlist
 * {@code @PermitAll} abaixo, que vive só aqui, nunca espalhada por módulo. Transporte
 * exclusivamente via header {@code Authorization: Bearer} — nunca cookie. Claims fixos
 * exigidos no token: {@code sub} (id do usuário) e {@code roles} (perfis globais, RF13);
 * nenhum módulo inventa nome de claim próprio.</p>
 *
 * <p>Autorização fina por perfil (RF13) é responsabilidade de cada módulo consumidor,
 * não deste filtro — aqui só se garante que o token existe, é válido e carrega as duas
 * claims obrigatórias, disponibilizando-as via propriedades da requisição para uso
 * downstream ({@link #REQUEST_PROPERTY_USUARIO_ID} e {@link #REQUEST_PROPERTY_ROLES}).</p>
 */
@Provider
@PreMatching
@Priority(Priorities.AUTHENTICATION)
public class JwtSecurityFilter implements ContainerRequestFilter {

    /** Allowlist única de endpoints públicos — não pertence a nenhum módulo (AD-2). */
    private static final Set<String> ALLOWLIST = Set.of(
            "/auth/login",
            "/auth/registro",
            "/auth/confirmacao-email",
            "/q/health",
            "/q/health/live",
            "/q/health/ready");

    public static final String REQUEST_PROPERTY_USUARIO_ID = "pacext.usuarioId";
    public static final String REQUEST_PROPERTY_ROLES = "pacext.roles";

    private static final String BEARER_PREFIX = "Bearer ";

    @Inject
    JWTParser jwtParser;

    @Inject
    UsuarioRepository usuarioRepository;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        // Preflight de CORS nunca carrega Authorization — deixa passar para o handler de CORS central (AD-2/AD-6).
        if (HttpMethod.OPTIONS.equals(requestContext.getMethod())) {
            return;
        }

        String rawPath = requestContext.getUriInfo().getPath();
        String path = rawPath == null ? "/" : (rawPath.startsWith("/") ? rawPath : "/" + rawPath);
        if (isAllowlisted(path)) {
            return;
        }

        String authorizationHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
        if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            abort(requestContext, "Requisição sem header Authorization: Bearer <token>.");
            return;
        }

        String token = authorizationHeader.substring(BEARER_PREFIX.length()).trim();
        if (token.isEmpty()) {
            abort(requestContext, "Token JWT ausente após o prefixo Bearer.");
            return;
        }

        try {
            JsonWebToken jwt = jwtParser.parse(token);
            String usuarioId = jwt.getSubject();
            Set<String> roles = jwt.getGroups();

            if (usuarioId == null || usuarioId.isBlank()) {
                abort(requestContext, "Token não contém a claim obrigatória 'sub'.");
                return;
            }
            if (roles == null || roles.isEmpty()) {
                abort(requestContext, "Token não contém a claim obrigatória 'roles'.");
                return;
            }
            if (tokenEmitidoAntesDoLogout(usuarioId, jwt)) {
                abort(requestContext, "Token JWT inválido ou expirado.");
                return;
            }

            requestContext.setProperty(REQUEST_PROPERTY_USUARIO_ID, usuarioId);
            requestContext.setProperty(REQUEST_PROPERTY_ROLES, roles);
        } catch (ParseException | RuntimeException e) {
            abort(requestContext, "Token JWT inválido ou expirado.");
        }
    }

    /**
     * Logout (Story 1.6, RF10/RF11): token continua criptograficamente válido até o
     * {@code exp} natural, mas deixa de ser aceito se foi emitido ({@code iat}) antes do
     * último logout do usuário — {@code sessaoValidaDesde} é gravado por
     * {@code AuthService.logout}. {@code null} nesse campo (usuário nunca deslogou) não
     * restringe nada.
     */
    private boolean tokenEmitidoAntesDoLogout(String usuarioId, JsonWebToken jwt) {
        Usuario usuario = usuarioRepository.findById(Long.valueOf(usuarioId));
        if (usuario == null || usuario.sessaoValidaDesde == null) {
            return false;
        }
        Instant emitidoEm = Instant.ofEpochSecond(jwt.getIssuedAtTime());
        return emitidoEm.isBefore(usuario.sessaoValidaDesde);
    }

    /**
     * Match exato de segmento de path — {@code startsWith} puro deixaria rotas futuras
     * como {@code /auth/login-tentativas} ou {@code /q/healthz} escaparem indevidamente
     * da autenticação por coincidirem com o prefixo de uma entrada da allowlist.
     */
    private boolean isAllowlisted(String path) {
        return ALLOWLIST.stream().anyMatch(allowed -> path.equals(allowed) || path.startsWith(allowed + "/"));
    }

    private void abort(ContainerRequestContext requestContext, String detalhes) {
        ErroResponse erro = ErroResponse.of("NAO_AUTENTICADO", "Autenticação necessária.", detalhes);
        requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                .type(MediaType.APPLICATION_JSON)
                .entity(erro)
                .build());
    }
}
