package com.demo.leak.model;

import java.util.HashMap;
import java.util.Map;

public class ExcelRow {

    private final int rowIndex;
    private final Map<String, String> cells;
    private final String checksum;

    public ExcelRow(int rowIndex, Map<String, String> cells, String checksum) {
        this.rowIndex = rowIndex;
        this.cells = new HashMap<>(cells);
        this.checksum = checksum;
    }

    public int getRowIndex() {
        return rowIndex;
    }

    public Map<String, String> getCells() {
        return cells;
    }

    public String getChecksum() {
        return checksum;
    }

    public long estimateSizeBytes() {
        long size = 32;
        for (Map.Entry<String, String> entry : cells.entrySet()) {
            size += entry.getKey().length() * 2L;
            size += entry.getValue().length() * 2L;
        }
        return size + checksum.length() * 2L;
    }
}
