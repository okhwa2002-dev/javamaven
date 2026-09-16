package com.domain;

import com.cmn.utils.ValidationUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * 사용자 수정 요청. UPDATE SQL 이 다루는 필드(username, email)만 노출한다.
 * loginId 와 password 는 이 경로에서 변경할 수 없다.
 */
@Schema(description = "사용자 수정 요청")
@Getter
@Setter
public class UserUpdateRequest {

    @NotBlank(message = "username is required")
    @Size(max = ValidationUtil.USERNAME_MAX_LENGTH, message = ValidationUtil.USERNAME_MESSAGE)
    @Schema(description = "사용자명", example = "hong")
    private String username;

    @NotBlank(message = "email is required")
    @Email(message = ValidationUtil.EMAIL_MESSAGE)
    @Size(max = ValidationUtil.EMAIL_MAX_LENGTH, message = ValidationUtil.EMAIL_MESSAGE)
    @Schema(description = "이메일", example = "hong@example.com")
    private String email;
}
