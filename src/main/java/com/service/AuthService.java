package com.service;

import com.cmn.exception.UnauthorizedException;
import com.cmn.jwt.JwtTokenProvider;
import com.domain.LoginRequest;
import com.domain.LoginResponse;
import com.domain.UserDto;
import com.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    // 아이디 존재 여부가 드러나지 않도록 실패 사유를 구분하지 않는다.
    private static final String LOGIN_FAILED = "아이디 또는 비밀번호가 올바르지 않습니다.";

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        if (request.getLoginId() == null || request.getLoginId().isBlank()) {
            throw new IllegalArgumentException("loginId is required");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new IllegalArgumentException("password is required");
        }

        UserDto user = userMapper.selectByLoginId(request.getLoginId());
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            // 입력한 비밀번호는 로그에 남기지 않는다.
            log.warn("Login failed loginId={}", request.getLoginId());
            throw new UnauthorizedException(LOGIN_FAILED);
        }

        String accessToken = tokenProvider.createToken(user.getId(), user.getLoginId());
        return new LoginResponse(user.getId(), user.getLoginId(), user.getUsername(), user.getEmail(),
                accessToken, tokenProvider.getExpiresInSeconds());
    }
}
