/**
 * Excel工具类
 * 
 * 功能：
 * - 导出数据到Excel文件
 * - 从Excel文件导入数据
 * 
 * 支持：
 * - .xlsx 格式（Excel 2007+）
 * - .xls 格式（Excel 97-2003）
 */
package com.ishop.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Excel工具类
 * 提供Excel文件的读写功能
 */
public class ExcelUtil {

    /**
     * 导出数据到Excel文件
     * 
     * @param dataList 数据列表（每行是一个Map）
     * @param headers 表头（列名 -> 显示名称）
     * @param sheetName 工作表名称
     * @return Excel文件字节数组
     */
    public static byte[] exportExcel(List<Map<String, Object>> dataList, 
                                     Map<String, String> headers, 
                                     String sheetName) throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            
            Sheet sheet = workbook.createSheet(sheetName);
            
            // 创建表头样式
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            
            // 创建表头行
            Row headerRow = sheet.createRow(0);
            String[] headerKeys = headers.keySet().toArray(new String[0]);
            for (int i = 0; i < headerKeys.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers.get(headerKeys[i]));
                cell.setCellStyle(headerStyle);
            }
            
            // 填充数据
            for (int rowNum = 0; rowNum < dataList.size(); rowNum++) {
                Row row = sheet.createRow(rowNum + 1);
                Map<String, Object> data = dataList.get(rowNum);
                for (int colNum = 0; colNum < headerKeys.length; colNum++) {
                    Cell cell = row.createCell(colNum);
                    Object value = data.get(headerKeys[colNum]);
                    setCellValue(cell, value);
                }
            }
            
            // 自动调整列宽
            for (int i = 0; i < headerKeys.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            workbook.write(out);
            return out.toByteArray();
        }
    }
    
    /**
     * 从Excel文件导入数据
     * 
     * @param inputStream Excel文件输入流
     * @param headers 表头映射（显示名称 -> 列名）
     * @return 数据列表
     */
    public static List<Map<String, Object>> importExcel(InputStream inputStream,
                                                         Map<String, String> headers) throws IOException {
        List<Map<String, Object>> dataList = new ArrayList<>();
        
        try (Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            
            // 获取表头行
            Row headerRow = sheet.getRow(0);
            Map<Integer, String> headerMap = new HashMap<>();
            for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                Cell cell = headerRow.getCell(i);
                String headerValue = getCellValue(cell).toString();
                // 找到对应的列名
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    if (entry.getValue().equals(headerValue)) {
                        headerMap.put(i, entry.getKey());
                        break;
                    }
                }
            }
            
            // 读取数据行
            for (int rowNum = 1; rowNum <= sheet.getLastRowNum(); rowNum++) {
                Row row = sheet.getRow(rowNum);
                if (row == null) continue;
                
                Map<String, Object> data = new HashMap<>();
                boolean hasData = false;
                for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                    String key = headerMap.get(i);
                    if (key != null) {
                        Cell cell = row.getCell(i);
                        Object value = getCellValue(cell);
                        data.put(key, value);
                        if (value != null && !value.toString().isEmpty()) {
                            hasData = true;
                        }
                    }
                }
                if (hasData) {
                    dataList.add(data);
                }
            }
        }
        
        return dataList;
    }
    
    /**
     * 设置单元格值
     */
    private static void setCellValue(Cell cell, Object value) {
        if (value == null) {
            cell.setBlank();
        } else if (value instanceof String) {
            cell.setCellValue((String) value);
        } else if (value instanceof Number) {
            cell.setCellValue(((Number) value).doubleValue());
        } else if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value);
        } else {
            cell.setCellValue(value.toString());
        }
    }
    
    /**
     * 获取单元格值
     */
    private static Object getCellValue(Cell cell) {
        if (cell == null) return null;
        
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> {
                double num = cell.getNumericCellValue();
                yield num == Math.floor(num) ? (long) num : num;
            }
            case BOOLEAN -> cell.getBooleanCellValue();
            case FORMULA -> cell.getCellFormula();
            default -> null;
        };
    }
}