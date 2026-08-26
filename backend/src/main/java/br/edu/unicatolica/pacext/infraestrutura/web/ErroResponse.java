package br.edu.unicatolica.pacext.infraestrutura.web;

/**
 * Envelope de erro padrão (AD-5 da Architecture Spine): toda resposta de erro de todo
 * endpoint REST segue exatamente esta forma — {@code {"error": {"code","message","details"}}}
 * — nenhum módulo inventa o próprio formato. Espelha o schema {@code Erro} de
 * {@code openapi.yaml}.
 */
public record ErroResponse(Erro error) {

    public record Erro(String code, String message, String details) {
    }

    public static ErroResponse of(String code, String message, String details) {
        return new ErroResponse(new Erro(code, message, details));
    }
}
