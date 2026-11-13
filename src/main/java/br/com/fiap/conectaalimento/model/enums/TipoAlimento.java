package br.com.fiap.conectaalimento.model.enums;

/** Tipo do alimento: define qual subclasse de Alimento é criada (polimorfismo). */
public enum TipoAlimento implements Descritivel {

    PERECIVEL("Perecível"),
    NAO_PERECIVEL("Não perecível");

    private final String descricao;

    TipoAlimento(String descricao) {
        this.descricao = descricao;
    }

    @Override
    public String getDescricao() {
        return descricao;
    }
}
