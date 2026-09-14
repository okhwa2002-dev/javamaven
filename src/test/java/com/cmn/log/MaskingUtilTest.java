package com.cmn.log;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MaskingUtilTest {

    // ----- loginId -----

    @Test
    void maskLoginId_typical() {
        assertEquals("h***", MaskingUtil.maskLoginId("hong"));
        assertEquals("a***", MaskingUtil.maskLoginId("admin"));
    }

    @Test
    void maskLoginId_tooShort_returnsMaskOnly() {
        assertEquals("***", MaskingUtil.maskLoginId("a"));
        assertEquals("***", MaskingUtil.maskLoginId(""));
    }

    @Test
    void maskLoginId_null_returnsLiteralNull() {
        assertEquals("null", MaskingUtil.maskLoginId(null));
    }

    // ----- email -----

    @Test
    void maskEmail_typical() {
        assertEquals("h***@example.com", MaskingUtil.maskEmail("hong@example.com"));
        assertEquals("a***@b.co", MaskingUtil.maskEmail("a@b.co"));
    }

    @Test
    void maskEmail_missingAt_returnsMaskOnly() {
        assertEquals("***", MaskingUtil.maskEmail("no-at-sign"));
    }

    @Test
    void maskEmail_atAtStartOrEnd_returnsMaskOnly() {
        assertEquals("***", MaskingUtil.maskEmail("@example.com"));
        assertEquals("***", MaskingUtil.maskEmail("hong@"));
    }

    @Test
    void maskEmail_null_returnsLiteralNull() {
        assertEquals("null", MaskingUtil.maskEmail(null));
    }
}
