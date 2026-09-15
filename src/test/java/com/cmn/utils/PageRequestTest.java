package com.cmn.utils;

import com.cmn.utils.pages.PageRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PageRequestTest {

    @Test
    void defaults_areZeroAndTwenty() {
        PageRequest req = new PageRequest();
        assertEquals(0, req.getSafePage());
        assertEquals(20, req.getSafeSize());
        assertEquals(0, req.getOffset());
    }

    @Test
    void negativePage_clampedToZero() {
        PageRequest req = new PageRequest();
        req.setPage(-3);
        assertEquals(0, req.getSafePage());
    }

    @Test
    void sizeBelowOne_clampedToOne() {
        PageRequest req = new PageRequest();
        req.setSize(0);
        assertEquals(1, req.getSafeSize());
    }

    @Test
    void sizeAboveMax_clampedToMax() {
        PageRequest req = new PageRequest();
        req.setSize(9999);
        assertEquals(PageRequest.MAX_PAGE_SIZE, req.getSafeSize());
    }

    @Test
    void offset_isPageTimesSize_afterClamp() {
        PageRequest req = new PageRequest();
        req.setPage(2);
        req.setSize(20);
        assertEquals(40, req.getOffset());
    }

    @Test
    void offset_usesClampedValues() {
        PageRequest req = new PageRequest();
        req.setPage(-1);
        req.setSize(9999);
        // page 0, size 100 → offset 0
        assertEquals(0, req.getOffset());
    }
}
