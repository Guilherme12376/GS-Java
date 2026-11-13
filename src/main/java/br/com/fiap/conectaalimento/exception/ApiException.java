package br.com.fiap.conectaalimento.exception;

import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.List;

/**
 * Exceção base da aplicação.
 *
 * [HERANÇA] Todas as exceções de negócio herdam a mensagem e a lista de detalhes.
 * [POLIMORFISMO - override] Cada subclasse informa o próprio status HTTP em getStatus(),
 * e o GlobalExceptionHandler trata todas da mesma forma.
 */
public abstract class ApiException extends RuntimeException {

    private final List<String> detalhes;

    protected ApiException(String mensagem) {
        this(mensagem, new ArrayList<>());
    }

    protected ApiException(String mensagem, List<String> detalhes) {
        super(mensagem);
        this.detalhes = detalhes == null ? new ArrayList<>() : detalhes;
    }

    protected ApiException(String mensagem, Throwable causa) {
        super(mensagem, causa);
        this.detalhes = new ArrayList<>();
    }

    public abstract HttpStatus getStatus();

    public List<String> getDetalhes() {
        return detalhes;
    }
}
