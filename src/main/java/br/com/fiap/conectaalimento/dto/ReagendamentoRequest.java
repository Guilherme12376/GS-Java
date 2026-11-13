package br.com.fiap.conectaalimento.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/** Corpo do PUT de retirada (novo horário). */
public record ReagendamentoRequest(
        @NotNull(message = "é obrigatória") @Future(message = "deve estar no futuro") LocalDateTime dataHoraAgendada) {
}
