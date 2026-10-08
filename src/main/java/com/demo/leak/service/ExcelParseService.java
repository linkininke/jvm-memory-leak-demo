package com.demo.leak.service;

import com.demo.leak.audit.AuditTrailService;
import com.demo.leak.cache.ParseResultCache;
import com.demo.leak.config.AppProperties;
import com.demo.leak.model.ExcelRow;
import com.demo.leak.model.ParseResult;
import com.demo.leak.model.UploadResponse;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

@Service
public class ExcelParseService {

    private static final Logger log = LoggerFactory.getLogger(ExcelParseService.class);

    private final AppProperties properties;
    private final ParseResultCache cache;
    private final AuditTrailService auditTrailService;

    public ExcelParseService(AppProperties properties,
                             ParseResultCache cache,
                             AuditTrailService auditTrailService) {
        this.properties = properties;
        this.cache = cache;
        this.auditTrailService = auditTrailService;
    }

    public UploadResponse parseAndCache(String userId, MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "unknown.xlsx";
        log.info("开始解析 Excel: userId={}, fileName={}, size={} bytes", userId, fileName, file.getSize());

        ParseResult result = parseExcel(userId, fileName, file);
        cache.put(userId, result);
        auditTrailService.onParseCompleted(result);

        log.info("解析完成: userId={}, resultId={}, rows={}, estimatedSize={} bytes",
                userId, result.getResultId(), result.getTotalRows(), result.estimateSizeBytes());

        return new UploadResponse(
                result.getResultId(),
                userId,
                fileName,
                result.getTotalRows(),
                result.estimateSizeBytes(),
                userId,
                result.getParsedAt(),
                "文件解析成功，结果已写入全局缓存"
        );
    }

    private ParseResult parseExcel(String userId, String fileName, MultipartFile file) throws IOException {
        List<ExcelRow> rows = new ArrayList<>();
        int parsedFromFile = 0;

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                if (row.getRowNum() == 0) {
                    continue;
                }
                rows.add(toExcelRow(row));
                parsedFromFile++;
            }
        } catch (Exception ex) {
            log.warn("Excel 解析异常，将使用模拟数据填充: {}", ex.getMessage());
        }

        int targetRows = Math.max(properties.getRowsPerUpload(), parsedFromFile);
        while (rows.size() < targetRows) {
            rows.add(buildSimulatedRow(rows.size() + 1));
        }

        byte[] payload = buildPayload(properties.getPayloadSizeKb());
        String summary = "user=" + userId + ", file=" + fileName + ", rows=" + rows.size();

        return new ParseResult(userId, fileName, rows.size(), rows, payload, summary);
    }

    private ExcelRow toExcelRow(Row row) {
        Map<String, String> cells = new HashMap<>();
        for (Cell cell : row) {
            cells.put("col" + cell.getColumnIndex(), cell.toString());
        }
        return new ExcelRow(row.getRowNum(), cells, checksum(cells.toString()));
    }

    private ExcelRow buildSimulatedRow(int index) {
        Map<String, String> cells = new HashMap<>();
        cells.put("id", String.valueOf(index));
        cells.put("name", "product-" + index);
        cells.put("category", "CAT-" + (index % 50));
        cells.put("price", String.valueOf(19.99 + (index % 100)));
        cells.put("remark", "simulated-row-data-" + index + "-" + "x".repeat(32));
        return new ExcelRow(index, cells, checksum(cells.toString()));
    }

    private byte[] buildPayload(int sizeKb) {
        byte[] payload = new byte[sizeKb * 1024];
        for (int i = 0; i < payload.length; i++) {
            payload[i] = (byte) ('A' + (i % 26));
        }
        return payload;
    }

    private String checksum(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(input.getBytes()));
        } catch (NoSuchAlgorithmException e) {
            return Integer.toHexString(input.hashCode());
        }
    }
}
