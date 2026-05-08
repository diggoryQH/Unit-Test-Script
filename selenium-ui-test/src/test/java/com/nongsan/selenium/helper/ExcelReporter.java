package com.nongsan.selenium.helper;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.nongsan.selenium.model.TestResult;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Ghi bao cao ra file Excel.
 */
public class ExcelReporter {
    private static final String REPORT_DIR = "target/test-report";
    private static final String REPORT_PATH = REPORT_DIR + "/SELENIUM_TEST_REPORT.xlsx";
    private static final String[] HEADERS = {
        "MÃ TESTCASE", "MỤC ĐÍCH TEST", "CÁC BƯỚC THỰC HIỆN", 
        "DỮ LIỆU TEST", "SCRIPT", "KẾT QUẢ MONG ĐỢI", 
        "KẾT QUẢ KIỂM THỬ", "GHI CHÚ"
    };

    private static List<TestResult> results = new ArrayList<>();

    public static synchronized void addResult(TestResult result) {
        results.add(result);
    }

    public static synchronized void flush() {
        try {
            Files.createDirectories(Paths.get(REPORT_DIR));
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Test Report");

            // Header Style
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Pass Style
            CellStyle passStyle = workbook.createCellStyle();
            passStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
            passStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Fail Style
            CellStyle failStyle = workbook.createCellStyle();
            failStyle.setFillForegroundColor(IndexedColors.ROSE.getIndex());
            failStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Create Header Row
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < HEADERS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(HEADERS[i]);
                cell.setCellStyle(headerStyle);
            }

            // Create Data Rows
            int rowNum = 1;
            for (TestResult tr : results) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(tr.getTestCaseId());
                row.createCell(1).setCellValue(tr.getPurpose());
                row.createCell(2).setCellValue(tr.getSteps());
                row.createCell(3).setCellValue(tr.getTestData());
                row.createCell(4).setCellValue(tr.getScriptMethod());
                row.createCell(5).setCellValue(tr.getExpectedResult());
                
                Cell resultCell = row.createCell(6);
                String actualResult = tr.getActualResult();
                resultCell.setCellValue(actualResult);
                
                if (actualResult != null) {
                    if (actualResult.toUpperCase().contains("PASS")) {
                        resultCell.setCellStyle(passStyle);
                    } else if (actualResult.toUpperCase().contains("FAIL")) {
                        resultCell.setCellStyle(failStyle);
                    }
                }
                
                row.createCell(7).setCellValue(tr.getNotes());
            }

            // Auto-size columns
            for (int i = 0; i < HEADERS.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Write to file
            try (FileOutputStream fos = new FileOutputStream(REPORT_PATH)) {
                workbook.write(fos);
                System.out.println("Test report generated at: " + REPORT_PATH);
            }

        } catch (IOException e) {
            System.err.println("Failed to write Excel report: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
