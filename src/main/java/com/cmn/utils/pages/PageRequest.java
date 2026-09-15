package com.cmn.utils.pages;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 페이징 요청 공통 파라미터.
 * page 는 0-indexed. 잘못된 값이 들어와도 서비스 계층에서 별도 검증 없이 사용할 수 있도록
 * getSafePage / getSafeSize / getOffset 에서 자동으로 보정한다.
 */
@Getter
@Setter
public class PageRequest {

    /** 페이지 크기 상한. 클라이언트가 큰 값을 넣어도 서버 부담을 제한한다. */
    public static final int MAX_PAGE_SIZE = 100;

    @Schema(description = "페이지 번호 (0부터 시작)", example = "0", defaultValue = "0")
    private int page = 0;

    @Schema(description = "페이지 크기 (1~" + MAX_PAGE_SIZE + ")", example = "20", defaultValue = "20")
    private int size = 20;

    public int getSafePage() {
        return Math.max(page, 0);
    }

    public int getSafeSize() {
        return Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
    }

    public int getOffset() {
        return getSafePage() * getSafeSize();
    }
}
