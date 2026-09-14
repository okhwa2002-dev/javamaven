package com.cmn.config;

import com.cmn.filter.ParamFilterProperties;
import com.cmn.filter.ParameterSecurityFilter;
import com.cmn.filter.SecurityHeadersFilter;
import com.cmn.filter.SqlKeywordDetector;
import com.cmn.filter.XssPatternDetector;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Configuration
@EnableConfigurationProperties(ParamFilterProperties.class)
public class FilterConfig {

    @Bean
    public SqlKeywordDetector sqlKeywordDetector(ParamFilterProperties props) {
        return new SqlKeywordDetector(props.getSqlKeywords(), props.getSqlSyntax());
    }

    @Bean
    public XssPatternDetector xssPatternDetector(ParamFilterProperties props) {
        return new XssPatternDetector(props.getXssPatterns());
    }

    @Bean
    public FilterRegistrationBean<ParameterSecurityFilter> parameterSecurityFilterRegistration(
            SqlKeywordDetector sqlDetector,
            XssPatternDetector xssDetector,
            ObjectMapper objectMapper,
            ParamFilterProperties props) {
        FilterRegistrationBean<ParameterSecurityFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new ParameterSecurityFilter(sqlDetector, xssDetector, objectMapper));
        registration.addUrlPatterns("/*");
        registration.setEnabled(props.isEnabled());
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 10);
        return registration;
    }

    @Bean
    public FilterRegistrationBean<SecurityHeadersFilter> securityHeadersFilterRegistration() {
        FilterRegistrationBean<SecurityHeadersFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new SecurityHeadersFilter());
        registration.addUrlPatterns("/*");
        // 파라미터 검사 필터보다 뒤에 두어 응답 시점에 헤더를 부여한다.
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 20);
        return registration;
    }
}
