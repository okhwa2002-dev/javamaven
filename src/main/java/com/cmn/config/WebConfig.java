package com.cmn.config;

import com.cmn.jwt.JwtAuthInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final String[] allowedOrigins;
    private final JwtAuthInterceptor jwtAuthInterceptor;

    public WebConfig(@Value("${cors.allowed-origins}") String[] allowedOrigins,
                     JwtAuthInterceptor jwtAuthInterceptor) {
        this.allowedOrigins = allowedOrigins;
        this.jwtAuthInterceptor = jwtAuthInterceptor;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 인증이 필요한 경로만 명시한다. 새 API 추가 시 여기에 등록할 것.
        // 메서드 단위 예외(회원가입 POST /users)는 JwtConfig 의 permitted 목록에서 처리한다.
        registry.addInterceptor(jwtAuthInterceptor)
                .addPathPatterns("/users", "/users/**");
    }
}
