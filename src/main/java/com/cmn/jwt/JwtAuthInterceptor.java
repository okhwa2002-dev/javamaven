package com.cmn.jwt;

import com.cmn.exception.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Set;

public class JwtAuthInterceptor implements HandlerInterceptor {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider tokenProvider;

    /**
     * 인증 없이 허용할 요청. "METHOD URI" 형식.
     * excludePathPatterns 는 경로만 보고 메서드를 구분하지 못해 별도로 둔다.
     */
    private final Set<String> permitted;

    public JwtAuthInterceptor(JwtTokenProvider tokenProvider, Set<String> permitted) {
        this.tokenProvider = tokenProvider;
        this.permitted = permitted;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // CORS preflight 는 Authorization 헤더를 싣지 않으므로 통과시킨다.
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        if (permitted.contains(request.getMethod() + " " + request.getRequestURI())) {
            return true;
        }

        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            throw new UnauthorizedException("인증이 필요합니다.");
        }

        AuthenticatedUser user = tokenProvider.parse(header.substring(BEARER_PREFIX.length()).trim());
        request.setAttribute(AuthenticatedUser.ATTRIBUTE, user);
        return true;
    }
}
