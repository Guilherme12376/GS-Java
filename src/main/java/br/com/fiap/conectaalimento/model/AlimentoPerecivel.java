package br.com.fiap.conectaalimento.model;

import br.com.fiap.conectaalimento.model.enums.CategoriaAlimento;
import br.com.fiap.conectaalimento.model.enums.CondicaoArmazenamento;
import br.com.fiap.conectaalimento.model.enums.TipoAlimento;
import br.com.fiap.conectaalimento.model.enums.UnidadeMedida;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Alimento perecível (pães, frutas, laticínios, refeições prontas...).
 */
public class AlimentoPerecivel extends Alimento {

    private CondicaoArmazenamento condicaoArmazenamento;

    /** Construtor padrão. */
    public AlimentoPerecivel() {
        super();
        this.condicaoArmazenamento = CondicaoArmazenamento.TEMPERATURA_AMBIENTE;
    }

    /** Construtor não padrão: reaproveita o construtor de Alimento via super(...). */
    public AlimentoPerecivel(Long id, String nome, CategoriaAlimento categoria, double quantidade,
                             UnidadeMedida unidade, LocalDate dataValidade, LocalDateTime inicioRetirada,
                             LocalDateTime fimRetirada, EstabelecimentoDoador doador,
                             CondicaoArmazenamento condicaoArmazenamento) {
        super(id, nome, categoria, quantidade, unidade, dataValidade, inicioRetirada, fimRetirada, doador);
        this.condicaoArmazenamento = condicaoArmazenamento;
    }

    /** [POLIMORFISMO - override] */
    @Override
    public TipoAlimento getTipo() {
        return TipoAlimento.PERECIVEL;
    }

    /** [POLIMORFISMO - override] */
    @Override
    public CondicaoArmazenamento getCondicaoArmazenamento() {
        return condicaoArmazenamento;
    }

    /** [POLIMORFISMO - override] Regras comuns + cadeia de frio para laticínios e carnes. */
    @Override
    public List<String> validarSegurancaAlimentar() {
        List<String> pendencias = validarRegrasComuns();
        if (condicaoArmazenamento == null) {
            pendencias.add("Alimentos perecíveis precisam informar a condição de armazenamento.");
        } else if (getCategoria() != null && getCategoria().exigeRefrigeracao()
                && condicaoArmazenamento == CondicaoArmazenamento.TEMPERATURA_AMBIENTE) {
            pendencias.add("Itens da categoria '" + getCategoria().getDescricao()
                    + "' precisam estar refrigerados ou congelados para serem doados.");
        }
        return pendencias;
    }

    /** [POLIMORFISMO - override] */
    @Override
    public String orientacoesEspecificas() {
        return "PERECÍVEIS: mantenha a cadeia de frio (refrigerados de 0 °C a 5 °C; congelados a -18 °C "
                + "ou menos), use caixa térmica no transporte, combine a retirada para o menor tempo possível "
                + "e doe refeições prontas no mesmo dia do preparo.";
    }

    public void setCondicaoArmazenamento(CondicaoArmazenamento condicaoArmazenamento) {
        this.condicaoArmazenamento = condicaoArmazenamento;
    }
}
