package com.ishop.yaml;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "api")
public class ApiRouteConfig {
    private List<RouteDefinition> routes = new ArrayList<>();

    @Data
    public static class RouteDefinition {
        private String path;
        private String procedure;
        private String method;
    }
}
