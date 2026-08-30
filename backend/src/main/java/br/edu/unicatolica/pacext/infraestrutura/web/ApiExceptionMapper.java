package br.edu.unicatolica.pacext.infraestrutura.web;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Traduz {@link ApiException} para o envelope de erro padrão (AD-5), com o status HTTP que
 * a própria exceção já carrega. É o único lugar que conhece a forma de {@link ErroResponse}
 * para rejeições de negócio — nenhum módulo monta a resposta de erro à mão.
 */
@Provider
public class ApiExceptionMapper implements ExceptionMapper<ApiException> {

    @Override
    public Response toResponse(ApiException exception) {
        ErroResponse corpo = ErroResponse.of(exception.getCode(), exception.getMessage(), exception.getDetails());
        return Response.status(exception.getStatus()) // int — Response.Status não cobre 422 (AD-5)
                .type(MediaType.APPLICATION_JSON)
                .entity(corpo)
                .build();
    }
}
