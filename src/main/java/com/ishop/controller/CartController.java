package com.ishop.controller;

import com.ishop.procedure.ProcedureCaller;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final ProcedureCaller procedureCaller;

    @PostMapping("/add")
    public Map<String, Object> addToCart(@RequestBody Map<String, Object> params) {
        procedureCaller.callProcedure("add_to_cart",
            toLong(params.get("userId")),
            toLong(params.get("productId")),
            toInt(params.get("quantity")));
        return Map.of("success", true);
    }

    @GetMapping("/user/{userId}")
    public List<Map<String, Object>> getUserCart(@PathVariable Long userId) {
        return procedureCaller.callProcedure("get_user_cart", userId);
    }

    private Long toLong(Object obj) {
        if (obj == null) return null;
        if (obj instanceof Number) return ((Number) obj).longValue();
        return Long.parseLong(obj.toString());
    }

    private Integer toInt(Object obj) {
        if (obj == null) return 1;
        if (obj instanceof Number) return ((Number) obj).intValue();
        return Integer.parseInt(obj.toString());
    }
}
