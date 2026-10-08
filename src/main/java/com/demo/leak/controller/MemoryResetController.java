package com.demo.leak.controller;

import com.demo.leak.cache.ParseResultCache;
import com.demo.leak.support.GcRetentionStore;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping
public class MemoryResetController {

    private final ParseResultCache parseResultCache;
    private final GcRetentionStore gcRetentionStore;

    public MemoryResetController(ParseResultCache parseResultCache,
                                 GcRetentionStore gcRetentionStore) {
        this.parseResultCache = parseResultCache;
        this.gcRetentionStore = gcRetentionStore;
    }

    /**
     * 清空应用内所有堆内持有区，并建议 JVM 执行 GC。
     * 注意：仅释放本应用持有的对象；要完全归零请重启进程。
     */
    @GetMapping("/memory/reset")
    public ResponseEntity<Map<String, Object>> resetMemory() {
        int cacheEntries = parseResultCache.size();
        long cacheBytes = parseResultCache.totalEstimatedBytes();
        int retainedObjects = gcRetentionStore.promotedCount();
        long retainedBytes = gcRetentionStore.promotedBytes();

        parseResultCache.clear();
        gcRetentionStore.clear();
        System.gc();

        return ResponseEntity.ok(Map.of(
                "status", "ok",
                "clearedCacheEntries", cacheEntries,
                "clearedCacheBytes", cacheBytes,
                "clearedRetainedObjects", retainedObjects,
                "clearedRetainedBytes", retainedBytes,
                "message", "应用内持有对象已清空。若需完全重置堆，请重启应用进程"
        ));
    }
}
