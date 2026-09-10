package com.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Schema(description = "사용자 정보")
@Getter
@Setter
public class UserDto {

    @Schema(description = "사용자 id", example = "1")
    private Long id;

    @Schema(description = "사용자명", example = "hong")
    private String username;

    @Schema(description = "이메일", example = "hong@example.com")
    private String email;

    @Schema(description = "로그인 아이디", example = "hong")
    private String loginId;

    // 응답 직렬화에서 제외한다. 해시가 API로 새어나가지 않도록.
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Schema(description = "비밀번호", accessMode = Schema.AccessMode.WRITE_ONLY)
    private String password;

    @Schema(description = "생성 시각", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;

    @Schema(description = "수정 시각", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime updatedAt;
}
