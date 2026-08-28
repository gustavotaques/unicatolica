package br.edu.unicatolica.pacext.identidade.web;

import br.edu.unicatolica.pacext.identidade.dominio.AcessoNegadoException;
import br.edu.unicatolica.pacext.infraestrutura.web.ErroResponse;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Mapeia {@link AcessoNegadoException} para 403 no envelope {@link ErroResponse} (AD-5) —
 * padrão de recusa por perfil a ser reaproveitado por outros módulos.
 */
@Provider
public class AcessoNegadoExceptionMapper implements ExceptionMapper<AcessoNegadoException> {

    @Override
    public Response toResponse(AcessoNegadoException exception) {
        ErroResponse erro = ErroResponse.of(
                "ACESSO_NEGADO", "Você não tem permissão para executar esta ação.", null);
        return Response.status(Response.Status.FORBIDDEN)
                .type(MediaType.APPLICATION_JSON)
                .entity(erro)
                .build();
    }
}
