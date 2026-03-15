package com.ishop.controller;

import com.ishop.dto.CreateOrderRequest;
import com.ishop.dto.Result;
import com.ishop.procedure.ProcedureCaller;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "订单管理", description = "订单相关接口")
@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final ProcedureCaller procedureCaller;

    @Operation(summary = "创建订单", description = "创建新订单")
    @PostMapping("/create")
    public Result<Map<String, Object>> createOrder(@RequestBody CreateOrderRequest request) {
        List<Map<String, Object>> result = procedureCaller.callProcedure("create_order",
            request.getUserId(),
            request.getItems() != null ? request.getItems().toString() : "",
            request.getAddress());
        return result.isEmpty() ? Result.success(Map.of("success", true)) : Result.success(result.get(0));
    }

    @Operation(summary = "获取订单", description = "根据ID获取订单详情")
    @GetMapping("/{id}")
    public Result<Map<String, Object>> getOrderById(@PathVariable Long id) {
        return Result.success(procedureCaller.callProcedureSingle("get_order_by_id", id));
    }

    @Operation(summary = "获取用户订单", description = "获取用户的所有订单")
    @GetMapping("/user/{userId}")
    public Result<List<Map<String, Object>>> getUserOrders(
            @PathVariable Long userId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "20") Integer limit) {
        return Result.success(procedureCaller.callProcedure("get_user_orders", userId, status, limit));
    }
}
