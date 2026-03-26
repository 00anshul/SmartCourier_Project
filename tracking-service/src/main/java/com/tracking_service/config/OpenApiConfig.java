package com.tracking_service.config; // Adjust package name if needed!

import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.List;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenApiCustomizer customOpenApi() {
        return openApi -> {
            openApi.setServers(List.of(
                // Note the /tracking path here!
                new Server().url("http://localhost:8080/gateway").description("API Gateway Route")
            ));
        };
    }
}