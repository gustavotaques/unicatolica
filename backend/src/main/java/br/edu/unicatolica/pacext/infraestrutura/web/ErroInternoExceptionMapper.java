package br.edu.unicatolica.pacext.infraestrutura.web;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

/**
 * Rede de segurança final (AD-5, status 500): qualquer exceção não tratada por um mapper
 * mais específico ({@link ApiExceptionMapper}, {@link ValidacaoBeanExceptionMapper}) cai
 * aqui. Loga a stack trace completa para diagnóstico, mas nunca vaza detalhe interno
 * (mensagem de exceção, stack trace) na resposta ao cliente.
 */
@Provider
public class ErroInternoExceptionMapper implements ExceptionMapper<Exception> {

    private static final Logger LOG = Logger.getLogger(ErroInternoExceptionMapper.class);

    @Override
    public Response toResponse(Exception exception) {
        LOG.error("Erro não tratado ao processar requisição.", exception);

        ErroResponse corpo = ErroResponse.of("ERRO_INTERNO", "Ocorreu um erro inesperado. Tente novamente.", null);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .type(MediaType.APPLICATION_JSON)
                .entity(corpo)
                .build();
    }
}
