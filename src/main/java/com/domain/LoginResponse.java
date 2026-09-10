package com.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Schema(description = "로그인 응답")
@Getter
public class LoginResponse {

    @Schema(description = "사용자 id", example = "1")
    private final Long id;

    @Schema(description = "로그인 아이디", example = "hong")
    private final String loginId;

    @Schema(description = "사용자명", example = "홍길동")
    private final String username;

    @Schema(description = "이메일", example = "hong@example.com")
    private final String email;

    @Schema(description = "액세스 토큰", example = "eyJhbGciOiJIUzI1NiJ9...")
    private final String accessToken;

    @Schema(description = "토큰 타입", example = "Bearer")
    private final String tokenType = "Bearer";

    @Schema(description = "토큰 유효 시간(초)", example = "3600")
    private final long expiresIn;

    public LoginResponse(Long id, String loginId, String username, String email,
                         String accessToken, long expiresIn) {
        this.id = id;
        this.loginId = loginId;
        this.username = username;
        this.email = email;
        this.accessToken = accessToken;
        this.expiresIn = expiresIn;
    }
}
