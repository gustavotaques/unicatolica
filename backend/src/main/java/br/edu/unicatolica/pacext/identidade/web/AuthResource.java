package br.edu.unicatolica.pacext.identidade.web;

import br.edu.unicatolica.pacext.identidade.AuthService;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * {@code POST /auth/login} — Story 1.4. Rota pública (allowlist do
 * {@code JwtSecurityFilter}, AD-2). Credenciais inválidas viram {@code ApiException} em
 * {@link AuthService}, traduzida pelo {@code ApiExceptionMapper} global — nenhum
 * try/catch manual aqui.
 */
@Path("/auth/login")
public class AuthResource {

    @Inject
    AuthService authService;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(LoginRequest request) {
        String token = authService.autenticar(request.email(), request.senha());
        return Response.ok(new LoginResponse(token)).build();
    }
}
