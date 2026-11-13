package br.com.fiap.conectaalimento.bo;

import br.com.fiap.conectaalimento.dao.AlimentoDAO;
import br.com.fiap.conectaalimento.dao.RetiradaDAO;
import br.com.fiap.conectaalimento.dao.SolicitacaoDAO;
import br.com.fiap.conectaalimento.dto.RetiradaRequest;
import br.com.fiap.conectaalimento.exception.ConflitoException;
import br.com.fiap.conectaalimento.exception.RecursoNaoEncontradoException;
import br.com.fiap.conectaalimento.exception.RegraNegocioException;
import br.com.fiap.conectaalimento.model.Alimento;
import br.com.fiap.conectaalimento.model.Retirada;
import br.com.fiap.conectaalimento.model.Solicitacao;
import br.com.fiap.conectaalimento.model.enums.StatusAlimento;
import br.com.fiap.conectaalimento.model.enums.StatusSolicitacao;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Regras de negócio da logística: agendar a retirada (US12/US21), reagendar, confirmar entrega (US13),
 * confirmar recebimento (US22) e cancelar o agendamento. A doação conclui com as duas confirmações.
 */
@Service
public class RetiradaBO {

    private final RetiradaDAO retiradaDAO;
    private final SolicitacaoDAO solicitacaoDAO;
    private final AlimentoDAO alimentoDAO;

    public RetiradaBO(RetiradaDAO retiradaDAO, SolicitacaoDAO solicitacaoDAO, AlimentoDAO alimentoDAO) {
        this.retiradaDAO = retiradaDAO;
        this.solicitacaoDAO = solicitacaoDAO;
        this.alimentoDAO = alimentoDAO;
    }

    public List<Retirada> listar() {
        return retiradaDAO.listar();
    }

    public Retirada buscar(Long id) {
        return retiradaDAO.buscarPorId(id).orElseThrow(() -> new RecursoNaoEncontradoException("Retirada", id));
    }

    @Transactional
    public Retirada agendar(RetiradaRequest req) {
        Solicitacao solicitacao = solicitacaoDAO.buscarPorId(req.solicitacaoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Solicitação", req.solicitacaoId()));
        retiradaDAO.buscarPorSolicitacao(solicitacao.getId()).ifPresent(existente -> {
            throw new ConflitoException("A solicitação já tem a retirada #" + existente.getId()
                    + " agendada. Para mudar o horário use PUT /api/retiradas/" + existente.getId() + ".");
        });
        if (solicitacao.getStatus() != StatusSolicitacao.ACEITA) {
            throw new RegraNegocioException("Só é possível agendar a retirada de solicitações ACEITA (status atual: "
                    + solicitacao.getStatus() + ").");
        }
        Alimento alimento = solicitacao.getAlimento();
        validarHorario(alimento, req.dataHoraAgendada());

        Retirada retirada = retiradaDAO.inserir(new Retirada(null, solicitacao, req.dataHoraAgendada()));
        solicitacao.setStatus(StatusSolicitacao.AGENDADA);
        solicitacaoDAO.atualizar(solicitacao);
        alimentoDAO.atualizarStatus(alimento.getId(), StatusAlimento.RETIRADA_AGENDADA);
        alimento.setStatus(StatusAlimento.RETIRADA_AGENDADA);
        return retirada;
    }

    @Transactional
    public Retirada reagendar(Long id, LocalDateTime novaDataHora) {
        Retirada retirada = buscar(id);
        validarHorario(retirada.getSolicitacao().getAlimento(), novaDataHora);
        retirada.reagendar(novaDataHora);
        retiradaDAO.atualizar(retirada);
        return retirada;
    }

    /** US13 - Doador confirma a entrega (sem quantidade = quantidade solicitada). */
    @Transactional
    public Retirada confirmarEntrega(Long id, Double quantidade) {
        Retirada retirada = buscar(id);
        double qtd = validarQuantidade(retirada, quantidade);
        retirada.confirmarEntrega(qtd);
        retiradaDAO.atualizar(retirada);
        concluirSeConfirmada(retirada);
        return retirada;
    }

    /** US22 - Instituição confirma o recebimento, com observação opcional. */
    @Transactional
    public Retirada confirmarRecebimento(Long id, Double quantidade, String observacao) {
        Retirada retirada = buscar(id);
        double qtd = validarQuantidade(retirada, quantidade);
        if (observacao == null || observacao.isBlank()) {
            retirada.confirmarRecebimento(qtd);
        } else {
            retirada.confirmarRecebimento(qtd, observacao.trim());
        }
        retiradaDAO.atualizar(retirada);
        concluirSeConfirmada(retirada);
        return retirada;
    }

    /** Cancela o agendamento (sem confirmações): a solicitação volta para ACEITA. */
    @Transactional
    public void cancelar(Long id) {
        Retirada retirada = buscar(id);
        if (retirada.possuiConfirmacao()) {
            throw new RegraNegocioException("A retirada já tem confirmação registrada e não pode ser cancelada.");
        }
        Solicitacao solicitacao = retirada.getSolicitacao();
        retiradaDAO.remover(id);
        if (solicitacao.getStatus() == StatusSolicitacao.AGENDADA) {
            solicitacao.setStatus(StatusSolicitacao.ACEITA);
            solicitacaoDAO.atualizar(solicitacao);
            alimentoDAO.atualizarStatus(solicitacao.getAlimento().getId(), StatusAlimento.RESERVADO);
        }
    }

    private void validarHorario(Alimento alimento, LocalDateTime dataHora) {
        if (dataHora.isBefore(LocalDateTime.now())) {
            throw new RegraNegocioException("O horário da retirada precisa estar no futuro.");
        }
        if (!alimento.horarioDentroDaJanela(dataHora)) {
            throw new RegraNegocioException("O horário precisa estar dentro da janela de retirada do doador: "
                    + alimento.janelaFormatada() + ".");
        }
    }

    private double validarQuantidade(Retirada retirada, Double quantidade) {
        Solicitacao solicitacao = retirada.getSolicitacao();
        if (solicitacao.getStatus() != StatusSolicitacao.AGENDADA) {
            throw new RegraNegocioException("A solicitação desta retirada não está com status AGENDADA (status: "
                    + solicitacao.getStatus() + ").");
        }
        double qtd = quantidade == null ? solicitacao.getQuantidadeSolicitada() : quantidade;
        if (qtd <= 0 || qtd > solicitacao.getQuantidadeSolicitada()) {
            throw new RegraNegocioException("A quantidade deve ser maior que zero e no máximo "
                    + solicitacao.getQuantidadeSolicitada() + ".");
        }
        return qtd;
    }

    private void concluirSeConfirmada(Retirada retirada) {
        if (retirada.isConcluida()) {
            Solicitacao solicitacao = retirada.getSolicitacao();
            solicitacao.setStatus(StatusSolicitacao.CONCLUIDA);
            solicitacaoDAO.atualizar(solicitacao);
            alimentoDAO.atualizarStatus(solicitacao.getAlimento().getId(), StatusAlimento.CONCLUIDO);
            solicitacao.getAlimento().setStatus(StatusAlimento.CONCLUIDO);
        }
    }
}
