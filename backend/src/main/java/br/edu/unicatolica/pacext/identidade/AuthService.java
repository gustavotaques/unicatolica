package br.edu.unicatolica.pacext.identidade;

import br.edu.unicatolica.pacext.infraestrutura.auditoria.AuditoriaService;
import br.edu.unicatolica.pacext.infraestrutura.web.ApiException;
import io.quarkus.elytron.security.common.BcryptUtil;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.security.PrivateKey;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Login e emissão de sessão JWT (Story 1.4, RF06/RF07/RF08). Único ponto do backend que
 * emite token — sempre com claims {@code sub} (id do usuário) e {@code roles} (papéis
 * globais), conforme exigido pelo filtro de segurança global (AD-2).
 */
@ApplicationScoped
public class AuthService {

    @Inject
    UsuarioRepository usuarioRepository;

    @Inject
    AuditoriaService auditoriaService;

    @Inject
    PrivateKey privateKey;

    @ConfigProperty(name = "mp.jwt.verify.issuer")
    String issuer;

    /**
     * @throws ApiException 401 {@code CREDENCIAL_INVALIDA} se o e-mail não existe ou a
     *         senha não confere — mensagem genérica, nunca indica qual das duas, para não
     *         vazar quais e-mails existem na base (RF07). Ou 401 {@code EMAIL_NAO_CONFIRMADO}
     *         se a credencial está correta mas o e-mail ainda não foi confirmado — mensagem
     *         distinta da anterior, com opção de reenvio (RF01.2, Story 1.3). A checagem de
     *         confirmação só acontece depois de validar a senha, de propósito: senha errada
     *         sempre cai no CREDENCIAL_INVALIDA genérico, mesmo para e-mail não confirmado —
     *         senão daria pra enumerar contas pendentes de confirmação testando senhas.
     */
    public String autenticar(String email, String senha) {
        Usuario usuario = buscarUsuarioComCredencialValida(email, senha);

        if (!usuario.emailConfirmado) {
            throw ApiException.naoAutenticado("EMAIL_NAO_CONFIRMADO",
                    "Confirme seu e-mail antes de entrar. Reenviar confirmação", null);
        }

        Set<String> papeis = usuario.papeis.stream().map(Enum::name).collect(Collectors.toSet());
        String token = Jwt.claims()
                .issuer(issuer)
                .subject(String.valueOf(usuario.id))
                .groups(papeis)
                .sign(privateKey);

        auditoriaService.registrar(usuario.id, "identidade", "LOGIN", "Login bem-sucedido.");
        return token;
    }

    private Usuario buscarUsuarioComCredencialValida(String email, String senha) {
        Optional<Usuario> usuario = usuarioRepository.buscarPorEmail(email);
        if (usuario.isEmpty() || !BcryptUtil.matches(senha, usuario.get().senhaHash)) {
            throw ApiException.naoAutenticado("CREDENCIAL_INVALIDA", "E-mail ou senha inválidos.", null);
        }
        return usuario.get();
    }
}
