package br.com.fiap.conectaalimento.controller;

import br.com.fiap.conectaalimento.bo.AlimentoBO;
import br.com.fiap.conectaalimento.bo.InstituicaoBO;
import br.com.fiap.conectaalimento.bo.SolicitacaoBO;
import br.com.fiap.conectaalimento.dto.InstituicaoRequest;
import br.com.fiap.conectaalimento.model.Alimento;
import br.com.fiap.conectaalimento.model.InstituicaoReceptora;
import br.com.fiap.conectaalimento.model.Solicitacao;
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
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/instituicoes")
@Tag(name = "Instituições receptoras", description = "Cadastro de ONGs, cozinhas comunitárias e organizações sociais")
public class InstituicaoController {

    private final InstituicaoBO bo;
    private final AlimentoBO alimentoBO;
    private final SolicitacaoBO solicitacaoBO;

    public InstituicaoController(InstituicaoBO bo, AlimentoBO alimentoBO, SolicitacaoBO solicitacaoBO) {
        this.bo = bo;
        this.alimentoBO = alimentoBO;
        this.solicitacaoBO = solicitacaoBO;
    }

    @GetMapping
    @Operation(summary = "Lista todas as instituições receptoras")
    public ResponseEntity<List<InstituicaoReceptora>> listar() {
        return ResponseEntity.ok(bo.listar());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca uma instituição pelo id")
    public ResponseEntity<InstituicaoReceptora> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(bo.buscar(id));
    }

    @GetMapping("/{id}/solicitacoes")
    @Operation(summary = "Lista as solicitações feitas pela instituição")
    public ResponseEntity<List<Solicitacao>> listarSolicitacoes(@PathVariable Long id) {
        bo.buscar(id);
        return ResponseEntity.ok(solicitacaoBO.listar(null, id, null));
    }

    @GetMapping("/{id}/alimentos-proximos")
    @Operation(summary = "Alimentos disponíveis ordenados pela distância até a instituição")
    public ResponseEntity<List<Alimento>> alimentosProximos(@PathVariable Long id) {
        return ResponseEntity.ok(alimentoBO.buscarDisponiveis(null, null, null, id));
    }

    @PostMapping
    @Operation(summary = "Cadastra uma instituição receptora (e-mail único)")
    public ResponseEntity<InstituicaoReceptora> cadastrar(@Valid @RequestBody InstituicaoRequest req) {
        InstituicaoReceptora criada = bo.cadastrar(req);
        return ResponseEntity.created(URI.create("/api/instituicoes/" + criada.getId())).body(criada);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza os dados da instituição (senha opcional)")
    public ResponseEntity<InstituicaoReceptora> atualizar(@PathVariable Long id,
                                                          @Valid @RequestBody InstituicaoRequest req) {
        return ResponseEntity.ok(bo.atualizar(id, req));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove a instituição (somente se não tiver solicitações)")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        bo.remover(id);
        return ResponseEntity.noContent().build();
    }
}
