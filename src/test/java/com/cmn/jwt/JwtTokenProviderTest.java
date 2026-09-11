package com.cmn.jwt;

import com.cmn.exception.UnauthorizedException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtTokenProviderTest {

    private static final String ISSUER = "javamaven-test";

    private SecretKey key;
    private JwtTokenProvider provider;

    @BeforeEach
    void setUp() {
        key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        provider = new JwtTokenProvider(key, 60, ISSUER);
    }

    @Test
    void createToken_thenParse_roundTrip() {
        String token = provider.createToken(42L, "hong");

        AuthenticatedUser auth = provider.parse(token);

        assertEquals(42L, auth.userId());
        assertEquals("hong", auth.loginId());
    }

    @Test
    void createToken_returnsNonEmptyString() {
        String token = provider.createToken(1L, "x");
        assertNotNull(token);
        assertEquals(3, token.split("\\.").length, "JWT 는 3파트여야 한다");
    }

    @Test
    void getExpiresInSeconds_returnsMinutesTimes60() {
        JwtTokenProvider p = new JwtTokenProvider(key, 15, ISSUER);
        assertEquals(900L, p.getExpiresInSeconds());
    }

    // ----- 실패 케이스: 모두 UnauthorizedException 로 통일되어야 한다 -----

    @Test
    void parse_wrongSignature_throwsUnauthorized() {
        SecretKey otherKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        JwtTokenProvider otherProvider = new JwtTokenProvider(otherKey, 60, ISSUER);
        String forged = otherProvider.createToken(1L, "x");

        assertThrows(UnauthorizedException.class, () -> provider.parse(forged));
    }

    @Test
    void parse_wrongIssuer_throwsUnauthorized() {
        String badIssuerToken = Jwts.builder()
                .subject("1")
                .claim("loginId", "x")
                .issuer("someone-else")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(key)
                .compact();

        assertThrows(UnauthorizedException.class, () -> provider.parse(badIssuerToken));
    }

    @Test
    void parse_expiredToken_throwsUnauthorized() {
        long now = System.currentTimeMillis();
        String expired = Jwts.builder()
                .subject("1")
                .claim("loginId", "x")
                .issuer(ISSUER)
                .issuedAt(new Date(now - 120_000))
                .expiration(new Date(now - 60_000))
                .signWith(key)
                .compact();

        assertThrows(UnauthorizedException.class, () -> provider.parse(expired));
    }

    @Test
    void parse_malformedToken_throwsUnauthorized() {
        assertThrows(UnauthorizedException.class, () -> provider.parse("not.a.jwt"));
    }

    @Test
    void parse_null_throwsUnauthorized() {
        assertThrows(UnauthorizedException.class, () -> provider.parse(null));
    }
}
