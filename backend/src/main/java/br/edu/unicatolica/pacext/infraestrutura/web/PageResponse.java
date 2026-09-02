package br.edu.unicatolica.pacext.infraestrutura.web;

import java.util.List;

/**
 * Envelope de paginação padrão (AD-4) — todo endpoint de listagem devolve isso, nunca
 * inventa a própria forma de paginar. Espelha o schema {@code PageResponse} de
 * {@code openapi.yaml}.
 */
public record PageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages) {

    public static <T> PageResponse<T> de(List<T> content, int page, int size, long totalElements) {
        int totalPages = size <= 0 ? 0 : (int) Math.ceil((double) totalElements / size);
        return new PageResponse<>(content, page, size, totalElements, totalPages);
    }
}
