package br.edu.unicatolica.pacext.compartilhado.erro;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * Respostas de erro prontas para quem não pode lançar {@link ApiException}: os filtros de
 * autenticação encerram a requisição com {@code abortWith(...)}, e o
 * {@code JwtSecurityFilter} roda antes do roteamento, onde nenhum {@code ExceptionMapper}
 * atua. Mesmo envelope do {@link ApiExceptionMapper} (AD-5).
 */
public final class RespostasErro {

    private RespostasErro() {
    }

    /** 401 {@code NAO_AUTENTICADO} com o motivo em {@code details}. */
    public static Response naoAutenticado(String detalhes) {
        return Response.status(Response.Status.UNAUTHORIZED)
                .type(MediaType.APPLICATION_JSON)
                .entity(ErroResponse.of("NAO_AUTENTICADO", "Autenticação necessária.", detalhes))
                .build();
    }
}
