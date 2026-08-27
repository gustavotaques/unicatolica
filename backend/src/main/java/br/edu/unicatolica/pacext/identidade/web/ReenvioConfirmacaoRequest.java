package br.edu.unicatolica.pacext.identidade.web;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** Corpo de {@code POST /auth/confirmacao-email/reenvio} (Story 1.3). */
public record ReenvioConfirmacaoRequest(
        @NotBlank(message = "email é obrigatório") @Email(message = "formato de e-mail inválido") String email) {
}
