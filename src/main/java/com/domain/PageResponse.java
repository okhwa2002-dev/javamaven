package com.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.util.List;

/**
 * 페이징 응답의 공통 wrapper.
 * page 는 0-indexed 이며, size 는 1..100 범위로 제한된다(서비스 계층에서 강제).
 */
@Schema(description = "페이징 응답")
@Getter
public class PageResponse<T> {

    @Schema(description = "현재 페이지의 데이터")
    private final List<T> content;

    @Schema(description = "현재 페이지 번호 (0-indexed)", example = "0")
    private final int page;

    @Schema(description = "페이지 크기", example = "20")
    private final int size;

    @Schema(description = "전체 데이터 개수", example = "137")
    private final long totalElements;

    @Schema(description = "전체 페이지 개수", example = "7")
    private final int totalPages;

    public PageResponse(List<T> content, int page, int size, long totalElements) {
        this.content = content;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = size == 0 ? 0 : (int) Math.ceil((double) totalElements / size);
    }
}
