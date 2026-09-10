package com.cmn.config;

import com.cmn.jwt.JwtAuthInterceptor;
import com.cmn.jwt.JwtProperties;
import com.cmn.jwt.JwtTokenProvider;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Set;

@Slf4j
@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class JwtConfig {

    // HMAC-SHA256 최소 키 길이
    private static final int MIN_SECRET_BYTES = 32;

    @Bean
    public JwtTokenProvider jwtTokenProvider(JwtProperties props, Environment env) {
        return new JwtTokenProvider(resolveKey(props, env), props.getExpirationMinutes(), props.getIssuer());
    }

    @Bean
    public JwtAuthInterceptor jwtAuthInterceptor(JwtTokenProvider tokenProvider) {
        // 회원가입은 토큰 없이 호출할 수 있어야 한다.
        return new JwtAuthInterceptor(tokenProvider, Set.of("POST /users"));
    }

    /**
     * 운영에서는 JWT_SECRET 이 반드시 있어야 한다. 없으면 기동을 중단한다.
     * (임의 키로 뜨면 인스턴스마다 서명이 달라져 토큰이 서로 통하지 않는다.)
     * 로컬 개발 편의를 위해 dev 에서만 임의 키를 생성한다. 재기동 시 기존 토큰은 무효가 된다.
     */
    private SecretKey resolveKey(JwtProperties props, Environment env) {
        String secret = props.getSecret();
        boolean prod = env.acceptsProfiles(profiles -> profiles.test("prod"));

        if (secret == null || secret.isBlank()) {
            if (prod) {
                throw new IllegalStateException(
                        "security.jwt.secret (JWT_SECRET) is required when the prod profile is active");
            }
            log.warn("JWT_SECRET 이 설정되지 않아 임의 키를 생성합니다. 재기동하면 기존 토큰은 무효가 됩니다.");
            return Keys.secretKeyFor(SignatureAlgorithm.HS256);
        }

        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < MIN_SECRET_BYTES) {
            throw new IllegalStateException(
                    "security.jwt.secret must be at least " + MIN_SECRET_BYTES + " bytes (현재 " + bytes.length + ")");
        }
        return Keys.hmacShaKeyFor(bytes);
    }
}
