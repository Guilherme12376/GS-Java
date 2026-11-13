package br.com.fiap.conectaalimento.model;

import java.time.LocalDateTime;

/**
 * Superclasse de todas as entidades (tabelas) do sistema.
 *
 * [HERANÇA] Concentra o identificador único (id) e a data de cadastro,
 * herdados por Usuario, Alimento, Solicitacao e Retirada.
 */
public abstract class Entidade {

    private Long id;
    private LocalDateTime dataCadastro;

    /** Construtor padrão. */
    public Entidade() {
        this.dataCadastro = LocalDateTime.now().withNano(0);
    }

    /** Construtor não padrão (reaproveita o padrão com this()). */
    public Entidade(Long id) {
        this();
        this.id = id;
    }

    /** Resumo de uma linha. [POLIMORFISMO - override] cada entidade define o seu. */
    public abstract String resumo();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getDataCadastro() {
        return dataCadastro;
    }

    public void setDataCadastro(LocalDateTime dataCadastro) {
        this.dataCadastro = dataCadastro;
    }

    /** [POLIMORFISMO - override] Entidades são iguais quando têm o mesmo tipo e o mesmo id. */
    @Override
    public boolean equals(Object outro) {
        if (this == outro) {
            return true;
        }
        if (outro == null || getClass() != outro.getClass()) {
            return false;
        }
        return id != null && id.equals(((Entidade) outro).id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }

    @Override
    public String toString() {
        return resumo();
    }
}
