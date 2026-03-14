/**
 * 用户Service
 * 
 * 功能：
 * - 用户CRUD操作
 * - 用户Excel导入导出
 */
package com.ishop.service;

import com.ishop.util.ExcelUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户Service
 * 处理用户相关业务逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final JdbcTemplate jdbcTemplate;

    /**
     * 用户表头映射（数据库字段 -> Excel显示名称）
     */
    private static final Map<String, String> USER_HEADERS = new HashMap<>();
    static {
        USER_HEADERS.put("username", "用户名");
        USER_HEADERS.put("password", "密码");
        USER_HEADERS.put("nickname", "昵称");
        USER_HEADERS.put("email", "邮箱");
        USER_HEADERS.put("phone", "手机号");
        USER_HEADERS.put("address", "地址");
        USER_HEADERS.put("status", "状态");
    }

    /**
     * 获取所有用户列表
     */
    public List<Map<String, Object>> getAllUsers() {
        String sql = "SELECT id, username, nickname, email, phone, address, status, created_at FROM users ORDER BY id";
        return jdbcTemplate.queryForList(sql);
    }

    /**
     * 根据ID获取用户
     */
    public Map<String, Object> getUserById(Long id) {
        List<Map<String, Object>> results = jdbcTemplate.queryForList(
            "SELECT id, username, nickname, email, phone, address, status, created_at FROM users WHERE id = ?", id);
        return results.isEmpty() ? null : results.get(0);
    }

    /**
     * 导出用户到Excel
     * 
     * @return Excel文件字节数组
     */
    public byte[] exportUsers() throws Exception {
        List<Map<String, Object>> users = getAllUsers();
        
        // 移除敏感字段
        for (Map<String, Object> user : users) {
            user.remove("password");
        }
        
        // 转换日期格式
        for (Map<String, Object> user : users) {
            if (user.get("created_at") != null) {
                user.put("created_at", user.get("created_at").toString());
            }
        }
        
        return ExcelUtil.exportExcel(users, USER_HEADERS, "用户列表");
    }

    /**
     * 从Excel导入用户
     * 
     * @param file Excel文件
     * @return 导入结果
     */
    public Map<String, Object> importUsers(MultipartFile file) throws Exception {
        // 读取Excel数据
        List<Map<String, Object>> excelData = ExcelUtil.importExcel(
            file.getInputStream(), USER_HEADERS);
        
        if (excelData.isEmpty()) {
            return Map.of("success", false, "message", "Excel文件为空");
        }
        
        int successCount = 0;
        int errorCount = 0;
        List<String> errors = new ArrayList<>();
        
        for (int i = 0; i < excelData.size(); i++) {
            Map<String, Object> row = excelData.get(i);
            int rowNum = i + 2; // Excel行号（从2开始，1是表头）
            
            try {
                String username = getStringValue(row.get("username"));
                String password = getStringValue(row.get("password"));
                String nickname = getStringValue(row.get("nickname"));
                String email = getStringValue(row.get("email"));
                String phone = getStringValue(row.get("phone"));
                String address = getStringValue(row.get("address"));
                
                // 验证必填字段
                if (username == null || username.isEmpty()) {
                    errors.add("第" + rowNum + "行：用户名不能为空");
                    errorCount++;
                    continue;
                }
                if (password == null || password.isEmpty()) {
                    errors.add("第" + rowNum + "行：密码不能为空");
                    errorCount++;
                    continue;
                }
                
                // 检查用户名是否已存在
                List<Map<String, Object>> existUsers = jdbcTemplate.queryForList(
                    "SELECT id FROM users WHERE username = ?", username);
                if (!existUsers.isEmpty()) {
                    errors.add("第" + rowNum + "行：用户名 '" + username + "' 已存在");
                    errorCount++;
                    continue;
                }
                
                // 插入用户
                jdbcTemplate.update(
                    "INSERT INTO users (username, password, nickname, email, phone, address, status) VALUES (?, ?, ?, ?, ?, ?, 'active')",
                    username, password, nickname, email, phone, address);
                
                successCount++;
                
            } catch (Exception e) {
                errors.add("第" + rowNum + "行：" + e.getMessage());
                errorCount++;
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", errorCount == 0);
        result.put("successCount", successCount);
        result.put("errorCount", errorCount);
        result.put("totalCount", excelData.size());
        if (!errors.isEmpty()) {
            result.put("errors", errors);
        }
        
        return result;
    }

    /**
     * 获取字符串值
     */
    private String getStringValue(Object value) {
        if (value == null) return null;
        String str = value.toString().trim();
        return str.isEmpty() ? null : str;
    }
}