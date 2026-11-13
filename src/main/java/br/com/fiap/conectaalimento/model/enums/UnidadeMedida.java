package br.com.fiap.conectaalimento.model.enums;

/**
 * Unidades de medida da quantidade de um alimento.
 */
public enum UnidadeMedida implements Descritivel {

    KG("Quilogramas", "kg"),
    UNIDADE("Unidades", "un."),
    LITRO("Litros", "L"),
    PACOTE("Pacotes / caixas", "pct.");

    private final String descricao;
    private final String sigla;

    UnidadeMedida(String descricao, String sigla) {
        this.descricao = descricao;
        this.sigla = sigla;
    }

    @Override
    public String getDescricao() {
        return descricao;
    }

    public String getSigla() {
        return sigla;
    }
}
