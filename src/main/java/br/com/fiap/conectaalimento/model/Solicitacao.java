package br.com.fiap.conectaalimento.model;

import br.com.fiap.conectaalimento.exception.RegraNegocioException;
import br.com.fiap.conectaalimento.model.enums.StatusSolicitacao;

import java.time.LocalDateTime;

/**
 * Pedido de uma instituição para receber um alimento. Tabela T_CA_SOLICITACAO.
 * Concentra as transições de estado: PENDENTE -> ACEITA -> AGENDADA -> CONCLUIDA
 * (ou RECUSADA / NAO_ATENDIDA / CANCELADA).
 */
public class Solicitacao extends Entidade {

    private Alimento alimento;
    private InstituicaoReceptora instituicao;
    private double quantidadeSolicitada;
    private StatusSolicitacao status;
    private String motivo;
    private LocalDateTime dataResposta;

    /** Construtor padrão. */
    public Solicitacao() {
        super();
        this.status = StatusSolicitacao.PENDENTE;
    }

    /** Construtor não padrão. */
    public Solicitacao(Long id, Alimento alimento, InstituicaoReceptora instituicao, double quantidadeSolicitada) {
        super(id);
        this.alimento = alimento;
        this.instituicao = instituicao;
        this.quantidadeSolicitada = quantidadeSolicitada;
        this.status = StatusSolicitacao.PENDENTE;
    }

    /** Solicitação em andamento (aparece no JSON como "ativa"). */
    public boolean isAtiva() {
        return status == StatusSolicitacao.PENDENTE || status == StatusSolicitacao.ACEITA
                || status == StatusSolicitacao.AGENDADA;
    }

    public void aceitar() {
        exigirStatus(StatusSolicitacao.PENDENTE, "aceitar");
        status = StatusSolicitacao.ACEITA;
        dataResposta = LocalDateTime.now().withNano(0);
    }

    /** [POLIMORFISMO - overload] Recusa sem motivo (o motivo é opcional). */
    public void recusar() {
        recusar("Motivo não informado pelo doador");
    }

    /** [POLIMORFISMO - overload] Recusa informando o motivo. */
    public void recusar(String motivoRecusa) {
        exigirStatus(StatusSolicitacao.PENDENTE, "recusar");
        status = StatusSolicitacao.RECUSADA;
        motivo = motivoRecusa;
        dataResposta = LocalDateTime.now().withNano(0);
    }

    public void marcarComoNaoAtendida(String motivoNaoAtendimento) {
        if (isAtiva()) {
            status = StatusSolicitacao.NAO_ATENDIDA;
            motivo = motivoNaoAtendimento;
            dataResposta = LocalDateTime.now().withNano(0);
        }
    }

    public void cancelar(String motivoCancelamento) {
        if (isAtiva()) {
            status = StatusSolicitacao.CANCELADA;
            motivo = motivoCancelamento;
            dataResposta = LocalDateTime.now().withNano(0);
        }
    }

    public void alterarQuantidade(double novaQuantidade) {
        exigirStatus(StatusSolicitacao.PENDENTE, "alterar");
        if (novaQuantidade <= 0 || novaQuantidade > alimento.getQuantidade()) {
            throw new RegraNegocioException("A quantidade deve ser maior que zero e no máximo "
                    + alimento.getQuantidade() + " " + alimento.getUnidade().getSigla() + ".");
        }
        this.quantidadeSolicitada = novaQuantidade;
    }

    private void exigirStatus(StatusSolicitacao esperado, String acao) {
        if (status != esperado) {
            throw new RegraNegocioException("Não é possível " + acao + " a solicitação #" + getId()
                    + ": o status atual é " + status + " e precisa ser " + esperado + ".");
        }
    }

    /** [POLIMORFISMO - override] */
    @Override
    public String resumo() {
        return "Solicitação #" + getId() + " (" + status + ")";
    }

    public Alimento getAlimento() {
        return alimento;
    }

    public void setAlimento(Alimento alimento) {
        this.alimento = alimento;
    }

    public InstituicaoReceptora getInstituicao() {
        return instituicao;
    }

    public void setInstituicao(InstituicaoReceptora instituicao) {
        this.instituicao = instituicao;
    }

    public double getQuantidadeSolicitada() {
        return quantidadeSolicitada;
    }

    public void setQuantidadeSolicitada(double quantidadeSolicitada) {
        this.quantidadeSolicitada = quantidadeSolicitada;
    }

    public StatusSolicitacao getStatus() {
        return status;
    }

    public void setStatus(StatusSolicitacao status) {
        this.status = status;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public LocalDateTime getDataResposta() {
        return dataResposta;
    }

    public void setDataResposta(LocalDateTime dataResposta) {
        this.dataResposta = dataResposta;
    }
}
