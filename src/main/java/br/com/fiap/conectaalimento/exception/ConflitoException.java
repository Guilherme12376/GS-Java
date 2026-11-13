package br.com.fiap.conectaalimento.exception;

import org.springframework.http.HttpStatus;

/** 409 - conflito com o estado atual (e-mail/CNPJ duplicado, registro em uso...). */
public class ConflitoException extends ApiException {

    public ConflitoException(String mensagem) {
        super(mensagem);
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.CONFLICT;
    }
}
