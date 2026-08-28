package br.edu.unicatolica.pacext.identidade.web;

import br.edu.unicatolica.pacext.identidade.dominio.AcessoNegadoException;
import br.edu.unicatolica.pacext.identidade.dominio.Usuario;
import br.edu.unicatolica.pacext.identidade.dominio.UsuarioRepository;
import br.edu.unicatolica.pacext.identidade.infraestrutura.UsuarioAutenticado;
import br.edu.unicatolica.pacext.infraestrutura.web.ErroResponse;
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
 * (decidida aqui, no módulo, nunca no filtro) funcionam juntas.
 */
@Path("/usuarios")
public class UsuarioResource {

    @Inject
    UsuarioRepository usuarioRepository;

    @Inject
    UsuarioAutenticado usuarioAutenticado;

    /** Qualquer perfil autenticado pode ver o próprio perfil. */
    @GET
    @Path("/me")
    @Produces(MediaType.APPLICATION_JSON)
    public Response me() {
        Usuario usuario = usuarioRepository.findById(usuarioAutenticado.id());
        return respostaOuNaoEncontrado(usuario);
    }

    /**
     * Somente perfil {@code MODERADOR} pode ver o perfil de outro usuário. Recusa via
     * {@link AcessoNegadoException} (403) — existência do usuário não precisa ficar
     * oculta (AD-5).
     */
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response porId(@PathParam("id") Long id) {
        if (!usuarioAutenticado.possuiPerfil("MODERADOR")) {
            throw new AcessoNegadoException();
        }
        Usuario usuario = usuarioRepository.findById(id);
        return respostaOuNaoEncontrado(usuario);
    }

    private Response respostaOuNaoEncontrado(Usuario usuario) {
        if (usuario == null) {
            ErroResponse erro = ErroResponse.of("RECURSO_NAO_ENCONTRADO", "Usuário não encontrado.", null);
            return Response.status(Response.Status.NOT_FOUND).entity(erro).build();
        }
        return Response.ok(UsuarioResponse.de(usuario)).build();
    }
}
