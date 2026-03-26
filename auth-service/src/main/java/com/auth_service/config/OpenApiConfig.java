package com.auth_service.config;

import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
     OpenApiCustomizer customOpenApi() {
        return openApi -> {
            // This runs at the very end and OVERWRITES the "Generated server url"
            openApi.setServers(List.of(
                new Server().url("http://localhost:8080/gateway").description("API Gateway Route")
            ));
        };
    }
}