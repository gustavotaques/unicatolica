package br.edu.unicatolica.pacext.infraestrutura.web;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.stream.Collectors;

/**
 * Traduz falhas de Bean Validation (anotações {@code @NotBlank}, {@code @Email}, etc. nos
 * DTOs de request) para o envelope de erro padrão (AD-5), status 422 — mesma família dos
 * demais erros de validação de campo (RF03/RF04).
 */
@Provider
public class ValidacaoBeanExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

    @Override
    public Response toResponse(ConstraintViolationException exception) {
        String detalhes = exception.getConstraintViolations().stream()
                .map(this::formatarViolacao)
                .collect(Collectors.joining("; "));

        ErroResponse corpo = ErroResponse.of("DADOS_INVALIDOS", "Corpo da requisição inválido.", detalhes);
        return Response.status(422) // Response.Status não cobre 422 (AD-5)
                .type(MediaType.APPLICATION_JSON)
                .entity(corpo)
                .build();
    }

    private String formatarViolacao(ConstraintViolation<?> violacao) {
        String campo = violacao.getPropertyPath().toString();
        return campo + ": " + violacao.getMessage();
    }
}
