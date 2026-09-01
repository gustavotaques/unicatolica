package br.edu.unicatolica.pacext.infraestrutura.seguranca;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.edu.unicatolica.pacext.identidade.dominio.Usuario;
import br.edu.unicatolica.pacext.identidade.dominio.UsuarioRepository;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Response;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

/**
 * Testa {@link SessaoInvalidadaFilter} isoladamente (mesmo padrão de {@code
 * JwtSecurityFilterTest} — sem subir o runtime completo do Quarkus), cobrindo os critérios
 * de aceite da Story 1.6 (RF10/RF11) que antes viviam em {@code JwtSecurityFilterTest}
 * quando essa checagem ainda rodava dentro de {@link JwtSecurityFilter} (defeito D1).
 */
class SessaoInvalidadaFilterTest {

    private final UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
    private final SessaoInvalidadaFilter filter = new SessaoInvalidadaFilter();

    SessaoInvalidadaFilterTest() {
        filter.usuarioRepository = usuarioRepository;
    }

    @BeforeEach
    void resetUsuarioRepository() {
        when(usuarioRepository.findById(any())).thenReturn(null);
    }

    private ContainerRequestContext mockContext(String usuarioId, Instant emitidoEm) {
        ContainerRequestContext requestContext = mock(ContainerRequestContext.class);
        when(requestContext.getProperty(JwtSecurityFilter.REQUEST_PROPERTY_USUARIO_ID)).thenReturn(usuarioId);
        when(requestContext.getProperty(JwtSecurityFilter.REQUEST_PROPERTY_EMITIDO_EM)).thenReturn(emitidoEm);
        return requestContext;
    }

    @Test
    void naoFazNadaQuandoUsuarioIdNaoFoiPopulado() {
        ContainerRequestContext requestContext = mockContext(null, null);

        filter.filter(requestContext);

        verify(requestContext, never()).abortWith(any());
        verify(usuarioRepository, never()).findById(any());
    }

    @Test
    void naoFazNadaQuandoUsuarioNuncaDeslogou() {
        ContainerRequestContext requestContext = mockContext("42", Instant.now());
        Usuario usuario = new Usuario();
        usuario.id = 42L;
        usuario.sessaoValidaDesde = null;
        when(usuarioRepository.findById(42L)).thenReturn(usuario);

        filter.filter(requestContext);

        verify(requestContext, never()).abortWith(any());
    }

    /** Fecha o critério de aceite da Story 1.6: token emitido antes do logout deve virar 401. */
    @Test
    void rejeitaTokenEmitidoAntesDoLogout() {
        Usuario usuario = new Usuario();
        usuario.id = 42L;
        usuario.sessaoValidaDesde = Instant.now().plusSeconds(60);
        when(usuarioRepository.findById(42L)).thenReturn(usuario);
        ContainerRequestContext requestContext = mockContext("42", Instant.now());

        filter.filter(requestContext);

        ArgumentCaptor<Response> captor = ArgumentCaptor.forClass(Response.class);
        verify(requestContext).abortWith(captor.capture());
        org.junit.jupiter.api.Assertions.assertEquals(
                Response.Status.UNAUTHORIZED.getStatusCode(), captor.getValue().getStatus());
    }

    /** Logout de OUTRO usuário não pode afetar um token ainda válido do usuário atual. */
    @Test
    void aceitaTokenEmitidoAposLogoutDoProprioUsuario() {
        Usuario usuario = new Usuario();
        usuario.id = 42L;
        usuario.sessaoValidaDesde = Instant.now().minusSeconds(60);
        when(usuarioRepository.findById(42L)).thenReturn(usuario);
        ContainerRequestContext requestContext = mockContext("42", Instant.now());

        filter.filter(requestContext);

        verify(requestContext, never()).abortWith(any());
    }

    /**
     * Corrida de mesmo segundo: o claim {@code iat} do JWT e {@code sessaoValidaDesde} (ver
     * {@code AuthService.logout} / {@code AuthServiceTest}) só têm precisão de segundo. Um
     * token cujo {@code iat} truncado coincide com o instante do logout foi necessariamente
     * emitido antes dele, então precisa ser rejeitado (comparação usa {@code <=}, não
     * {@code <}) para que o logout invalide o token imediatamente (RF10/RF11).
     */
    @Test
    void rejeitaTokenEmitidoNoMesmoSegundoDoLogout() {
        Instant mesmoSegundo = Instant.now().truncatedTo(java.time.temporal.ChronoUnit.SECONDS);
        Usuario usuario = new Usuario();
        usuario.id = 42L;
        usuario.sessaoValidaDesde = mesmoSegundo;
        when(usuarioRepository.findById(42L)).thenReturn(usuario);
        ContainerRequestContext requestContext = mockContext("42", mesmoSegundo);

        filter.filter(requestContext);

        ArgumentCaptor<Response> captor = ArgumentCaptor.forClass(Response.class);
        verify(requestContext).abortWith(captor.capture());
        org.junit.jupiter.api.Assertions.assertEquals(
                Response.Status.UNAUTHORIZED.getStatusCode(), captor.getValue().getStatus());
    }
}
