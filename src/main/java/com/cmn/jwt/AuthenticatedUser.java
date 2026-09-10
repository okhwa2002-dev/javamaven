package com.cmn.jwt;

/** 토큰에서 복원한 인증 주체. 요청 속성으로 컨트롤러에 전달된다. */
public record AuthenticatedUser(Long userId, String loginId) {

    /** 요청 속성 키. */
    public static final String ATTRIBUTE = "authenticatedUser";
}
