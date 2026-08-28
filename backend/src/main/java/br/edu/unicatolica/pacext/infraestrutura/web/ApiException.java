package br.edu.unicatolica.pacext.infraestrutura.web;

import jakarta.ws.rs.core.Response;

/**
 * Exceção base para toda rejeição de negócio que deve virar uma resposta HTTP no envelope
 * padrão (AD-5) — nenhum {@code Resource}/{@code Service} monta {@link ErroResponse} à mão;
 * lança esta exceção (ou uma das fábricas estáticas) e deixa o {@link ApiExceptionMapper}
 * traduzir para a resposta.
 */
public class ApiException extends RuntimeException {

    /** Código HTTP como {@code int}, não {@link Response.Status} — o enum da JAX-RS não tem 422 (AD-5). */
    private final int status;
    private final String code;
    private final String details;

    public ApiException(int status, String code, String message, String details) {
        super(message);
        this.status = status;
        this.code = code;
        this.details = details;
    }

    public static ApiException validacao(String code, String message, String details) {
        return new ApiException(422, code, message, details);
    }

    public static ApiException naoAutenticado(String code, String message, String details) {
        return new ApiException(Response.Status.UNAUTHORIZED.getStatusCode(), code, message, details);
    }

    public static ApiException conflito(String code, String message, String details) {
        return new ApiException(Response.Status.CONFLICT.getStatusCode(), code, message, details);
    }

    public static ApiException naoEncontrado(String code, String message, String details) {
        return new ApiException(Response.Status.NOT_FOUND.getStatusCode(), code, message, details);
    }

    public static ApiException semPermissao(String code, String message, String details) {
        return new ApiException(Response.Status.FORBIDDEN.getStatusCode(), code, message, details);
    }

    public int getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }

    public String getDetails() {
        return details;
    }
}
