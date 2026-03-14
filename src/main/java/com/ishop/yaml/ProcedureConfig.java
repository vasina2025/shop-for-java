package com.ishop.yaml;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "procedures")
public class ProcedureConfig {
    private Map<String, ProcedureDefinition> procedures = new HashMap<>();

    @Data
    public static class ProcedureDefinition {
        private String sql;
    }
}
