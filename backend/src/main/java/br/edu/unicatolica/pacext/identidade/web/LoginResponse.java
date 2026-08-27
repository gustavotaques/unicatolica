package br.edu.unicatolica.pacext.identidade.web;

/** Resposta de {@code POST /auth/login} bem-sucedido — token via header Bearer daqui em diante (AD-2). */
public record LoginResponse(String token) {
}
