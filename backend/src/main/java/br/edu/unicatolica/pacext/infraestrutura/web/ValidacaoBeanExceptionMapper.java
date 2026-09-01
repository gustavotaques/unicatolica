package br.edu.unicatolica.pacext.infraestrutura.web;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
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
        return nomeDoCampo(violacao.getPropertyPath()) + ": " + violacao.getMessage();
    }

    /**
     * Só o último segmento do path (o nome do campo em si) — o path completo de uma
     * violação em parâmetro de método JAX-RS inclui o nome do método e do parâmetro (ex.:
     * {@code registrar.request.email}), detalhe interno de implementação que não pode
     * vazar pro cliente (defeito D6).
     *
     * <p>Nós sem nome (violação de classe/cross-parameter) são ignorados no laço — sem
     * essa checagem, {@code node.getName()} retorna {@code null} para esses casos e o
     * método devolvia {@code null} em vez do último nome válido, gerando "null: mensagem"
     * na resposta ao cliente.</p>
     */
    private String nomeDoCampo(Path path) {
        String nome = null;
        for (Path.Node node : path) {
            if (node.getName() != null) {
                nome = node.getName();
            }
        }
        return nome != null ? nome : "validacao";
    }
}
