package com.ishop.controller;

import com.ishop.procedure.ProcedureCaller;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/category")
@RequiredArgsConstructor
public class CategoryController {

    private final ProcedureCaller procedureCaller;

    @GetMapping("/tree")
    public List<Map<String, Object>> getCategoryTree() {
        return procedureCaller.callProcedure("get_category_tree");
    }
}
