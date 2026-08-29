package br.edu.unicatolica.pacext.infraestrutura.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

/**
 * Testa {@link ApiExceptionMapper} isoladamente — preenche o item pendente de
 * deferred-work.md ("ExceptionMapper global ... hoje só implementado para 401").
 */
class ApiExceptionMapperTest {

    private final ApiExceptionMapper mapper = new ApiExceptionMapper();

    @Test
    void traduzApiExceptionParaEnvelopeDeErroComStatusCorrespondente() {
        ApiException excecao = ApiException.conflito("EMAIL_JA_CADASTRADO", "Esse e-mail já tem uma conta.",
                "detalhe");

        Response resposta = mapper.toResponse(excecao);

        assertEquals(Response.Status.CONFLICT.getStatusCode(), resposta.getStatus());
        ErroResponse corpo = assertInstanceOf(ErroResponse.class, resposta.getEntity());
        assertEquals("EMAIL_JA_CADASTRADO", corpo.error().code());
        assertEquals("Esse e-mail já tem uma conta.", corpo.error().message());
        assertEquals("detalhe", corpo.error().details());
    }

    @Test
    void statusDaRespostaSegueOStatusDaExcecao() {
        ApiException excecao = ApiException.validacao("IDADE_MINIMA_NAO_ATENDIDA", "mensagem", null);

        Response resposta = mapper.toResponse(excecao);

        assertEquals(422, resposta.getStatus());
    }
}
