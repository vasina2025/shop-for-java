/**
 * 用户导入导出控制器
 * 
 * 功能：
 * - 导出用户Excel
 * - 导入用户Excel
 */
package com.ishop.controller;

import com.ishop.dto.Result;
import com.ishop.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 用户导入导出接口
 */
@Tag(name = "用户导入导出", description = "用户Excel导入导出接口")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserImportExportController {

    private final UserService userService;

    /**
     * 导出用户到Excel
     * 
     * @return Excel文件
     */
    @Operation(summary = "导出用户", description = "导出所有用户到Excel文件")
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportUsers() {
        try {
            byte[] excelData = userService.exportUsers();
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=users.xlsx")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(excelData);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 导入用户Excel
     * 
     * @param file Excel文件
     * @return 导入结果
     */
    @Operation(summary = "导入用户", description = "从Excel文件导入用户数据")
    @PostMapping("/import")
    public Result<Map<String, Object>> importUsers(@RequestParam("file") MultipartFile file) {
        // 检查文件是否为空
        if (file.isEmpty()) {
            return Result.error("请选择要导入的文件");
        }
        
        // 检查文件类型
        String filename = file.getOriginalFilename();
        if (filename == null || (!filename.endsWith(".xlsx") && !filename.endsWith(".xls"))) {
            return Result.error("请上传Excel文件(.xlsx或.xls)");
        }
        
        try {
            Map<String, Object> result = userService.importUsers(file);
            boolean success = (boolean) result.get("success");
            if (success) {
                return Result.success("导入成功", result);
            } else {
                return Result.error((String) result.get("message"));
            }
        } catch (Exception e) {
            return Result.error("导入失败: " + e.getMessage());
        }
    }

    /**
     * 下载导入模板
     * 
     * @return Excel模板文件
     */
    @Operation(summary = "下载模板", description = "下载用户导入Excel模板")
    @GetMapping("/template")
    public ResponseEntity<byte[]> downloadTemplate() {
        try {
            byte[] templateData = userService.exportUsers();
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=user_template.xlsx")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(templateData);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}