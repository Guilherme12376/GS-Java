package br.com.fiap.conectaalimento.model;

import br.com.fiap.conectaalimento.model.enums.CategoriaAlimento;
import br.com.fiap.conectaalimento.model.enums.CondicaoArmazenamento;
import br.com.fiap.conectaalimento.model.enums.TipoAlimento;
import br.com.fiap.conectaalimento.model.enums.UnidadeMedida;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Alimento não perecível (grãos, massas, enlatados...). Só é aceito com embalagem íntegra.
 */
public class AlimentoNaoPerecivel extends Alimento {

    private boolean embalagemIntegra;
    private String lote;

    /** Construtor padrão. */
    public AlimentoNaoPerecivel() {
        super();
        this.embalagemIntegra = true;
    }

    /** Construtor não padrão: reaproveita o construtor de Alimento via super(...). */
    public AlimentoNaoPerecivel(Long id, String nome, CategoriaAlimento categoria, double quantidade,
                                UnidadeMedida unidade, LocalDate dataValidade, LocalDateTime inicioRetirada,
                                LocalDateTime fimRetirada, EstabelecimentoDoador doador, boolean embalagemIntegra,
                                String lote) {
        super(id, nome, categoria, quantidade, unidade, dataValidade, inicioRetirada, fimRetirada, doador);
        this.embalagemIntegra = embalagemIntegra;
        this.lote = lote;
    }

    /** [POLIMORFISMO - override] */
    @Override
    public TipoAlimento getTipo() {
        return TipoAlimento.NAO_PERECIVEL;
    }

    /** [POLIMORFISMO - override] Não perecíveis ficam sempre em temperatura ambiente. */
    @Override
    public CondicaoArmazenamento getCondicaoArmazenamento() {
        return CondicaoArmazenamento.TEMPERATURA_AMBIENTE;
    }

    /** [POLIMORFISMO - override] Regras comuns + embalagem íntegra + categoria compatível. */
    @Override
    public List<String> validarSegurancaAlimentar() {
        List<String> pendencias = validarRegrasComuns();
        if (!embalagemIntegra) {
            pendencias.add("Não perecíveis só podem ser doados com a embalagem original íntegra.");
        }
        if (getCategoria() != null && getCategoria().exigeRefrigeracao()) {
            pendencias.add("A categoria '" + getCategoria().getDescricao() + "' deve ser cadastrada como PERECIVEL.");
        }
        return pendencias;
    }

    /** [POLIMORFISMO - override] */
    @Override
    public String orientacoesEspecificas() {
        return "NÃO PERECÍVEIS: confira se a embalagem está fechada, sem furos, amassados profundos ou "
                + "estufamento, e armazene em local seco, arejado e longe de produtos de limpeza.";
    }

    public boolean isEmbalagemIntegra() {
        return embalagemIntegra;
    }

    public void setEmbalagemIntegra(boolean embalagemIntegra) {
        this.embalagemIntegra = embalagemIntegra;
    }

    public String getLote() {
        return lote;
    }

    public void setLote(String lote) {
        this.lote = lote;
    }
}
