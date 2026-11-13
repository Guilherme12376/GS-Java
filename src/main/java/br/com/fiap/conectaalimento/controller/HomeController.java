package br.com.fiap.conectaalimento.controller;

import br.com.fiap.conectaalimento.bo.AutenticacaoBO;
import br.com.fiap.conectaalimento.bo.IndicadoresBO;
import br.com.fiap.conectaalimento.dto.IndicadoresResponse;
import br.com.fiap.conectaalimento.dto.LoginRequest;
import br.com.fiap.conectaalimento.dto.LoginResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@Tag(name = "Geral", description = "Página inicial, status, login e indicadores de impacto")
public class HomeController {

    private final AutenticacaoBO autenticacaoBO;
    private final IndicadoresBO indicadoresBO;
    private final JdbcTemplate jdbcTemplate;

    public HomeController(AutenticacaoBO autenticacaoBO, IndicadoresBO indicadoresBO, JdbcTemplate jdbcTemplate) {
        this.autenticacaoBO = autenticacaoBO;
        this.indicadoresBO = indicadoresBO;
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/")
    @Operation(summary = "Apresentação da API e links úteis")
    public Map<String, Object> inicio() {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("projeto", "Conecta Alimento - API RESTful");
        info.put("descricao", "Conecta estabelecimentos com excedentes alimentares a instituições sociais "
                + "(piloto Bom Retiro, Pari e Brás - São Paulo).");
        info.put("documentacao", "/swagger-ui.html");
        info.put("status", "/api/status");
        info.put("recursos", new String[]{"/api/estabelecimentos", "/api/instituicoes", "/api/alimentos",
                "/api/alimentos/disponiveis", "/api/solicitacoes", "/api/retiradas", "/api/indicadores", "/api/login"});
        return info;
    }

    @GetMapping("/api/status")
    @Operation(summary = "Verifica se a API e o banco de dados estão respondendo")
    public ResponseEntity<Map<String, Object>> status() {
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("api", "UP");
        status.put("horario", LocalDateTime.now().withNano(0));
        try {
            jdbcTemplate.queryForObject("SELECT 1 FROM DUAL", Integer.class);
            status.put("banco", "UP");
            return ResponseEntity.ok(status);
        } catch (RuntimeException e) {
            status.put("banco", "DOWN");
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(status);
        }
    }

    @PostMapping("/api/login")
    @Operation(summary = "Login com e-mail e senha; devolve o id e o perfil do usuário")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(autenticacaoBO.autenticar(req));
    }

    @GetMapping("/api/indicadores")
    @Operation(summary = "Painel de impacto: kg doados, doações concluídas, % retirado, tempo médio...")
    public ResponseEntity<IndicadoresResponse> indicadores() {
        return ResponseEntity.ok(indicadoresBO.calcular());
    }
}
