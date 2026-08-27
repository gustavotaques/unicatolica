package br.edu.unicatolica.pacext.identidade;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Optional;

/** Repository do módulo Identidade — único ponto de acesso à tabela {@code usuario} (AD-3). */
@ApplicationScoped
public class UsuarioRepository implements PanacheRepository<Usuario> {

    public boolean existePorEmail(String email) {
        return count("lower(email) = ?1", email.toLowerCase()) > 0;
    }

    public Optional<Usuario> buscarPorEmail(String email) {
        return find("lower(email) = ?1", email.toLowerCase()).firstResultOptional();
    }

    public Optional<Usuario> buscarPorTokenConfirmacao(String token) {
        return find("tokenConfirmacaoEmail = ?1", token).firstResultOptional();
    }
}
