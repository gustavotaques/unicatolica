/**
 * Módulo de Identidade e Acesso (RF01–RF13).
 *
 * <p>Dono da tabela {@code usuario} (AD-3 — limites de módulo dentro do monólito):
 * nenhum outro módulo lê ou escreve nela diretamente; outros módulos guardam só o
 * {@code usuario_id} (sem FK nem relação JPA) e leem pela API pública abaixo.
 * Organizado em subpacotes por camada — {@code web} (Resource/DTO), {@code aplicacao}
 * (Service, regra de negócio), {@code dominio} (entidade, Repository, exceções de
 * domínio) — ver {@code docs/como-funciona.md}.</p>
 *
 * <p><b>API pública</b> (raiz do pacote): {@link br.edu.unicatolica.pacext.identidade.UsuarioConsulta}
 * e {@link br.edu.unicatolica.pacext.identidade.UsuarioResumo}. Também implementa
 * {@code compartilhado.seguranca.SessaoConsulta}, usada pelo filtro de logout.</p>
 *
 * <p><b>Dependências:</b> só {@code compartilhado} (módulo folha, verificado por
 * {@code ArquiteturaTest}; a chamada a {@code comunidades.AutoJoinCursoService} no
 * cadastro sai no PR 5b da reestruturação).</p>
 *
 * <p>Implementa Story 1.2 (cadastro), Story 1.3 (confirmação de e-mail), Story 1.4
 * (login/emissão de JWT), Story 1.5 (bloqueio de acesso/restrição por perfil, via
 * {@code UsuarioAutenticado} + {@code /usuarios/me}/{@code /usuarios/{id}}) e Story 1.6
 * (logout).</p>
 */
package br.edu.unicatolica.pacext.identidade;
