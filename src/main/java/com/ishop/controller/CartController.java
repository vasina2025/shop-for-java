package com.ishop.controller;

import com.ishop.dto.AddCartRequest;
import com.ishop.dto.Result;
import com.ishop.procedure.ProcedureCaller;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "购物车管理", description = "购物车相关接口")
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final ProcedureCaller procedureCaller;

    @Operation(summary = "添加商品到购物车", description = "将商品添加到用户购物车")
    @PostMapping("/add")
    public Result<Map<String, Object>> addToCart(@RequestBody AddCartRequest request) {
        procedureCaller.callProcedure("add_to_cart",
            request.getUserId(),
            request.getProductId(),
            request.getQuantity());
        return Result.success(Map.of("success", true));
    }

    @Operation(summary = "获取购物车", description = "获取用户的购物车商品列表")
    @GetMapping("/user/{userId}")
    public Result<List<Map<String, Object>>> getUserCart(@PathVariable Long userId) {
        return Result.success(procedureCaller.callProcedure("get_user_cart", userId));
    }
}
