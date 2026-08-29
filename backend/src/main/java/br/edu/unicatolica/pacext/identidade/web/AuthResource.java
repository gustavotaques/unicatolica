package br.edu.unicatolica.pacext.identidade.web;

import br.edu.unicatolica.pacext.identidade.aplicacao.AuthService;
import br.edu.unicatolica.pacext.identidade.dominio.CredenciaisInvalidasException;
import br.edu.unicatolica.pacext.identidade.dominio.EmailNaoConfirmadoException;
import br.edu.unicatolica.pacext.infraestrutura.web.ErroResponse;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/** Endpoint público de login (RF06/RF07) — allowlisted em {@code JwtSecurityFilter} (AD-2). */
@Path("/auth")
public class AuthResource {

    @Inject
    AuthService authService;

    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(LoginRequest request) {
        try {
            String token = authService.autenticar(request.email(), request.senha());
            return Response.ok(new LoginResponse(token)).build();
        } catch (CredenciaisInvalidasException e) {
            ErroResponse erro = ErroResponse.of("CREDENCIAL_INVALIDA", "E-mail ou senha inválidos.", null);
            return Response.status(Response.Status.UNAUTHORIZED).entity(erro).build();
        } catch (EmailNaoConfirmadoException e) {
            ErroResponse erro = ErroResponse.of("EMAIL_NAO_CONFIRMADO",
                    "Confirme seu e-mail antes de entrar. Reenviar confirmação", null);
            return Response.status(Response.Status.UNAUTHORIZED).entity(erro).build();
        }
    }
}
