package br.edu.unicatolica.pacext.identidade;

import java.util.Collection;
import java.util.Map;

/**
 * Leitura de usuários por outros módulos (API pública de Identidade). Os outros módulos
 * guardam só o {@code usuario_id}, sem FK nem relação JPA, e resolvem nome/curso por aqui.
 */
public interface UsuarioConsulta {

    /**
     * Consulta em lote, para que uma listagem não gere uma consulta por item (N+1).
     *
     * @return resumo de cada id encontrado, indexado pelo id; ids inexistentes ficam de fora.
     */
    Map<Long, UsuarioResumo> buscarResumos(Collection<Long> ids);
}
