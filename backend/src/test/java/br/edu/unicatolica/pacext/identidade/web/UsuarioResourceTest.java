package br.edu.unicatolica.pacext.identidade.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import br.edu.unicatolica.pacext.identidade.dominio.AcessoNegadoException;
import br.edu.unicatolica.pacext.identidade.dominio.NaoAutenticadoException;
import br.edu.unicatolica.pacext.identidade.dominio.Usuario;
import br.edu.unicatolica.pacext.identidade.dominio.UsuarioRepository;
import br.edu.unicatolica.pacext.identidade.infraestrutura.UsuarioAutenticado;
import br.edu.unicatolica.pacext.infraestrutura.web.ErroResponse;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

/**
 * Testa {@link UsuarioResource} chamando os métodos diretamente (sem subir o runtime
 * JAX-RS completo — mesmo padrão de {@code AuthResourceTest}/{@code JwtSecurityFilterTest},
 * sem Docker disponível). Cobre os quatro cenários da I/O Matrix da spec 1.5 que dependem
 * do módulo {@code identidade} (os dois cenários de token ausente/expirado são cobertos por
 * {@code JwtSecurityFilterTest}, responsabilidade exclusiva do filtro — AD-2).
 */
class UsuarioResourceTest {

    private final UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
    private final UsuarioAutenticado usuarioAutenticado = mock(UsuarioAutenticado.class);
    private final UsuarioResource resource = new UsuarioResource();

    UsuarioResourceTest() {
        resource.usuarioRepository = usuarioRepository;
        resource.usuarioAutenticado = usuarioAutenticado;
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
        when(usuarioAutenticado.id()).thenReturn(1L);
        when(usuarioRepository.findById(1L)).thenReturn(usuario(1L, "Ana Aluna", "ana@catolicasc.edu.br", "ALUNO"));

        Response response = resource.me();

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        UsuarioResponse corpo = (UsuarioResponse) response.getEntity();
        assertEquals(1L, corpo.id());
        assertEquals("Ana Aluna", corpo.nome());
        assertEquals("ana@catolicasc.edu.br", corpo.email());
        assertEquals("ALUNO", corpo.perfil());
    }

    @Test
    void alunoTentandoVerOutroUsuarioLancaAcessoNegado() {
        when(usuarioAutenticado.possuiPerfil("MODERADOR")).thenReturn(false);

        assertThrows(AcessoNegadoException.class, () -> resource.porId(99L));
    }

    @Test
    void moderadorVeOutroUsuarioRetorna200() {
        when(usuarioAutenticado.possuiPerfil("MODERADOR")).thenReturn(true);
        when(usuarioRepository.findById(7L)).thenReturn(usuario(7L, "Beto Moderador", "beto@catolicasc.edu.br", "ALUNO"));

        Response response = resource.porId(7L);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        UsuarioResponse corpo = (UsuarioResponse) response.getEntity();
        assertEquals(7L, corpo.id());
        assertEquals("Beto Moderador", corpo.nome());
    }

    @Test
    void moderadorConsultandoIdInexistenteRetorna404() {
        when(usuarioAutenticado.possuiPerfil("MODERADOR")).thenReturn(true);
        when(usuarioRepository.findById(404L)).thenReturn(null);

        Response response = resource.porId(404L);

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        ErroResponse corpo = (ErroResponse) response.getEntity();
        assertNotNull(corpo.error());
        assertEquals("RECURSO_NAO_ENCONTRADO", corpo.error().code());
    }

    @Test
    void mapperTraduzAcessoNegadoParaEnvelope403() {
        AcessoNegadoExceptionMapper mapper = new AcessoNegadoExceptionMapper();

        Response response = mapper.toResponse(new AcessoNegadoException());

        assertEquals(Response.Status.FORBIDDEN.getStatusCode(), response.getStatus());
        ErroResponse corpo = (ErroResponse) response.getEntity();
        assertNotNull(corpo.error());
        assertEquals("ACESSO_NEGADO", corpo.error().code());
    }

    @Test
    void mapperTraduzNaoAutenticadoParaEnvelope401() {
        NaoAutenticadoExceptionMapper mapper = new NaoAutenticadoExceptionMapper();

        Response response = mapper.toResponse(new NaoAutenticadoException());

        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
        ErroResponse corpo = (ErroResponse) response.getEntity();
        assertNotNull(corpo.error());
        assertEquals("NAO_AUTENTICADO", corpo.error().code());
    }
}
