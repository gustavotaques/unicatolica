/**
 * Módulo de Comunidades (RF21–RF31).
 *
 * <p>Dono das tabelas {@code comunidade}/{@code comunidade_membro} (AD-3 — limites de
 * módulo dentro do monólito): nenhum outro módulo escreve nelas diretamente — o único
 * ponto de entrada externo é a interface publicada {@link AutoJoinCursoService}. Esta
 * fatia implementa só o mecanismo de auto-join (Story 2.3/RF24.1); criação de comunidade
 * pelo administrador (Story 2.1) e por aluno (Story 2.2) ainda não implementadas — as
 * tabelas já existem (changelog {@code comunidades-001-...}) mas sem endpoint de criação.</p>
 */
package br.edu.unicatolica.pacext.comunidades;
