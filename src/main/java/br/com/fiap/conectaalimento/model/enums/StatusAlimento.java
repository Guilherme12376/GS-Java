package br.com.fiap.conectaalimento.model.enums;

/** Ciclo de vida de um alimento: DISPONIVEL > SOLICITADO > RESERVADO > RETIRADA_AGENDADA > CONCLUIDO (ou REMOVIDO / EXPIRADO). */
public enum StatusAlimento implements Descritivel {

    DISPONIVEL("Disponível"),
    SOLICITADO("Solicitado"),
    RESERVADO("Reservado"),
    RETIRADA_AGENDADA("Retirada agendada"),
    CONCLUIDO("Concluído"),
    REMOVIDO("Removido"),
    EXPIRADO("Expirado");

    private final String descricao;

    StatusAlimento(String descricao) {
        this.descricao = descricao;
    }

    @Override
    public String getDescricao() {
        return descricao;
    }
}
