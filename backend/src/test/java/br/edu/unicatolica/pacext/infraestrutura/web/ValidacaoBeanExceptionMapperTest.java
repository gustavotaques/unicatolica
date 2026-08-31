package br.edu.unicatolica.pacext.infraestrutura.web;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.edu.unicatolica.pacext.identidade.web.CadastroRequest;
import br.edu.unicatolica.pacext.identidade.web.CadastroResource;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.executable.ExecutableValidator;
import jakarta.ws.rs.core.Response;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * Defeito D6: a resposta de erro de validação vazava o caminho interno do parâmetro do
 * método JAX-RS (ex.: "registrar.request.email") em vez de só o nome do campo que o
 * usuário reconhece ("email"). Reproduz o cenário via validação de método real (o mesmo
 * mecanismo que o JAX-RS usa para {@code @Valid} em parâmetro de recurso), não um
 * {@link ConstraintViolation} fabricado à mão, pra não mascarar a forma real do path.
 */
class ValidacaoBeanExceptionMapperTest {

    private final ValidacaoBeanExceptionMapper mapper = new ValidacaoBeanExceptionMapper();

    @Test
    void detalhesNaoVazaCaminhoInternoDoParametroJaxRs() throws NoSuchMethodException {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        ExecutableValidator executableValidator = validator.forExecutables();
        CadastroResource resource = new CadastroResource();
        Method metodo = CadastroResource.class.getMethod("registrar", CadastroRequest.class);
        CadastroRequest requestInvalido = new CadastroRequest(
                "Nome", "email-invalido", "Senha123!", "Curso", LocalDate.of(2000, 1, 1));

        Set<ConstraintViolation<CadastroResource>> violacoes =
                executableValidator.validateParameters(resource, metodo, new Object[] { requestInvalido });
        ConstraintViolationException exception = new ConstraintViolationException(violacoes);

        Response resposta = mapper.toResponse(exception);
        ErroResponse corpo = (ErroResponse) resposta.getEntity();

        assertFalse(corpo.error().details().contains("registrar"), "não pode vazar o nome do método JAX-RS");
        assertFalse(corpo.error().details().contains("arg0"), "não pode vazar o índice/nome interno do parâmetro");
        assertTrue(corpo.error().details().contains("email: formato de e-mail inválido"),
                "precisa manter o nome do campo e a mensagem, só sem o prefixo interno");
    }
}
