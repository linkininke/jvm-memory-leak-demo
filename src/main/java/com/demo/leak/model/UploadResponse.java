package com.demo.leak.model;

import java.time.LocalDateTime;

public record UploadResponse(
        String resultId,
        String userId,
        String fileName,
        int totalRows,
        long estimatedSizeBytes,
        String cacheKey,
        LocalDateTime parsedAt,
        String message
) {
}
