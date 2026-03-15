package com.ishop.controller;

import com.ishop.dto.Result;
import com.ishop.procedure.ProcedureCaller;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "分类管理", description = "商品分类相关接口")
@RestController
@RequestMapping("/api/category")
@RequiredArgsConstructor
public class CategoryController {

    private final ProcedureCaller procedureCaller;

    @Operation(summary = "获取分类树", description = "获取所有商品分类的树形结构")
    @GetMapping("/tree")
    public Result<List<Map<String, Object>>> getCategoryTree() {
        return Result.success(procedureCaller.callProcedure("get_category_tree"));
    }
}
