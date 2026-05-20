package com.conduit.shared.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI conduitOpenAPI() {
    return new OpenAPI()
        .info(
            new Info()
                .title("Conduit API")
                .description("RealWorld Conduit API — Medium.com clone")
                .version("1.0.0"))
        .components(
            new Components()
                .addSecuritySchemes(
                    "Token",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.APIKEY)
                        .in(SecurityScheme.In.HEADER)
                        .name("Authorization")
                        .description(
                            "JWT token with 'Token ' prefix. Example: `Token eyJhbGci...`")));
  }
}
