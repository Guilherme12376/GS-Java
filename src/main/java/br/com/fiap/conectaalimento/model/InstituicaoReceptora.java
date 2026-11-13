package br.com.fiap.conectaalimento.model;

import br.com.fiap.conectaalimento.model.enums.TipoInstituicao;

/**
 * ONG, cozinha comunitária ou organização social que recebe doações. Tabela T_CA_INSTITUICAO.
 */
public class InstituicaoReceptora extends Usuario {

    private TipoInstituicao tipoInstituicao;
    private int capacidadeAtendimento;

    /** Construtor padrão. */
    public InstituicaoReceptora() {
        super();
    }

    /** Construtor não padrão: reaproveita o construtor de Usuario via super(...). */
    public InstituicaoReceptora(Long id, String nome, String responsavel, String email, String senha,
                                String telefone, Endereco endereco, TipoInstituicao tipoInstituicao,
                                int capacidadeAtendimento) {
        super(id, nome, responsavel, email, senha, telefone, endereco);
        this.tipoInstituicao = tipoInstituicao;
        this.capacidadeAtendimento = capacidadeAtendimento;
    }

    /** [POLIMORFISMO - override] */
    @Override
    public String getTipoPerfil() {
        return "INSTITUICAO_RECEPTORA";
    }

    /** [POLIMORFISMO - override] */
    @Override
    public String resumo() {
        return "Instituição #" + getId() + " - " + getNome();
    }

    public TipoInstituicao getTipoInstituicao() {
        return tipoInstituicao;
    }

    public void setTipoInstituicao(TipoInstituicao tipoInstituicao) {
        this.tipoInstituicao = tipoInstituicao;
    }

    public int getCapacidadeAtendimento() {
        return capacidadeAtendimento;
    }

    public void setCapacidadeAtendimento(int capacidadeAtendimento) {
        this.capacidadeAtendimento = capacidadeAtendimento;
    }
}
