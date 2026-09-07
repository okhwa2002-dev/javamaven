package com.cmn.filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class XssPatternDetectorTest {

    private XssPatternDetector detector;

    @BeforeEach
    void setUp() {
        detector = new XssPatternDetector(List.of(
                "<script", "</script", "<iframe",
                "onerror=", "onload=", "onclick=",
                "javascript:", "vbscript:", "data:text/html",
                "eval(", "expression("
        ));
    }

    @Test
    void returnsEmptyForSafeValue() {
        assertThat(detector.detect("hello")).isEmpty();
        assertThat(detector.detect("normal text with < and >")).isEmpty();
        assertThat(detector.detect("")).isEmpty();
        assertThat(detector.detect(null)).isEmpty();
    }

    @Test
    void detectsScriptTag() {
        assertThat(detector.detect("<script>alert(1)</script>")).contains("<script");
        assertThat(detector.detect("<SCRIPT>")).contains("<script");
    }

    @Test
    void detectsEventHandler() {
        assertThat(detector.detect("<img onerror=alert(1)>")).contains("onerror=");
        assertThat(detector.detect("<a onclick=x>")).contains("onclick=");
    }

    @Test
    void detectsJavascriptScheme() {
        assertThat(detector.detect("javascript:alert(1)")).contains("javascript:");
        assertThat(detector.detect("JavaScript:void(0)")).contains("javascript:");
    }

    @Test
    void detectsUrlEncodedPayload() {
        assertThat(detector.detect("%3Cscript%3Ealert(1)%3C%2Fscript%3E")).contains("<script");
        assertThat(detector.detect("javascript%3Aalert(1)")).contains("javascript:");
    }
}
