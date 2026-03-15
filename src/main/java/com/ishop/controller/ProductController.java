package com.ishop.controller;

import com.ishop.dto.Result;
import com.ishop.procedure.ProcedureCaller;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "商品管理", description = "商品相关接口")
@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProcedureCaller procedureCaller;

    @Operation(summary = "获取商品列表", description = "分页获取商品列表")
    @GetMapping("/list")
    public Result<List<Map<String, Object>>> getProductList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        return Result.success(procedureCaller.callProcedure("get_product_list", page, pageSize));
    }

    @Operation(summary = "获取商品详情", description = "根据ID获取商品详情")
    @GetMapping("/{id}")
    public Result<Map<String, Object>> getProductById(@PathVariable Long id) {
        return Result.success(procedureCaller.callProcedureSingle("get_product_by_id", id));
    }

    @Operation(summary = "搜索商品", description = "根据关键词或分类搜索商品")
    @GetMapping("/search")
    public Result<List<Map<String, Object>>> searchProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        return Result.success(procedureCaller.callProcedure("search_products", 
            keyword != null ? keyword : "", categoryId, pageSize));
    }
}
