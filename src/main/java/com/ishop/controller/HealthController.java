package com.ishop.controller;

import com.ishop.dto.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "系统", description = "系统健康检查")
@RestController
@RequestMapping("/api")
public class HealthController {

    private final JdbcTemplate jdbcTemplate;

    public HealthController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Operation(summary = "健康检查", description = "检查系统状态和数据库连接")
    @GetMapping("/health")
    public Result<Map<String, Object>> health() {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "UP");
        result.put("timestamp", System.currentTimeMillis());
        
        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            result.put("database", "connected");
        } catch (Exception e) {
            result.put("database", "disconnected");
            result.put("dbError", e.getMessage());
            result.put("status", "DOWN");
        }
        
        return Result.success(result);
    }
}
