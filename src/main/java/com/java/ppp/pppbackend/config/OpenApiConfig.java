package com.java.ppp.pppbackend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI knowledgeManagementSystemAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Policy Procedure Platform API")
                        .description("Comprehensive REST API for Knowledge Management System")
                        .contact(new Contact()
                                .name("Policy Procedure Platform Support")
                                .url("https://ppp.com/support")
                                .email("support@ppp.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html"))
                        .version("1.0.0"))
                .servers(Arrays.asList(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Local Development Server"),
                        new Server()
                                .url("https://api.ppp.com")
                                .description("Production Server")
                ))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .name("bearerAuth")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT"))
                        .addSchemas("ErrorResponse", new ObjectSchema()
                                .addProperty("timestamp", new StringSchema().format("date-time"))
                                .addProperty("status", new IntegerSchema())
                                .addProperty("error", new StringSchema())
                                .addProperty("message", new StringSchema())
                                .addProperty("path", new StringSchema())));
    }
}
