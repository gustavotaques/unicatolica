package br.edu.unicatolica.pacext.comunidades;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

/** Repository do módulo Comunidades — único ponto de acesso à tabela {@code comunidade_membro} (AD-3). */
@ApplicationScoped
public class ComunidadeMembroRepository implements PanacheRepository<ComunidadeMembro> {

    public boolean existeAssociacao(Comunidade comunidade, Long usuarioId) {
        return count("comunidade = ?1 and usuarioId = ?2", comunidade, usuarioId) > 0;
    }

    public long removerAssociacao(Comunidade comunidade, Long usuarioId) {
        return delete("comunidade = ?1 and usuarioId = ?2", comunidade, usuarioId);
    }
}
