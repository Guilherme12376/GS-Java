package br.com.fiap.conectaalimento.bo;

import br.com.fiap.conectaalimento.dao.AlimentoDAO;
import br.com.fiap.conectaalimento.dao.EstabelecimentoDAO;
import br.com.fiap.conectaalimento.dao.InstituicaoDAO;
import br.com.fiap.conectaalimento.dao.RetiradaDAO;
import br.com.fiap.conectaalimento.dao.SolicitacaoDAO;
import br.com.fiap.conectaalimento.dto.IndicadoresResponse;
import br.com.fiap.conectaalimento.model.Alimento;
import br.com.fiap.conectaalimento.model.Retirada;
import br.com.fiap.conectaalimento.model.Solicitacao;
import br.com.fiap.conectaalimento.model.enums.StatusAlimento;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** US25 - Indicadores de impacto do piloto (seção "Indicadores de Impacto" do projeto). */
@Service
public class IndicadoresBO {

    private final EstabelecimentoDAO estabelecimentoDAO;
    private final InstituicaoDAO instituicaoDAO;
    private final AlimentoDAO alimentoDAO;
    private final SolicitacaoDAO solicitacaoDAO;
    private final RetiradaDAO retiradaDAO;
    private final AlimentoBO alimentoBO;

    public IndicadoresBO(EstabelecimentoDAO estabelecimentoDAO, InstituicaoDAO instituicaoDAO, AlimentoDAO alimentoDAO,
                         SolicitacaoDAO solicitacaoDAO, RetiradaDAO retiradaDAO, AlimentoBO alimentoBO) {
        this.estabelecimentoDAO = estabelecimentoDAO;
        this.instituicaoDAO = instituicaoDAO;
        this.alimentoDAO = alimentoDAO;
        this.solicitacaoDAO = solicitacaoDAO;
        this.retiradaDAO = retiradaDAO;
        this.alimentoBO = alimentoBO;
    }

    public IndicadoresResponse calcular() {
        alimentoBO.atualizarExpirados();

        long concluidas = 0;
        double somaHoras = 0;
        Map<String, Double> porUnidade = new LinkedHashMap<>();
        Set<Long> instituicoesAtendidas = new HashSet<>();
        Set<Long> doadoresQueDoaram = new HashSet<>();
        for (Retirada r : retiradaDAO.listar()) {
            if (!r.isConcluida()) {
                continue;
            }
            concluidas++;
            Solicitacao s = r.getSolicitacao();
            Alimento a = s.getAlimento();
            double quantidade = r.getQuantidadeRecebida() != null ? r.getQuantidadeRecebida() : s.getQuantidadeSolicitada();
            porUnidade.merge(a.getUnidade().name(), quantidade, Double::sum);
            instituicoesAtendidas.add(s.getInstituicao().getId());
            doadoresQueDoaram.add(a.getDoador().getId());
            LocalDateTime conclusao = r.getDataEntrega().isAfter(r.getDataRecebimento())
                    ? r.getDataEntrega() : r.getDataRecebimento();
            somaHoras += Duration.between(a.getDataCadastro(), conclusao).toMinutes() / 60.0;
        }

        long disponiveis = 0;
        long alimentosConcluidos = 0;
        long encerrados = 0;
        for (Alimento a : alimentoDAO.listar()) {
            StatusAlimento st = a.getStatus();
            if (a.aceitaSolicitacoes()) {
                disponiveis++;
            }
            if (st == StatusAlimento.CONCLUIDO) {
                alimentosConcluidos++;
            }
            if (st == StatusAlimento.CONCLUIDO || st == StatusAlimento.EXPIRADO || st == StatusAlimento.REMOVIDO) {
                encerrados++;
            }
        }

        List<Solicitacao> solicitacoes = solicitacaoDAO.listar();
        long aceitas = 0;
        long recusadas = 0;
        long naoAtendidas = 0;
        long pendentes = 0;
        for (Solicitacao s : solicitacoes) {
            switch (s.getStatus()) {
                case ACEITA, AGENDADA, CONCLUIDA -> aceitas++;
                case RECUSADA -> recusadas++;
                case NAO_ATENDIDA, CANCELADA -> naoAtendidas++;
                case PENDENTE -> pendentes++;
            }
        }

        return new IndicadoresResponse(
                concluidas,
                porUnidade.getOrDefault("KG", 0.0),
                porUnidade,
                estabelecimentoDAO.contar(),
                doadoresQueDoaram.size(),
                instituicaoDAO.contar(),
                instituicoesAtendidas.size(),
                disponiveis,
                encerrados == 0 ? 0 : arredondar(alimentosConcluidos * 100.0 / encerrados),
                concluidas == 0 ? 0 : arredondar(somaHoras / concluidas),
                solicitacoes.size(),
                aceitas,
                recusadas,
                naoAtendidas,
                pendentes);
    }

    private static double arredondar(double valor) {
        return Math.round(valor * 10) / 10.0;
    }
}
