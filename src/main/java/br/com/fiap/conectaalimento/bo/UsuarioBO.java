package br.com.fiap.conectaalimento.bo;

import br.com.fiap.conectaalimento.dao.EstabelecimentoDAO;
import br.com.fiap.conectaalimento.dao.InstituicaoDAO;
import br.com.fiap.conectaalimento.dto.EnderecoRequest;
import br.com.fiap.conectaalimento.exception.ConflitoException;
import br.com.fiap.conectaalimento.exception.RegraNegocioException;
import br.com.fiap.conectaalimento.model.Endereco;
import br.com.fiap.conectaalimento.model.Usuario;

/**
 * Regras comuns aos dois tipos de usuário.
 *
 * [HERANÇA] EstabelecimentoBO e InstituicaoBO herdam a validação de e-mail único,
 * a montagem do endereço, a normalização de dados e o tratamento de senha.
 */
public abstract class UsuarioBO {

    protected final EstabelecimentoDAO estabelecimentoDAO;
    protected final InstituicaoDAO instituicaoDAO;

    protected UsuarioBO(EstabelecimentoDAO estabelecimentoDAO, InstituicaoDAO instituicaoDAO) {
        this.estabelecimentoDAO = estabelecimentoDAO;
        this.instituicaoDAO = instituicaoDAO;
    }

    /**
     * O e-mail é o login, então precisa ser único entre estabelecimentos E instituições.
     * O usuário que está sendo alterado (usuarioAtual) pode manter o próprio e-mail.
     */
    protected void validarEmailDisponivel(String email, Usuario usuarioAtual) {
        estabelecimentoDAO.buscarPorEmail(email).ifPresent(existente -> {
            if (!existente.equals(usuarioAtual)) {
                throw new ConflitoException("Já existe um usuário cadastrado com o e-mail " + email + ".");
            }
        });
        instituicaoDAO.buscarPorEmail(email).ifPresent(existente -> {
            if (!existente.equals(usuarioAtual)) {
                throw new ConflitoException("Já existe um usuário cadastrado com o e-mail " + email + ".");
            }
        });
    }

    protected void exigirSenhaNoCadastro(String senha) {
        if (senha == null || senha.isBlank()) {
            throw new RegraNegocioException("Informe a senha (mínimo 6 caracteres).");
        }
    }

    /** No PUT a senha é opcional: só é trocada quando enviada. */
    protected void trocarSenhaSeInformada(Usuario usuario, String senha) {
        if (senha != null && !senha.isBlank()) {
            usuario.definirSenha(senha);
        }
    }

    /** Sem latitude/longitude, usa o centro do bairro (geolocalização aproximada). */
    protected Endereco montarEndereco(EnderecoRequest req) {
        if (req.latitude() != null && req.longitude() != null) {
            return new Endereco(req.logradouro().trim(), req.numero().trim(), req.bairro(), digitos(req.cep()),
                    req.latitude(), req.longitude());
        }
        return new Endereco(req.logradouro().trim(), req.numero().trim(), req.bairro(), digitos(req.cep()));
    }

    protected static String digitos(String texto) {
        return texto == null ? null : texto.replaceAll("\\D", "");
    }

    protected static String normalizarEmail(String email) {
        return email.trim().toLowerCase();
    }
}
