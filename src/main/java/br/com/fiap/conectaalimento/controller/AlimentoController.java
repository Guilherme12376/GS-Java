package br.com.fiap.conectaalimento.controller;

import br.com.fiap.conectaalimento.bo.AlimentoBO;
import br.com.fiap.conectaalimento.dto.AlimentoRequest;
import br.com.fiap.conectaalimento.model.Alimento;
import br.com.fiap.conectaalimento.model.enums.Bairro;
import br.com.fiap.conectaalimento.model.enums.CategoriaAlimento;
import br.com.fiap.conectaalimento.model.enums.StatusAlimento;
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
import java.util.Map;

@RestController
@RequestMapping("/api/alimentos")
@Tag(name = "Alimentos", description = "Excedentes cadastrados pelos doadores")
public class AlimentoController {

    private final AlimentoBO bo;

    public AlimentoController(AlimentoBO bo) {
        this.bo = bo;
    }

    @GetMapping
    @Operation(summary = "Lista todos os alimentos (filtro opcional por status)")
    public ResponseEntity<List<Alimento>> listar(@RequestParam(required = false) StatusAlimento status) {
        return ResponseEntity.ok(bo.listar(status));
    }

    @GetMapping("/disponiveis")
    @Operation(summary = "Alimentos abertos a solicitações, com filtros combináveis; "
            + "com instituicaoId ordena pela distância até a instituição")
    public ResponseEntity<List<Alimento>> disponiveis(@RequestParam(required = false) CategoriaAlimento categoria,
                                                      @RequestParam(required = false) Bairro bairro,
                                                      @RequestParam(required = false) Integer prazoDias,
                                                      @RequestParam(required = false) Long instituicaoId) {
        return ResponseEntity.ok(bo.buscarDisponiveis(categoria, bairro, prazoDias, instituicaoId));
    }

    @GetMapping("/orientacoes")
    @Operation(summary = "Orientações de segurança alimentar (gerais e por tipo de alimento)")
    public ResponseEntity<Map<String, String>> orientacoes() {
        return ResponseEntity.ok(bo.orientacoes());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalhes de um alimento")
    public ResponseEntity<Alimento> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(bo.buscar(id));
    }

    @PostMapping
    @Operation(summary = "Cadastra um alimento excedente (valida a segurança alimentar conforme o tipo)")
    public ResponseEntity<Alimento> cadastrar(@Valid @RequestBody AlimentoRequest req) {
        Alimento criado = bo.cadastrar(req);
        return ResponseEntity.created(URI.create("/api/alimentos/" + criado.getId())).body(criado);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza um alimento ainda não reservado")
    public ResponseEntity<Alimento> atualizar(@PathVariable Long id, @Valid @RequestBody AlimentoRequest req) {
        return ResponseEntity.ok(bo.atualizar(id, req));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove o alimento da lista pública (status REMOVIDO) e cancela as solicitações ativas")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        bo.remover(id);
        return ResponseEntity.noContent().build();
    }
}
