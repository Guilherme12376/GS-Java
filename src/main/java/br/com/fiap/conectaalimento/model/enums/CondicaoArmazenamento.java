package br.com.fiap.conectaalimento.model.enums;

/** Condição em que o alimento fica armazenado até a retirada. */
public enum CondicaoArmazenamento implements Descritivel {

    TEMPERATURA_AMBIENTE("Temperatura ambiente"),
    REFRIGERADO("Refrigerado (0 °C a 5 °C)"),
    CONGELADO("Congelado (-18 °C ou menos)");

    private final String descricao;

    CondicaoArmazenamento(String descricao) {
        this.descricao = descricao;
    }

    @Override
    public String getDescricao() {
        return descricao;
    }
}
