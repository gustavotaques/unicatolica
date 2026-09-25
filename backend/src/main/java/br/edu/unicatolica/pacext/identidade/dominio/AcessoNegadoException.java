package br.edu.unicatolica.pacext.identidade.dominio;

import br.edu.unicatolica.pacext.compartilhado.erro.ApiException;
import jakarta.ws.rs.core.Response;

/**
 * Lançada quando um usuário autenticado não tem permissão (perfil) para a ação
 * solicitada, mas a existência do recurso não precisa ficar oculta (AD-5) — vira 403
 * {@code ACESSO_NEGADO} pelo {@code ApiExceptionMapper}. Padrão de recusa por perfil a
 * ser reaproveitado por outros módulos.
 */
public class AcessoNegadoException extends ApiException {

    public AcessoNegadoException() {
        super(Response.Status.FORBIDDEN.getStatusCode(), "ACESSO_NEGADO",
                "Você não tem permissão para executar esta ação.", null);
    }
}
