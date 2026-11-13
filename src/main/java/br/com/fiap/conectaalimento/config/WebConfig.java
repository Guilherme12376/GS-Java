package br.com.fiap.conectaalimento.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** CORS liberado para o front-end e informações da documentação Swagger. */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }

    @Bean
    public OpenAPI documentacao() {
        return new OpenAPI().info(new Info()
                .title("Conecta Alimento API")
                .version("1.0.0")
                .description("API RESTful que conecta estabelecimentos com excedentes alimentares a instituições "
                        + "sociais (piloto Bom Retiro, Pari e Brás - São Paulo). Global Solution 2026.2 - FIAP. "
                        + "Datas no formato ISO: 2026-11-20 e 2026-11-20T18:00:00."));
    }
}
