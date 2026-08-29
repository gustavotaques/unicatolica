package br.edu.unicatolica.pacext.identidade.aplicacao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.edu.unicatolica.pacext.identidade.dominio.CredenciaisInvalidasException;
import br.edu.unicatolica.pacext.identidade.dominio.EmailNaoConfirmadoException;
import br.edu.unicatolica.pacext.identidade.dominio.PasswordHasher;
import br.edu.unicatolica.pacext.identidade.dominio.Usuario;
import br.edu.unicatolica.pacext.identidade.dominio.UsuarioRepository;
import br.edu.unicatolica.pacext.infraestrutura.auditoria.AuditoriaService;
import io.smallrye.jwt.auth.principal.DefaultJWTParser;
import io.smallrye.jwt.auth.principal.JWTAuthContextInfo;
import io.smallrye.jwt.util.KeyUtils;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Optional;
import java.util.Set;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Testa {@link AuthService} isoladamente (sem Docker/Dev Services, mesmo padrão de
 * {@code JwtSecurityFilterTest}) — repositório mockado via Mockito, par de chaves gerado
 * em memória a cada execução (nenhuma chave, de teste ou não, fica commitada no repo).
 */
class AuthServiceTest {

    private static final String ISSUER = "https://pacext.unicatolica.edu.br";

    private static PrivateKey privateKey;
    private static PublicKey publicKey;

    private final UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
    private final AuditoriaService auditoriaService = mock(AuditoriaService.class);
    private final AuthService authService = new AuthService();

    @BeforeAll
    static void carregarChaves() throws Exception {
        KeyPair par = KeyUtils.generateKeyPair(2048);
        privateKey = par.getPrivate();
        publicKey = par.getPublic();
    }

    AuthServiceTest() {
        authService.usuarioRepository = usuarioRepository;
        authService.passwordHasher = new PasswordHasher();
        authService.auditoriaService = auditoriaService;
        authService.privateKey = privateKey;
        authService.issuer = ISSUER;
    }

    private Usuario usuarioConfirmado(String senha) {
        Usuario usuario = new Usuario();
        usuario.id = 42L;
        usuario.email = "aluno.teste@catolicasc.edu.br";
        usuario.senhaHash = authService.passwordHasher.gerarHash(senha);
        usuario.perfil = "ALUNO";
        usuario.emailConfirmado = true;
        return usuario;
    }

    @Test
    void autenticaEEmiteTokenComSubERolesParaCredenciaisValidas() throws Exception {
        Usuario usuario = usuarioConfirmado("Senha123!");
        when(usuarioRepository.buscarPorEmail(usuario.email)).thenReturn(Optional.of(usuario));

        String token = authService.autenticar(usuario.email, "Senha123!");

        JWTAuthContextInfo contextInfo = new JWTAuthContextInfo(publicKey, ISSUER);
        contextInfo.setGroupsPath("roles");
        JsonWebToken jwt = new DefaultJWTParser(contextInfo).parse(token);
        assertEquals("42", jwt.getSubject());
        assertEquals(Set.of("ALUNO"), jwt.getGroups());
    }

    @Test
    void registraEventoDeAuditoriaAoAutenticarComSucesso() {
        Usuario usuario = usuarioConfirmado("Senha123!");
        when(usuarioRepository.buscarPorEmail(usuario.email)).thenReturn(Optional.of(usuario));

        authService.autenticar(usuario.email, "Senha123!");

        verify(auditoriaService).registrar(eq(42L), eq("identidade"), eq("LOGIN"), any());
    }

    @Test
    void rejeitaComCredenciaisInvalidasQuandoEmailNaoExiste() {
        when(usuarioRepository.buscarPorEmail("desconhecido@catolicasc.edu.br")).thenReturn(Optional.empty());

        assertThrows(CredenciaisInvalidasException.class,
                () -> authService.autenticar("desconhecido@catolicasc.edu.br", "qualquer"));
    }

    @Test
    void rejeitaComCredenciaisInvalidasQuandoSenhaEstaErrada() {
        Usuario usuario = usuarioConfirmado("Senha123!");
        when(usuarioRepository.buscarPorEmail(usuario.email)).thenReturn(Optional.of(usuario));

        assertThrows(CredenciaisInvalidasException.class,
                () -> authService.autenticar(usuario.email, "senha-errada"));
    }

    @Test
    void rejeitaComEmailNaoConfirmadoQuandoCredencialEstaCorreta() {
        Usuario usuario = usuarioConfirmado("Senha123!");
        usuario.emailConfirmado = false;
        when(usuarioRepository.buscarPorEmail(usuario.email)).thenReturn(Optional.of(usuario));

        assertThrows(EmailNaoConfirmadoException.class,
                () -> authService.autenticar(usuario.email, "Senha123!"));
    }

    @Test
    void rejeitaComCredenciaisInvalidasQuandoSenhaErradaMesmoComEmailNaoConfirmado() {
        // Senha errada nunca deve revelar que o e-mail existe mas está pendente de
        // confirmação — sempre CredenciaisInvalidasException (RF07/anti-enumeração).
        Usuario usuario = usuarioConfirmado("Senha123!");
        usuario.emailConfirmado = false;
        when(usuarioRepository.buscarPorEmail(usuario.email)).thenReturn(Optional.of(usuario));

        assertThrows(CredenciaisInvalidasException.class,
                () -> authService.autenticar(usuario.email, "senha-errada"));
    }
}
