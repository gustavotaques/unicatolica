package br.edu.unicatolica.pacext.identidade.dominio;

/**
 * Lançada no login (Story 1.4) quando a credencial está correta mas o e-mail ainda não
 * foi confirmado (Story 1.3, RF01.2) — mensagem distinta de
 * {@link CredenciaisInvalidasException}, com orientação de reenvio. Só é lançada depois
 * de confirmar e-mail/senha corretos, nunca antes — senão daria pra descobrir que um
 * e-mail existe (mas está pendente de confirmação) testando senhas.
 */
public class EmailNaoConfirmadoException extends RuntimeException {
}
