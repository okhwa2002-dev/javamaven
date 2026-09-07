package com.cmn.filter;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SqlKeywordDetector {

    private final Pattern keywordPattern;
    private final List<String> syntaxTokens;

    public SqlKeywordDetector(List<String> keywords, List<String> syntaxTokens) {
        this.keywordPattern = buildKeywordPattern(keywords);
        this.syntaxTokens = syntaxTokens == null ? List.of() : syntaxTokens.stream()
                .map(String::toLowerCase)
                .toList();
    }

    public Optional<String> detect(String value) {
        if (value == null || value.isEmpty()) {
            return Optional.empty();
        }
        if (keywordPattern != null) {
            Matcher matcher = keywordPattern.matcher(value);
            if (matcher.find()) {
                return Optional.of(matcher.group(1).toUpperCase());
            }
        }
        String lower = value.toLowerCase();
        for (String token : syntaxTokens) {
            if (lower.contains(token)) {
                return Optional.of(token);
            }
        }
        return Optional.empty();
    }

    private static Pattern buildKeywordPattern(List<String> keywords) {
        if (keywords == null || keywords.isEmpty()) {
            return null;
        }
        String joined = keywords.stream()
                .map(Pattern::quote)
                .reduce((a, b) -> a + "|" + b)
                .orElseThrow();
        return Pattern.compile("\\b(" + joined + ")\\b", Pattern.CASE_INSENSITIVE);
    }
}
