package br.com.fiap.conectaalimento.controller;

import br.com.fiap.conectaalimento.bo.EstabelecimentoBO;
import br.com.fiap.conectaalimento.dto.EstabelecimentoRequest;
import br.com.fiap.conectaalimento.model.Alimento;
import br.com.fiap.conectaalimento.model.EstabelecimentoDoador;
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
@RequestMapping("/api/estabelecimentos")
@Tag(name = "Estabelecimentos doadores", description = "Cadastro de padarias, mercados e demais doadores")
public class EstabelecimentoController {

    private final EstabelecimentoBO bo;

    public EstabelecimentoController(EstabelecimentoBO bo) {
        this.bo = bo;
    }

    @GetMapping
    @Operation(summary = "Lista todos os estabelecimentos doadores")
    public ResponseEntity<List<EstabelecimentoDoador>> listar() {
        return ResponseEntity.ok(bo.listar());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um estabelecimento pelo id")
    public ResponseEntity<EstabelecimentoDoador> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(bo.buscar(id));
    }

    @GetMapping("/{id}/alimentos")
    @Operation(summary = "Lista os alimentos cadastrados pelo estabelecimento, com o status de cada um")
    public ResponseEntity<List<Alimento>> listarAlimentos(@PathVariable Long id) {
        return ResponseEntity.ok(bo.listarAlimentos(id));
    }

    @PostMapping
    @Operation(summary = "Cadastra um estabelecimento doador (e-mail e CNPJ únicos)")
    public ResponseEntity<EstabelecimentoDoador> cadastrar(@Valid @RequestBody EstabelecimentoRequest req) {
        EstabelecimentoDoador criado = bo.cadastrar(req);
        return ResponseEntity.created(URI.create("/api/estabelecimentos/" + criado.getId())).body(criado);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza os dados do estabelecimento (senha opcional)")
    public ResponseEntity<EstabelecimentoDoador> atualizar(@PathVariable Long id,
                                                           @Valid @RequestBody EstabelecimentoRequest req) {
        return ResponseEntity.ok(bo.atualizar(id, req));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove o estabelecimento (somente se não tiver alimentos cadastrados)")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        bo.remover(id);
        return ResponseEntity.noContent().build();
    }
}
