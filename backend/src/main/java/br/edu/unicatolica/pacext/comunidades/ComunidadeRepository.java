package br.edu.unicatolica.pacext.comunidades;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Optional;

/** Repository do módulo Comunidades — único ponto de acesso à tabela {@code comunidade} (AD-3). */
@ApplicationScoped
public class ComunidadeRepository implements PanacheRepository<Comunidade> {

    /** Busca case-insensitive — o texto de curso vem digitado livremente no cadastro (Story 1.2). */
    public Optional<Comunidade> buscarPorTipoENome(TipoComunidade tipo, String nome) {
        return find("tipo = ?1 and lower(nome) = ?2", tipo, nome.toLowerCase()).firstResultOptional();
    }
}
