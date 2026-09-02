package br.edu.unicatolica.pacext.identidade.aplicacao;

import br.edu.unicatolica.pacext.identidade.dominio.GeradorTokenConfirmacao;
import br.edu.unicatolica.pacext.identidade.dominio.Usuario;
import br.edu.unicatolica.pacext.identidade.dominio.UsuarioRepository;
import br.edu.unicatolica.pacext.infraestrutura.auditoria.AuditoriaService;
import br.edu.unicatolica.pacext.infraestrutura.email.EmailService;
import br.edu.unicatolica.pacext.infraestrutura.web.ApiException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/** Regras de negócio da Story 1.3 (Confirmação de e-mail antes do primeiro login — RF01.2). */
@ApplicationScoped
public class ConfirmacaoEmailService {

    @Inject
    UsuarioRepository usuarioRepository;

    @Inject
    GeradorTokenConfirmacao geradorTokenConfirmacao;

    @Inject
    EmailService emailService;

    @Inject
    AuditoriaService auditoriaService;

    @ConfigProperty(name = "identidade.confirmacao-email.token-validade-horas")
    long tokenValidadeHoras;

    @ConfigProperty(name = "app.frontend-url")
    String frontendUrl;

    /**
     * Confirma o e-mail a partir do token do link. Idempotente: clicar de novo num link já
     * usado com sucesso não é um erro, só não faz nada.
     */
    @Transactional
    public void confirmar(String token) {
        Usuario usuario = usuarioRepository.buscarPorTokenConfirmacao(token)
                .orElseThrow(() -> ApiException.naoEncontrado("TOKEN_CONFIRMACAO_INVALIDO",
                        "Link de confirmação inválido ou já utilizado.", null));

        if (usuario.emailConfirmado) {
            return;
        }
        if (usuario.tokenConfirmacaoExpiraEm == null || usuario.tokenConfirmacaoExpiraEm.isBefore(Instant.now())) {
            throw ApiException.validacao("TOKEN_CONFIRMACAO_EXPIRADO",
                    "Link de confirmação expirado. Solicite um novo.", null);
        }

        usuario.emailConfirmado = true;
        usuario.tokenConfirmacaoEmail = null;
        usuario.tokenConfirmacaoExpiraEm = null;
        usuario.atualizadoEm = Instant.now();

        auditoriaService.registrar(usuario.id, "identidade", "EMAIL_CONFIRMADO", "Usuario", usuario.id, null);
    }

    /**
     * Reenvia a confirmação. Nunca revela se o e-mail existe ou não na base — silenciosamente
     * não faz nada para e-mail desconhecido ou já confirmado, evitando enumeração de contas
     * (mesmo princípio de RF07 aplicado aqui).
     */
    @Transactional
    public void reenviarConfirmacao(String email) {
        Optional<Usuario> usuarioOpt = usuarioRepository.buscarPorEmail(email.trim().toLowerCase());
        if (usuarioOpt.isEmpty()) {
            return;
        }

        Usuario usuario = usuarioOpt.get();
        if (usuario.emailConfirmado) {
            return;
        }

        usuario.tokenConfirmacaoEmail = geradorTokenConfirmacao.gerar();
        usuario.tokenConfirmacaoExpiraEm = Instant.now().plus(tokenValidadeHoras, ChronoUnit.HOURS);

        String linkConfirmacao = frontendUrl + "/confirmar-email?token=" + usuario.tokenConfirmacaoEmail;
        emailService.enviarConfirmacaoCadastro(usuario.email, usuario.nome, linkConfirmacao);
    }
}
