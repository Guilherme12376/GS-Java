package br.com.fiap.conectaalimento.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** Corpo do login. */
public record LoginRequest(
        @NotBlank(message = "é obrigatório") @Email(message = "formato inválido") String email,
        @NotBlank(message = "é obrigatória") String senha) {
}
