package com.ishop.controller;

import com.ishop.procedure.ProcedureCaller;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final ProcedureCaller procedureCaller;

    @PostMapping("/create")
    public Map<String, Object> createOrder(@RequestBody Map<String, Object> params) {
        List<Map<String, Object>> result = procedureCaller.callProcedure("create_order",
            toLong(params.get("userId")),
            params.get("items") != null ? params.get("items").toString() : "",
            toString(params.get("address")));
        return result.isEmpty() ? Map.of("success", true) : result.get(0);
    }

    @GetMapping("/{id}")
    public Map<String, Object> getOrderById(@PathVariable Long id) {
        return procedureCaller.callProcedureSingle("get_order_by_id", id);
    }

    @GetMapping("/user/{userId}")
    public List<Map<String, Object>> getUserOrders(
            @PathVariable Long userId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "20") Integer limit) {
        return procedureCaller.callProcedure("get_user_orders", userId, status, limit);
    }

    private Long toLong(Object obj) {
        if (obj == null) return null;
        if (obj instanceof Number) return ((Number) obj).longValue();
        return Long.parseLong(obj.toString());
    }

    private String toString(Object obj) {
        return obj != null ? obj.toString() : null;
    }
}
