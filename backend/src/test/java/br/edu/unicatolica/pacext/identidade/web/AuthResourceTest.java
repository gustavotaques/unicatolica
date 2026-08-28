package br.edu.unicatolica.pacext.identidade.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import br.edu.unicatolica.pacext.identidade.AuthService;
import br.edu.unicatolica.pacext.infraestrutura.web.ApiException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * Testa {@link AuthResource} chamando o método diretamente (sem subir o runtime JAX-RS
 * completo — mesmo padrão de {@code JwtSecurityFilterTest}, sem Docker disponível).
 * Credenciais inválidas propagam {@link ApiException} — quem traduz para 401 é o
 * {@code ApiExceptionMapper} global, não este teste (testado em {@code ApiExceptionMapperTest}).
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
    void propagaApiExceptionParaCredenciaisInvalidas() {
        when(authService.autenticar(Mockito.anyString(), Mockito.anyString()))
                .thenThrow(ApiException.naoAutenticado("CREDENCIAL_INVALIDA", "E-mail ou senha inválidos.", null));

        ApiException erro = assertThrows(ApiException.class,
                () -> resource.login(new LoginRequest("aluno@catolicasc.edu.br", "senha-errada")));

        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), erro.getStatus());
        assertEquals("CREDENCIAL_INVALIDA", erro.getCode());
    }
}
