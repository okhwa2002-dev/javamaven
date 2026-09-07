package com.cmn.filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class SqlKeywordDetectorTest {

    private SqlKeywordDetector detector;

    @BeforeEach
    void setUp() {
        detector = new SqlKeywordDetector(
                List.of("SELECT", "INSERT", "UPDATE", "DELETE", "DROP", "UNION"),
                List.of("--", "/*", "*/", ";", "xp_", "sp_")
        );
    }

    @Test
    void returnsEmptyForSafeValue() {
        assertThat(detector.detect("john")).isEmpty();
        assertThat(detector.detect("hello world")).isEmpty();
        assertThat(detector.detect("")).isEmpty();
        assertThat(detector.detect(null)).isEmpty();
    }

    @Test
    void detectsKeywordCaseInsensitive() {
        assertThat(detector.detect("SELECT")).contains("SELECT");
        assertThat(detector.detect("select")).contains("SELECT");
        assertThat(detector.detect("SeLeCt")).contains("SELECT");
    }

    @Test
    void respectsWordBoundary() {
        assertThat(detector.detect("selection")).isEmpty();
        assertThat(detector.detect("dropbox")).isEmpty();
        assertThat(detector.detect("insertion")).isEmpty();
    }

    @Test
    void detectsKeywordInPhrase() {
        assertThat(detector.detect("1 UNION select 1")).isPresent();
        assertThat(detector.detect("hello; DROP table")).isPresent();
    }

    @Test
    void detectsSyntaxTokens() {
        assertThat(detector.detect("abc--")).contains("--");
        assertThat(detector.detect("a/*b*/c")).isPresent();
        assertThat(detector.detect("id;delete")).isPresent();
    }
}
