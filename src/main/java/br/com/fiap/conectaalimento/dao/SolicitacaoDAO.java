package br.com.fiap.conectaalimento.dao;

import br.com.fiap.conectaalimento.model.Alimento;
import br.com.fiap.conectaalimento.model.AlimentoPerecivel;
import br.com.fiap.conectaalimento.model.InstituicaoReceptora;
import br.com.fiap.conectaalimento.model.Solicitacao;
import br.com.fiap.conectaalimento.model.enums.StatusSolicitacao;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Acesso à tabela T_CA_SOLICITACAO. */
@Repository
public class SolicitacaoDAO extends BaseDAO<Solicitacao> {

    private final AlimentoDAO alimentoDAO;
    private final InstituicaoDAO instituicaoDAO;

    public SolicitacaoDAO(DataSource dataSource, AlimentoDAO alimentoDAO, InstituicaoDAO instituicaoDAO) {
        super(dataSource);
        this.alimentoDAO = alimentoDAO;
        this.instituicaoDAO = instituicaoDAO;
    }

    @Override
    protected String getTabela() {
        return "T_CA_SOLICITACAO";
    }

    @Override
    protected String getColunaId() {
        return "ID_SOLICITACAO";
    }

    public Solicitacao inserir(Solicitacao s) {
        long id = inserir("INSERT INTO T_CA_SOLICITACAO (ID_ALIMENTO, ID_INSTITUICAO, QT_SOLICITADA, ST_SOLICITACAO, "
                        + "DS_MOTIVO, DT_RESPOSTA, DT_CADASTRO) VALUES (?,?,?,?,?,?,?)",
                s.getAlimento().getId(), s.getInstituicao().getId(), s.getQuantidadeSolicitada(), s.getStatus(),
                s.getMotivo(), s.getDataResposta(), s.getDataCadastro());
        s.setId(id);
        return s;
    }

    public void atualizar(Solicitacao s) {
        executar("UPDATE T_CA_SOLICITACAO SET QT_SOLICITADA = ?, ST_SOLICITACAO = ?, DS_MOTIVO = ?, DT_RESPOSTA = ? "
                        + "WHERE ID_SOLICITACAO = ?",
                s.getQuantidadeSolicitada(), s.getStatus(), s.getMotivo(), s.getDataResposta(), s.getId());
    }

    public List<Solicitacao> listarPorAlimento(Long alimentoId) {
        return consultar("SELECT * FROM T_CA_SOLICITACAO WHERE ID_ALIMENTO = ? ORDER BY ID_SOLICITACAO", alimentoId);
    }

    public long contarPorInstituicao(Long instituicaoId) {
        return contar("SELECT COUNT(*) FROM T_CA_SOLICITACAO WHERE ID_INSTITUICAO = ?", instituicaoId);
    }

    /** Consulta com filtros opcionais (parâmetros nulos são ignorados). */
    public List<Solicitacao> listarComFiltros(StatusSolicitacao status, Long instituicaoId, Long estabelecimentoId) {
        StringBuilder sql = new StringBuilder("SELECT S.* FROM T_CA_SOLICITACAO S "
                + "INNER JOIN T_CA_ALIMENTO A ON A.ID_ALIMENTO = S.ID_ALIMENTO WHERE 1 = 1");
        List<Object> parametros = new ArrayList<>();
        if (status != null) {
            sql.append(" AND S.ST_SOLICITACAO = ?");
            parametros.add(status);
        }
        if (instituicaoId != null) {
            sql.append(" AND S.ID_INSTITUICAO = ?");
            parametros.add(instituicaoId);
        }
        if (estabelecimentoId != null) {
            sql.append(" AND A.ID_ESTABELECIMENTO = ?");
            parametros.add(estabelecimentoId);
        }
        sql.append(" ORDER BY S.DT_CADASTRO DESC");
        return consultar(sql.toString(), parametros.toArray());
    }

    /** [POLIMORFISMO - override] */
    @Override
    protected Solicitacao mapear(ResultSet rs) throws SQLException {
        Solicitacao s = new Solicitacao();
        s.setId(rs.getLong("ID_SOLICITACAO"));
        s.setQuantidadeSolicitada(rs.getDouble("QT_SOLICITADA"));
        s.setStatus(opcao(rs, "ST_SOLICITACAO", StatusSolicitacao.class));
        s.setMotivo(rs.getString("DS_MOTIVO"));
        s.setDataResposta(dataHora(rs, "DT_RESPOSTA"));
        s.setDataCadastro(dataHora(rs, "DT_CADASTRO"));
        Alimento alimento = new AlimentoPerecivel();
        alimento.setId(rs.getLong("ID_ALIMENTO"));
        s.setAlimento(alimento);
        InstituicaoReceptora instituicao = new InstituicaoReceptora();
        instituicao.setId(rs.getLong("ID_INSTITUICAO"));
        s.setInstituicao(instituicao);
        return s;
    }

    /** [POLIMORFISMO - override] Carrega alimentos e instituições com uma consulta (IN) cada. */
    @Override
    protected List<Solicitacao> completar(List<Solicitacao> solicitacoes) {
        Set<Long> idsAlimento = new HashSet<>();
        Set<Long> idsInstituicao = new HashSet<>();
        for (Solicitacao s : solicitacoes) {
            idsAlimento.add(s.getAlimento().getId());
            idsInstituicao.add(s.getInstituicao().getId());
        }
        Map<Long, Alimento> alimentos = new HashMap<>();
        alimentoDAO.buscarPorIds(idsAlimento).forEach(a -> alimentos.put(a.getId(), a));
        Map<Long, InstituicaoReceptora> instituicoes = new HashMap<>();
        instituicaoDAO.buscarPorIds(idsInstituicao).forEach(i -> instituicoes.put(i.getId(), i));
        for (Solicitacao s : solicitacoes) {
            s.setAlimento(alimentos.get(s.getAlimento().getId()));
            s.setInstituicao(instituicoes.get(s.getInstituicao().getId()));
        }
        return solicitacoes;
    }
}
