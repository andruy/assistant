package com.andruy.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI openAPI() {
        SecurityScheme cookieAuth = new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.COOKIE)
                .name("JSESSIONID")
                .description("Login via /login.html to obtain a session cookie (JSESSIONID). Swagger UI requests reuse that cookie.");

        return new OpenAPI()
                .components(new Components().addSecuritySchemes("cookieAuth", cookieAuth))
                .addSecurityItem(new SecurityRequirement().addList("cookieAuth"));
    }
}
