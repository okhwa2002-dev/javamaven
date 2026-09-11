package com.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "로그인 요청")
@Getter
@Setter
public class LoginRequest {

    @NotBlank(message = "loginId is required")
    @Schema(description = "로그인 아이디", example = "hong")
    private String loginId;

    @NotBlank(message = "password is required")
    @Schema(description = "비밀번호", example = "password123!")
    private String password;
}
