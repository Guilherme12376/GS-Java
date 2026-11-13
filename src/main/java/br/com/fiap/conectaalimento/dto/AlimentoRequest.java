package br.com.fiap.conectaalimento.dto;

import br.com.fiap.conectaalimento.model.enums.CategoriaAlimento;
import br.com.fiap.conectaalimento.model.enums.CondicaoArmazenamento;
import br.com.fiap.conectaalimento.model.enums.TipoAlimento;
import br.com.fiap.conectaalimento.model.enums.UnidadeMedida;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Corpo do POST/PUT de alimento.
 * PERECIVEL exige condicaoArmazenamento; NAO_PERECIVEL exige embalagemIntegra = true.
 * inicioRetirada é opcional (padrão: agora).
 */
public record AlimentoRequest(
        @NotNull(message = "é obrigatório") Long estabelecimentoId,
        @NotBlank(message = "é obrigatório") @Size(min = 3, max = 120, message = "deve ter entre 3 e 120 caracteres")
        String nome,
        @NotNull(message = "é obrigatório (PERECIVEL ou NAO_PERECIVEL)") TipoAlimento tipo,
        @NotNull(message = "é obrigatória") CategoriaAlimento categoria,
        @NotNull(message = "é obrigatória") @Positive(message = "deve ser maior que zero")
        @DecimalMax(value = "100000", message = "valor muito alto") Double quantidade,
        @NotNull(message = "é obrigatória (KG, UNIDADE, LITRO ou PACOTE)") UnidadeMedida unidade,
        @NotNull(message = "é obrigatória") LocalDate dataValidade,
        LocalDateTime inicioRetirada,
        @NotNull(message = "é obrigatório") LocalDateTime fimRetirada,
        CondicaoArmazenamento condicaoArmazenamento,
        Boolean embalagemIntegra,
        @Size(max = 30, message = "deve ter no máximo 30 caracteres") String lote,
        @Size(max = 255, message = "deve ter no máximo 255 caracteres") String observacoes,
        @NotNull(message = "é obrigatória (ciência das orientações de segurança alimentar)")
        Boolean declaracaoSeguranca) {
}
