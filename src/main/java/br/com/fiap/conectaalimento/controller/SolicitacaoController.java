package br.com.fiap.conectaalimento.controller;

import br.com.fiap.conectaalimento.bo.SolicitacaoBO;
import br.com.fiap.conectaalimento.dto.QuantidadeRequest;
import br.com.fiap.conectaalimento.dto.RecusaRequest;
import br.com.fiap.conectaalimento.dto.SolicitacaoRequest;
import br.com.fiap.conectaalimento.model.Solicitacao;
import br.com.fiap.conectaalimento.model.enums.StatusSolicitacao;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/solicitacoes")
@Tag(name = "Solicitações", description = "Pedidos das instituições e respostas dos doadores")
public class SolicitacaoController {

    private final SolicitacaoBO bo;

    public SolicitacaoController(SolicitacaoBO bo) {
        this.bo = bo;
    }

    @GetMapping
    @Operation(summary = "Lista solicitações com filtros opcionais (status, instituição, estabelecimento)")
    public ResponseEntity<List<Solicitacao>> listar(@RequestParam(required = false) StatusSolicitacao status,
                                                    @RequestParam(required = false) Long instituicaoId,
                                                    @RequestParam(required = false) Long estabelecimentoId) {
        return ResponseEntity.ok(bo.listar(status, instituicaoId, estabelecimentoId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca uma solicitação pelo id")
    public ResponseEntity<Solicitacao> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(bo.buscar(id));
    }

    @PostMapping
    @Operation(summary = "Instituição solicita um alimento disponível")
    public ResponseEntity<Solicitacao> criar(@Valid @RequestBody SolicitacaoRequest req) {
        Solicitacao criada = bo.criar(req);
        return ResponseEntity.created(URI.create("/api/solicitacoes/" + criada.getId())).body(criada);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Altera a quantidade de uma solicitação PENDENTE")
    public ResponseEntity<Solicitacao> atualizar(@PathVariable Long id, @Valid @RequestBody QuantidadeRequest req) {
        return ResponseEntity.ok(bo.atualizarQuantidade(id, req.quantidade()));
    }

    @PutMapping("/{id}/aceitar")
    @Operation(summary = "Doador aceita a solicitação (alimento fica RESERVADO; as demais pendentes não são atendidas)")
    public ResponseEntity<Solicitacao> aceitar(@PathVariable Long id) {
        return ResponseEntity.ok(bo.aceitar(id));
    }

    @PutMapping("/{id}/recusar")
    @Operation(summary = "Doador recusa a solicitação (motivo opcional)")
    public ResponseEntity<Solicitacao> recusar(@PathVariable Long id,
                                               @Valid @RequestBody(required = false) RecusaRequest req) {
        return ResponseEntity.ok(bo.recusar(id, req == null ? null : req.motivo()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Instituição cancela a solicitação (PENDENTE ou ACEITA)")
    public ResponseEntity<Void> cancelar(@PathVariable Long id) {
        bo.cancelar(id);
        return ResponseEntity.noContent().build();
    }
}
