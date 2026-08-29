package br.edu.unicatolica.pacext.identidade.dominio;

/**
 * Lançada quando um usuário autenticado não tem permissão (perfil) para a ação
 * solicitada, mas a existência do recurso não precisa ficar oculta (AD-5) — mapeada
 * para 403 {@code ErroResponse} por {@code AcessoNegadoExceptionMapper}. Padrão de
 * recusa por perfil a ser reaproveitado por outros módulos.
 */
public class AcessoNegadoException extends RuntimeException {
}
