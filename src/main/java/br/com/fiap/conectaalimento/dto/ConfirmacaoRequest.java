package br.com.fiap.conectaalimento.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/** Corpo (opcional) das confirmações de entrega/recebimento. Sem quantidade = quantidade solicitada. */
public record ConfirmacaoRequest(
        @Positive(message = "deve ser maior que zero") Double quantidade,
        @Size(max = 255, message = "deve ter no máximo 255 caracteres") String observacao) {
}
