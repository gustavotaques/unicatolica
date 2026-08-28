/**
 * Módulo de Identidade e Acesso (RF01–RF13).
 *
 * <p>Dono da tabela {@code usuario}/{@code usuario_papel} (AD-3 — limites de módulo dentro
 * do monólito): nenhum outro módulo lê ou escreve nelas diretamente; leitura cross-módulo
 * é sempre por id simples (sem relação JPA cruzando pacote), nunca acesso a repositório
 * alheio. Implementa Story 1.2 (cadastro), Story 1.3 (confirmação de e-mail) e Story 1.4
 * (login/emissão de JWT); bloqueio de acesso/restrição por perfil (Story 1.5) e logout
 * (Story 1.6) ainda não implementados.</p>
 */
package br.edu.unicatolica.pacext.identidade;
