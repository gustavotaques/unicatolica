package br.edu.unicatolica.pacext.identidade.dominio;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Optional;

/** Repositório próprio do módulo Identidade — nenhum outro módulo acessa {@code usuario} diretamente (AD-3). */
@ApplicationScoped
public class UsuarioRepository implements PanacheRepository<Usuario> {

    public Optional<Usuario> buscarPorEmail(String email) {
        return find("email", email).firstResultOptional();
    }

    /** Cadastro (Story 1.2, RF02) normaliza e-mail para minúsculas antes de gravar/consultar. */
    public boolean existePorEmail(String email) {
        return count("email", email) > 0;
    }

    /** Confirmação de e-mail (Story 1.3) — lookup pelo token de uso único do link. */
    public Optional<Usuario> buscarPorTokenConfirmacao(String token) {
        return find("tokenConfirmacaoEmail", token).firstResultOptional();
    }
}
