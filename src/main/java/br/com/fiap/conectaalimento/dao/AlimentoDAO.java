package br.com.fiap.conectaalimento.dao;

import br.com.fiap.conectaalimento.model.Alimento;
import br.com.fiap.conectaalimento.model.AlimentoNaoPerecivel;
import br.com.fiap.conectaalimento.model.AlimentoPerecivel;
import br.com.fiap.conectaalimento.model.EstabelecimentoDoador;
import br.com.fiap.conectaalimento.model.enums.CategoriaAlimento;
import br.com.fiap.conectaalimento.model.enums.CondicaoArmazenamento;
import br.com.fiap.conectaalimento.model.enums.StatusAlimento;
import br.com.fiap.conectaalimento.model.enums.TipoAlimento;
import br.com.fiap.conectaalimento.model.enums.UnidadeMedida;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Acesso à tabela T_CA_ALIMENTO. */
@Repository
public class AlimentoDAO extends BaseDAO<Alimento> {

    private final EstabelecimentoDAO estabelecimentoDAO;

    public AlimentoDAO(DataSource dataSource, EstabelecimentoDAO estabelecimentoDAO) {
        super(dataSource);
        this.estabelecimentoDAO = estabelecimentoDAO;
    }

    @Override
    protected String getTabela() {
        return "T_CA_ALIMENTO";
    }

    @Override
    protected String getColunaId() {
        return "ID_ALIMENTO";
    }

    public Alimento inserir(Alimento a) {
        long id = inserir("INSERT INTO T_CA_ALIMENTO (ID_ESTABELECIMENTO, NM_ALIMENTO, TP_ALIMENTO, DS_CATEGORIA, "
                        + "QT_ALIMENTO, DS_UNIDADE, DT_VALIDADE, DT_INICIO_RETIRADA, DT_FIM_RETIRADA, DS_ARMAZENAMENTO, "
                        + "ST_EMBALAGEM_INTEGRA, DS_LOTE, DS_OBSERVACAO, ST_DECLARACAO, ST_ALIMENTO, DT_CADASTRO) "
                        + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                a.getDoador().getId(), a.getNome(), a.getTipo(), a.getCategoria(), a.getQuantidade(), a.getUnidade(),
                a.getDataValidade(), a.getInicioRetirada(), a.getFimRetirada(), a.getCondicaoArmazenamento(),
                embalagemIntegra(a), lote(a), a.getObservacoes(), a.isDeclaracaoSeguranca(), a.getStatus(),
                a.getDataCadastro());
        a.setId(id);
        return a;
    }

    public void atualizar(Alimento a) {
        executar("UPDATE T_CA_ALIMENTO SET NM_ALIMENTO = ?, TP_ALIMENTO = ?, DS_CATEGORIA = ?, QT_ALIMENTO = ?, "
                        + "DS_UNIDADE = ?, DT_VALIDADE = ?, DT_INICIO_RETIRADA = ?, DT_FIM_RETIRADA = ?, "
                        + "DS_ARMAZENAMENTO = ?, ST_EMBALAGEM_INTEGRA = ?, DS_LOTE = ?, DS_OBSERVACAO = ?, "
                        + "ST_DECLARACAO = ?, ST_ALIMENTO = ? WHERE ID_ALIMENTO = ?",
                a.getNome(), a.getTipo(), a.getCategoria(), a.getQuantidade(), a.getUnidade(), a.getDataValidade(),
                a.getInicioRetirada(), a.getFimRetirada(), a.getCondicaoArmazenamento(), embalagemIntegra(a), lote(a),
                a.getObservacoes(), a.isDeclaracaoSeguranca(), a.getStatus(), a.getId());
    }

    public void atualizarStatus(Long id, StatusAlimento status) {
        executar("UPDATE T_CA_ALIMENTO SET ST_ALIMENTO = ? WHERE ID_ALIMENTO = ?", status, id);
    }

    public List<Alimento> listarPorEstabelecimento(Long estabelecimentoId) {
        return consultar("SELECT * FROM T_CA_ALIMENTO WHERE ID_ESTABELECIMENTO = ? ORDER BY DT_CADASTRO DESC",
                estabelecimentoId);
    }

    public List<Alimento> listarPorStatus(StatusAlimento status) {
        return consultar("SELECT * FROM T_CA_ALIMENTO WHERE ST_ALIMENTO = ? ORDER BY ID_ALIMENTO", status);
    }

    /** Alimentos que ainda podem expirar (disponíveis, solicitados ou reservados). */
    public List<Alimento> listarAtivosSemAgendamento() {
        return consultar("SELECT * FROM T_CA_ALIMENTO WHERE ST_ALIMENTO IN ('DISPONIVEL','SOLICITADO','RESERVADO')");
    }

    /** Alimentos que ainda aceitam solicitações (disponíveis ou já solicitados). */
    public List<Alimento> listarAbertosParaSolicitacao() {
        return consultar("SELECT * FROM T_CA_ALIMENTO WHERE ST_ALIMENTO IN ('DISPONIVEL','SOLICITADO') "
                + "ORDER BY DT_FIM_RETIRADA");
    }

    public long contarPorEstabelecimento(Long estabelecimentoId) {
        return contar("SELECT COUNT(*) FROM T_CA_ALIMENTO WHERE ID_ESTABELECIMENTO = ?", estabelecimentoId);
    }

    /**
     * [POLIMORFISMO] A coluna TP_ALIMENTO decide qual subclasse é criada;
     * o restante do sistema trabalha apenas com o tipo Alimento.
     */
    @Override
    protected Alimento mapear(ResultSet rs) throws SQLException {
        TipoAlimento tipo = opcao(rs, "TP_ALIMENTO", TipoAlimento.class);
        Alimento a;
        if (tipo == TipoAlimento.PERECIVEL) {
            AlimentoPerecivel perecivel = new AlimentoPerecivel();
            perecivel.setCondicaoArmazenamento(opcao(rs, "DS_ARMAZENAMENTO", CondicaoArmazenamento.class));
            a = perecivel;
        } else {
            AlimentoNaoPerecivel naoPerecivel = new AlimentoNaoPerecivel();
            naoPerecivel.setEmbalagemIntegra(simNao(rs, "ST_EMBALAGEM_INTEGRA"));
            naoPerecivel.setLote(rs.getString("DS_LOTE"));
            a = naoPerecivel;
        }
        a.setId(rs.getLong("ID_ALIMENTO"));
        a.setNome(rs.getString("NM_ALIMENTO"));
        a.setCategoria(opcao(rs, "DS_CATEGORIA", CategoriaAlimento.class));
        a.setQuantidade(rs.getDouble("QT_ALIMENTO"));
        a.setUnidade(opcao(rs, "DS_UNIDADE", UnidadeMedida.class));
        a.setDataValidade(data(rs, "DT_VALIDADE"));
        a.setInicioRetirada(dataHora(rs, "DT_INICIO_RETIRADA"));
        a.setFimRetirada(dataHora(rs, "DT_FIM_RETIRADA"));
        a.setObservacoes(rs.getString("DS_OBSERVACAO"));
        a.setDeclaracaoSeguranca(simNao(rs, "ST_DECLARACAO"));
        a.setStatus(opcao(rs, "ST_ALIMENTO", StatusAlimento.class));
        a.setDataCadastro(dataHora(rs, "DT_CADASTRO"));
        EstabelecimentoDoador doador = new EstabelecimentoDoador();
        doador.setId(rs.getLong("ID_ESTABELECIMENTO"));
        a.setDoador(doador);
        return a;
    }

    /** [POLIMORFISMO - override] Carrega os doadores com uma única consulta (IN). */
    @Override
    protected List<Alimento> completar(List<Alimento> alimentos) {
        Set<Long> ids = new HashSet<>();
        alimentos.forEach(a -> ids.add(a.getDoador().getId()));
        Map<Long, EstabelecimentoDoador> doadores = new HashMap<>();
        estabelecimentoDAO.buscarPorIds(ids).forEach(d -> doadores.put(d.getId(), d));
        alimentos.forEach(a -> a.setDoador(doadores.get(a.getDoador().getId())));
        return alimentos;
    }

    private Boolean embalagemIntegra(Alimento a) {
        return a instanceof AlimentoNaoPerecivel n ? n.isEmbalagemIntegra() : Boolean.TRUE;
    }

    private String lote(Alimento a) {
        return a instanceof AlimentoNaoPerecivel n ? n.getLote() : null;
    }
}
