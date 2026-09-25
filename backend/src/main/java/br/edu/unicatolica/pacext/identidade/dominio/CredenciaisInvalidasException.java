package br.edu.unicatolica.pacext.identidade.dominio;

import br.edu.unicatolica.pacext.compartilhado.erro.ApiException;
import jakarta.ws.rs.core.Response;

/**
 * Lançada quando e-mail ou senha estão incorretos, ou o e-mail ainda não foi
 * confirmado — a mensagem exposta ao cliente é sempre genérica (RF07), nunca indica
 * qual das duas condições falhou, para não vazar quais e-mails existem na base.
 * Vira 401 {@code CREDENCIAL_INVALIDA} pelo {@code ApiExceptionMapper}.
 */
public class CredenciaisInvalidasException extends ApiException {

    public CredenciaisInvalidasException() {
        super(Response.Status.UNAUTHORIZED.getStatusCode(), "CREDENCIAL_INVALIDA",
                "E-mail ou senha inválidos.", null);
    }
}
