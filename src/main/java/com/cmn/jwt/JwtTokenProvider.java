package com.cmn.jwt;

import com.cmn.exception.UnauthorizedException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;

@Slf4j
public class JwtTokenProvider {

    private static final String CLAIM_LOGIN_ID = "loginId";

    private final SecretKey key;
    private final long expirationMinutes;
    private final String issuer;

    public JwtTokenProvider(SecretKey key, long expirationMinutes, String issuer) {
        this.key = key;
        this.expirationMinutes = expirationMinutes;
        this.issuer = issuer;
    }

    public String createToken(Long userId, String loginId) {
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(expirationMinutes * 60);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim(CLAIM_LOGIN_ID, loginId)
                .issuer(issuer)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(key)
                .compact();
    }

    /**
     * 토큰을 검증하고 인증 주체를 돌려준다.
     * 만료·위조·형식 오류를 모두 401 로 통일해 어떤 이유로 실패했는지 노출하지 않는다.
     */
    public AuthenticatedUser parse(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .requireIssuer(issuer)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return new AuthenticatedUser(
                    Long.valueOf(claims.getSubject()),
                    claims.get(CLAIM_LOGIN_ID, String.class)
            );
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("JWT validation failed: {}", e.getMessage());
            throw new UnauthorizedException("유효하지 않은 토큰입니다.");
        }
    }

    public long getExpiresInSeconds() {
        return expirationMinutes * 60;
    }
}
