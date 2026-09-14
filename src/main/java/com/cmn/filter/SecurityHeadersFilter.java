package com.cmn.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 모든 응답에 표준 보안 헤더를 부여한다.
 *
 * <ul>
 *   <li><b>X-Content-Type-Options: nosniff</b> — 브라우저 MIME 스니핑 차단</li>
 *   <li><b>X-Frame-Options: DENY</b> — clickjacking 방지(iframe 삽입 차단)</li>
 *   <li><b>Referrer-Policy: no-referrer</b> — 이 서비스에서 나가는 요청에 referer 미노출</li>
 *   <li><b>Cache-Control: no-store</b> — API 응답이 프록시·브라우저에 캐시되지 않도록</li>
 *   <li><b>Strict-Transport-Security</b> — HSTS. HTTPS 종단이 이 앱이 아니면 리버스 프록시 쪽에 두는 게 정석이라 여기선 생략</li>
 * </ul>
 *
 * CSP 는 REST API 백엔드에서는 큰 효용이 없어 넣지 않는다. HTML 렌더 시점 추가 검토.
 */
public class SecurityHeadersFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("X-Frame-Options", "DENY");
        response.setHeader("Referrer-Policy", "no-referrer");
        // 이미 컨트롤러가 Cache-Control 을 세팅했다면 덮어쓰지 않는다.
        if (!response.containsHeader("Cache-Control")) {
            response.setHeader("Cache-Control", "no-store");
        }
        chain.doFilter(request, response);
    }
}
