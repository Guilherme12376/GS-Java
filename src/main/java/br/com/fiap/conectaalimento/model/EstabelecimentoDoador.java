package br.com.fiap.conectaalimento.model;

import br.com.fiap.conectaalimento.model.enums.TipoEstabelecimento;

/**
 * Estabelecimento comercial que cadastra excedentes de alimentos. Tabela T_CA_ESTABELECIMENTO.
 */
public class EstabelecimentoDoador extends Usuario {

    private String cnpj;
    private TipoEstabelecimento tipoEstabelecimento;

    /** Construtor padrão. */
    public EstabelecimentoDoador() {
        super();
    }

    /** Construtor não padrão: reaproveita o construtor de Usuario via super(...). */
    public EstabelecimentoDoador(Long id, String nome, String responsavel, String email, String senha,
                                 String telefone, Endereco endereco, String cnpj,
                                 TipoEstabelecimento tipoEstabelecimento) {
        super(id, nome, responsavel, email, senha, telefone, endereco);
        this.cnpj = cnpj;
        this.tipoEstabelecimento = tipoEstabelecimento;
    }

    /** [POLIMORFISMO - override] */
    @Override
    public String getTipoPerfil() {
        return "ESTABELECIMENTO_DOADOR";
    }

    /** [POLIMORFISMO - override] */
    @Override
    public String resumo() {
        return "Estabelecimento #" + getId() + " - " + getNome();
    }

    public String getCnpj() {
        return cnpj;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }

    public TipoEstabelecimento getTipoEstabelecimento() {
        return tipoEstabelecimento;
    }

    public void setTipoEstabelecimento(TipoEstabelecimento tipoEstabelecimento) {
        this.tipoEstabelecimento = tipoEstabelecimento;
    }
}
