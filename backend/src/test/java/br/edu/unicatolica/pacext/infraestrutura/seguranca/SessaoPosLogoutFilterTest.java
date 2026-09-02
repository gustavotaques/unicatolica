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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

/**
 * Testa {@link SessaoPosLogoutFilter} isoladamente — critério de aceite da Story 1.6
 * (token emitido antes do logout do usuário deixa de ser aceito). A checagem de
 * estrutura/assinatura do JWT é responsabilidade de {@link JwtSecurityFilter}, testada
 * separadamente; aqui assume-se que ele já rodou e preencheu as propriedades da
 * requisição.
 */
class SessaoPosLogoutFilterTest {

    private final UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
    private final SessaoPosLogoutFilter filter = new SessaoPosLogoutFilter();

    @BeforeEach
    void setUp() {
        filter.usuarioRepository = usuarioRepository;
    }

    private ContainerRequestContext mockContext(String usuarioId, Long emitidoEmEpoch) {
        ContainerRequestContext requestContext = mock(ContainerRequestContext.class);
        when(requestContext.getProperty(JwtSecurityFilter.REQUEST_PROPERTY_USUARIO_ID)).thenReturn(usuarioId);
        when(requestContext.getProperty(JwtSecurityFilter.REQUEST_PROPERTY_EMITIDO_EM)).thenReturn(emitidoEmEpoch);
        return requestContext;
    }

    @Test
    void naoFazNadaQuandoRotaEraDaAllowlist() {
        ContainerRequestContext requestContext = mockContext(null, null);

        filter.filter(requestContext);

        verify(requestContext, never()).abortWith(any());
        verify(usuarioRepository, never()).findById(any());
    }

    @Test
    void naoFazNadaQuandoUsuarioNuncaDeslogou() {
        when(usuarioRepository.findById(42L)).thenReturn(usuario(null));
        ContainerRequestContext requestContext = mockContext("42", Instant.now().getEpochSecond());

        filter.filter(requestContext);

        verify(requestContext, never()).abortWith(any());
    }

    /** Fecha o critério de aceite da Story 1.6: token emitido antes do logout deve virar 401. */
    @Test
    void rejeitaTokenEmitidoAntesDoLogout() {
        when(usuarioRepository.findById(42L)).thenReturn(usuario(Instant.now().plusSeconds(60)));
        ContainerRequestContext requestContext = mockContext("42", Instant.now().getEpochSecond());

        filter.filter(requestContext);

        assertAbortedWith401(requestContext);
    }

    /** Logout de OUTRO usuário não pode afetar um token ainda válido do usuário atual. */
    @Test
    void aceitaTokenEmitidoAposLogoutDoProprioUsuario() {
        when(usuarioRepository.findById(42L)).thenReturn(usuario(Instant.now().minusSeconds(60)));
        ContainerRequestContext requestContext = mockContext("42", Instant.now().getEpochSecond());

        filter.filter(requestContext);

        verify(requestContext, never()).abortWith(any());
    }

    private Usuario usuario(Instant sessaoValidaDesde) {
        Usuario usuario = new Usuario();
        usuario.id = 42L;
        usuario.sessaoValidaDesde = sessaoValidaDesde;
        return usuario;
    }

    @SuppressWarnings("unchecked")
    private void assertAbortedWith401(ContainerRequestContext requestContext) {
        ArgumentCaptor<Response> captor = ArgumentCaptor.forClass(Response.class);
        verify(requestContext).abortWith(captor.capture());
        Assertions.assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), captor.getValue().getStatus());
        Assertions.assertFalse(captor.getValue().getEntity() == null);
    }
}
