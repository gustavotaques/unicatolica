package br.edu.unicatolica.pacext.identidade.dominio;

import jakarta.enterprise.context.ApplicationScoped;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Gera o token de uso único do link de confirmação de e-mail (RF01.2, Story 1.3) —
 * mesmo espírito de {@link PasswordHasher}: encapsula um detalhe de segurança do módulo
 * Identidade num bean próprio, em vez de espalhar {@link SecureRandom} pelos serviços.
 */
@ApplicationScoped
public class GeradorTokenConfirmacao {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int TAMANHO_BYTES = 32;

    public String gerar() {
        byte[] bytes = new byte[TAMANHO_BYTES];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
