package br.com.fiap.conectaalimento.bo;

import br.com.fiap.conectaalimento.dao.EstabelecimentoDAO;
import br.com.fiap.conectaalimento.dao.InstituicaoDAO;
import br.com.fiap.conectaalimento.dto.LoginRequest;
import br.com.fiap.conectaalimento.dto.LoginResponse;
import br.com.fiap.conectaalimento.exception.CredenciaisInvalidasException;
import br.com.fiap.conectaalimento.model.Usuario;
import org.springframework.stereotype.Service;

import java.util.Optional;

/** US03 - Login com e-mail e senha, identificando o perfil do usuário. */
@Service
public class AutenticacaoBO {

    private final EstabelecimentoDAO estabelecimentoDAO;
    private final InstituicaoDAO instituicaoDAO;

    public AutenticacaoBO(EstabelecimentoDAO estabelecimentoDAO, InstituicaoDAO instituicaoDAO) {
        this.estabelecimentoDAO = estabelecimentoDAO;
        this.instituicaoDAO = instituicaoDAO;
    }

    public LoginResponse autenticar(LoginRequest req) {
        String email = req.email().trim().toLowerCase();
        Optional<? extends Usuario> usuario = estabelecimentoDAO.buscarPorEmail(email);
        if (usuario.isEmpty()) {
            usuario = instituicaoDAO.buscarPorEmail(email);
        }
        Usuario encontrado = usuario.orElseThrow(CredenciaisInvalidasException::new);
        if (!encontrado.autenticar(req.senha())) {
            throw new CredenciaisInvalidasException();
        }
        // [POLIMORFISMO] getTipoPerfil() devolve o perfil conforme o tipo real do usuário
        return new LoginResponse(encontrado.getId(), encontrado.getNome(), encontrado.getResponsavel(),
                encontrado.getEmail(), encontrado.getTipoPerfil());
    }
}
