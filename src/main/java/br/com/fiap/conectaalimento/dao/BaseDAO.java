package br.com.fiap.conectaalimento.dao;

import br.com.fiap.conectaalimento.exception.BancoDeDadosException;
import br.com.fiap.conectaalimento.exception.ConflitoException;
import br.com.fiap.conectaalimento.model.Entidade;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * DAO genérico com JDBC puro (PreparedStatement).
 *
 * [HERANÇA] Os DAOs concretos herdam buscarPorId, listar, remover, contar e os métodos
 * auxiliares de execução de SQL; implementam apenas o mapeamento e as consultas próprias.
 * [POLIMORFISMO - override] mapear(...) e completar(...) são sobrescritos por cada DAO.
 *
 * As conexões vêm do pool do Spring (DataSourceUtils), para que vários DAOs participem
 * da mesma transação aberta pelos BOs (@Transactional).
 */
public abstract class BaseDAO<T extends Entidade> {

    protected final DataSource dataSource;

    protected BaseDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    protected abstract String getTabela();

    protected abstract String getColunaId();

    /** Converte a linha atual do ResultSet em objeto do model. */
    protected abstract T mapear(ResultSet rs) throws SQLException;

    /** Carrega os objetos relacionados após a consulta (padrão: nada a fazer). */
    protected List<T> completar(List<T> itens) {
        return itens;
    }

    // ---------------------------------------------------------- operações comuns

    public Optional<T> buscarPorId(Long id) {
        List<T> resultado = consultar("SELECT * FROM " + getTabela() + " WHERE " + getColunaId() + " = ?", id);
        return resultado.isEmpty() ? Optional.empty() : Optional.of(resultado.get(0));
    }

    public List<T> listar() {
        return consultar("SELECT * FROM " + getTabela() + " ORDER BY " + getColunaId());
    }

    public List<T> buscarPorIds(Collection<Long> ids) {
        if (ids.isEmpty()) {
            return new ArrayList<>();
        }
        String marcadores = String.join(",", Collections.nCopies(ids.size(), "?"));
        return consultar("SELECT * FROM " + getTabela() + " WHERE " + getColunaId() + " IN (" + marcadores + ")",
                ids.toArray());
    }

    public boolean remover(Long id) {
        return executar("DELETE FROM " + getTabela() + " WHERE " + getColunaId() + " = ?", id) > 0;
    }

    public long contar() {
        return contar("SELECT COUNT(*) FROM " + getTabela());
    }

    // ---------------------------------------------------------- execução de SQL

    protected List<T> consultar(String sql, Object... parametros) {
        List<T> lista = new ArrayList<>();
        Connection conexao = null;
        try {
            conexao = DataSourceUtils.getConnection(dataSource);
            try (PreparedStatement ps = conexao.prepareStatement(sql)) {
                definirParametros(ps, parametros);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        lista.add(mapear(rs));
                    }
                }
            }
        } catch (SQLException e) {
            throw traduzir(e);
        } finally {
            DataSourceUtils.releaseConnection(conexao, dataSource);
        }
        // relacionamentos são carregados depois de liberar a conexão
        return completar(lista);
    }

    /** INSERT que devolve o id gerado pela coluna IDENTITY. */
    protected long inserir(String sql, Object... parametros) {
        Connection conexao = null;
        try {
            conexao = DataSourceUtils.getConnection(dataSource);
            try (PreparedStatement ps = conexao.prepareStatement(sql, new String[]{getColunaId()})) {
                definirParametros(ps, parametros);
                ps.executeUpdate();
                try (ResultSet chaves = ps.getGeneratedKeys()) {
                    if (!chaves.next()) {
                        throw new SQLException("O banco não retornou o id gerado.");
                    }
                    return chaves.getLong(1);
                }
            }
        } catch (SQLException e) {
            throw traduzir(e);
        } finally {
            DataSourceUtils.releaseConnection(conexao, dataSource);
        }
    }

    /** UPDATE / DELETE: devolve a quantidade de linhas afetadas. */
    protected int executar(String sql, Object... parametros) {
        Connection conexao = null;
        try {
            conexao = DataSourceUtils.getConnection(dataSource);
            try (PreparedStatement ps = conexao.prepareStatement(sql)) {
                definirParametros(ps, parametros);
                return ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw traduzir(e);
        } finally {
            DataSourceUtils.releaseConnection(conexao, dataSource);
        }
    }

    protected long contar(String sql, Object... parametros) {
        Connection conexao = null;
        try {
            conexao = DataSourceUtils.getConnection(dataSource);
            try (PreparedStatement ps = conexao.prepareStatement(sql)) {
                definirParametros(ps, parametros);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() ? rs.getLong(1) : 0;
                }
            }
        } catch (SQLException e) {
            throw traduzir(e);
        } finally {
            DataSourceUtils.releaseConnection(conexao, dataSource);
        }
    }

    /** Converte os tipos Java para os tipos do banco (enum -> texto, boolean -> 'S'/'N', datas...). */
    private void definirParametros(PreparedStatement ps, Object... parametros) throws SQLException {
        for (int i = 0; i < parametros.length; i++) {
            Object valor = parametros[i];
            int posicao = i + 1;
            if (valor == null) {
                ps.setNull(posicao, Types.VARCHAR);
            } else if (valor instanceof LocalDateTime dataHora) {
                ps.setTimestamp(posicao, Timestamp.valueOf(dataHora));
            } else if (valor instanceof LocalDate data) {
                ps.setDate(posicao, Date.valueOf(data));
            } else if (valor instanceof Boolean logico) {
                ps.setString(posicao, logico ? "S" : "N");
            } else if (valor instanceof Enum<?> opcao) {
                ps.setString(posicao, opcao.name());
            } else if (valor instanceof Long numero) {
                ps.setLong(posicao, numero);
            } else if (valor instanceof Integer numero) {
                ps.setInt(posicao, numero);
            } else if (valor instanceof Double numero) {
                ps.setDouble(posicao, numero);
            } else {
                ps.setString(posicao, valor.toString());
            }
        }
    }

    private RuntimeException traduzir(SQLException e) {
        boolean violacaoIntegridade = e instanceof SQLIntegrityConstraintViolationException
                || (e.getSQLState() != null && e.getSQLState().startsWith("23"));
        if (violacaoIntegridade) {
            return new ConflitoException("A operação viola uma restrição do banco de dados "
                    + "(registro duplicado ou vinculado a outros registros).");
        }
        return new BancoDeDadosException("Erro ao acessar o banco de dados.", e);
    }

    // ---------------------------------------------------------- leitura de colunas

    protected static LocalDateTime dataHora(ResultSet rs, String coluna) throws SQLException {
        Timestamp valor = rs.getTimestamp(coluna);
        return valor == null ? null : valor.toLocalDateTime();
    }

    protected static LocalDate data(ResultSet rs, String coluna) throws SQLException {
        Date valor = rs.getDate(coluna);
        return valor == null ? null : valor.toLocalDate();
    }

    protected static Double decimal(ResultSet rs, String coluna) throws SQLException {
        double valor = rs.getDouble(coluna);
        return rs.wasNull() ? null : valor;
    }

    protected static boolean simNao(ResultSet rs, String coluna) throws SQLException {
        return "S".equals(rs.getString(coluna));
    }

    protected static <E extends Enum<E>> E opcao(ResultSet rs, String coluna, Class<E> tipo) throws SQLException {
        String valor = rs.getString(coluna);
        return valor == null ? null : Enum.valueOf(tipo, valor.trim());
    }
}
