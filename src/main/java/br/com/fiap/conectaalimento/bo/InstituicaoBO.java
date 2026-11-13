package br.com.fiap.conectaalimento.bo;

import br.com.fiap.conectaalimento.dao.EstabelecimentoDAO;
import br.com.fiap.conectaalimento.dao.InstituicaoDAO;
import br.com.fiap.conectaalimento.dao.SolicitacaoDAO;
import br.com.fiap.conectaalimento.dto.InstituicaoRequest;
import br.com.fiap.conectaalimento.exception.ConflitoException;
import br.com.fiap.conectaalimento.exception.RecursoNaoEncontradoException;
import br.com.fiap.conectaalimento.model.InstituicaoReceptora;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Regras de negócio das instituições receptoras (US02, US04). */
@Service
public class InstituicaoBO extends UsuarioBO {

    private final SolicitacaoDAO solicitacaoDAO;

    public InstituicaoBO(EstabelecimentoDAO estabelecimentoDAO, InstituicaoDAO instituicaoDAO,
                         SolicitacaoDAO solicitacaoDAO) {
        super(estabelecimentoDAO, instituicaoDAO);
        this.solicitacaoDAO = solicitacaoDAO;
    }

    public List<InstituicaoReceptora> listar() {
        return instituicaoDAO.listar();
    }

    public InstituicaoReceptora buscar(Long id) {
        return instituicaoDAO.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Instituição", id));
    }

    @Transactional
    public InstituicaoReceptora cadastrar(InstituicaoRequest req) {
        exigirSenhaNoCadastro(req.senha());
        String email = normalizarEmail(req.email());
        validarEmailDisponivel(email, null);
        InstituicaoReceptora nova = new InstituicaoReceptora(null, req.nome().trim(), req.responsavel().trim(), email,
                req.senha(), digitos(req.telefone()), montarEndereco(req.endereco()), req.tipoInstituicao(),
                req.capacidadeAtendimento());
        return instituicaoDAO.inserir(nova);
    }

    @Transactional
    public InstituicaoReceptora atualizar(Long id, InstituicaoRequest req) {
        InstituicaoReceptora atual = buscar(id);
        String email = normalizarEmail(req.email());
        validarEmailDisponivel(email, atual);
        atual.setNome(req.nome().trim());
        atual.setResponsavel(req.responsavel().trim());
        atual.setEmail(email);
        atual.setTipoInstituicao(req.tipoInstituicao());
        atual.setCapacidadeAtendimento(req.capacidadeAtendimento());
        atual.atualizarContato(digitos(req.telefone()), montarEndereco(req.endereco()));
        trocarSenhaSeInformada(atual, req.senha());
        instituicaoDAO.atualizar(atual);
        return atual;
    }

    /** Só remove instituições sem solicitações (preserva o histórico de recebimentos). */
    @Transactional
    public void remover(Long id) {
        buscar(id);
        long solicitacoes = solicitacaoDAO.contarPorInstituicao(id);
        if (solicitacoes > 0) {
            throw new ConflitoException("A instituição possui " + solicitacoes
                    + " solicitação(ões) registrada(s) e não pode ser removida, para preservar o histórico.");
        }
        instituicaoDAO.remover(id);
    }
}
