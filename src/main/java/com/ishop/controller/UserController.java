/**
 * 用户控制器
 * 登录、注册、用户信息
 */
package com.ishop.controller;

import com.ishop.config.JwtUtil;
import com.ishop.dto.LoginRequest;
import com.ishop.dto.RegisterRequest;
import com.ishop.dto.Result;
import com.ishop.procedure.ProcedureCaller;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "用户管理", description = "用户登录、注册接口")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final JdbcTemplate jdbcTemplate;
    private final ProcedureCaller procedureCaller;
    private final JwtUtil jwtUtil;

    @Operation(summary = "用户登录", description = "用户名密码登录")
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody LoginRequest request) {
        String username = request.getUsername();
        String password = request.getPassword();
        
        if (username == null || password == null) {
            return Result.error("用户名和密码不能为空");
        }
        
        List<Map<String, Object>> results = jdbcTemplate.queryForList(
            "SELECT id, username, nickname FROM users WHERE username = ? AND password = ? AND status = 'active'",
            username, password);
        
        if (results.isEmpty()) {
            return Result.error("用户名或密码错误");
        }
        
        Map<String, Object> user = results.get(0);
        Long userId = ((Number) user.get("id")).longValue();
        
        // 生成Token
        String token = jwtUtil.generateToken(userId, username);
        user.put("token", token);
        
        return Result.success(user);
    }

    @Operation(summary = "用户注册", description = "新用户注册")
    @PostMapping("/register")
    public Result<Map<String, Object>> register(@RequestBody RegisterRequest request) {
        String username = request.getUsername();
        String password = request.getPassword();
        String email = request.getEmail();
        String phone = request.getPhone();
        String nickname = request.getNickname();
        
        if (username == null || password == null) {
            return Result.error("用户名和密码不能为空");
        }
        
        // 检查用户名是否存在
        List<Map<String, Object>> exist = jdbcTemplate.queryForList(
            "SELECT id FROM users WHERE username = ?", username);
        if (!exist.isEmpty()) {
            return Result.error("用户名已存在");
        }
        
        // 插入用户
        jdbcTemplate.update(
            "INSERT INTO users (username, password, email, phone, nickname, status) VALUES (?, ?, ?, ?, ?, 'active')",
            username, password, email, phone, nickname != null ? nickname : username);
        
        // 获取新用户
        List<Map<String, Object>> users = jdbcTemplate.queryForList(
            "SELECT id, username, nickname FROM users WHERE username = ?", username);
        
        if (users.isEmpty()) {
            return Result.error("注册失败");
        }
        
        Map<String, Object> user = users.get(0);
        Long userId = ((Number) user.get("id")).longValue();
        
        // 生成Token
        String token = jwtUtil.generateToken(userId, username);
        user.put("token", token);
        
        return Result.success(user);
    }

    @Operation(summary = "获取用户信息", description = "根据ID获取用户信息")
    @GetMapping("/{id}")
    public Result<Map<String, Object>> getUserById(@PathVariable Long id) {
        List<Map<String, Object>> results = jdbcTemplate.queryForList(
            "SELECT id, username, nickname, email, phone, address FROM users WHERE id = ?", id);
        
        if (results.isEmpty()) {
            return Result.error("用户不存在");
        }
        
        return Result.success(results.get(0));
    }

    @Operation(summary = "获取当前用户信息", description = "通过Token获取当前用户信息")
    @GetMapping("/info")
    public Result<Map<String, Object>> getCurrentUserInfo(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Result.error(401, "未登录");
        }
        
        try {
            String token = authHeader.substring(7);
            Long userId = jwtUtil.extractUserId(token);
            
            List<Map<String, Object>> results = jdbcTemplate.queryForList(
                "SELECT id, username, nickname, email, phone, address FROM users WHERE id = ?", userId);
            
            if (results.isEmpty()) {
                return Result.error("用户不存在");
            }
            
            return Result.success(results.get(0));
        } catch (Exception e) {
            return Result.error(401, "Token无效");
        }
    }

    private String toString(Object obj) {
        return obj != null ? obj.toString().trim() : null;
    }
}
