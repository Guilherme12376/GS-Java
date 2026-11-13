package br.com.fiap.conectaalimento.exception;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Camada de tratamento de exceções: converte qualquer erro em uma resposta JSON
 * padronizada (ErroResposta) com o status HTTP adequado.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** Exceções da aplicação: o status vem da própria exceção (polimorfismo). */
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErroResposta> tratarApiException(ApiException e, HttpServletRequest req) {
        if (e.getStatus().is5xxServerError()) {
            LOG.error("Erro interno em {}: {}", req.getRequestURI(), e.getMessage(), e);
        }
        return responder(e.getStatus(), e.getMessage(), req, e.getDetalhes());
    }

    /** 400 - campos inválidos no corpo da requisição (Bean Validation). */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResposta> tratarValidacao(MethodArgumentNotValidException e, HttpServletRequest req) {
        List<String> detalhes = new ArrayList<>();
        e.getBindingResult().getFieldErrors().forEach(erro ->
                detalhes.add(erro.getField() + ": " + erro.getDefaultMessage()));
        return responder(HttpStatus.BAD_REQUEST, "Existem campos inválidos na requisição.", req, detalhes);
    }

    /** 400 - JSON malformado, data fora do padrão ISO ou valor de enum inexistente. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResposta> tratarJsonInvalido(HttpMessageNotReadableException e, HttpServletRequest req) {
        LOG.warn("JSON inválido em {}: {}", req.getRequestURI(), e.getMostSpecificCause().getMessage());
        List<String> detalhes = new ArrayList<>();
        if (e.getCause() instanceof JsonMappingException jme && !jme.getPath().isEmpty()) {
            String campo = jme.getPath().stream()
                    .map(ref -> ref.getFieldName() != null ? ref.getFieldName() : "[" + ref.getIndex() + "]")
                    .collect(Collectors.joining("."));
            String detalhe = campo + ": valor inválido";
            if (jme instanceof InvalidFormatException ife && ife.getTargetType() != null
                    && ife.getTargetType().isEnum()) {
                detalhe += " (aceitos: " + Arrays.stream(ife.getTargetType().getEnumConstants())
                        .map(c -> ((Enum<?>) c).name()).collect(Collectors.joining(", ")) + ")";
            }
            detalhes.add(detalhe);
        }
        return responder(HttpStatus.BAD_REQUEST,
                "Corpo da requisição inválido. Confira o JSON, as datas (formato 2026-11-20T18:00:00) "
                        + "e os valores permitidos dos campos de opção.", req, detalhes);
    }

    /** 400 - parâmetro de URL com tipo errado (ex.: /api/alimentos/abc ou status inexistente). */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErroResposta> tratarTipoParametro(MethodArgumentTypeMismatchException e,
                                                            HttpServletRequest req) {
        return responder(HttpStatus.BAD_REQUEST,
                "Valor inválido para o parâmetro '" + e.getName() + "': " + e.getValue(), req, List.of());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErroResposta> tratarParametroAusente(MissingServletRequestParameterException e,
                                                               HttpServletRequest req) {
        return responder(HttpStatus.BAD_REQUEST,
                "Parâmetro obrigatório ausente: " + e.getParameterName(), req, List.of());
    }

    /** 405 - método HTTP não suportado no endpoint. */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErroResposta> tratarMetodo(HttpRequestMethodNotSupportedException e, HttpServletRequest req) {
        return responder(HttpStatus.METHOD_NOT_ALLOWED,
                "Método " + e.getMethod() + " não é suportado neste endpoint.", req, List.of());
    }

    /** 404 - rota inexistente. */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErroResposta> tratarRotaInexistente(NoResourceFoundException e, HttpServletRequest req) {
        return responder(HttpStatus.NOT_FOUND, "Endpoint não encontrado.", req, List.of());
    }

    /** 500 - falha de acesso ao banco (conexão, SQL...). */
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErroResposta> tratarBanco(DataAccessException e, HttpServletRequest req) {
        LOG.error("Erro de banco de dados em {}", req.getRequestURI(), e);
        return responder(HttpStatus.INTERNAL_SERVER_ERROR,
                "Não foi possível acessar o banco de dados. Tente novamente em instantes.", req, List.of());
    }

    /** 500 - qualquer erro não previsto (a mensagem técnica fica apenas no log). */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResposta> tratarGenerico(Exception e, HttpServletRequest req) {
        LOG.error("Erro inesperado em {}", req.getRequestURI(), e);
        return responder(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno inesperado.", req, List.of());
    }

    private ResponseEntity<ErroResposta> responder(HttpStatus status, String mensagem, HttpServletRequest req,
                                                   List<String> detalhes) {
        ErroResposta corpo = new ErroResposta(LocalDateTime.now().withNano(0), status.value(),
                status.getReasonPhrase(), mensagem, req.getRequestURI(), detalhes);
        return ResponseEntity.status(status).body(corpo);
    }
}
