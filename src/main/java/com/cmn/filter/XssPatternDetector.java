package com.cmn.filter;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

public class XssPatternDetector {

    private final List<String> patterns;

    public XssPatternDetector(List<String> patterns) {
        this.patterns = patterns == null ? List.of() : patterns.stream()
                .map(String::toLowerCase)
                .toList();
    }

    public Optional<String> detect(String value) {
        if (value == null || value.isEmpty()) {
            return Optional.empty();
        }
        String decoded = decode(value).toLowerCase();
        for (String pattern : patterns) {
            if (decoded.contains(pattern)) {
                return Optional.of(pattern);
            }
        }
        return Optional.empty();
    }

    private static String decode(String value) {
        try {
            return URLDecoder.decode(value, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            return value;
        }
    }
}
