package br.com.fiap.conectaalimento.dao;

import br.com.fiap.conectaalimento.model.Retirada;
import br.com.fiap.conectaalimento.model.Solicitacao;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/** Acesso à tabela T_CA_RETIRADA. */
@Repository
public class RetiradaDAO extends BaseDAO<Retirada> {

    private final SolicitacaoDAO solicitacaoDAO;

    public RetiradaDAO(DataSource dataSource, SolicitacaoDAO solicitacaoDAO) {
        super(dataSource);
        this.solicitacaoDAO = solicitacaoDAO;
    }

    @Override
    protected String getTabela() {
        return "T_CA_RETIRADA";
    }

    @Override
    protected String getColunaId() {
        return "ID_RETIRADA";
    }

    public Retirada inserir(Retirada r) {
        long id = inserir("INSERT INTO T_CA_RETIRADA (ID_SOLICITACAO, DT_AGENDADA, ST_ENTREGA, DT_ENTREGA, QT_ENTREGUE, "
                        + "ST_RECEBIMENTO, DT_RECEBIMENTO, QT_RECEBIDA, DS_OBSERVACAO, DT_CADASTRO) "
                        + "VALUES (?,?,?,?,?,?,?,?,?,?)",
                r.getSolicitacao().getId(), r.getDataHoraAgendada(), r.isEntregaConfirmada(), r.getDataEntrega(),
                r.getQuantidadeEntregue(), r.isRecebimentoConfirmado(), r.getDataRecebimento(),
                r.getQuantidadeRecebida(), r.getObservacao(), r.getDataCadastro());
        r.setId(id);
        return r;
    }

    public void atualizar(Retirada r) {
        executar("UPDATE T_CA_RETIRADA SET DT_AGENDADA = ?, ST_ENTREGA = ?, DT_ENTREGA = ?, QT_ENTREGUE = ?, "
                        + "ST_RECEBIMENTO = ?, DT_RECEBIMENTO = ?, QT_RECEBIDA = ?, DS_OBSERVACAO = ? "
                        + "WHERE ID_RETIRADA = ?",
                r.getDataHoraAgendada(), r.isEntregaConfirmada(), r.getDataEntrega(), r.getQuantidadeEntregue(),
                r.isRecebimentoConfirmado(), r.getDataRecebimento(), r.getQuantidadeRecebida(), r.getObservacao(),
                r.getId());
    }

    public Optional<Retirada> buscarPorSolicitacao(Long solicitacaoId) {
        List<Retirada> r = consultar("SELECT * FROM T_CA_RETIRADA WHERE ID_SOLICITACAO = ?", solicitacaoId);
        return r.isEmpty() ? Optional.empty() : Optional.of(r.get(0));
    }

    /** [POLIMORFISMO - override] */
    @Override
    protected Retirada mapear(ResultSet rs) throws SQLException {
        Retirada r = new Retirada();
        r.setId(rs.getLong("ID_RETIRADA"));
        r.setDataHoraAgendada(dataHora(rs, "DT_AGENDADA"));
        r.setEntregaConfirmada(simNao(rs, "ST_ENTREGA"));
        r.setDataEntrega(dataHora(rs, "DT_ENTREGA"));
        r.setQuantidadeEntregue(decimal(rs, "QT_ENTREGUE"));
        r.setRecebimentoConfirmado(simNao(rs, "ST_RECEBIMENTO"));
        r.setDataRecebimento(dataHora(rs, "DT_RECEBIMENTO"));
        r.setQuantidadeRecebida(decimal(rs, "QT_RECEBIDA"));
        r.setObservacao(rs.getString("DS_OBSERVACAO"));
        r.setDataCadastro(dataHora(rs, "DT_CADASTRO"));
        Solicitacao solicitacao = new Solicitacao();
        solicitacao.setId(rs.getLong("ID_SOLICITACAO"));
        r.setSolicitacao(solicitacao);
        return r;
    }

    /** [POLIMORFISMO - override] Carrega as solicitações com uma única consulta (IN). */
    @Override
    protected List<Retirada> completar(List<Retirada> retiradas) {
        Set<Long> ids = new HashSet<>();
        retiradas.forEach(r -> ids.add(r.getSolicitacao().getId()));
        Map<Long, Solicitacao> solicitacoes = new HashMap<>();
        solicitacaoDAO.buscarPorIds(ids).forEach(s -> solicitacoes.put(s.getId(), s));
        retiradas.forEach(r -> r.setSolicitacao(solicitacoes.get(r.getSolicitacao().getId())));
        return retiradas;
    }
}
