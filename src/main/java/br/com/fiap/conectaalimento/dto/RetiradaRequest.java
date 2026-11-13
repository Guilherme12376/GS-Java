package br.com.fiap.conectaalimento.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/** Corpo do POST de retirada (agenda a coleta de uma solicitação ACEITA). */
public record RetiradaRequest(
        @NotNull(message = "é obrigatório") Long solicitacaoId,
        @NotNull(message = "é obrigatória") @Future(message = "deve estar no futuro") LocalDateTime dataHoraAgendada) {
}
