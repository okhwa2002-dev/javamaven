package com.cmn.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ParameterSecurityFilterTest {

    private ParameterSecurityFilter filter;

    @BeforeEach
    void setUp() {
        SqlKeywordDetector sql = new SqlKeywordDetector(
                List.of("SELECT", "DROP", "UNION"),
                List.of("--", ";")
        );
        XssPatternDetector xss = new XssPatternDetector(
                List.of("<script", "javascript:", "onerror=")
        );
        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        filter = new ParameterSecurityFilter(sql, xss, mapper);
    }

    @Test
    void passesSafeRequest() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/users");
        request.setParameter("name", "john");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(chain.getRequest()).isNotNull();
    }

    @Test
    void blocksSqlKeywordInQueryParam() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/users");
        request.setParameter("name", "'; DROP table users--");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.getContentAsString()).contains("SQL keyword");
        assertThat(chain.getRequest()).isNull();
    }

    @Test
    void blocksXssPatternInQueryParam() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/users");
        request.setParameter("q", "<script>alert(1)</script>");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.getContentAsString()).contains("XSS pattern");
        assertThat(chain.getRequest()).isNull();
    }

    @Test
    void blocksUrlEncodedXss() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/users");
        request.setParameter("q", "%3Cscript%3E");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.getContentAsString()).contains("XSS pattern");
    }

    @Test
    void blocksInjectionInPathSegment() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/users/DROP");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.getContentAsString()).contains("SQL keyword");
    }

    @Test
    void allowsPathWithSubstringMatchButNoBoundary() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/users/selection");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(200);
    }
}
