package com.demo.leak.controller;

import com.demo.leak.cache.ParseResultCache;
import com.demo.leak.model.UploadResponse;
import com.demo.leak.service.ExcelParseService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping
public class UploadController {

    private final ExcelParseService excelParseService;
    private final ParseResultCache parseResultCache;

    public UploadController(ExcelParseService excelParseService,
                            ParseResultCache parseResultCache) {
        this.excelParseService = excelParseService;
        this.parseResultCache = parseResultCache;
    }

    @PostMapping(value = "/upload/{userId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadResponse> upload(
            @PathVariable String userId,
            @RequestParam("file") MultipartFile file) throws IOException {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        UploadResponse response = excelParseService.parseAndCache(userId, file);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/cache/stats")
    public Map<String, Object> cacheStats() {
        return Map.of(
                "cacheEntries", parseResultCache.size(),
                "cacheEstimatedBytes", parseResultCache.totalEstimatedBytes()
        );
    }
}
