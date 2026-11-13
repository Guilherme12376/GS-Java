package br.com.fiap.conectaalimento.model.enums;

/** Tipos de estabelecimento comercial que podem doar alimentos. */
public enum TipoEstabelecimento implements Descritivel {

    PADARIA("Padaria"),
    MERCADO("Mercado / supermercado"),
    RESTAURANTE("Restaurante / lanchonete"),
    HORTIFRUTI("Hortifruti / feira"),
    OUTRO("Outro comércio de alimentos");

    private final String descricao;

    TipoEstabelecimento(String descricao) {
        this.descricao = descricao;
    }

    @Override
    public String getDescricao() {
        return descricao;
    }
}
