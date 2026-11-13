package br.com.fiap.conectaalimento.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/** Corpo do POST de solicitação (instituição pede um alimento). */
public record SolicitacaoRequest(
        @NotNull(message = "é obrigatório") Long alimentoId,
        @NotNull(message = "é obrigatório") Long instituicaoId,
        @NotNull(message = "é obrigatória") @Positive(message = "deve ser maior que zero") Double quantidade) {
}
