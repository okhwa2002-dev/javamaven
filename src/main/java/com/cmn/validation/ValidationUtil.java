package com.cmn.validation;

import java.security.SecureRandom;
import java.util.regex.Pattern;

/**
 * 도메인 필드 규칙(정규식·길이 상한)과 그에 맞는 검증·랜덤 생성 함수를 한 곳에 모은다.
 *
 * <p>규칙은 상수(public static final)로 노출하여 DTO 의 Bean Validation 어노테이션
 * (예: {@code @Pattern(regexp = ValidationUtil.PASSWORD_REGEX)}) 에서도 참조할 수 있게 한다.
 * 검증 함수(isValidXxx / requireValidXxx)는 웹 경계가 아닌 서비스·배치·CLI 에서도
 * 동일한 규칙을 강제할 때 사용한다.
 */
public final class ValidationUtil {

    private ValidationUtil() {}

    private static final SecureRandom RANDOM = new SecureRandom();

    // ==========================================================
    // 비밀번호
    //   - 영문/숫자/특수문자 각 1개 이상
    //   - 길이 8~72 (72 는 BCrypt 가 사용하는 상한)
    // ==========================================================
    public static final int PASSWORD_MIN_LENGTH = 8;
    public static final int PASSWORD_MAX_LENGTH = 72;
    public static final String PASSWORD_REGEX =
            "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+=\\-]).{8,72}$";
    public static final String PASSWORD_MESSAGE =
            "password는 영문/숫자/특수문자를 포함한 8~72자여야 합니다";
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(PASSWORD_REGEX);

    public static boolean isValidPassword(String value) {
        return value != null && PASSWORD_PATTERN.matcher(value).matches();
    }

    public static void requireValidPassword(String value) {
        if (!isValidPassword(value)) {
            throw new IllegalArgumentException(PASSWORD_MESSAGE);
        }
    }

    // ==========================================================
    // 로그인 아이디 (loginId)
    //   - 첫 글자는 영문
    //   - 이후 영문/숫자/언더스코어
    //   - 길이 3~50
    // ==========================================================
    public static final int LOGIN_ID_MIN_LENGTH = 3;
    public static final int LOGIN_ID_MAX_LENGTH = 50;
    public static final String LOGIN_ID_REGEX = "^[a-zA-Z][a-zA-Z0-9_]{2,49}$";
    public static final String LOGIN_ID_MESSAGE =
            "loginId는 영문으로 시작하고 영문/숫자/언더스코어 3~50자여야 합니다";
    private static final Pattern LOGIN_ID_PATTERN = Pattern.compile(LOGIN_ID_REGEX);

    public static boolean isValidLoginId(String value) {
        return value != null && LOGIN_ID_PATTERN.matcher(value).matches();
    }

    public static void requireValidLoginId(String value) {
        if (!isValidLoginId(value)) {
            throw new IllegalArgumentException(LOGIN_ID_MESSAGE);
        }
    }

    // ==========================================================
    // 사용자명(username) - 표시용 이름
    // ==========================================================
    public static final int USERNAME_MAX_LENGTH = 50;
    public static final String USERNAME_MESSAGE = "username은 1~50자여야 합니다";

    public static boolean isValidUsername(String value) {
        return value != null && !value.isBlank() && value.length() <= USERNAME_MAX_LENGTH;
    }

    public static void requireValidUsername(String value) {
        if (!isValidUsername(value)) {
            throw new IllegalArgumentException(USERNAME_MESSAGE);
        }
    }

    // ==========================================================
    // 이메일
    //   - 형식 검증은 Bean Validation 의 @Email 을 그대로 활용하고,
    //     여기서는 최대 길이와 서비스 계층용 최소 유효성만 제공한다.
    // ==========================================================
    public static final int EMAIL_MAX_LENGTH = 100;
    // 상세 형식 검증용 (RFC 준수 완벽 정규식은 지양, 실무 관례상 사용하는 단순 형태)
    public static final String EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    public static final String EMAIL_MESSAGE = "email 형식이 올바르지 않습니다";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

    public static boolean isValidEmail(String value) {
        return value != null
                && value.length() <= EMAIL_MAX_LENGTH
                && EMAIL_PATTERN.matcher(value).matches();
    }

    public static void requireValidEmail(String value) {
        if (!isValidEmail(value)) {
            throw new IllegalArgumentException(EMAIL_MESSAGE);
        }
    }

    // ==========================================================
    // 전화번호 (한국 휴대전화 기준 예: 01012345678, 010-1234-5678)
    // ==========================================================
    public static final String PHONE_REGEX = "^01[0-9]-?\\d{3,4}-?\\d{4}$";
    public static final String PHONE_MESSAGE = "phone 형식이 올바르지 않습니다 (예: 010-1234-5678)";
    private static final Pattern PHONE_PATTERN = Pattern.compile(PHONE_REGEX);

    public static boolean isValidPhone(String value) {
        return value != null && PHONE_PATTERN.matcher(value).matches();
    }

    public static void requireValidPhone(String value) {
        if (!isValidPhone(value)) {
            throw new IllegalArgumentException(PHONE_MESSAGE);
        }
    }

    // ==========================================================
    // 랜덤 값 생성
    //   - 모두 SecureRandom 기반 (암호학적 안전성 확보)
    // ==========================================================
    private static final char[] LOWER = "abcdefghijklmnopqrstuvwxyz".toCharArray();
    private static final char[] LETTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
    private static final char[] DIGITS = "0123456789".toCharArray();
    private static final char[] ALPHANUMERIC =
            "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();
    private static final char[] PASSWORD_SPECIALS = "!@#$%^&*_-+=".toCharArray();
    private static final char[] PASSWORD_POOL =
            "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@#$%^&*_-+=".toCharArray();

    /**
     * 지정한 길이의 영숫자 랜덤 문자열을 반환한다.
     */
    public static String randomAlphanumeric(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("length must be > 0");
        }
        return pickFrom(ALPHANUMERIC, length);
    }

    /**
     * LOGIN_ID_REGEX 를 만족하는 랜덤 loginId 를 반환한다. (12자)
     */
    public static String randomLoginId() {
        return randomLoginId(12);
    }

    /**
     * 규칙에 맞는 loginId 를 지정 길이로 생성한다.
     * (첫 글자는 소문자 영문, 이후 영숫자)
     */
    public static String randomLoginId(int length) {
        if (length < LOGIN_ID_MIN_LENGTH || length > LOGIN_ID_MAX_LENGTH) {
            throw new IllegalArgumentException(
                    "loginId length must be within [" + LOGIN_ID_MIN_LENGTH + ", " + LOGIN_ID_MAX_LENGTH + "]");
        }
        StringBuilder sb = new StringBuilder(length);
        sb.append(LOWER[RANDOM.nextInt(LOWER.length)]);
        for (int i = 1; i < length; i++) {
            sb.append(ALPHANUMERIC[RANDOM.nextInt(ALPHANUMERIC.length)]);
        }
        return sb.toString();
    }

    /**
     * PASSWORD_REGEX 를 만족하는 랜덤 비밀번호를 반환한다. (12자)
     * 관리자 초기 발급·임시 비밀번호 용도.
     */
    public static String randomPassword() {
        return randomPassword(12);
    }

    /**
     * 규칙에 맞는 비밀번호를 지정 길이로 생성한다.
     * 영문·숫자·특수문자 각 최소 1개를 보장하기 위해 카테고리별로 먼저 채운 뒤 셔플한다.
     */
    public static String randomPassword(int length) {
        if (length < PASSWORD_MIN_LENGTH || length > PASSWORD_MAX_LENGTH) {
            throw new IllegalArgumentException(
                    "password length must be within [" + PASSWORD_MIN_LENGTH + ", " + PASSWORD_MAX_LENGTH + "]");
        }
        char[] out = new char[length];
        out[0] = LETTERS[RANDOM.nextInt(LETTERS.length)];
        out[1] = DIGITS[RANDOM.nextInt(DIGITS.length)];
        out[2] = PASSWORD_SPECIALS[RANDOM.nextInt(PASSWORD_SPECIALS.length)];
        for (int i = 3; i < length; i++) {
            out[i] = PASSWORD_POOL[RANDOM.nextInt(PASSWORD_POOL.length)];
        }
        shuffle(out);
        return new String(out);
    }

    private static String pickFrom(char[] pool, int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(pool[RANDOM.nextInt(pool.length)]);
        }
        return sb.toString();
    }

    private static void shuffle(char[] arr) {
        // Fisher-Yates
        for (int i = arr.length - 1; i > 0; i--) {
            int j = RANDOM.nextInt(i + 1);
            char t = arr[i];
            arr[i] = arr[j];
            arr[j] = t;
        }
    }
}
