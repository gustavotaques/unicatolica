package br.edu.unicatolica.pacext.identidade.infraestrutura;

import br.edu.unicatolica.pacext.identidade.dominio.NaoAutenticadoException;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.jwt.JsonWebToken;

/**
 * Leitura da identidade autenticada da requisição corrente (Story 1.5, RF12/RF13).
 *
 * <p>Não substitui o {@code JwtSecurityFilter} (AD-2) — só expõe, via bean CDI
 * {@code @RequestScoped}, o que o SmallRye JWT já populou como {@link JsonWebToken}
 * independentemente das properties que o filtro grava no {@code ContainerRequestContext}.
 * Ponto de reuso: qualquer módulo que precise decidir autorização fina por perfil injeta
 * este bean em vez de ler {@code ContainerRequestContext}/nomes de property string.</p>
 */
@RequestScoped
public class UsuarioAutenticado {

    @Inject
    JsonWebToken jsonWebToken;

    /**
     * @return id (claim {@code sub}) do usuário autenticado na requisição corrente.
     * @throws NaoAutenticadoException se a claim {@code sub} estiver ausente ou não for
     *     numérica — guarda defensiva contra drift entre o {@code JwtSecurityFilter} (que já
     *     validou o token) e o {@link JsonWebToken} injetado via CDI; nunca deveria ocorrer
     *     em uso normal.
     */
    public Long id() {
        try {
            return Long.valueOf(jsonWebToken.getSubject());
        } catch (NumberFormatException e) {
            throw new NaoAutenticadoException();
        }
    }

    /** @return {@code true} se a claim {@code roles} do token contém o perfil informado. */
    public boolean possuiPerfil(String perfil) {
        return jsonWebToken.getGroups() != null && jsonWebToken.getGroups().contains(perfil);
    }
}
