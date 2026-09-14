package com.cmn.log;

/**
 * 로그에 사용자 식별정보를 남길 때 원문 대신 마스킹된 값을 사용하기 위한 유틸.
 * 완전 익명화가 아니라 "브라우징이 어려운 정도"로 축약하는 게 목적이다.
 */
public final class MaskingUtil {

    private MaskingUtil() {}

    private static final String MASK = "***";

    /**
     * loginId 를 첫 글자 + "***" 로 축약한다.
     * <pre>
     *   "hong"     -> "h***"
     *   "a"        -> "***"
     *   null       -> "null"
     * </pre>
     */
    public static String maskLoginId(String value) {
        if (value == null) {
            return "null";
        }
        if (value.length() < 2) {
            return MASK;
        }
        return value.charAt(0) + MASK;
    }

    /**
     * 이메일을 로컬파트 첫 글자 + "***" + 도메인 으로 축약한다.
     * <pre>
     *   "hong@example.com"  -> "h***@example.com"
     *   "a@b.co"            -> "a***@b.co"
     *   "no-at"             -> "***"
     *   null                -> "null"
     * </pre>
     */
    public static String maskEmail(String value) {
        if (value == null) {
            return "null";
        }
        int at = value.indexOf('@');
        if (at <= 0 || at == value.length() - 1) {
            return MASK;
        }
        return value.charAt(0) + MASK + value.substring(at);
    }
}
