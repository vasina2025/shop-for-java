package com.ishop.controller;

import com.ishop.procedure.ProcedureCaller;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProcedureCaller procedureCaller;

    @GetMapping("/list")
    public List<Map<String, Object>> getProductList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        return procedureCaller.callProcedure("get_product_list", page, pageSize);
    }

    @GetMapping("/{id}")
    public Map<String, Object> getProductById(@PathVariable Long id) {
        return procedureCaller.callProcedureSingle("get_product_by_id", id);
    }

    @GetMapping("/search")
    public List<Map<String, Object>> searchProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        return procedureCaller.callProcedure("search_products", 
            keyword != null ? keyword : "", categoryId, pageSize);
    }
}
