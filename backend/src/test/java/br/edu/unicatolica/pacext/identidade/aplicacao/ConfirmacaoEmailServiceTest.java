package br.edu.unicatolica.pacext.identidade.aplicacao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.edu.unicatolica.pacext.identidade.dominio.GeradorTokenConfirmacao;
import br.edu.unicatolica.pacext.identidade.dominio.Usuario;
import br.edu.unicatolica.pacext.identidade.dominio.UsuarioRepository;
import br.edu.unicatolica.pacext.infraestrutura.auditoria.AuditoriaService;
import br.edu.unicatolica.pacext.infraestrutura.email.EmailService;
import br.edu.unicatolica.pacext.infraestrutura.web.ApiException;
import jakarta.ws.rs.core.Response;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Testa {@link ConfirmacaoEmailService} isoladamente com Mockito — critérios da Story 1.3. */
class ConfirmacaoEmailServiceTest {

    private static final String TOKEN = "token-valido";

    private ConfirmacaoEmailService service;
    private UsuarioRepository usuarioRepository;
    private EmailService emailService;
    private AuditoriaService auditoriaService;

    @BeforeEach
    void setUp() {
        service = new ConfirmacaoEmailService();
        usuarioRepository = mock(UsuarioRepository.class);
        emailService = mock(EmailService.class);
        auditoriaService = mock(AuditoriaService.class);

        service.usuarioRepository = usuarioRepository;
        service.geradorTokenConfirmacao = new GeradorTokenConfirmacao();
        service.emailService = emailService;
        service.auditoriaService = auditoriaService;
        service.tokenValidadeHoras = 24;
        service.frontendUrl = "http://localhost:4200";
    }

    private Usuario usuarioPendente() {
        Usuario usuario = new Usuario();
        usuario.id = 7L;
        usuario.nome = "Ana Silva";
        usuario.email = "ana@catolicasc.edu.br";
        usuario.emailConfirmado = false;
        usuario.tokenConfirmacaoEmail = TOKEN;
        usuario.tokenConfirmacaoExpiraEm = Instant.now().plusSeconds(3600);
        return usuario;
    }

    @Test
    void confirmaEmailComTokenValido() {
        Usuario usuario = usuarioPendente();
        when(usuarioRepository.buscarPorTokenConfirmacao(TOKEN)).thenReturn(Optional.of(usuario));

        service.confirmar(TOKEN);

        assertTrue(usuario.emailConfirmado);
        // tokenConfirmacaoEmail NÃO é zerado (defeito D5) — continua apontando pro usuário
        // pra próxima busca por esse mesmo token achar o registro e cair no branch idempotente.
        assertEquals(TOKEN, usuario.tokenConfirmacaoEmail);
        verify(auditoriaService).registrar(eq(7L), eq("identidade"), eq("EMAIL_CONFIRMADO"), eq("Usuario"), eq(7L),
                eq(null));
    }

    @Test
    void confirmarNovamenteComEmailJaConfirmadoEIdempotente() {
        Usuario usuario = usuarioPendente();
        usuario.emailConfirmado = true;
        when(usuarioRepository.buscarPorTokenConfirmacao(TOKEN)).thenReturn(Optional.of(usuario));

        service.confirmar(TOKEN);

        verify(auditoriaService, never()).registrar(any(), any(), any(), any(), any(), any());
    }

    /**
     * Defeito D5: {@code openapi.yaml} promete idempotência ("clicar de novo num link já
     * usado com sucesso não é um erro"), mas o segundo clique com o mesmo token real
     * devolvia 404 — o teste acima ({@code confirmarNovamenteComEmailJaConfirmadoEIdempotente})
     * mascarava isso porque configura {@code emailConfirmado=true} artificialmente, sem
     * passar pelo primeiro {@code confirmar()} de verdade. Este teste encadeia duas
     * chamadas reais com o MESMO mock de busca por token (como o banco faria, já que o
     * token deixou de ser zerado ao confirmar) e prova que a segunda não lança erro.
     */
    @Test
    void confirmarDeNovoComOMesmoTokenAposConfirmacaoBemSucedidaNaoLancaErro() {
        Usuario usuario = usuarioPendente();
        when(usuarioRepository.buscarPorTokenConfirmacao(TOKEN)).thenReturn(Optional.of(usuario));

        service.confirmar(TOKEN);
        service.confirmar(TOKEN);

        assertTrue(usuario.emailConfirmado);
    }

    @Test
    void rejeitaTokenInexistente() {
        when(usuarioRepository.buscarPorTokenConfirmacao("invalido")).thenReturn(Optional.empty());

        ApiException erro = assertThrows(ApiException.class, () -> service.confirmar("invalido"));

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), erro.getStatus());
        assertEquals("TOKEN_CONFIRMACAO_INVALIDO", erro.getCode());
    }

    @Test
    void rejeitaTokenExpirado() {
        Usuario usuario = usuarioPendente();
        usuario.tokenConfirmacaoExpiraEm = Instant.now().minusSeconds(1);
        when(usuarioRepository.buscarPorTokenConfirmacao(TOKEN)).thenReturn(Optional.of(usuario));

        ApiException erro = assertThrows(ApiException.class, () -> service.confirmar(TOKEN));

        assertEquals(422, erro.getStatus());
        assertEquals("TOKEN_CONFIRMACAO_EXPIRADO", erro.getCode());
    }

    @Test
    void reenvioGeraNovoTokenEEnviaEmailQuandoPendente() {
        Usuario usuario = usuarioPendente();
        when(usuarioRepository.buscarPorEmail(usuario.email)).thenReturn(Optional.of(usuario));

        service.reenviarConfirmacao(usuario.email);

        verify(emailService).enviarConfirmacaoCadastro(eq(usuario.email), anyString(), anyString());
    }

    @Test
    void reenvioNaoRevelaEmailInexistente() {
        when(usuarioRepository.buscarPorEmail("desconhecido@catolicasc.edu.br")).thenReturn(Optional.empty());

        service.reenviarConfirmacao("desconhecido@catolicasc.edu.br");

        verify(emailService, never()).enviarConfirmacaoCadastro(any(), any(), any());
    }

    @Test
    void reenvioNaoFazNadaSeJaConfirmado() {
        Usuario usuario = usuarioPendente();
        usuario.emailConfirmado = true;
        when(usuarioRepository.buscarPorEmail(usuario.email)).thenReturn(Optional.of(usuario));

        service.reenviarConfirmacao(usuario.email);

        verify(emailService, never()).enviarConfirmacaoCadastro(any(), any(), any());
    }
}
