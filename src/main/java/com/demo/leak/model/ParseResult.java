package com.demo.leak.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Excel 解析结果，单次上传会生成一个较大的对象实例。
 */
public class ParseResult {

    private final String resultId;
    private final String userId;
    private final String fileName;
    private final LocalDateTime parsedAt;
    private final int totalRows;
    private final List<ExcelRow> rows;
    private final byte[] rawPayload;
    private final String summary;

    public ParseResult(String userId, String fileName, int totalRows,
                       List<ExcelRow> rows, byte[] rawPayload, String summary) {
        this.resultId = UUID.randomUUID().toString();
        this.userId = userId;
        this.fileName = fileName;
        this.parsedAt = LocalDateTime.now();
        this.totalRows = totalRows;
        this.rows = new ArrayList<>(rows);
        this.rawPayload = rawPayload;
        this.summary = summary;
    }

    public String getResultId() {
        return resultId;
    }

    public String getUserId() {
        return userId;
    }

    public String getFileName() {
        return fileName;
    }

    public LocalDateTime getParsedAt() {
        return parsedAt;
    }

    public int getTotalRows() {
        return totalRows;
    }

    public List<ExcelRow> getRows() {
        return rows;
    }

    public byte[] getRawPayload() {
        return rawPayload;
    }

    public String getSummary() {
        return summary;
    }

    public long estimateSizeBytes() {
        long size = rawPayload.length;
        for (ExcelRow row : rows) {
            size += row.estimateSizeBytes();
        }
        return size;
    }
}
