package com.ishop.controller;

import com.ishop.procedure.ProcedureCaller;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final ProcedureCaller procedureCaller;

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, Object> params) {
        List<Map<String, Object>> result = procedureCaller.callProcedure("user_login",
            toString(params.get("username")),
            toString(params.get("password")));
        return result.isEmpty() ? Map.of("success", false, "message", "登录失败") : result.get(0);
    }

    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody Map<String, Object> params) {
        List<Map<String, Object>> result = procedureCaller.callProcedure("register_user",
            toString(params.get("username")),
            toString(params.get("password")),
            toString(params.get("email")),
            toString(params.get("phone")));
        return result.isEmpty() ? Map.of("success", true) : result.get(0);
    }

    private String toString(Object obj) {
        return obj != null ? obj.toString() : null;
    }
}
