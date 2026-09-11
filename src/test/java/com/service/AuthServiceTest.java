package com.service;

import com.cmn.exception.UnauthorizedException;
import com.cmn.jwt.JwtTokenProvider;
import com.domain.LoginRequest;
import com.domain.LoginResponse;
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

    @Test
    void login_success_returnsIssuedToken() {
        when(userMapper.selectByLoginId("hong")).thenReturn(storedUser());
        when(passwordEncoder.matches("plain", "$2a$10$hashedvalue")).thenReturn(true);
        when(tokenProvider.createToken(1L, "hong")).thenReturn("jwt-token");
        when(tokenProvider.getExpiresInSeconds()).thenReturn(3600L);

        LoginResponse res = authService.login(req("hong", "plain"));

        assertEquals(1L, res.getId());
        assertEquals("hong", res.getLoginId());
        assertEquals("홍길동", res.getUsername());
        assertEquals("hong@example.com", res.getEmail());
        assertEquals("jwt-token", res.getAccessToken());
        assertEquals(3600L, res.getExpiresIn());
        assertEquals("Bearer", res.getTokenType());
    }

    @Test
    void login_userNotFound_throwsUnauthorized_andDoesNotIssueToken() {
        when(userMapper.selectByLoginId("nobody")).thenReturn(null);

        assertThrows(UnauthorizedException.class,
                () -> authService.login(req("nobody", "any")));

        verifyNoInteractions(tokenProvider);
    }

    @Test
    void login_wrongPassword_throwsUnauthorized_andDoesNotIssueToken() {
        when(userMapper.selectByLoginId("hong")).thenReturn(storedUser());
        when(passwordEncoder.matches("bad", "$2a$10$hashedvalue")).thenReturn(false);

        assertThrows(UnauthorizedException.class,
                () -> authService.login(req("hong", "bad")));

        verifyNoInteractions(tokenProvider);
    }

    @Test
    void login_success_passwordEncoderIsInvokedWithSubmittedPlaintext() {
        when(userMapper.selectByLoginId("hong")).thenReturn(storedUser());
        when(passwordEncoder.matches("plain", "$2a$10$hashedvalue")).thenReturn(true);
        when(tokenProvider.createToken(1L, "hong")).thenReturn("t");
        when(tokenProvider.getExpiresInSeconds()).thenReturn(60L);

        authService.login(req("hong", "plain"));

        verify(passwordEncoder).matches("plain", "$2a$10$hashedvalue");
    }
}
