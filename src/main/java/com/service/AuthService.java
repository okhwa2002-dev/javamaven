package com.service;

import com.cmn.exception.UnauthorizedException;
import com.cmn.jwt.JwtTokenProvider;
import com.domain.LoginRequest;
import com.domain.LoginResponse;
import com.domain.TokenResponse;
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
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public LoginResponse login(LoginRequest request) {
        UserDto user = userMapper.selectByLoginId(request.getLoginId());
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            // 입력한 비밀번호는 로그에 남기지 않는다.
            log.warn("Login failed loginId={}", request.getLoginId());
            throw new UnauthorizedException(LOGIN_FAILED);
        }

        String accessToken = tokenProvider.createToken(user.getId(), user.getLoginId());
        String refreshToken = refreshTokenService.issue(user.getId());
        return new LoginResponse(user.getId(), user.getLoginId(), user.getUsername(), user.getEmail(),
                accessToken, refreshToken, tokenProvider.getExpiresInSeconds());
    }

    /**
     * 리프레시 토큰을 회전하며 새 액세스 토큰을 발급한다.
     * 리프레시가 유효하지 않으면 401.
     */
    @Transactional
    public TokenResponse refresh(String rawRefreshToken) {
        RefreshTokenService.Rotation rotation = refreshTokenService.rotate(rawRefreshToken);
        UserDto user = userMapper.selectById(rotation.userId());
        if (user == null) {
            // refresh_tokens 에는 있으나 users 는 삭제된 예외 상태
            throw new UnauthorizedException("유효하지 않은 리프레시 토큰입니다.");
        }
        String accessToken = tokenProvider.createToken(user.getId(), user.getLoginId());
        return new TokenResponse(accessToken, rotation.rawToken(), tokenProvider.getExpiresInSeconds());
    }

    /**
     * 로그아웃. 제출된 리프레시 토큰만 폐기한다(같은 유저의 다른 세션은 유지).
     */
    @Transactional
    public void logout(String rawRefreshToken) {
        refreshTokenService.revoke(rawRefreshToken);
    }
}
