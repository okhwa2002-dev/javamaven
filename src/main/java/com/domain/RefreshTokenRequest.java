package com.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "리프레시 토큰 재발급/로그아웃 요청")
@Getter
@Setter
public class RefreshTokenRequest {

    @NotBlank(message = "refreshToken is required")
    @Schema(description = "리프레시 토큰")
    private String refreshToken;
}
