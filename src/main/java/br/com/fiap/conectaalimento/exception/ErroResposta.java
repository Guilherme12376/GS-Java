package br.com.fiap.conectaalimento.exception;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Corpo JSON padronizado de todas as respostas de erro da API.
 */
public record ErroResposta(
        LocalDateTime timestamp,
        int status,
        String erro,
        String mensagem,
        String caminho,
        List<String> detalhes) {
}
