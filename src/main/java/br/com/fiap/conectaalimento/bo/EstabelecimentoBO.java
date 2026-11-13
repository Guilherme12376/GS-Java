package br.com.fiap.conectaalimento.bo;

import br.com.fiap.conectaalimento.dao.AlimentoDAO;
import br.com.fiap.conectaalimento.dao.EstabelecimentoDAO;
import br.com.fiap.conectaalimento.dao.InstituicaoDAO;
import br.com.fiap.conectaalimento.dto.EstabelecimentoRequest;
import br.com.fiap.conectaalimento.exception.ConflitoException;
import br.com.fiap.conectaalimento.exception.RecursoNaoEncontradoException;
import br.com.fiap.conectaalimento.model.Alimento;
import br.com.fiap.conectaalimento.model.EstabelecimentoDoador;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Regras de negócio dos estabelecimentos doadores (US01, US04). */
@Service
public class EstabelecimentoBO extends UsuarioBO {

    private final AlimentoDAO alimentoDAO;
    private final AlimentoBO alimentoBO;

    public EstabelecimentoBO(EstabelecimentoDAO estabelecimentoDAO, InstituicaoDAO instituicaoDAO,
                             AlimentoDAO alimentoDAO, AlimentoBO alimentoBO) {
        super(estabelecimentoDAO, instituicaoDAO);
        this.alimentoDAO = alimentoDAO;
        this.alimentoBO = alimentoBO;
    }

    public List<EstabelecimentoDoador> listar() {
        return estabelecimentoDAO.listar();
    }

    public EstabelecimentoDoador buscar(Long id) {
        return estabelecimentoDAO.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Estabelecimento", id));
    }

    @Transactional
    public EstabelecimentoDoador cadastrar(EstabelecimentoRequest req) {
        exigirSenhaNoCadastro(req.senha());
        String email = normalizarEmail(req.email());
        String cnpj = digitos(req.cnpj());
        validarEmailDisponivel(email, null);
        validarCnpjDisponivel(cnpj, null);
        EstabelecimentoDoador novo = new EstabelecimentoDoador(null, req.nome().trim(), req.responsavel().trim(), email,
                req.senha(), digitos(req.telefone()), montarEndereco(req.endereco()), cnpj, req.tipoEstabelecimento());
        return estabelecimentoDAO.inserir(novo);
    }

    @Transactional
    public EstabelecimentoDoador atualizar(Long id, EstabelecimentoRequest req) {
        EstabelecimentoDoador atual = buscar(id);
        String email = normalizarEmail(req.email());
        String cnpj = digitos(req.cnpj());
        validarEmailDisponivel(email, atual);
        validarCnpjDisponivel(cnpj, atual);
        atual.setNome(req.nome().trim());
        atual.setResponsavel(req.responsavel().trim());
        atual.setEmail(email);
        atual.setCnpj(cnpj);
        atual.setTipoEstabelecimento(req.tipoEstabelecimento());
        atual.atualizarContato(digitos(req.telefone()), montarEndereco(req.endereco()));
        trocarSenhaSeInformada(atual, req.senha());
        estabelecimentoDAO.atualizar(atual);
        return atual;
    }

    /** Só remove estabelecimentos sem alimentos cadastrados (preserva o histórico das doações). */
    @Transactional
    public void remover(Long id) {
        buscar(id);
        long alimentos = alimentoDAO.contarPorEstabelecimento(id);
        if (alimentos > 0) {
            throw new ConflitoException("O estabelecimento possui " + alimentos
                    + " alimento(s) cadastrado(s) e não pode ser removido, para preservar o histórico de doações.");
        }
        estabelecimentoDAO.remover(id);
    }

    /** US08 - Alimentos do estabelecimento com o status de cada um. */
    public List<Alimento> listarAlimentos(Long id) {
        buscar(id);
        alimentoBO.atualizarExpirados();
        return alimentoDAO.listarPorEstabelecimento(id);
    }

    private void validarCnpjDisponivel(String cnpj, EstabelecimentoDoador atual) {
        estabelecimentoDAO.buscarPorCnpj(cnpj).ifPresent(existente -> {
            if (!existente.equals(atual)) {
                throw new ConflitoException("Já existe um estabelecimento cadastrado com este CNPJ.");
            }
        });
    }
}
