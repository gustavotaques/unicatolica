package br.edu.unicatolica.pacext.identidade.dominio;

import at.favre.lib.crypto.bcrypt.BCrypt;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Encapsula o hashing de senha do módulo Identidade — nenhum outro ponto do sistema
 * compara ou gera hash de senha diretamente (RF06/RF07, RNF04/OWASP ASVS).
 */
@ApplicationScoped
public class PasswordHasher {

    public String gerarHash(String senha) {
        return BCrypt.withDefaults().hashToString(12, senha.toCharArray());
    }

    public boolean confere(String senha, String hash) {
        return BCrypt.verifyer().verify(senha.toCharArray(), hash).verified;
    }
}
