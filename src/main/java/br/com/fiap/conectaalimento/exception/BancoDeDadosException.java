package br.com.fiap.conectaalimento.exception;

import org.springframework.http.HttpStatus;

/** 500 - falha ao acessar o banco de dados (a causa técnica fica só no log). */
public class BancoDeDadosException extends ApiException {

    public BancoDeDadosException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
}
