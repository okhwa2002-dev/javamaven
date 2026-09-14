package com.cmn.filter;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SecurityHeadersFilterTest {

    private SecurityHeadersFilter filter;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private FilterChain chain;

    @BeforeEach
    void setUp() {
        filter = new SecurityHeadersFilter();
        request = new MockHttpServletRequest("GET", "/anything");
        response = new MockHttpServletResponse();
        chain = new MockFilterChain();
    }

    @Test
    void adds_defaultSecurityHeaders() throws Exception {
        filter.doFilter(request, response, chain);

        assertEquals("nosniff", response.getHeader("X-Content-Type-Options"));
        assertEquals("DENY", response.getHeader("X-Frame-Options"));
        assertEquals("no-referrer", response.getHeader("Referrer-Policy"));
        assertEquals("no-store", response.getHeader("Cache-Control"));
    }

    @Test
    void preservesExistingCacheControl() throws Exception {
        // 컨트롤러가 이미 Cache-Control 을 세팅한 시나리오
        response.setHeader("Cache-Control", "public, max-age=60");

        filter.doFilter(request, response, chain);

        assertEquals("public, max-age=60", response.getHeader("Cache-Control"),
                "이미 세팅된 Cache-Control 은 덮어쓰지 않는다");
        // 다른 보안 헤더는 여전히 세팅되어야 한다
        assertEquals("nosniff", response.getHeader("X-Content-Type-Options"));
    }

    @Test
    void doesNotAffectResponseBody() throws Exception {
        MockFilterChain writeChain = new MockFilterChain(
                new jakarta.servlet.http.HttpServlet() {
                    @Override
                    protected void service(jakarta.servlet.http.HttpServletRequest req,
                                           jakarta.servlet.http.HttpServletResponse resp) throws java.io.IOException {
                        resp.getWriter().write("payload");
                    }
                });

        filter.doFilter(request, response, writeChain);

        assertEquals("payload", response.getContentAsString());
        assertTrue(response.getHeaderNames().contains("X-Content-Type-Options"));
    }
}
