package br.com.fiap.conectaalimento.exception;

import org.springframework.http.HttpStatus;

/** 401 - e-mail ou senha incorretos no login. */
public class CredenciaisInvalidasException extends ApiException {

    public CredenciaisInvalidasException() {
        super("E-mail ou senha incorretos.");
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.UNAUTHORIZED;
    }
}
