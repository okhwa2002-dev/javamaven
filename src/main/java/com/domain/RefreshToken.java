package com.domain;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * refresh_tokens 테이블 매핑용 VO. 응답으로 직접 노출되지 않는다.
 * token_hash 는 raw 토큰의 SHA-256 이며 raw 값은 서버가 재구성할 수 없다.
 */
@Getter
@Setter
public class RefreshToken {

    private Long id;
    private Long userId;
    private String tokenHash;
    private LocalDateTime expiresAt;
    private LocalDateTime revokedAt;
    private LocalDateTime createdAt;
}
