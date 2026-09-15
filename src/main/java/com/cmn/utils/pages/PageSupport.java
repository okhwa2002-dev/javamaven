package com.cmn.utils.pages;

import com.domain.PageResponse;

import java.util.List;
import java.util.function.Function;
import java.util.function.LongSupplier;

/**
 * count 쿼리 + list 쿼리를 조합해 {@link PageResponse} 를 만드는 헬퍼.
 * total 이 0 인 경우 list 쿼리를 생략해 불필요한 DB 왕복을 피한다.
 */
public final class PageSupport {

    private PageSupport() {
    }

    public static <T> PageResponse<T> of(
            PageRequest req,
            LongSupplier counter,
            Function<PageRequest, List<T>> lister) {
        long total = counter.getAsLong();
        List<T> content = total == 0 ? List.of() : lister.apply(req);
        return new PageResponse<>(content, req.getSafePage(), req.getSafeSize(), total);
    }
}
