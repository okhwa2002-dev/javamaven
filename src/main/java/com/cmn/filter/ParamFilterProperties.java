package com.cmn.filter;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Getter
@Setter
@ConfigurationProperties(prefix = "security.param-filter")
public class ParamFilterProperties {

    private boolean enabled = true;

    private List<String> sqlKeywords = List.of(
            "SELECT", "INSERT", "UPDATE", "DELETE",
            "DROP", "CREATE", "ALTER", "TRUNCATE",
            "EXEC", "UNION"
    );

    private List<String> sqlSyntax = List.of(
            "--", "/*", "*/", ";", "xp_", "sp_"
    );

    private List<String> xssPatterns = List.of(
            "<script", "</script", "<iframe", "<object", "<embed",
            "onerror=", "onload=", "onclick=", "onmouseover=",
            "javascript:", "vbscript:", "data:text/html",
            "eval(", "expression("
    );
}
