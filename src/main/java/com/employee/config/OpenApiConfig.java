package com.employee.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

        @Bean
        public OpenAPI myOpenAPI() {
                Contact contact = new Contact();
                contact.setEmail("your.email@example.com");
                contact.setName("Your Name");

                SecurityScheme securityScheme = new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT");

                return new OpenAPI()
                        .info(new Info()
                                .title("Employee Management API")
                                .version("1.0")
                                .description("API documentation for Employee Management System")
                                .contact(contact))
                        .schemaRequirement("bearerAuth", securityScheme)
                        .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
        }
}