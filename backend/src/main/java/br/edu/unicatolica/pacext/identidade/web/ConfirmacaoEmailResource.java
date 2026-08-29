package br.edu.unicatolica.pacext.identidade.web;

import br.edu.unicatolica.pacext.identidade.aplicacao.ConfirmacaoEmailService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * {@code /auth/confirmacao-email/*} — Story 1.3. Rotas públicas, agrupadas sob o prefixo
 * {@code /auth/confirmacao-email} da allowlist do {@code JwtSecurityFilter} (AD-2).
 */
@Path("/auth/confirmacao-email")
public class ConfirmacaoEmailResource {

    @Inject
    ConfirmacaoEmailService confirmacaoEmailService;

    /** Chamado pela tela do frontend ao extrair o token do link recebido por e-mail. */
    @POST
    @Path("/{token}")
    public Response confirmar(@PathParam("token") String token) {
        confirmacaoEmailService.confirmar(token);
        return Response.noContent().build();
    }

    /**
     * Reenvio da confirmação (tela "Verifique seu e-mail" e bloqueio de login com e-mail
     * não confirmado, Story 1.3). Sempre responde 202 — nunca revela se o e-mail existe.
     */
    @POST
    @Path("/reenvio")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response reenviar(@Valid ReenvioConfirmacaoRequest request) {
        confirmacaoEmailService.reenviarConfirmacao(request.email());
        return Response.accepted().build();
    }
}
