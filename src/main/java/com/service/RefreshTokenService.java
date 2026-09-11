package com.service;

import com.cmn.exception.UnauthorizedException;
import com.cmn.jwt.JwtProperties;
import com.domain.RefreshToken;
import com.mapper.RefreshTokenMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    // 32바이트 = 256비트 엔트로피. base64url 로 인코딩하면 43자.
    private static final int TOKEN_BYTES = 32;
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Base64.Encoder URL_ENCODER = Base64.getUrlEncoder().withoutPadding();

    private final RefreshTokenMapper mapper;
    private final JwtProperties props;

    /**
     * 새 리프레시 토큰을 발급하고 해시를 저장한다.
     * @return 클라이언트에 반환할 raw 토큰 문자열
     */
    @Transactional
    public String issue(Long userId) {
        String raw = generateRawToken();
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(props.getRefreshExpirationDays());

        RefreshToken entity = new RefreshToken();
        entity.setUserId(userId);
        entity.setTokenHash(sha256(raw));
        entity.setExpiresAt(expiresAt);
        mapper.insert(entity);

        return raw;
    }

    /**
     * 리프레시 토큰을 검증한다. 유효하지 않으면 UnauthorizedException.
     * @return 토큰 소유자 userId
     */
    @Transactional(readOnly = true)
    public Long verify(String rawToken) {
        return loadValid(rawToken).getUserId();
    }

    /**
     * 리프레시 토큰을 회전한다. 기존 토큰은 폐기되고 새 토큰이 발급된다.
     * @return (userId, newRawToken)
     */
    @Transactional
    public Rotation rotate(String rawToken) {
        RefreshToken stored = loadValid(rawToken);
        mapper.revokeById(stored.getId());
        String newRaw = issue(stored.getUserId());
        return new Rotation(stored.getUserId(), newRaw);
    }

    /**
     * 리프레시 토큰을 폐기(로그아웃)한다.
     * 이미 폐기됐거나 만료됐어도 조용히 통과시킨다(멱등성 확보).
     */
    @Transactional
    public void revoke(String rawToken) {
        RefreshToken stored = mapper.selectByHash(sha256(rawToken));
        if (stored != null && stored.getRevokedAt() == null) {
            mapper.revokeById(stored.getId());
        }
    }

    private RefreshToken loadValid(String rawToken) {
        RefreshToken stored = mapper.selectByHash(sha256(rawToken));
        if (stored == null
                || stored.getRevokedAt() != null
                || stored.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new UnauthorizedException("유효하지 않은 리프레시 토큰입니다.");
        }
        return stored;
    }

    private static String generateRawToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        RANDOM.nextBytes(bytes);
        return URL_ENCODER.encodeToString(bytes);
    }

    static String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    public record Rotation(Long userId, String rawToken) {}
}
