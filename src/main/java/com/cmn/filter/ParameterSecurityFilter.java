package com.cmn.filter;

import com.cmn.exception.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class ParameterSecurityFilter extends OncePerRequestFilter {

    private static final String SQL_PREFIX = "SQL:";
    private static final String XSS_PREFIX = "XSS:";

    private final SqlKeywordDetector sqlDetector;
    private final XssPatternDetector xssDetector;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        for (Map.Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
            String key = entry.getKey();
            for (String value : entry.getValue()) {
                Optional<String> violation = inspect(value);
                if (violation.isPresent()) {
                    reject(request, response, violation.get(), "param:" + key);
                    return;
                }
            }
        }

        String uri = request.getRequestURI();
        if (uri != null) {
            for (String segment : uri.split("/")) {
                if (segment.isEmpty()) {
                    continue;
                }
                Optional<String> violation = inspect(segment);
                if (violation.isPresent()) {
                    reject(request, response, violation.get(), "path");
                    return;
                }
            }
        }

        chain.doFilter(request, response);
    }

    private Optional<String> inspect(String value) {
        Optional<String> sql = sqlDetector.detect(value);
        if (sql.isPresent()) {
            return Optional.of(SQL_PREFIX + sql.get());
        }
        return xssDetector.detect(value).map(v -> XSS_PREFIX + v);
    }

    private void reject(HttpServletRequest request, HttpServletResponse response, String violation, String source)
            throws IOException {
        String type = violation.startsWith(SQL_PREFIX) ? "SQL keyword" : "XSS pattern";
        log.warn("Blocked request path={} source={} violation={}", request.getRequestURI(), source, violation);

        ErrorResponse body = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Invalid parameter: " + type + " detected",
                request.getRequestURI()
        );

        response.setStatus(HttpStatus.BAD_REQUEST.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        objectMapper.writeValue(response.getWriter(), body);
    }
}
