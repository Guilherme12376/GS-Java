package br.com.fiap.conectaalimento.controller;

import br.com.fiap.conectaalimento.bo.RetiradaBO;
import br.com.fiap.conectaalimento.dto.ConfirmacaoRequest;
import br.com.fiap.conectaalimento.dto.ReagendamentoRequest;
import br.com.fiap.conectaalimento.dto.RetiradaRequest;
import br.com.fiap.conectaalimento.model.Retirada;
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
@RequestMapping("/api/retiradas")
@Tag(name = "Retiradas", description = "Agendamento e confirmação da entrega/recebimento")
public class RetiradaController {

    private final RetiradaBO bo;

    public RetiradaController(RetiradaBO bo) {
        this.bo = bo;
    }

    @GetMapping
    @Operation(summary = "Lista todas as retiradas")
    public ResponseEntity<List<Retirada>> listar() {
        return ResponseEntity.ok(bo.listar());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca uma retirada pelo id")
    public ResponseEntity<Retirada> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(bo.buscar(id));
    }

    @PostMapping
    @Operation(summary = "Agenda a retirada de uma solicitação ACEITA, dentro da janela do doador")
    public ResponseEntity<Retirada> agendar(@Valid @RequestBody RetiradaRequest req) {
        Retirada criada = bo.agendar(req);
        return ResponseEntity.created(URI.create("/api/retiradas/" + criada.getId())).body(criada);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Reagenda a retirada (somente antes de qualquer confirmação)")
    public ResponseEntity<Retirada> reagendar(@PathVariable Long id, @Valid @RequestBody ReagendamentoRequest req) {
        return ResponseEntity.ok(bo.reagendar(id, req.dataHoraAgendada()));
    }

    @PutMapping("/{id}/confirmar-entrega")
    @Operation(summary = "Doador confirma a entrega (sem corpo = quantidade solicitada)")
    public ResponseEntity<Retirada> confirmarEntrega(@PathVariable Long id,
                                                     @Valid @RequestBody(required = false) ConfirmacaoRequest req) {
        return ResponseEntity.ok(bo.confirmarEntrega(id, req == null ? null : req.quantidade()));
    }

    @PutMapping("/{id}/confirmar-recebimento")
    @Operation(summary = "Instituição confirma o recebimento; com as duas confirmações a doação é CONCLUÍDA")
    public ResponseEntity<Retirada> confirmarRecebimento(@PathVariable Long id,
                                                         @Valid @RequestBody(required = false) ConfirmacaoRequest req) {
        return ResponseEntity.ok(bo.confirmarRecebimento(id, req == null ? null : req.quantidade(),
                req == null ? null : req.observacao()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancela o agendamento (sem confirmações); a solicitação volta para ACEITA")
    public ResponseEntity<Void> cancelar(@PathVariable Long id) {
        bo.cancelar(id);
        return ResponseEntity.noContent().build();
    }
}
