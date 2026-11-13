package br.com.fiap.conectaalimento.dao;

import br.com.fiap.conectaalimento.model.Endereco;
import br.com.fiap.conectaalimento.model.EstabelecimentoDoador;
import br.com.fiap.conectaalimento.model.enums.Bairro;
import br.com.fiap.conectaalimento.model.enums.TipoEstabelecimento;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/** Acesso à tabela T_CA_ESTABELECIMENTO. */
@Repository
public class EstabelecimentoDAO extends BaseDAO<EstabelecimentoDoador> {

    public EstabelecimentoDAO(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    protected String getTabela() {
        return "T_CA_ESTABELECIMENTO";
    }

    @Override
    protected String getColunaId() {
        return "ID_ESTABELECIMENTO";
    }

    public EstabelecimentoDoador inserir(EstabelecimentoDoador e) {
        Endereco end = e.getEndereco();
        long id = inserir("INSERT INTO T_CA_ESTABELECIMENTO (NM_ESTABELECIMENTO, NM_RESPONSAVEL, DS_EMAIL, DS_SENHA, "
                        + "NR_TELEFONE, NR_CNPJ, TP_ESTABELECIMENTO, DS_LOGRADOURO, NR_LOGRADOURO, NM_BAIRRO, NR_CEP, "
                        + "NR_LATITUDE, NR_LONGITUDE, DT_CADASTRO) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                e.getNome(), e.getResponsavel(), e.getEmail(), e.getSenhaHash(), e.getTelefone(), e.getCnpj(),
                e.getTipoEstabelecimento(), end.getLogradouro(), end.getNumero(), end.getBairro(), end.getCep(),
                end.getLatitude(), end.getLongitude(), e.getDataCadastro());
        e.setId(id);
        return e;
    }

    public void atualizar(EstabelecimentoDoador e) {
        Endereco end = e.getEndereco();
        executar("UPDATE T_CA_ESTABELECIMENTO SET NM_ESTABELECIMENTO = ?, NM_RESPONSAVEL = ?, DS_EMAIL = ?, "
                        + "DS_SENHA = ?, NR_TELEFONE = ?, NR_CNPJ = ?, TP_ESTABELECIMENTO = ?, DS_LOGRADOURO = ?, "
                        + "NR_LOGRADOURO = ?, NM_BAIRRO = ?, NR_CEP = ?, NR_LATITUDE = ?, NR_LONGITUDE = ? "
                        + "WHERE ID_ESTABELECIMENTO = ?",
                e.getNome(), e.getResponsavel(), e.getEmail(), e.getSenhaHash(), e.getTelefone(), e.getCnpj(),
                e.getTipoEstabelecimento(), end.getLogradouro(), end.getNumero(), end.getBairro(), end.getCep(),
                end.getLatitude(), end.getLongitude(), e.getId());
    }

    public Optional<EstabelecimentoDoador> buscarPorEmail(String email) {
        List<EstabelecimentoDoador> r = consultar("SELECT * FROM T_CA_ESTABELECIMENTO WHERE DS_EMAIL = ?", email);
        return r.isEmpty() ? Optional.empty() : Optional.of(r.get(0));
    }

    public Optional<EstabelecimentoDoador> buscarPorCnpj(String cnpj) {
        List<EstabelecimentoDoador> r = consultar("SELECT * FROM T_CA_ESTABELECIMENTO WHERE NR_CNPJ = ?", cnpj);
        return r.isEmpty() ? Optional.empty() : Optional.of(r.get(0));
    }

    /** [POLIMORFISMO - override] */
    @Override
    protected EstabelecimentoDoador mapear(ResultSet rs) throws SQLException {
        EstabelecimentoDoador e = new EstabelecimentoDoador();
        e.setId(rs.getLong("ID_ESTABELECIMENTO"));
        e.setNome(rs.getString("NM_ESTABELECIMENTO"));
        e.setResponsavel(rs.getString("NM_RESPONSAVEL"));
        e.setEmail(rs.getString("DS_EMAIL"));
        e.setSenhaHash(rs.getString("DS_SENHA"));
        e.setTelefone(rs.getString("NR_TELEFONE"));
        e.setCnpj(rs.getString("NR_CNPJ"));
        e.setTipoEstabelecimento(opcao(rs, "TP_ESTABELECIMENTO", TipoEstabelecimento.class));
        e.setEndereco(new Endereco(rs.getString("DS_LOGRADOURO"), rs.getString("NR_LOGRADOURO"),
                opcao(rs, "NM_BAIRRO", Bairro.class), rs.getString("NR_CEP"),
                rs.getDouble("NR_LATITUDE"), rs.getDouble("NR_LONGITUDE")));
        e.setDataCadastro(dataHora(rs, "DT_CADASTRO"));
        return e;
    }
}
