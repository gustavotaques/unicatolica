package br.edu.unicatolica.pacext.identidade.dominio;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PasswordHasherTest {

    private final PasswordHasher hasher = new PasswordHasher();

    @Test
    void gerarHashNuncaRetornaASenhaEmTextoPuro() {
        String hash = hasher.gerarHash("Senha123!");

        assertNotEquals("Senha123!", hash);
        assertTrue(hash.startsWith("$2"));
    }

    @Test
    void confereRetornaTrueParaSenhaCorreta() {
        String hash = hasher.gerarHash("Senha123!");

        assertTrue(hasher.confere("Senha123!", hash));
    }

    @Test
    void confereRetornaFalseParaSenhaIncorreta() {
        String hash = hasher.gerarHash("Senha123!");

        assertFalse(hasher.confere("senha-errada", hash));
    }
}
