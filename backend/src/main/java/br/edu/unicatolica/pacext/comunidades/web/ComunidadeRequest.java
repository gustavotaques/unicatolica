package br.edu.unicatolica.pacext.comunidades.web;

/**
 * Corpo de {@code POST /comunidades} (Story 2.2). Sem campo {@code tipo} de propósito —
 * toda comunidade criada por aqui é ABERTA (RF21.2); tipo CURSO só existe via seed
 * (Story 2.1, protótipo — ver docs/modelo-epico-2-comunidades.md).
 */
public record ComunidadeRequest(String nome, String descricao) {
}
