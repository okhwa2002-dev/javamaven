package com.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

/**
 * 토큰 재발급 응답. 리프레시 회전에 따라 refreshToken 도 새로 발급된다.
 */
@Schema(description = "토큰 재발급 응답")
@Getter
public class TokenResponse {

    @Schema(description = "액세스 토큰")
    private final String accessToken;

    @Schema(description = "리프레시 토큰(회전된 새 토큰)")
    private final String refreshToken;

    @Schema(description = "토큰 타입", example = "Bearer")
    private final String tokenType = "Bearer";

    @Schema(description = "액세스 토큰 유효 시간(초)")
    private final long expiresIn;

    public TokenResponse(String accessToken, String refreshToken, long expiresIn) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
    }
}
