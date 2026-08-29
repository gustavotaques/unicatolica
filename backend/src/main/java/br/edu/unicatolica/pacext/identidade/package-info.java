/**
 * Módulo de Identidade e Acesso (RF01–RF13).
 *
 * <p>Dono da tabela {@code usuario} (AD-3 — limites de módulo dentro do monólito):
 * nenhum outro módulo lê ou escreve nela diretamente; leitura cross-módulo é sempre por
 * id simples (sem relação JPA cruzando pacote), nunca acesso a repositório alheio.
 * Organizado em subpacotes por camada — {@code web} (Resource/DTO), {@code aplicacao}
 * (Service, regra de negócio), {@code dominio} (entidade, Repository, exceções de
 * domínio) — ver {@code docs/arquitetura-camadas-explicacao.md}.</p>
 *
 * <p>Implementa Story 1.2 (cadastro), Story 1.3 (confirmação de e-mail), Story 1.4
 * (login/emissão de JWT) e Story 1.5 (bloqueio de acesso/restrição por perfil, via
 * {@code UsuarioAutenticado} + {@code /usuarios/me}/{@code /usuarios/{id}}); logout
 * (Story 1.6) ainda não implementado.</p>
 */
package br.edu.unicatolica.pacext.identidade;
