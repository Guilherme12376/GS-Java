package br.com.fiap.conectaalimento.dto;

import br.com.fiap.conectaalimento.model.enums.TipoEstabelecimento;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Corpo do POST/PUT de estabelecimento doador. No PUT, a senha é opcional (sem senha = mantém a atual). */
public record EstabelecimentoRequest(
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
        @NotBlank(message = "é obrigatório")
        @Pattern(regexp = "^\\d{2}\\.?\\d{3}\\.?\\d{3}/?\\d{4}-?\\d{2}$", message = "deve ter 14 dígitos")
        String cnpj,
        @NotNull(message = "é obrigatório (PADARIA, MERCADO, RESTAURANTE, HORTIFRUTI ou OUTRO)")
        TipoEstabelecimento tipoEstabelecimento,
        @NotNull(message = "é obrigatório") @Valid
        EnderecoRequest endereco) {
}
