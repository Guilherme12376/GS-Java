package br.com.fiap.conectaalimento.model.enums;

/**
 * Contrato dos enums do domínio que possuem uma descrição amigável
 * (usada nas mensagens de erro e de negócio).
 */
public interface Descritivel {

    String getDescricao();
}
