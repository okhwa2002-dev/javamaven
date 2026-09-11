package com.service;

import com.cmn.exception.UnauthorizedException;
import com.cmn.jwt.JwtProperties;
import com.domain.RefreshToken;
import com.mapper.RefreshTokenMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock RefreshTokenMapper mapper;
    @Mock JwtProperties props;
    @InjectMocks RefreshTokenService service;

    private RefreshToken storedValid(Long id, Long userId, String hash) {
        RefreshToken t = new RefreshToken();
        t.setId(id);
        t.setUserId(userId);
        t.setTokenHash(hash);
        t.setExpiresAt(LocalDateTime.now().plusDays(1));
        t.setRevokedAt(null);
        t.setCreatedAt(LocalDateTime.now());
        return t;
    }

    // ---------- issue ----------

    @Test
    void issue_storesHash_notRaw_andReturnsRawToken() {
        when(props.getRefreshExpirationDays()).thenReturn(14L);

        String raw = service.issue(1L);

        assertNotNull(raw);
        assertTrue(raw.length() >= 40, "base64url 인코딩된 32바이트 토큰은 43자");

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(mapper).insert(captor.capture());
        RefreshToken saved = captor.getValue();

        assertEquals(1L, saved.getUserId());
        assertEquals(64, saved.getTokenHash().length(), "SHA-256 hex는 64자");
        assertNotEquals(raw, saved.getTokenHash(), "raw 값이 DB에 들어가면 안 된다");
        assertEquals(RefreshTokenService.sha256(raw), saved.getTokenHash());
        // expiresAt 은 대략 14일 뒤
        assertTrue(saved.getExpiresAt().isAfter(LocalDateTime.now().plusDays(13)));
    }

    @Test
    void issue_generatesDifferentTokens() {
        when(props.getRefreshExpirationDays()).thenReturn(14L);

        String a = service.issue(1L);
        String b = service.issue(1L);

        assertNotEquals(a, b);
    }

    // ---------- verify ----------

    @Test
    void verify_valid_returnsUserId() {
        String raw = "abc";
        RefreshToken stored = storedValid(10L, 5L, RefreshTokenService.sha256(raw));
        when(mapper.selectByHash(RefreshTokenService.sha256(raw))).thenReturn(stored);

        assertEquals(5L, service.verify(raw));
    }

    @Test
    void verify_notFound_throwsUnauthorized() {
        when(mapper.selectByHash(any())).thenReturn(null);

        assertThrows(UnauthorizedException.class, () -> service.verify("unknown"));
    }

    @Test
    void verify_revoked_throwsUnauthorized() {
        RefreshToken revoked = storedValid(10L, 5L, RefreshTokenService.sha256("x"));
        revoked.setRevokedAt(LocalDateTime.now().minusMinutes(1));
        when(mapper.selectByHash(RefreshTokenService.sha256("x"))).thenReturn(revoked);

        assertThrows(UnauthorizedException.class, () -> service.verify("x"));
    }

    @Test
    void verify_expired_throwsUnauthorized() {
        RefreshToken expired = storedValid(10L, 5L, RefreshTokenService.sha256("x"));
        expired.setExpiresAt(LocalDateTime.now().minusMinutes(1));
        when(mapper.selectByHash(RefreshTokenService.sha256("x"))).thenReturn(expired);

        assertThrows(UnauthorizedException.class, () -> service.verify("x"));
    }

    // ---------- rotate ----------

    @Test
    void rotate_revokesOld_andIssuesNew() {
        String oldRaw = "oldtoken";
        RefreshToken stored = storedValid(10L, 5L, RefreshTokenService.sha256(oldRaw));
        when(mapper.selectByHash(RefreshTokenService.sha256(oldRaw))).thenReturn(stored);
        when(props.getRefreshExpirationDays()).thenReturn(14L);

        RefreshTokenService.Rotation result = service.rotate(oldRaw);

        assertEquals(5L, result.userId());
        assertNotNull(result.rawToken());
        assertNotEquals(oldRaw, result.rawToken());

        verify(mapper).revokeById(10L);
        // 새 토큰이 insert 됐다
        verify(mapper).insert(any(RefreshToken.class));
    }

    @Test
    void rotate_invalidToken_throwsUnauthorized_andDoesNotIssueNew() {
        when(mapper.selectByHash(any())).thenReturn(null);

        assertThrows(UnauthorizedException.class, () -> service.rotate("bogus"));

        verify(mapper, never()).revokeById(anyLong());
        verify(mapper, never()).insert(any(RefreshToken.class));
    }

    // ---------- revoke ----------

    @Test
    void revoke_valid_revokesOnce() {
        String raw = "rr";
        RefreshToken stored = storedValid(20L, 5L, RefreshTokenService.sha256(raw));
        when(mapper.selectByHash(RefreshTokenService.sha256(raw))).thenReturn(stored);

        service.revoke(raw);

        verify(mapper).revokeById(20L);
    }

    @Test
    void revoke_notFound_isSilent() {
        when(mapper.selectByHash(any())).thenReturn(null);

        // 예외를 던지지 않아야 한다 (멱등 동작)
        service.revoke("unknown");

        verify(mapper, never()).revokeById(anyLong());
    }

    @Test
    void revoke_alreadyRevoked_isSilent() {
        String raw = "rr";
        RefreshToken revoked = storedValid(20L, 5L, RefreshTokenService.sha256(raw));
        revoked.setRevokedAt(LocalDateTime.now().minusMinutes(1));
        when(mapper.selectByHash(RefreshTokenService.sha256(raw))).thenReturn(revoked);

        service.revoke(raw);

        verify(mapper, never()).revokeById(anyLong());
    }
}
