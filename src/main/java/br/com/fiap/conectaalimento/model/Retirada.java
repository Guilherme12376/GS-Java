package br.com.fiap.conectaalimento.model;

import br.com.fiap.conectaalimento.exception.RegraNegocioException;

import java.time.LocalDateTime;

/**
 * Agendamento e confirmação da retirada de uma doação. Tabela T_CA_RETIRADA.
 * A doação só é concluída quando o doador confirma a ENTREGA e a instituição o RECEBIMENTO.
 */
public class Retirada extends Entidade {

    private Solicitacao solicitacao;
    private LocalDateTime dataHoraAgendada;
    private boolean entregaConfirmada;
    private LocalDateTime dataEntrega;
    private Double quantidadeEntregue;
    private boolean recebimentoConfirmado;
    private LocalDateTime dataRecebimento;
    private Double quantidadeRecebida;
    private String observacao;

    /** Construtor padrão. */
    public Retirada() {
        super();
    }

    /** Construtor não padrão. */
    public Retirada(Long id, Solicitacao solicitacao, LocalDateTime dataHoraAgendada) {
        super(id);
        this.solicitacao = solicitacao;
        this.dataHoraAgendada = dataHoraAgendada;
    }

    public boolean possuiConfirmacao() {
        return entregaConfirmada || recebimentoConfirmado;
    }

    public void reagendar(LocalDateTime novaDataHora) {
        if (possuiConfirmacao()) {
            throw new RegraNegocioException("Não é possível reagendar: a retirada já tem confirmação registrada.");
        }
        this.dataHoraAgendada = novaDataHora;
    }

    public void confirmarEntrega(double quantidade) {
        if (entregaConfirmada) {
            throw new RegraNegocioException("A entrega desta retirada já foi confirmada.");
        }
        this.quantidadeEntregue = quantidade;
        this.dataEntrega = LocalDateTime.now().withNano(0);
        this.entregaConfirmada = true;
    }

    /** [POLIMORFISMO - overload] Confirma o recebimento sem observação. */
    public void confirmarRecebimento(double quantidade) {
        confirmarRecebimento(quantidade, null);
    }

    /** [POLIMORFISMO - overload] Confirma o recebimento com observação. */
    public void confirmarRecebimento(double quantidade, String observacaoRecebimento) {
        if (recebimentoConfirmado) {
            throw new RegraNegocioException("O recebimento desta retirada já foi confirmado.");
        }
        this.quantidadeRecebida = quantidade;
        this.dataRecebimento = LocalDateTime.now().withNano(0);
        this.observacao = observacaoRecebimento;
        this.recebimentoConfirmado = true;
    }

    /** Doação concluída = entrega e recebimento confirmados (no JSON: "concluida"). */
    public boolean isConcluida() {
        return entregaConfirmada && recebimentoConfirmado;
    }

    /** [POLIMORFISMO - override] */
    @Override
    public String resumo() {
        return "Retirada #" + getId() + " agendada para " + dataHoraAgendada;
    }

    public Solicitacao getSolicitacao() {
        return solicitacao;
    }

    public void setSolicitacao(Solicitacao solicitacao) {
        this.solicitacao = solicitacao;
    }

    public LocalDateTime getDataHoraAgendada() {
        return dataHoraAgendada;
    }

    public void setDataHoraAgendada(LocalDateTime dataHoraAgendada) {
        this.dataHoraAgendada = dataHoraAgendada;
    }

    public boolean isEntregaConfirmada() {
        return entregaConfirmada;
    }

    public void setEntregaConfirmada(boolean entregaConfirmada) {
        this.entregaConfirmada = entregaConfirmada;
    }

    public LocalDateTime getDataEntrega() {
        return dataEntrega;
    }

    public void setDataEntrega(LocalDateTime dataEntrega) {
        this.dataEntrega = dataEntrega;
    }

    public Double getQuantidadeEntregue() {
        return quantidadeEntregue;
    }

    public void setQuantidadeEntregue(Double quantidadeEntregue) {
        this.quantidadeEntregue = quantidadeEntregue;
    }

    public boolean isRecebimentoConfirmado() {
        return recebimentoConfirmado;
    }

    public void setRecebimentoConfirmado(boolean recebimentoConfirmado) {
        this.recebimentoConfirmado = recebimentoConfirmado;
    }

    public LocalDateTime getDataRecebimento() {
        return dataRecebimento;
    }

    public void setDataRecebimento(LocalDateTime dataRecebimento) {
        this.dataRecebimento = dataRecebimento;
    }

    public Double getQuantidadeRecebida() {
        return quantidadeRecebida;
    }

    public void setQuantidadeRecebida(Double quantidadeRecebida) {
        this.quantidadeRecebida = quantidadeRecebida;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }
}
