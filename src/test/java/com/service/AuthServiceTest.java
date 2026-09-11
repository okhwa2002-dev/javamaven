package com.service;

import com.cmn.exception.UnauthorizedException;
import com.cmn.jwt.JwtTokenProvider;
import com.domain.LoginRequest;
import com.domain.LoginResponse;
import com.domain.TokenResponse;
import com.domain.UserDto;
import com.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UserMapper userMapper;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtTokenProvider tokenProvider;
    @Mock RefreshTokenService refreshTokenService;
    @InjectMocks AuthService authService;

    private LoginRequest req(String loginId, String password) {
        LoginRequest r = new LoginRequest();
        r.setLoginId(loginId);
        r.setPassword(password);
        return r;
    }

    private UserDto storedUser() {
        UserDto u = new UserDto();
        u.setId(1L);
        u.setLoginId("hong");
        u.setUsername("홍길동");
        u.setEmail("hong@example.com");
        u.setPassword("$2a$10$hashedvalue");
        return u;
    }

    // ---------- login ----------

    @Test
    void login_success_returnsIssuedTokens() {
        when(userMapper.selectByLoginId("hong")).thenReturn(storedUser());
        when(passwordEncoder.matches("plain", "$2a$10$hashedvalue")).thenReturn(true);
        when(tokenProvider.createToken(1L, "hong")).thenReturn("jwt-access");
        when(refreshTokenService.issue(1L)).thenReturn("refresh-abc");
        when(tokenProvider.getExpiresInSeconds()).thenReturn(900L);

        LoginResponse res = authService.login(req("hong", "plain"));

        assertEquals(1L, res.getId());
        assertEquals("hong", res.getLoginId());
        assertEquals("홍길동", res.getUsername());
        assertEquals("hong@example.com", res.getEmail());
        assertEquals("jwt-access", res.getAccessToken());
        assertEquals("refresh-abc", res.getRefreshToken());
        assertEquals(900L, res.getExpiresIn());
        assertEquals("Bearer", res.getTokenType());
    }

    @Test
    void login_userNotFound_throwsUnauthorized_andDoesNotIssueTokens() {
        when(userMapper.selectByLoginId("nobody")).thenReturn(null);

        assertThrows(UnauthorizedException.class,
                () -> authService.login(req("nobody", "any")));

        verifyNoInteractions(tokenProvider);
        verifyNoInteractions(refreshTokenService);
    }

    @Test
    void login_wrongPassword_throwsUnauthorized_andDoesNotIssueTokens() {
        when(userMapper.selectByLoginId("hong")).thenReturn(storedUser());
        when(passwordEncoder.matches("bad", "$2a$10$hashedvalue")).thenReturn(false);

        assertThrows(UnauthorizedException.class,
                () -> authService.login(req("hong", "bad")));

        verifyNoInteractions(tokenProvider);
        verifyNoInteractions(refreshTokenService);
    }

    @Test
    void login_success_passwordEncoderIsInvokedWithSubmittedPlaintext() {
        when(userMapper.selectByLoginId("hong")).thenReturn(storedUser());
        when(passwordEncoder.matches("plain", "$2a$10$hashedvalue")).thenReturn(true);
        when(tokenProvider.createToken(1L, "hong")).thenReturn("t");
        when(refreshTokenService.issue(1L)).thenReturn("r");
        when(tokenProvider.getExpiresInSeconds()).thenReturn(60L);

        authService.login(req("hong", "plain"));

        verify(passwordEncoder).matches("plain", "$2a$10$hashedvalue");
    }

    // ---------- refresh ----------

    @Test
    void refresh_success_rotatesAndIssuesNewAccessToken() {
        when(refreshTokenService.rotate("old-refresh"))
                .thenReturn(new RefreshTokenService.Rotation(1L, "new-refresh"));
        UserDto user = storedUser();
        when(userMapper.selectById(1L)).thenReturn(user);
        when(tokenProvider.createToken(1L, "hong")).thenReturn("new-access");
        when(tokenProvider.getExpiresInSeconds()).thenReturn(900L);

        TokenResponse res = authService.refresh("old-refresh");

        assertEquals("new-access", res.getAccessToken());
        assertEquals("new-refresh", res.getRefreshToken());
        assertEquals(900L, res.getExpiresIn());
        assertEquals("Bearer", res.getTokenType());
    }

    @Test
    void refresh_userMissing_throwsUnauthorized() {
        when(refreshTokenService.rotate("old-refresh"))
                .thenReturn(new RefreshTokenService.Rotation(99L, "new-refresh"));
        when(userMapper.selectById(99L)).thenReturn(null);

        assertThrows(UnauthorizedException.class,
                () -> authService.refresh("old-refresh"));
    }

    // ---------- logout ----------

    @Test
    void logout_delegatesToRefreshTokenService() {
        authService.logout("some-refresh");

        verify(refreshTokenService).revoke("some-refresh");
    }
}
