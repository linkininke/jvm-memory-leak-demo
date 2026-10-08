package com.demo.leak.cache;

import com.demo.leak.model.ParseResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 全局解析结果缓存，按用户维度保留最近一次解析结果。
 */
@Component
public class ParseResultCache {

    private static final Logger log = LoggerFactory.getLogger(ParseResultCache.class);

    private final Map<String, ParseResult> cache = new ConcurrentHashMap<>();

    public void put(String userId, ParseResult result) {
        cache.put(userId, result);
        log.debug("缓存已更新: userId={}, resultId={}, cacheSize={}", userId, result.getResultId(), cache.size());
    }

    public ParseResult get(String userId) {
        return cache.get(userId);
    }

    public Collection<ParseResult> getAll() {
        return cache.values();
    }

    public int size() {
        return cache.size();
    }

    public long totalEstimatedBytes() {
        return cache.values().stream()
                .mapToLong(ParseResult::estimateSizeBytes)
                .sum();
    }

    public void clear() {
        cache.clear();
        log.info("解析结果缓存已清空");
    }
}
