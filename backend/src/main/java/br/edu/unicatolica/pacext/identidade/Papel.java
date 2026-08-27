package br.edu.unicatolica.pacext.identidade;

/**
 * Papéis globais de plataforma (RF12/RF13) — valores possíveis do claim {@code roles} do
 * JWT (AD-2). {@code MODERADOR} só passa a ser atribuído quando o Epic 12 (Moderação) for
 * construído; o valor já existe aqui para não exigir migração de enum depois.
 */
public enum Papel {
    ALUNO,
    ADMINISTRADOR,
    MODERADOR
}
