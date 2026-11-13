package br.com.fiap.conectaalimento.dto;

import br.com.fiap.conectaalimento.model.enums.TipoInstituicao;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Corpo do POST/PUT de instituição receptora. No PUT, a senha é opcional (sem senha = mantém a atual). */
public record InstituicaoRequest(
        @NotBlank(message = "é obrigatório") @Size(min = 3, max = 120, message = "deve ter entre 3 e 120 caracteres")
        String nome,
        @NotBlank(message = "é obrigatório") @Size(min = 3, max = 100, message = "deve ter entre 3 e 100 caracteres")
        String responsavel,
        @NotBlank(message = "é obrigatório") @Email(message = "formato inválido") @Size(max = 120, message = "muito longo")
        String email,
        @Size(min = 6, max = 60, message = "deve ter entre 6 e 60 caracteres")
        String senha,
        @NotBlank(message = "é obrigatório")
        @Pattern(regexp = "^\\(?\\d{2}\\)?\\s?9?\\d{4}-?\\d{4}$", message = "use DDD + número (ex.: 11987654321)")
        String telefone,
        @NotNull(message = "é obrigatório (ONG, COZINHA_COMUNITARIA, ABRIGO, PROJETO_SOCIAL ou OUTRA)")
        TipoInstituicao tipoInstituicao,
        @NotNull(message = "é obrigatória") @Min(value = 1, message = "deve ser maior que zero")
        @Max(value = 100000, message = "valor muito alto")
        Integer capacidadeAtendimento,
        @NotNull(message = "é obrigatório") @Valid
        EnderecoRequest endereco) {
}
