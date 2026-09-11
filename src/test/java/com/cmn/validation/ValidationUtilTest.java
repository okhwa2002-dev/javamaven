package com.cmn.validation;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidationUtilTest {

    // ----- password -----

    @Test
    void password_regex_accepts_valid() {
        assertTrue(ValidationUtil.isValidPassword("Abcd123!"));
        assertTrue(ValidationUtil.isValidPassword("longPass@word_1234"));
    }

    @Test
    void password_regex_rejects_missing_category() {
        assertFalse(ValidationUtil.isValidPassword("onlyletters"));   // no digit/special
        assertFalse(ValidationUtil.isValidPassword("12345678"));      // no letter/special
        assertFalse(ValidationUtil.isValidPassword("Abcd1234"));      // no special
        assertFalse(ValidationUtil.isValidPassword("Ab!1"));          // too short
        assertFalse(ValidationUtil.isValidPassword(null));
    }

    @Test
    void requirePassword_throws_on_invalid() {
        assertThrows(IllegalArgumentException.class,
                () -> ValidationUtil.requireValidPassword("short"));
        assertDoesNotThrow(() -> ValidationUtil.requireValidPassword("Abcd123!"));
    }

    // ----- loginId -----

    @Test
    void loginId_regex_accepts_valid() {
        assertTrue(ValidationUtil.isValidLoginId("hong"));
        assertTrue(ValidationUtil.isValidLoginId("user_01"));
        assertTrue(ValidationUtil.isValidLoginId("Abc123_xyz"));
    }

    @Test
    void loginId_regex_rejects_invalid() {
        assertFalse(ValidationUtil.isValidLoginId("1user"));     // starts with digit
        assertFalse(ValidationUtil.isValidLoginId("_user"));     // starts with underscore
        assertFalse(ValidationUtil.isValidLoginId("ab"));        // too short
        assertFalse(ValidationUtil.isValidLoginId("hong!"));     // invalid char
        assertFalse(ValidationUtil.isValidLoginId(null));
    }

    // ----- email / phone -----

    @Test
    void email_regex_accepts_valid() {
        assertTrue(ValidationUtil.isValidEmail("a@b.co"));
        assertTrue(ValidationUtil.isValidEmail("hong.gil-dong@example.com"));
    }

    @Test
    void email_regex_rejects_invalid() {
        assertFalse(ValidationUtil.isValidEmail("no-at"));
        assertFalse(ValidationUtil.isValidEmail("a@b"));
        assertFalse(ValidationUtil.isValidEmail(null));
    }

    @Test
    void phone_regex_accepts_valid() {
        assertTrue(ValidationUtil.isValidPhone("01012345678"));
        assertTrue(ValidationUtil.isValidPhone("010-1234-5678"));
        assertTrue(ValidationUtil.isValidPhone("011-123-4567"));
    }

    @Test
    void phone_regex_rejects_invalid() {
        assertFalse(ValidationUtil.isValidPhone("021234567"));   // 지역번호
        assertFalse(ValidationUtil.isValidPhone("010-12-5678")); // 중간 자릿수 부족
        assertFalse(ValidationUtil.isValidPhone(null));
    }

    // ----- 생성기가 검증기를 통과하는지 (핵심 회귀 방지) -----

    @RepeatedTest(50)
    void randomLoginId_output_matches_regex() {
        String v = ValidationUtil.randomLoginId();
        assertTrue(ValidationUtil.isValidLoginId(v), "generated loginId is invalid: " + v);
    }

    @RepeatedTest(50)
    void randomPassword_output_matches_regex() {
        String v = ValidationUtil.randomPassword();
        assertTrue(ValidationUtil.isValidPassword(v), "generated password is invalid: " + v);
    }

    @Test
    void randomAlphanumeric_length_and_charset() {
        String v = ValidationUtil.randomAlphanumeric(20);
        assertTrue(v.length() == 20 && v.matches("[A-Za-z0-9]+"));
        assertThrows(IllegalArgumentException.class, () -> ValidationUtil.randomAlphanumeric(0));
    }

    @Test
    void randomLoginId_rejects_out_of_range_length() {
        assertThrows(IllegalArgumentException.class, () -> ValidationUtil.randomLoginId(2));
        assertThrows(IllegalArgumentException.class, () -> ValidationUtil.randomLoginId(51));
    }

    @Test
    void randomPassword_rejects_out_of_range_length() {
        assertThrows(IllegalArgumentException.class, () -> ValidationUtil.randomPassword(7));
        assertThrows(IllegalArgumentException.class, () -> ValidationUtil.randomPassword(73));
    }
}
