package com.demo.leak.support;

import com.demo.leak.cache.ParseResultCache;
import com.demo.leak.model.ParseResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 解析摘要索引构建器：异步重建缓存数据的校验索引，供诊断接口使用。
 */
@Component
public class ParseSummaryIndexer {

    private static final Logger log = LoggerFactory.getLogger(ParseSummaryIndexer.class);

    private final ExecutorService indexerPool = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r, "parse-summary-indexer");
        thread.setDaemon(true);
        return thread;
    });

    private final AtomicBoolean rebuildInProgress = new AtomicBoolean(false);

    public void scheduleRebuild(String taskId, ParseResultCache cache) {
        if (!rebuildInProgress.compareAndSet(false, true)) {
            log.warn("摘要索引重建已在进行中，忽略重复任务: taskId={}", taskId);
            return;
        }

        indexerPool.submit(() -> {
            try {
                rebuildIndex(taskId, cache);
            } catch (Exception ex) {
                log.error("摘要索引重建失败: taskId={}", taskId, ex);
            } finally {
                rebuildInProgress.set(false);
            }
        });
    }

    private void rebuildIndex(String taskId, ParseResultCache cache) {
        log.info("开始重建解析摘要索引: taskId={}", taskId);

        long rollingChecksum = 0L;
        int processed = 0;

        for (ParseResult result : cache.getAll()) {
            rollingChecksum ^= digestSegment(result.getSummary());
            processed++;
        }

        long indexFingerprint = finalizeIndex(rollingChecksum, processed);

        log.info("解析摘要索引重建完成: taskId={}, segments={}, fingerprint={}",
                taskId, processed, indexFingerprint);
    }

    private long finalizeIndex(long checksum, int segmentCount) {
        return digestSegment(checksum + ":" + segmentCount);
    }

    private long digestSegment(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            String hex = HexFormat.of().formatHex(hash);
            long value = 0L;
            for (int i = 0; i < hex.length(); i++) {
                value = value * 31 + hex.charAt(i);
            }
            return value;
        } catch (NoSuchAlgorithmException e) {
            return input.hashCode();
        }
    }
}
