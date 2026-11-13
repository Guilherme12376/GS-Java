package br.com.fiap.conectaalimento.model;

import br.com.fiap.conectaalimento.model.enums.CategoriaAlimento;
import br.com.fiap.conectaalimento.model.enums.CondicaoArmazenamento;
import br.com.fiap.conectaalimento.model.enums.StatusAlimento;
import br.com.fiap.conectaalimento.model.enums.TipoAlimento;
import br.com.fiap.conectaalimento.model.enums.UnidadeMedida;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Excedente de alimento cadastrado por um estabelecimento (abstrato). Tabela T_CA_ALIMENTO.
 *
 * [HERANÇA] AlimentoPerecivel e AlimentoNaoPerecivel herdam atributos, construtor e
 * métodos (validarRegrasComuns, getPrazoLimiteRetirada, estaExpirado...).
 * [POLIMORFISMO - override] Cada tipo aplica as próprias regras de segurança alimentar.
 */
public abstract class Alimento extends Entidade {

    protected static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    protected static final DateTimeFormatter FORMATO_DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private String nome;
    private CategoriaAlimento categoria;
    private double quantidade;
    private UnidadeMedida unidade;
    private LocalDate dataValidade;
    private LocalDateTime inicioRetirada;
    private LocalDateTime fimRetirada;
    private String observacoes;
    private boolean declaracaoSeguranca;
    private StatusAlimento status;
    private EstabelecimentoDoador doador;

    /** Calculada nas buscas por proximidade (não é coluna do banco). */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double distanciaKm;

    /** Construtor padrão. */
    public Alimento() {
        super();
        this.status = StatusAlimento.DISPONIVEL;
    }

    /** Construtor não padrão (encadeado com Entidade via super). */
    public Alimento(Long id, String nome, CategoriaAlimento categoria, double quantidade, UnidadeMedida unidade,
                    LocalDate dataValidade, LocalDateTime inicioRetirada, LocalDateTime fimRetirada,
                    EstabelecimentoDoador doador) {
        super(id);
        this.nome = nome;
        this.categoria = categoria;
        this.quantidade = quantidade;
        this.unidade = unidade;
        this.dataValidade = dataValidade;
        this.inicioRetirada = inicioRetirada;
        this.fimRetirada = fimRetirada;
        this.doador = doador;
        this.status = StatusAlimento.DISPONIVEL;
    }

    // ---------------- métodos abstratos (override nas subclasses) ----------------

    public abstract TipoAlimento getTipo();

    public abstract CondicaoArmazenamento getCondicaoArmazenamento();

    /** Lista de pendências de segurança alimentar (vazia = pode ser publicado). */
    public abstract List<String> validarSegurancaAlimentar();

    public abstract String orientacoesEspecificas();

    // ---------------- métodos herdados pelas subclasses ----------------

    protected List<String> validarRegrasComuns() {
        List<String> pendencias = new ArrayList<>();
        if (quantidade <= 0) {
            pendencias.add("A quantidade deve ser maior que zero.");
        }
        if (dataValidade == null) {
            pendencias.add("Informe a validade ou o prazo de uso.");
        } else if (dataValidade.isBefore(LocalDate.now())) {
            pendencias.add("O alimento está vencido e não pode ser doado.");
        }
        if (inicioRetirada == null || fimRetirada == null) {
            pendencias.add("Informe o início e o fim da janela de retirada.");
        } else {
            if (!fimRetirada.isAfter(inicioRetirada)) {
                pendencias.add("O fim da janela de retirada deve ser depois do início.");
            }
            if (!fimRetirada.isAfter(LocalDateTime.now())) {
                pendencias.add("A janela de retirada já terminou.");
            }
            if (dataValidade != null && fimRetirada.isAfter(dataValidade.atTime(23, 59))) {
                pendencias.add("A retirada precisa terminar até a data de validade ("
                        + dataValidade.format(FORMATO_DATA) + ").");
            }
        }
        if (!declaracaoSeguranca) {
            pendencias.add("É obrigatório declarar ciência das orientações de segurança alimentar "
                    + "(declaracaoSeguranca = true).");
        }
        return pendencias;
    }

    public static String orientacoesGerais() {
        return "Doe apenas alimentos dentro da validade e sem sinais de deterioração; mantenha-os bem "
                + "armazenados até a retirada; informe com sinceridade quantidade, validade e conservação. "
                + "O cadastro NÃO é uma certificação sanitária: cada usuário é responsável pelas informações "
                + "prestadas e pelo respeito às normas sanitárias.";
    }

    /** Template Method: orientações gerais + específicas do tipo real do objeto. */
    public String orientacoesSeguranca() {
        return orientacoesGerais() + " " + orientacoesEspecificas();
    }

    /** Prazo real de retirada: o que vencer primeiro entre o fim da janela e o fim do dia da validade. */
    public LocalDateTime getPrazoLimiteRetirada() {
        if (fimRetirada == null || dataValidade == null) {
            return fimRetirada;
        }
        LocalDateTime fimValidade = dataValidade.atTime(23, 59);
        return fimRetirada.isBefore(fimValidade) ? fimRetirada : fimValidade;
    }

    /** [POLIMORFISMO - overload] O prazo de retirada já passou? */
    public boolean estaExpirado() {
        return estaExpirado(LocalDateTime.now());
    }

    /** [POLIMORFISMO - overload] O prazo de retirada já tinha passado na data de referência? */
    public boolean estaExpirado(LocalDateTime referencia) {
        LocalDateTime prazo = getPrazoLimiteRetirada();
        return prazo != null && referencia.isAfter(prazo);
    }

    public boolean aceitaSolicitacoes() {
        return (status == StatusAlimento.DISPONIVEL || status == StatusAlimento.SOLICITADO) && !estaExpirado();
    }

    public boolean podeSerEditado() {
        return status == StatusAlimento.DISPONIVEL || status == StatusAlimento.SOLICITADO;
    }

    public boolean podeSerRemovido() {
        return status != StatusAlimento.CONCLUIDO && status != StatusAlimento.REMOVIDO
                && status != StatusAlimento.EXPIRADO;
    }

    public boolean horarioDentroDaJanela(LocalDateTime dataHora) {
        return dataHora != null && !dataHora.isBefore(inicioRetirada) && !dataHora.isAfter(getPrazoLimiteRetirada());
    }

    public String janelaFormatada() {
        return inicioRetirada.format(FORMATO_DATA_HORA) + " até " + getPrazoLimiteRetirada().format(FORMATO_DATA_HORA);
    }

    /** [POLIMORFISMO - override] */
    @Override
    public String resumo() {
        return "Alimento #" + getId() + " - " + nome + " (" + status + ")";
    }

    // ---------------- getters e setters ----------------

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public CategoriaAlimento getCategoria() {
        return categoria;
    }

    public void setCategoria(CategoriaAlimento categoria) {
        this.categoria = categoria;
    }

    public double getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(double quantidade) {
        this.quantidade = quantidade;
    }

    public UnidadeMedida getUnidade() {
        return unidade;
    }

    public void setUnidade(UnidadeMedida unidade) {
        this.unidade = unidade;
    }

    public LocalDate getDataValidade() {
        return dataValidade;
    }

    public void setDataValidade(LocalDate dataValidade) {
        this.dataValidade = dataValidade;
    }

    public LocalDateTime getInicioRetirada() {
        return inicioRetirada;
    }

    public void setInicioRetirada(LocalDateTime inicioRetirada) {
        this.inicioRetirada = inicioRetirada;
    }

    public LocalDateTime getFimRetirada() {
        return fimRetirada;
    }

    public void setFimRetirada(LocalDateTime fimRetirada) {
        this.fimRetirada = fimRetirada;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public boolean isDeclaracaoSeguranca() {
        return declaracaoSeguranca;
    }

    public void setDeclaracaoSeguranca(boolean declaracaoSeguranca) {
        this.declaracaoSeguranca = declaracaoSeguranca;
    }

    public StatusAlimento getStatus() {
        return status;
    }

    public void setStatus(StatusAlimento status) {
        this.status = status;
    }

    public EstabelecimentoDoador getDoador() {
        return doador;
    }

    public void setDoador(EstabelecimentoDoador doador) {
        this.doador = doador;
    }

    public Double getDistanciaKm() {
        return distanciaKm;
    }

    public void setDistanciaKm(Double distanciaKm) {
        this.distanciaKm = distanciaKm;
    }
}
