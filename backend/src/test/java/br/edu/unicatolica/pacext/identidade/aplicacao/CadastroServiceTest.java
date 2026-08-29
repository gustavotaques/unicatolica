package br.edu.unicatolica.pacext.identidade.aplicacao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.edu.unicatolica.pacext.comunidades.AutoJoinCursoService;
import br.edu.unicatolica.pacext.identidade.dominio.GeradorTokenConfirmacao;
import br.edu.unicatolica.pacext.identidade.dominio.PasswordHasher;
import br.edu.unicatolica.pacext.identidade.dominio.Usuario;
import br.edu.unicatolica.pacext.identidade.dominio.UsuarioRepository;
import br.edu.unicatolica.pacext.infraestrutura.auditoria.AuditoriaService;
import br.edu.unicatolica.pacext.infraestrutura.email.EmailService;
import br.edu.unicatolica.pacext.infraestrutura.web.ApiException;
import jakarta.ws.rs.core.Response;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Testa {@link CadastroService} isoladamente com Mockito (sem subir o runtime do
 * Quarkus/Postgres — mesmo motivo de {@code JwtSecurityFilterTest}). Cobre cada critério
 * de aceite da Story 1.2.
 */
class CadastroServiceTest {

    private static final String EMAIL_INSTITUCIONAL = "aluno@catolicasc.edu.br";
    private static final String SENHA_VALIDA = "senha123";
    private static final LocalDate NASCIMENTO_MAIOR_IDADE = LocalDate.now().minusYears(20);

    private CadastroService service;
    private UsuarioRepository usuarioRepository;
    private AutoJoinCursoService autoJoinCursoService;
    private EmailService emailService;
    private AuditoriaService auditoriaService;

    @BeforeEach
    void setUp() {
        service = new CadastroService();
        usuarioRepository = mock(UsuarioRepository.class);
        autoJoinCursoService = mock(AutoJoinCursoService.class);
        emailService = mock(EmailService.class);
        auditoriaService = mock(AuditoriaService.class);

        service.usuarioRepository = usuarioRepository;
        service.passwordHasher = new PasswordHasher();
        service.geradorTokenConfirmacao = new GeradorTokenConfirmacao();
        service.autoJoinCursoService = autoJoinCursoService;
        service.emailService = emailService;
        service.auditoriaService = auditoriaService;
        service.dominioInstitucional = "catolicasc.edu.br";
        service.senhaTamanhoMinimo = 8;
        service.tokenValidadeHoras = 24;
        service.frontendUrl = "http://localhost:4200";

        // Simula o IDENTITY do banco, que o mock de repository não faz sozinho.
        doAnswer(invocation -> {
            Usuario usuario = invocation.getArgument(0);
            usuario.id = 42L;
            return null;
        }).when(usuarioRepository).persist(any(Usuario.class));
    }

    @Test
    void cadastraComSucessoQuandoTodosOsDadosSaoValidos() {
        Usuario usuario = service.cadastrar("Ana Silva", EMAIL_INSTITUCIONAL, SENHA_VALIDA, "Engenharia de Software",
                NASCIMENTO_MAIOR_IDADE);

        assertEquals("ana silva", usuario.nome.toLowerCase()); // normalização não altera capitalização, só trim
        assertEquals(EMAIL_INSTITUCIONAL, usuario.email);
        assertFalse(usuario.emailConfirmado); // RF01.2 — nenhuma sessão ativa/confirmação automática
        assertEquals("ALUNO", usuario.perfil);
        assertNotNull(usuario.tokenConfirmacaoEmail);

        verify(autoJoinCursoService).sincronizarCursoDoAluno(eq(42L), isNull(), eq("Engenharia de Software"));
        verify(emailService).enviarConfirmacaoCadastro(eq(EMAIL_INSTITUCIONAL), anyString(), anyString());
        verify(auditoriaService).registrar(eq(42L), eq("identidade"), eq("CADASTRO_REALIZADO"), eq("Usuario"),
                eq(42L), isNull());
    }

    @Test
    void rejeitaEmailDeDominioExterno() {
        ApiException erro = assertThrows(ApiException.class, () -> service.cadastrar("Ana Silva",
                "ana@gmail.com", SENHA_VALIDA, "Engenharia de Software", NASCIMENTO_MAIOR_IDADE));

        assertEquals(422, erro.getStatus());
        assertEquals("EMAIL_DOMINIO_EXTERNO", erro.getCode());
        assertEquals("Use seu e-mail institucional para se cadastrar.", erro.getMessage());
        verify(usuarioRepository, never()).persist(any(Usuario.class));
    }

    @Test
    void rejeitaEmailJaCadastrado() {
        when(usuarioRepository.existePorEmail(EMAIL_INSTITUCIONAL)).thenReturn(true);

        ApiException erro = assertThrows(ApiException.class, () -> service.cadastrar("Ana Silva",
                EMAIL_INSTITUCIONAL, SENHA_VALIDA, "Engenharia de Software", NASCIMENTO_MAIOR_IDADE));

        assertEquals(Response.Status.CONFLICT.getStatusCode(), erro.getStatus());
        assertEquals("EMAIL_JA_CADASTRADO", erro.getCode());
        assertEquals("Esse e-mail já tem uma conta. Esqueceu a senha?", erro.getMessage());
        verify(usuarioRepository, never()).persist(any(Usuario.class));
    }

    @Test
    void rejeitaSenhaForaDaPolitica() {
        ApiException erro = assertThrows(ApiException.class, () -> service.cadastrar("Ana Silva",
                EMAIL_INSTITUCIONAL, "123", "Engenharia de Software", NASCIMENTO_MAIOR_IDADE));

        assertEquals(422, erro.getStatus());
        assertEquals("SENHA_POLITICA_INVALIDA", erro.getCode());
    }

    @Test
    void rejeitaMenorDeDezoitoAnos() {
        LocalDate nascimentoMenorDeIdade = LocalDate.now().minusYears(17);

        ApiException erro = assertThrows(ApiException.class, () -> service.cadastrar("Ana Silva",
                EMAIL_INSTITUCIONAL, SENHA_VALIDA, "Engenharia de Software", nascimentoMenorDeIdade));

        assertEquals(422, erro.getStatus());
        assertEquals("IDADE_MINIMA_NAO_ATENDIDA", erro.getCode());
        verify(usuarioRepository, never()).persist(any(Usuario.class));
    }
}
