package br.edu.unicatolica.pacext.identidade.aplicacao;

import io.smallrye.jwt.util.KeyUtils;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import java.security.PrivateKey;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Produz a {@link PrivateKey} usada por {@link AuthService} para assinar o token de
 * sessão emitido no login — par correspondente à {@code publicKey.pem} que o
 * {@code JwtSecurityFilter} usa para validar (AD-2).
 *
 * <p>Escopo {@code @Dependent} (padrão, sem anotação) de propósito: um produtor de
 * {@link PrivateKey} em escopo normal (ex.: {@code @ApplicationScoped}) faz o Arc gerar
 * um client proxy da chave — e a API JCA ({@code Signature#initSign}) rejeita esse proxy
 * porque resolve o provider criptográfico pela classe concreta, não pela interface.</p>
 */
@ApplicationScoped
public class JwtSigningKeyProducer {

    @ConfigProperty(name = "pacext.jwt.sign.key.location")
    String chaveLocation;

    @Produces
    public PrivateKey privateKey() throws Exception {
        return KeyUtils.readPrivateKey(chaveLocation);
    }
}
