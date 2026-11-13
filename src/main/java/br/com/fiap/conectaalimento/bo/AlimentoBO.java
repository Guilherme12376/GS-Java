package br.com.fiap.conectaalimento.bo;

import br.com.fiap.conectaalimento.dao.AlimentoDAO;
import br.com.fiap.conectaalimento.dao.EstabelecimentoDAO;
import br.com.fiap.conectaalimento.dao.InstituicaoDAO;
import br.com.fiap.conectaalimento.dao.SolicitacaoDAO;
import br.com.fiap.conectaalimento.dto.AlimentoRequest;
import br.com.fiap.conectaalimento.exception.RecursoNaoEncontradoException;
import br.com.fiap.conectaalimento.exception.RegraNegocioException;
import br.com.fiap.conectaalimento.model.Alimento;
import br.com.fiap.conectaalimento.model.AlimentoNaoPerecivel;
import br.com.fiap.conectaalimento.model.AlimentoPerecivel;
import br.com.fiap.conectaalimento.model.EstabelecimentoDoador;
import br.com.fiap.conectaalimento.model.InstituicaoReceptora;
import br.com.fiap.conectaalimento.model.Solicitacao;
import br.com.fiap.conectaalimento.model.enums.Bairro;
import br.com.fiap.conectaalimento.model.enums.CategoriaAlimento;
import br.com.fiap.conectaalimento.model.enums.StatusAlimento;
import br.com.fiap.conectaalimento.model.enums.TipoAlimento;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Regras de negócio dos alimentos: cadastro com segurança alimentar (US05), edição (US06),
 * remoção (US07), consulta de disponíveis (US15), filtros (US16), proximidade (US17) e detalhes (US18).
 */
@Service
public class AlimentoBO {

    private final AlimentoDAO alimentoDAO;
    private final SolicitacaoDAO solicitacaoDAO;
    private final EstabelecimentoDAO estabelecimentoDAO;
    private final InstituicaoDAO instituicaoDAO;

    public AlimentoBO(AlimentoDAO alimentoDAO, SolicitacaoDAO solicitacaoDAO, EstabelecimentoDAO estabelecimentoDAO,
                      InstituicaoDAO instituicaoDAO) {
        this.alimentoDAO = alimentoDAO;
        this.solicitacaoDAO = solicitacaoDAO;
        this.estabelecimentoDAO = estabelecimentoDAO;
        this.instituicaoDAO = instituicaoDAO;
    }

    public List<Alimento> listar(StatusAlimento status) {
        atualizarExpirados();
        return status == null ? alimentoDAO.listar() : alimentoDAO.listarPorStatus(status);
    }

    /** [POLIMORFISMO - overload] Disponíveis sem filtros, ordenados pelo prazo de retirada. */
    public List<Alimento> buscarDisponiveis() {
        return buscarDisponiveis(null, null, null, null);
    }

    /**
     * [POLIMORFISMO - overload] Disponíveis com filtros combináveis (nulos são ignorados).
     * Com instituicaoId, calcula a distância até cada doador e ordena do mais próximo ao mais distante.
     */
    public List<Alimento> buscarDisponiveis(CategoriaAlimento categoria, Bairro bairro, Integer prazoDias,
                                            Long instituicaoId) {
        atualizarExpirados();
        LocalDateTime limite = prazoDias == null ? null : LocalDate.now().plusDays(prazoDias).atTime(23, 59);
        List<Alimento> resultado = new ArrayList<>();
        for (Alimento a : alimentoDAO.listarAbertosParaSolicitacao()) {
            if (!a.aceitaSolicitacoes()
                    || (categoria != null && a.getCategoria() != categoria)
                    || (bairro != null && a.getDoador().getEndereco().getBairro() != bairro)
                    || (limite != null && a.getPrazoLimiteRetirada().isAfter(limite))) {
                continue;
            }
            resultado.add(a);
        }
        if (instituicaoId == null) {
            resultado.sort(Comparator.comparing(Alimento::getPrazoLimiteRetirada));
            return resultado;
        }
        InstituicaoReceptora instituicao = instituicaoDAO.buscarPorId(instituicaoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Instituição", instituicaoId));
        for (Alimento a : resultado) {
            a.setDistanciaKm(Math.round(instituicao.distanciaAte(a.getDoador()) * 100) / 100.0);
        }
        resultado.sort(Comparator.comparing(Alimento::getDistanciaKm));
        return resultado;
    }

    public Alimento buscar(Long id) {
        atualizarExpirados();
        return alimentoDAO.buscarPorId(id).orElseThrow(() -> new RecursoNaoEncontradoException("Alimento", id));
    }

    /** US05 - Só publica se passar na validação de segurança alimentar do tipo do alimento. */
    @Transactional
    public Alimento cadastrar(AlimentoRequest req) {
        EstabelecimentoDoador doador = estabelecimentoDAO.buscarPorId(req.estabelecimentoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Estabelecimento", req.estabelecimentoId()));
        Alimento alimento = montar(req, doador);
        validarSegurancaAlimentar(alimento, "Alimento não publicado: corrija as pendências de segurança alimentar.");
        return alimentoDAO.inserir(alimento);
    }

    /** US06 - Edição permitida apenas antes da reserva; revalida a segurança alimentar. */
    @Transactional
    public Alimento atualizar(Long id, AlimentoRequest req) {
        Alimento atual = buscar(id);
        if (!atual.getDoador().getId().equals(req.estabelecimentoId())) {
            throw new RegraNegocioException("Não é permitido transferir o alimento para outro estabelecimento.");
        }
        if (!atual.podeSerEditado()) {
            throw new RegraNegocioException("Só é possível editar alimentos DISPONIVEL ou SOLICITADO (status atual: "
                    + atual.getStatus() + ").");
        }
        Alimento novo = montar(req, atual.getDoador());
        novo.setId(id);
        novo.setStatus(atual.getStatus());
        novo.setDataCadastro(atual.getDataCadastro());
        validarSegurancaAlimentar(novo, "Alteração não salva: corrija as pendências de segurança alimentar.");
        alimentoDAO.atualizar(novo);
        return novo;
    }

    /** US07 - Remoção lógica: o alimento sai da lista pública e as solicitações ativas são canceladas. */
    @Transactional
    public void remover(Long id) {
        Alimento alimento = buscar(id);
        if (!alimento.podeSerRemovido()) {
            throw new RegraNegocioException("O alimento não pode mais ser removido (status: " + alimento.getStatus() + ").");
        }
        alimentoDAO.atualizarStatus(id, StatusAlimento.REMOVIDO);
        for (Solicitacao s : solicitacaoDAO.listarPorAlimento(id)) {
            if (s.isAtiva()) {
                s.cancelar("Alimento removido pelo doador");
                solicitacaoDAO.atualizar(s);
            }
        }
    }

    /**
     * Orientações de segurança alimentar.
     * [POLIMORFISMO] a mesma chamada orientacoesEspecificas() devolve textos diferentes por tipo.
     */
    public Map<String, String> orientacoes() {
        Map<String, String> orientacoes = new LinkedHashMap<>();
        orientacoes.put("GERAL", Alimento.orientacoesGerais());
        List<Alimento> tipos = List.of(new AlimentoPerecivel(), new AlimentoNaoPerecivel());
        for (Alimento tipo : tipos) {
            orientacoes.put(tipo.getTipo().name(), tipo.orientacoesEspecificas());
        }
        return orientacoes;
    }

    /** Marca como EXPIRADO o alimento cujo prazo passou sem agendamento e encerra as solicitações ativas. */
    @Transactional
    public void atualizarExpirados() {
        for (Alimento a : alimentoDAO.listarAtivosSemAgendamento()) {
            if (a.estaExpirado()) {
                alimentoDAO.atualizarStatus(a.getId(), StatusAlimento.EXPIRADO);
                for (Solicitacao s : solicitacaoDAO.listarPorAlimento(a.getId())) {
                    if (s.isAtiva()) {
                        s.marcarComoNaoAtendida("O prazo de retirada expirou");
                        solicitacaoDAO.atualizar(s);
                    }
                }
            }
        }
    }

    /** [POLIMORFISMO] O tipo informado decide qual subclasse de Alimento é criada. */
    private Alimento montar(AlimentoRequest req, EstabelecimentoDoador doador) {
        LocalDateTime inicio = req.inicioRetirada() != null
                ? req.inicioRetirada() : LocalDateTime.now().withSecond(0).withNano(0);
        Alimento alimento;
        if (req.tipo() == TipoAlimento.PERECIVEL) {
            alimento = new AlimentoPerecivel(null, req.nome().trim(), req.categoria(), req.quantidade(), req.unidade(),
                    req.dataValidade(), inicio, req.fimRetirada(), doador, req.condicaoArmazenamento());
        } else {
            alimento = new AlimentoNaoPerecivel(null, req.nome().trim(), req.categoria(), req.quantidade(),
                    req.unidade(), req.dataValidade(), inicio, req.fimRetirada(), doador,
                    Boolean.TRUE.equals(req.embalagemIntegra()), textoOuNulo(req.lote()));
        }
        alimento.setObservacoes(textoOuNulo(req.observacoes()));
        alimento.setDeclaracaoSeguranca(Boolean.TRUE.equals(req.declaracaoSeguranca()));
        alimento.setStatus(StatusAlimento.DISPONIVEL);
        return alimento;
    }

    private void validarSegurancaAlimentar(Alimento alimento, String mensagem) {
        // [POLIMORFISMO] executa a regra de AlimentoPerecivel ou de AlimentoNaoPerecivel
        List<String> pendencias = alimento.validarSegurancaAlimentar();
        if (!pendencias.isEmpty()) {
            throw new RegraNegocioException(mensagem, pendencias);
        }
    }

    /** No Oracle, texto vazio vira NULL; por isso normalizamos aqui. */
    private static String textoOuNulo(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim();
    }
}
