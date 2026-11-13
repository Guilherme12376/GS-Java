package br.com.fiap.conectaalimento.dto;

import br.com.fiap.conectaalimento.model.enums.Bairro;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Endereço enviado nos cadastros. Latitude/longitude são opcionais (sem elas, usa o centro do bairro). */
public record EnderecoRequest(
        @NotBlank(message = "é obrigatório") @Size(max = 120, message = "deve ter no máximo 120 caracteres")
        String logradouro,
        @NotBlank(message = "é obrigatório") @Size(max = 10, message = "deve ter no máximo 10 caracteres")
        String numero,
        @NotNull(message = "é obrigatório (BOM_RETIRO, PARI ou BRAS)")
        Bairro bairro,
        @NotBlank(message = "é obrigatório") @Pattern(regexp = "^\\d{5}-?\\d{3}$", message = "deve ter 8 dígitos (ex.: 01121-000)")
        String cep,
        @DecimalMin(value = "-90", message = "inválida") @DecimalMax(value = "90", message = "inválida")
        Double latitude,
        @DecimalMin(value = "-180", message = "inválida") @DecimalMax(value = "180", message = "inválida")
        Double longitude) {
}
