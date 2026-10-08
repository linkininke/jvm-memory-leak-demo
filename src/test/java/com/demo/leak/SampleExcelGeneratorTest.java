package com.demo.leak;

import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 生成项目根目录下的 sample.xlsx，便于本地压测。
 */
class SampleExcelGeneratorTest {

    @Test
    void generateSampleExcel() throws IOException {
        Path output = Path.of("sample.xlsx");
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet("products");
            XSSFRow header = sheet.createRow(0);
            header.createCell(0).setCellValue("id");
            header.createCell(1).setCellValue("name");
            header.createCell(2).setCellValue("price");

            for (int i = 1; i <= 100; i++) {
                XSSFRow row = sheet.createRow(i);
                row.createCell(0).setCellValue(i);
                row.createCell(1).setCellValue("item-" + i);
                row.createCell(2).setCellValue(9.99 + i);
            }
            workbook.write(Files.newOutputStream(output));
        }
        System.out.println("已生成: " + output.toAbsolutePath());
    }
}
