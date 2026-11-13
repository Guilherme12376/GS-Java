package br.com.fiap.conectaalimento.model.enums;

/**
 * Categorias de alimentos. Algumas exigem refrigeração (regra de segurança alimentar).
 */
public enum CategoriaAlimento implements Descritivel {

    PANIFICACAO("Panificação", false),
    HORTIFRUTI("Hortifruti", false),
    LATICINIOS("Laticínios", true),
    CARNES("Carnes e frios", true),
    REFEICAO_PRONTA("Refeição pronta", false),
    MERCEARIA("Mercearia (secos)", false),
    BEBIDAS("Bebidas", false);

    private final String descricao;
    private final boolean exigeRefrigeracao;

    CategoriaAlimento(String descricao, boolean exigeRefrigeracao) {
        this.descricao = descricao;
        this.exigeRefrigeracao = exigeRefrigeracao;
    }

    @Override
    public String getDescricao() {
        return descricao;
    }

    public boolean exigeRefrigeracao() {
        return exigeRefrigeracao;
    }
}
