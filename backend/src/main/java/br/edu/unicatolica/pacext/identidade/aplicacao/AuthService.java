package br.edu.unicatolica.pacext.identidade.aplicacao;

import br.edu.unicatolica.pacext.identidade.dominio.CredenciaisInvalidasException;
import br.edu.unicatolica.pacext.identidade.dominio.EmailNaoConfirmadoException;
import br.edu.unicatolica.pacext.identidade.dominio.PasswordHasher;
import br.edu.unicatolica.pacext.identidade.dominio.Usuario;
import br.edu.unicatolica.pacext.identidade.dominio.UsuarioRepository;
import br.edu.unicatolica.pacext.infraestrutura.auditoria.AuditoriaService;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.security.PrivateKey;
import java.util.Optional;
import java.util.Set;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Login e emissão de sessão JWT (Story 1.4, RF06/RF07/RF08). Único ponto do backend que
 * emite token — sempre com claims {@code sub} (id do usuário) e {@code roles} (perfil
 * global), conforme exigido pelo filtro de segurança global (AD-2).
 */
@ApplicationScoped
public class AuthService {

    @Inject
    UsuarioRepository usuarioRepository;

    @Inject
    PasswordHasher passwordHasher;

    @Inject
    AuditoriaService auditoriaService;

    @Inject
    PrivateKey privateKey;

    @ConfigProperty(name = "mp.jwt.verify.issuer")
    String issuer;

    /**
     * @throws CredenciaisInvalidasException se o e-mail não existe ou a senha não confere —
     *         mensagem genérica, nunca indica qual das duas, para não vazar quais e-mails
     *         existem na base (RF07).
     * @throws EmailNaoConfirmadoException se a credencial está correta mas o e-mail ainda
     *         não foi confirmado (Story 1.3, RF01.2) — mensagem distinta da anterior. A
     *         checagem de confirmação só acontece depois de validar a senha, de propósito:
     *         senha errada sempre cai em CredenciaisInvalidasException, mesmo para e-mail
     *         não confirmado — senão daria pra enumerar contas pendentes testando senhas.
     */
    public String autenticar(String email, String senha) {
        Usuario usuario = buscarUsuarioComCredencialValida(email, senha);

        if (!usuario.emailConfirmado) {
            throw new EmailNaoConfirmadoException();
        }

        String token = Jwt.claims()
                .issuer(issuer)
                .subject(String.valueOf(usuario.id))
                .groups(Set.of(usuario.perfil))
                .sign(privateKey);

        auditoriaService.registrar(usuario.id, "identidade", "LOGIN", "Login bem-sucedido.");
        return token;
    }

    private Usuario buscarUsuarioComCredencialValida(String email, String senha) {
        Optional<Usuario> usuario = usuarioRepository.buscarPorEmail(email);
        if (usuario.isEmpty() || !passwordHasher.confere(senha, usuario.get().senhaHash)) {
            throw new CredenciaisInvalidasException();
        }
        return usuario.get();
    }
}
