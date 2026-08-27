package br.edu.unicatolica.pacext.identidade;

import java.security.SecureRandom;
import java.util.Base64;

/** Gera o token de uso único do link de confirmação de e-mail (RF01.2, Story 1.3). */
final class GeradorTokenConfirmacao {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int TAMANHO_BYTES = 32;

    private GeradorTokenConfirmacao() {
    }

    static String gerar() {
        byte[] bytes = new byte[TAMANHO_BYTES];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
