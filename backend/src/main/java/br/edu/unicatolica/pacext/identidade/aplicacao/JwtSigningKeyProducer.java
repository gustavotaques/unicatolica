package br.edu.unicatolica.pacext.identidade.aplicacao;

import io.smallrye.jwt.util.KeyUtils;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import java.security.PrivateKey;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Produz a {@link PrivateKey} usada por {@link AuthService} para assinar o token de
 * sessão emitido no login — par correspondente à chave pública configurada em
 * {@code mp.jwt.verify.publickey} que o {@code JwtSecurityFilter} usa para validar (AD-2).
 *
 * <p>Lida do conteúdo PEM bruto da variável de ambiente {@code JWT_PRIVATE_KEY} (nunca de
 * um arquivo commitado — ver {@code .env.example}), para que nenhuma chave privada, real
 * ou de desenvolvimento, fique versionada no repositório (RNF04/OWASP ASVS).</p>
 *
 * <p>Escopo {@code @Dependent} (padrão, sem anotação) de propósito: um produtor de
 * {@link PrivateKey} em escopo normal (ex.: {@code @ApplicationScoped}) faz o Arc gerar
 * um client proxy da chave — e a API JCA ({@code Signature#initSign}) rejeita esse proxy
 * porque resolve o provider criptográfico pela classe concreta, não pela interface.</p>
 */
@ApplicationScoped
public class JwtSigningKeyProducer {

    @ConfigProperty(name = "pacext.jwt.sign.key")
    String chaveConteudo;

    @Produces
    public PrivateKey privateKey() throws Exception {
        return KeyUtils.decodePrivateKey(chaveConteudo);
    }
}
