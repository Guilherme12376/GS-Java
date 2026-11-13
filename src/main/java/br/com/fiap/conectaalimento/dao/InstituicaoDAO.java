package br.com.fiap.conectaalimento.dao;

import br.com.fiap.conectaalimento.model.Endereco;
import br.com.fiap.conectaalimento.model.InstituicaoReceptora;
import br.com.fiap.conectaalimento.model.enums.Bairro;
import br.com.fiap.conectaalimento.model.enums.TipoInstituicao;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/** Acesso à tabela T_CA_INSTITUICAO. */
@Repository
public class InstituicaoDAO extends BaseDAO<InstituicaoReceptora> {

    public InstituicaoDAO(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    protected String getTabela() {
        return "T_CA_INSTITUICAO";
    }

    @Override
    protected String getColunaId() {
        return "ID_INSTITUICAO";
    }

    public InstituicaoReceptora inserir(InstituicaoReceptora i) {
        Endereco end = i.getEndereco();
        long id = inserir("INSERT INTO T_CA_INSTITUICAO (NM_INSTITUICAO, NM_RESPONSAVEL, DS_EMAIL, DS_SENHA, "
                        + "NR_TELEFONE, TP_INSTITUICAO, QT_CAPACIDADE, DS_LOGRADOURO, NR_LOGRADOURO, NM_BAIRRO, NR_CEP, "
                        + "NR_LATITUDE, NR_LONGITUDE, DT_CADASTRO) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                i.getNome(), i.getResponsavel(), i.getEmail(), i.getSenhaHash(), i.getTelefone(),
                i.getTipoInstituicao(), i.getCapacidadeAtendimento(), end.getLogradouro(), end.getNumero(),
                end.getBairro(), end.getCep(), end.getLatitude(), end.getLongitude(), i.getDataCadastro());
        i.setId(id);
        return i;
    }

    public void atualizar(InstituicaoReceptora i) {
        Endereco end = i.getEndereco();
        executar("UPDATE T_CA_INSTITUICAO SET NM_INSTITUICAO = ?, NM_RESPONSAVEL = ?, DS_EMAIL = ?, DS_SENHA = ?, "
                        + "NR_TELEFONE = ?, TP_INSTITUICAO = ?, QT_CAPACIDADE = ?, DS_LOGRADOURO = ?, NR_LOGRADOURO = ?, "
                        + "NM_BAIRRO = ?, NR_CEP = ?, NR_LATITUDE = ?, NR_LONGITUDE = ? WHERE ID_INSTITUICAO = ?",
                i.getNome(), i.getResponsavel(), i.getEmail(), i.getSenhaHash(), i.getTelefone(),
                i.getTipoInstituicao(), i.getCapacidadeAtendimento(), end.getLogradouro(), end.getNumero(),
                end.getBairro(), end.getCep(), end.getLatitude(), end.getLongitude(), i.getId());
    }

    public Optional<InstituicaoReceptora> buscarPorEmail(String email) {
        List<InstituicaoReceptora> r = consultar("SELECT * FROM T_CA_INSTITUICAO WHERE DS_EMAIL = ?", email);
        return r.isEmpty() ? Optional.empty() : Optional.of(r.get(0));
    }

    /** [POLIMORFISMO - override] */
    @Override
    protected InstituicaoReceptora mapear(ResultSet rs) throws SQLException {
        InstituicaoReceptora i = new InstituicaoReceptora();
        i.setId(rs.getLong("ID_INSTITUICAO"));
        i.setNome(rs.getString("NM_INSTITUICAO"));
        i.setResponsavel(rs.getString("NM_RESPONSAVEL"));
        i.setEmail(rs.getString("DS_EMAIL"));
        i.setSenhaHash(rs.getString("DS_SENHA"));
        i.setTelefone(rs.getString("NR_TELEFONE"));
        i.setTipoInstituicao(opcao(rs, "TP_INSTITUICAO", TipoInstituicao.class));
        i.setCapacidadeAtendimento(rs.getInt("QT_CAPACIDADE"));
        i.setEndereco(new Endereco(rs.getString("DS_LOGRADOURO"), rs.getString("NR_LOGRADOURO"),
                opcao(rs, "NM_BAIRRO", Bairro.class), rs.getString("NR_CEP"),
                rs.getDouble("NR_LATITUDE"), rs.getDouble("NR_LONGITUDE")));
        i.setDataCadastro(dataHora(rs, "DT_CADASTRO"));
        return i;
    }
}
