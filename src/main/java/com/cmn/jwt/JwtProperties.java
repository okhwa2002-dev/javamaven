package com.cmn.jwt;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "security.jwt")
public class JwtProperties {

    /**
     * HMAC-SHA256 서명 키. Base64 인코딩된 32바이트 이상 문자열.
     * 자격증명이므로 환경변수(JWT_SECRET)로만 주입한다.
     * 비워두면 dev 한정으로 기동 시 임의 키를 생성하고, prod 에서는 기동을 중단한다.
     */
    private String secret;

    /** 액세스 토큰 유효 시간(분). */
    private long expirationMinutes = 15;

    /** 리프레시 토큰 유효 시간(일). */
    private long refreshExpirationDays = 14;

    /** 토큰 발급자(iss) 클레임. */
    private String issuer = "javamaven";
}
