package br.com.fiap.conectaalimento.exception;

import org.springframework.http.HttpStatus;

import java.util.List;

/** 422 - os dados estão no formato certo, mas violam uma regra de negócio. */
public class RegraNegocioException extends ApiException {

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }

    /** [POLIMORFISMO - overload] Regra violada com a lista de pendências encontradas. */
    public RegraNegocioException(String mensagem, List<String> pendencias) {
        super(mensagem, pendencias);
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.UNPROCESSABLE_ENTITY;
    }
}
