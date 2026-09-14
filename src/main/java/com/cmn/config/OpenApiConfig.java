package com.cmn.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    // Swagger UI 우측 상단 "Authorize" 버튼에서 사용할 스킴 이름.
    private static final String BEARER_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI openAPI() {
        SecurityScheme jwt = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("`/auth/login` 응답의 accessToken 을 붙여넣으면 이후 요청에 Authorization 헤더가 자동 첨부됩니다.");

        return new OpenAPI()
                .info(new Info()
                        .title("javamaven API")
                        .description("Spring Boot 3.3.5 / MyBatis / PostgreSQL REST API")
                        .version("v1"))
                .components(new Components().addSecuritySchemes(BEARER_SCHEME, jwt));
    }
}
