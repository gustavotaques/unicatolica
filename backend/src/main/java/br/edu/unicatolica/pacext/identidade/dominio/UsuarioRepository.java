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
}
