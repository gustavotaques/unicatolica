package br.edu.unicatolica.pacext.identidade.web;

/** Corpo de {@code POST /auth/login} — JSON camelCase português (convenção do projeto). */
public record LoginRequest(String email, String senha) {
}
