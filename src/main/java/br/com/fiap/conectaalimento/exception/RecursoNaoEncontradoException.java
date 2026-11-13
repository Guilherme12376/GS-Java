package br.com.fiap.conectaalimento.exception;

import org.springframework.http.HttpStatus;

/** 404 - o registro pedido não existe. */
public class RecursoNaoEncontradoException extends ApiException {

    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }

    /** [POLIMORFISMO - overload] Monta a mensagem a partir do nome do recurso e do id. */
    public RecursoNaoEncontradoException(String recurso, Long id) {
        super(recurso + " com id " + id + " não encontrado(a).");
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.NOT_FOUND;
    }
}
