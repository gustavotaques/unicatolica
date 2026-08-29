package br.edu.unicatolica.pacext.identidade.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import br.edu.unicatolica.pacext.identidade.aplicacao.AuthService;
import br.edu.unicatolica.pacext.identidade.dominio.CredenciaisInvalidasException;
import br.edu.unicatolica.pacext.identidade.dominio.EmailNaoConfirmadoException;
import br.edu.unicatolica.pacext.infraestrutura.web.ErroResponse;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * Testa {@link AuthResource} chamando o método diretamente (sem subir o runtime JAX-RS
 * completo — mesmo padrão de {@code JwtSecurityFilterTest}, sem Docker disponível).
 */
class AuthResourceTest {

    private final AuthService authService = mock(AuthService.class);
    private final AuthResource resource = new AuthResource();

    AuthResourceTest() {
        resource.authService = authService;
    }

    @Test
    void retorna200ComTokenParaCredenciaisValidas() {
        when(authService.autenticar("aluno@catolicasc.edu.br", "Senha123!")).thenReturn("token-jwt-emitido");

        Response response = resource.login(new LoginRequest("aluno@catolicasc.edu.br", "Senha123!"));

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        LoginResponse corpo = (LoginResponse) response.getEntity();
        assertEquals("token-jwt-emitido", corpo.token());
    }

    @Test
    void retorna401ComEnvelopeDeErroParaCredenciaisInvalidas() {
        when(authService.autenticar(Mockito.anyString(), Mockito.anyString()))
                .thenThrow(new CredenciaisInvalidasException());

        Response response = resource.login(new LoginRequest("aluno@catolicasc.edu.br", "senha-errada"));

        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
        ErroResponse corpo = (ErroResponse) response.getEntity();
        assertNotNull(corpo.error());
        assertEquals("CREDENCIAL_INVALIDA", corpo.error().code());
    }

    @Test
    void retorna401ComMensagemDistintaParaEmailNaoConfirmado() {
        when(authService.autenticar(Mockito.anyString(), Mockito.anyString()))
                .thenThrow(new EmailNaoConfirmadoException());

        Response response = resource.login(new LoginRequest("aluno@catolicasc.edu.br", "Senha123!"));

        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
        ErroResponse corpo = (ErroResponse) response.getEntity();
        assertEquals("EMAIL_NAO_CONFIRMADO", corpo.error().code());
        assertEquals("Confirme seu e-mail antes de entrar. Reenviar confirmação", corpo.error().message());
    }
}
