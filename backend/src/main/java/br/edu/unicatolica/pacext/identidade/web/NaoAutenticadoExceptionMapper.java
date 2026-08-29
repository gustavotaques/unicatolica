package br.edu.unicatolica.pacext.identidade.web;

import br.edu.unicatolica.pacext.identidade.dominio.NaoAutenticadoException;
import br.edu.unicatolica.pacext.infraestrutura.web.ErroResponse;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Mapeia {@link NaoAutenticadoException} para 401 no envelope {@link ErroResponse} (AD-5) —
 * mesmo código {@code NAO_AUTENTICADO} usado pelo {@code JwtSecurityFilter} para falhas de
 * autenticação, agora para o caso em que a identidade não pôde ser lida do token já validado.
 */
@Provider
public class NaoAutenticadoExceptionMapper implements ExceptionMapper<NaoAutenticadoException> {

    @Override
    public Response toResponse(NaoAutenticadoException exception) {
        ErroResponse erro = ErroResponse.of("NAO_AUTENTICADO", "Autenticação necessária.", null);
        return Response.status(Response.Status.UNAUTHORIZED)
                .type(MediaType.APPLICATION_JSON)
                .entity(erro)
                .build();
    }
}
