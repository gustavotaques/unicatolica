package br.edu.unicatolica.pacext.identidade.web;

import br.edu.unicatolica.pacext.identidade.aplicacao.UsuarioService;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * Primeiros endpoints autenticados do sistema (Story 1.5, RF12/RF13) — prova end-to-end
 * de que autenticação (AD-2, {@code JwtSecurityFilter}) e autorização fina por perfil
 * (decidida no módulo, em {@link UsuarioService}, nunca no filtro) funcionam juntas.
 */
@Path("/usuarios")
public class UsuarioResource {

    @Inject
    UsuarioService usuarioService;

    @GET
    @Path("/me")
    @Produces(MediaType.APPLICATION_JSON)
    public Response me() {
        return Response.ok(UsuarioResponse.de(usuarioService.buscarProprio())).build();
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response porId(@PathParam("id") Long id) {
        return Response.ok(UsuarioResponse.de(usuarioService.buscarPorId(id))).build();
    }
}
