package br.com.fiap.conectaalimento.model.enums;

/** Tipos de instituição social que recebem doações. */
public enum TipoInstituicao implements Descritivel {

    ONG("ONG"),
    COZINHA_COMUNITARIA("Cozinha comunitária"),
    ABRIGO("Abrigo / casa de acolhida"),
    PROJETO_SOCIAL("Projeto social / coletivo"),
    OUTRA("Outra organização social");

    private final String descricao;

    TipoInstituicao(String descricao) {
        this.descricao = descricao;
    }

    @Override
    public String getDescricao() {
        return descricao;
    }
}
