package br.com.fiap.conectaalimento.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/** Corpo do PUT de solicitação (altera a quantidade enquanto ela está PENDENTE). */
public record QuantidadeRequest(
        @NotNull(message = "é obrigatória") @Positive(message = "deve ser maior que zero") Double quantidade) {
}
