package br.com.fiap.conectaalimento.bo;

import br.com.fiap.conectaalimento.dao.AlimentoDAO;
import br.com.fiap.conectaalimento.dao.InstituicaoDAO;
import br.com.fiap.conectaalimento.dao.SolicitacaoDAO;
import br.com.fiap.conectaalimento.dto.SolicitacaoRequest;
import br.com.fiap.conectaalimento.exception.ConflitoException;
import br.com.fiap.conectaalimento.exception.RecursoNaoEncontradoException;
import br.com.fiap.conectaalimento.exception.RegraNegocioException;
import br.com.fiap.conectaalimento.model.Alimento;
import br.com.fiap.conectaalimento.model.InstituicaoReceptora;
import br.com.fiap.conectaalimento.model.Solicitacao;
import br.com.fiap.conectaalimento.model.enums.StatusAlimento;
import br.com.fiap.conectaalimento.model.enums.StatusSolicitacao;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Regras de negócio das solicitações: solicitar (US19), acompanhar (US20), ver recebidas (US09),
 * aceitar (US10), recusar (US11) e cancelar.
 */
@Service
public class SolicitacaoBO {

    private final SolicitacaoDAO solicitacaoDAO;
    private final AlimentoDAO alimentoDAO;
    private final InstituicaoDAO instituicaoDAO;
    private final AlimentoBO alimentoBO;

    public SolicitacaoBO(SolicitacaoDAO solicitacaoDAO, AlimentoDAO alimentoDAO, InstituicaoDAO instituicaoDAO,
                         AlimentoBO alimentoBO) {
        this.solicitacaoDAO = solicitacaoDAO;
        this.alimentoDAO = alimentoDAO;
        this.instituicaoDAO = instituicaoDAO;
        this.alimentoBO = alimentoBO;
    }

    public List<Solicitacao> listar(StatusSolicitacao status, Long instituicaoId, Long estabelecimentoId) {
        alimentoBO.atualizarExpirados();
        return solicitacaoDAO.listarComFiltros(status, instituicaoId, estabelecimentoId);
    }

    public Solicitacao buscar(Long id) {
        return solicitacaoDAO.buscarPorId(id).orElseThrow(() -> new RecursoNaoEncontradoException("Solicitação", id));
    }

    /** US19 - Quantidade até o disponível e sem solicitação duplicada da mesma instituição. */
    @Transactional
    public Solicitacao criar(SolicitacaoRequest req) {
        alimentoBO.atualizarExpirados();
        Alimento alimento = alimentoDAO.buscarPorId(req.alimentoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Alimento", req.alimentoId()));
        InstituicaoReceptora instituicao = instituicaoDAO.buscarPorId(req.instituicaoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Instituição", req.instituicaoId()));
        if (!alimento.aceitaSolicitacoes()) {
            throw new RegraNegocioException("O alimento #" + alimento.getId()
                    + " não está aberto para solicitações (status: " + alimento.getStatus() + ").");
        }
        if (req.quantidade() > alimento.getQuantidade()) {
            throw new RegraNegocioException("Quantidade acima do disponível (" + alimento.getQuantidade() + " "
                    + alimento.getUnidade().getSigla() + ").");
        }
        for (Solicitacao existente : solicitacaoDAO.listarPorAlimento(alimento.getId())) {
            if (existente.isAtiva() && existente.getInstituicao().equals(instituicao)) {
                throw new ConflitoException("A instituição já tem a solicitação #" + existente.getId()
                        + " em andamento para este alimento.");
            }
        }
        Solicitacao nova = solicitacaoDAO.inserir(new Solicitacao(null, alimento, instituicao, req.quantidade()));
        if (alimento.getStatus() == StatusAlimento.DISPONIVEL) {
            alimentoDAO.atualizarStatus(alimento.getId(), StatusAlimento.SOLICITADO);
            alimento.setStatus(StatusAlimento.SOLICITADO);
        }
        return nova;
    }

    /** Altera a quantidade enquanto a solicitação ainda não foi respondida. */
    @Transactional
    public Solicitacao atualizarQuantidade(Long id, double quantidade) {
        Solicitacao solicitacao = buscar(id);
        solicitacao.alterarQuantidade(quantidade);
        solicitacaoDAO.atualizar(solicitacao);
        return solicitacao;
    }

    /** US10 - Ao aceitar, o alimento é reservado e as demais solicitações pendentes não são atendidas. */
    @Transactional
    public Solicitacao aceitar(Long id) {
        Solicitacao solicitacao = buscar(id);
        Alimento alimento = solicitacao.getAlimento();
        if (alimento.estaExpirado()) {
            throw new RegraNegocioException("O prazo de retirada deste alimento já terminou.");
        }
        solicitacao.aceitar();
        solicitacaoDAO.atualizar(solicitacao);
        alimentoDAO.atualizarStatus(alimento.getId(), StatusAlimento.RESERVADO);
        alimento.setStatus(StatusAlimento.RESERVADO);
        for (Solicitacao outra : solicitacaoDAO.listarPorAlimento(alimento.getId())) {
            if (!outra.equals(solicitacao) && outra.getStatus() == StatusSolicitacao.PENDENTE) {
                outra.marcarComoNaoAtendida("Alimento destinado a outra instituição");
                solicitacaoDAO.atualizar(outra);
            }
        }
        return solicitacao;
    }

    /** US11 - Recusa com motivo opcional; o alimento volta a ficar disponível se não houver outras pendentes. */
    @Transactional
    public Solicitacao recusar(Long id, String motivo) {
        Solicitacao solicitacao = buscar(id);
        if (motivo == null || motivo.isBlank()) {
            solicitacao.recusar();
        } else {
            solicitacao.recusar(motivo.trim());
        }
        solicitacaoDAO.atualizar(solicitacao);
        liberarAlimentoSeNaoHouverPendentes(solicitacao.getAlimento());
        return solicitacao;
    }

    /** Cancelamento pela instituição (apenas PENDENTE ou ACEITA). */
    @Transactional
    public void cancelar(Long id) {
        Solicitacao solicitacao = buscar(id);
        StatusSolicitacao status = solicitacao.getStatus();
        if (status == StatusSolicitacao.AGENDADA) {
            throw new RegraNegocioException("A solicitação tem retirada agendada: cancele a retirada antes "
                    + "(DELETE /api/retiradas/{id}).");
        }
        if (status != StatusSolicitacao.PENDENTE && status != StatusSolicitacao.ACEITA) {
            throw new RegraNegocioException("Só é possível cancelar solicitações PENDENTE ou ACEITA (status atual: "
                    + status + ").");
        }
        solicitacao.cancelar("Cancelada pela instituição");
        solicitacaoDAO.atualizar(solicitacao);
        Alimento alimento = solicitacao.getAlimento();
        if (status == StatusSolicitacao.ACEITA) {
            alimentoDAO.atualizarStatus(alimento.getId(), StatusAlimento.DISPONIVEL);
        } else {
            liberarAlimentoSeNaoHouverPendentes(alimento);
        }
    }

    private void liberarAlimentoSeNaoHouverPendentes(Alimento alimento) {
        boolean existemPendentes = solicitacaoDAO.listarPorAlimento(alimento.getId()).stream()
                .anyMatch(s -> s.getStatus() == StatusSolicitacao.PENDENTE);
        if (!existemPendentes && alimento.getStatus() == StatusAlimento.SOLICITADO) {
            alimentoDAO.atualizarStatus(alimento.getId(), StatusAlimento.DISPONIVEL);
            alimento.setStatus(StatusAlimento.DISPONIVEL);
        }
    }
}
