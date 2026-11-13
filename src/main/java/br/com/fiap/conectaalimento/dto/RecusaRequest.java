package br.com.fiap.conectaalimento.dto;

import jakarta.validation.constraints.Size;

/** Corpo (opcional) da recusa de uma solicitação. */
public record RecusaRequest(
        @Size(max = 255, message = "deve ter no máximo 255 caracteres") String motivo) {
}
