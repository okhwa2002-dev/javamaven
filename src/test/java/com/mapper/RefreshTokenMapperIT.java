package com.mapper;

import com.domain.RefreshToken;
import com.domain.UserDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@Testcontainers
@SpringBootTest
@Transactional
class RefreshTokenMapperIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    UserMapper userMapper;

    @Autowired
    RefreshTokenMapper refreshTokenMapper;

    private Long insertUser(String slug) {
        UserDto u = new UserDto();
        u.setUsername("u_" + slug);
        u.setEmail(slug + "@example.com");
        u.setLoginId(slug);
        u.setPassword("hashed");
        userMapper.insert(u);
        return u.getId();
    }

    private RefreshToken newToken(Long userId, String hash) {
        RefreshToken t = new RefreshToken();
        t.setUserId(userId);
        t.setTokenHash(hash);
        t.setExpiresAt(LocalDateTime.now().plusDays(14));
        return t;
    }

    @Test
    void insert_thenSelectByHash_roundTrip() {
        Long uid = insertUser("rt1");
        RefreshToken t = newToken(uid, "hash-abcdef");
        refreshTokenMapper.insert(t);
        assertNotNull(t.getId());

        RefreshToken found = refreshTokenMapper.selectByHash("hash-abcdef");
        assertNotNull(found);
        assertEquals(uid, found.getUserId());
        assertEquals("hash-abcdef", found.getTokenHash());
        assertNull(found.getRevokedAt(), "새로 발급된 토큰은 revoked_at 이 NULL");
        assertNotNull(found.getCreatedAt(), "created_at 은 DB default 로 채워짐");
    }

    @Test
    void selectByHash_notFound_returnsNull() {
        assertNull(refreshTokenMapper.selectByHash("no-such-hash"));
    }

    @Test
    void revokeById_setsRevokedAt_andSecondCallReturnsZero() {
        Long uid = insertUser("rt2");
        RefreshToken t = newToken(uid, "hash-2");
        refreshTokenMapper.insert(t);

        int first = refreshTokenMapper.revokeById(t.getId());
        int second = refreshTokenMapper.revokeById(t.getId());

        assertEquals(1, first, "최초 폐기는 1 rows affected");
        assertEquals(0, second, "이미 폐기된 상태에서 재호출은 0 (WHERE revoked_at IS NULL)");

        RefreshToken reloaded = refreshTokenMapper.selectByHash("hash-2");
        assertNotNull(reloaded.getRevokedAt());
    }

    @Test
    void deleteExpired_removesOnlyExpiredRows() {
        Long uid = insertUser("rt-clean");

        // 만료된 것
        RefreshToken expired = newToken(uid, "hash-expired");
        expired.setExpiresAt(LocalDateTime.now().minusMinutes(1));
        refreshTokenMapper.insert(expired);

        // 만료되지 않은 것
        RefreshToken valid = newToken(uid, "hash-valid");
        refreshTokenMapper.insert(valid);

        int removed = refreshTokenMapper.deleteExpired();

        assertEquals(1, removed, "만료된 행 하나만 삭제되어야 한다");
        assertNull(refreshTokenMapper.selectByHash("hash-expired"));
        assertNotNull(refreshTokenMapper.selectByHash("hash-valid"));
    }

    @Test
    void userDelete_cascadesToRefreshTokens() {
        Long uid = insertUser("rt3");
        RefreshToken t = newToken(uid, "hash-3");
        refreshTokenMapper.insert(t);
        assertNotNull(refreshTokenMapper.selectByHash("hash-3"));

        userMapper.deleteById(uid);

        assertNull(refreshTokenMapper.selectByHash("hash-3"),
                "user 삭제 시 refresh_tokens 는 FK CASCADE 로 함께 삭제되어야 한다");
    }
}
