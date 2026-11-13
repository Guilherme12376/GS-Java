package br.com.fiap.conectaalimento.model.enums;

/** Situação de uma solicitação feita por uma instituição receptora. */
public enum StatusSolicitacao implements Descritivel {

    PENDENTE("Pendente"),
    ACEITA("Aceita"),
    RECUSADA("Recusada"),
    NAO_ATENDIDA("Não atendida"),
    CANCELADA("Cancelada"),
    AGENDADA("Retirada agendada"),
    CONCLUIDA("Concluída");

    private final String descricao;

    StatusSolicitacao(String descricao) {
        this.descricao = descricao;
    }

    @Override
    public String getDescricao() {
        return descricao;
    }
}
