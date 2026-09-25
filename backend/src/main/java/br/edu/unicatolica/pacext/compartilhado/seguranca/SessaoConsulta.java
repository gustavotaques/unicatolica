package br.edu.unicatolica.pacext.compartilhado.seguranca;

import java.time.Instant;
import java.util.Optional;

/**
 * O que o {@link SessaoInvalidadaFilter} precisa saber sobre o usuário do token, sem
 * importar o módulo que guarda esse dado (regra 4: o transversal declara a interface e o
 * módulo implementa). Implementada por Identidade.
 */
public interface SessaoConsulta {

    /**
     * @return instante a partir do qual tokens do usuário são aceitos (último logout), ou
     *     vazio se o usuário não existe ou nunca deslogou.
     */
    Optional<Instant> sessaoValidaDesde(Long usuarioId);
}
