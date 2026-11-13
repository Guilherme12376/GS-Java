package br.com.fiap.conectaalimento;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

/**
 * Conecta Alimento - API RESTful (Global Solution 2026.2 - Domain Driven Design - Java).
 *
 * Camadas: controller -> bo -> dao -> banco Oracle, com model, dto e exception.
 */
@SpringBootApplication
public class ConectaAlimentoApplication {

    public static void main(String[] args) {
        // janelas de retirada e prazos seguem o horário de São Paulo, mesmo em servidores em UTC (Render)
        TimeZone.setDefault(TimeZone.getTimeZone("America/Sao_Paulo"));
        SpringApplication.run(ConectaAlimentoApplication.class, args);
    }
}
