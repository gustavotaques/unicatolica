package br.edu.unicatolica.pacext.identidade.dominio;

/**
 * Lançada quando a identidade do usuário autenticado não pode ser lida a partir do token
 * já validado pelo {@code JwtSecurityFilter} (AD-2) — ex.: claim {@code sub} ausente ou
 * não numérica — mapeada para 401 {@code ErroResponse} por
 * {@code NaoAutenticadoExceptionMapper}. Guarda defensiva: o filtro já garante a
 * presença/validade do token, então este caso só ocorre em drift de configuração entre
 * o filtro e o {@code JsonWebToken} injetado via CDI.
 */
public class NaoAutenticadoException extends RuntimeException {
}
