package br.edu.unicatolica.pacext.identidade.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import br.edu.unicatolica.pacext.identidade.aplicacao.UsuarioService;
import br.edu.unicatolica.pacext.identidade.dominio.Usuario;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

/**
 * Testa {@link UsuarioResource} chamando os métodos diretamente (sem subir o runtime
 * JAX-RS completo). O Resource só converte o que o {@link UsuarioService} devolve; a regra
 * de perfil e o 404 são testados em {@code UsuarioServiceTest}, e o envelope HTTP real em
 * {@code ErroEnvelopeFluxoTest}.
 */
class UsuarioResourceTest {

    private final UsuarioService usuarioService = mock(UsuarioService.class);
    private final UsuarioResource resource = new UsuarioResource();

    UsuarioResourceTest() {
        resource.usuarioService = usuarioService;
    }

    private static Usuario usuario(Long id, String nome, String email, String perfil) {
        Usuario usuario = new Usuario();
        usuario.id = id;
        usuario.nome = nome;
        usuario.email = email;
        usuario.perfil = perfil;
        return usuario;
    }

    @Test
    void meRetorna200ComOProprioUsuario() {
        when(usuarioService.buscarProprio()).thenReturn(usuario(1L, "Ana Aluna", "ana@catolicasc.edu.br", "ALUNO"));

        Response response = resource.me();

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        UsuarioResponse corpo = (UsuarioResponse) response.getEntity();
        assertEquals(1L, corpo.id());
        assertEquals("Ana Aluna", corpo.nome());
        assertEquals("ana@catolicasc.edu.br", corpo.email());
        assertEquals("ALUNO", corpo.perfil());
    }

    @Test
    void porIdRetorna200ComOUsuarioPedido() {
        when(usuarioService.buscarPorId(7L)).thenReturn(usuario(7L, "Beto Moderador", "beto@catolicasc.edu.br", "ALUNO"));

        Response response = resource.porId(7L);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        UsuarioResponse corpo = (UsuarioResponse) response.getEntity();
        assertEquals(7L, corpo.id());
        assertEquals("Beto Moderador", corpo.nome());
    }
}
