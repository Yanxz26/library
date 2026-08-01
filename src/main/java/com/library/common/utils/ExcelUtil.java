package com.library.common.utils;

import cn.hutool.core.util.StrUtil;
import com.library.common.exception.BusinessException;
import com.library.common.result.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Excel工具类
 *
 * @author Library Team
 */
@Slf4j
public class ExcelUtil {

    /**
     * 导出Excel
     */
    public static <T> void export(HttpServletResponse response, String fileName,
                                   List<T> dataList, String[] headers, String[] fields) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(fileName);

            // 创建表头样式
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            // 写入表头
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // 写入数据
            for (int i = 0; i < dataList.size(); i++) {
                Row row = sheet.createRow(i + 1);
                T data = dataList.get(i);
                for (int j = 0; j < fields.length; j++) {
                    Cell cell = row.createCell(j);
                    try {
                        Field field = getField(data.getClass(), fields[j]);
                        if (field != null) {
                            field.setAccessible(true);
                            Object value = field.get(data);
                            setCellValue(cell, value);
                        }
                    } catch (Exception e) {
                        cell.setCellValue("");
                    }
                }
            }

            // 自适应列宽
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                sheet.setColumnWidth(i, Math.min(sheet.getColumnWidth(i) + 2048, 65280));
            }

            // 设置响应头
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.name()).replace("+", "%20");
            response.setHeader("Content-Disposition", "attachment;filename*=UTF-8''" + encodedFileName + ".xlsx");

            workbook.write(response.getOutputStream());
        } catch (IOException e) {
            log.error("Excel导出失败", e);
            throw new BusinessException(ResultCode.FILE_EXPORT_ERROR);
        }
    }

    /**
     * 从Excel读取数据
     */
    public static List<List<String>> read(InputStream inputStream) {
        List<List<String>> result = new ArrayList<>();
        try (Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            for (int i = 0; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }
                List<String> rowData = new ArrayList<>();
                for (int j = 0; j < row.getLastCellNum(); j++) {
                    Cell cell = row.getCell(j);
                    rowData.add(getCellValue(cell));
                }
                result.add(rowData);
            }
        } catch (IOException e) {
            log.error("Excel读取失败", e);
            throw new BusinessException(ResultCode.FILE_FORMAT_ERROR, "请上传正确的Excel文件");
        }
        return result;
    }

    private static String getCellValue(Cell cell) {
        if (cell == null) {
            return "";
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                }
                double value = cell.getNumericCellValue();
                if (value == Math.floor(value) && !Double.isInfinite(value)) {
                    return String.valueOf((long) value);
                }
                return String.valueOf(value);
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return cell.getStringCellValue();
                } catch (Exception e) {
                    return String.valueOf(cell.getNumericCellValue());
                }
            default:
                return "";
        }
    }

    private static void setCellValue(Cell cell, Object value) {
        if (value == null) {
            cell.setCellValue("");
        } else if (value instanceof String) {
            cell.setCellValue((String) value);
        } else if (value instanceof Integer || value instanceof Long) {
            cell.setCellValue(((Number) value).doubleValue());
        } else if (value instanceof Double || value instanceof Float) {
            cell.setCellValue(((Number) value).doubleValue());
        } else if (value instanceof Date) {
            cell.setCellValue((Date) value);
        } else if (value instanceof LocalDateTime) {
            cell.setCellValue((LocalDateTime) value);
        } else if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value);
        } else {
            cell.setCellValue(value.toString());
        }
    }

    private static Field getField(Class<?> clazz, String fieldName) {
        while (clazz != null && clazz != Object.class) {
            try {
                return clazz.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        return null;
    }
}
