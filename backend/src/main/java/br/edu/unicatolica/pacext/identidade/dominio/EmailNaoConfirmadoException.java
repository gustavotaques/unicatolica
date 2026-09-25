package br.edu.unicatolica.pacext.identidade.dominio;

import br.edu.unicatolica.pacext.compartilhado.erro.ApiException;
import jakarta.ws.rs.core.Response;

/**
 * Lançada no login (Story 1.4) quando a credencial está correta mas o e-mail ainda não
 * foi confirmado (Story 1.3, RF01.2) — mensagem distinta de
 * {@link CredenciaisInvalidasException}, com orientação de reenvio. Só é lançada depois
 * de confirmar e-mail/senha corretos, nunca antes — senão daria pra descobrir que um
 * e-mail existe (mas está pendente de confirmação) testando senhas. Vira 401
 * {@code EMAIL_NAO_CONFIRMADO} pelo {@code ApiExceptionMapper}.
 */
public class EmailNaoConfirmadoException extends ApiException {

    public EmailNaoConfirmadoException() {
        super(Response.Status.UNAUTHORIZED.getStatusCode(), "EMAIL_NAO_CONFIRMADO",
                "Confirme seu e-mail antes de entrar. Reenviar confirmação", null);
    }
}
