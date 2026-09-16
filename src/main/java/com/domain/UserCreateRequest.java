package com.domain;

import com.cmn.utils.ValidationUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "사용자 생성 요청")
@Getter
@Setter
public class UserCreateRequest {

    @NotBlank(message = "username is required")
    @Size(max = ValidationUtil.USERNAME_MAX_LENGTH, message = ValidationUtil.USERNAME_MESSAGE)
    @Schema(description = "사용자명", example = "hong")
    private String username;

    @NotBlank(message = "email is required")
    @Email(message = ValidationUtil.EMAIL_MESSAGE)
    @Size(max = ValidationUtil.EMAIL_MAX_LENGTH, message = ValidationUtil.EMAIL_MESSAGE)
    @Schema(description = "이메일", example = "hong@example.com")
    private String email;

    @NotBlank(message = "loginId is required")
    @Pattern(regexp = ValidationUtil.LOGIN_ID_REGEX, message = ValidationUtil.LOGIN_ID_MESSAGE)
    @Schema(description = "로그인 아이디", example = "hong")
    private String loginId;

    @NotBlank(message = "password is required")
    @Pattern(regexp = ValidationUtil.PASSWORD_REGEX, message = ValidationUtil.PASSWORD_MESSAGE)
    @Schema(description = "비밀번호", example = "password123!")
    private String password;
}
