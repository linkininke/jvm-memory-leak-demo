package com.demo.leak.service;

import com.demo.leak.cache.ParseResultCache;
import com.demo.leak.support.ParseSummaryIndexer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * 解析结果诊断快照：对全局缓存中的数据进行一致性校验与摘要重建。
 */
@Service
public class DiagnosticSnapshotService {

    private static final Logger log = LoggerFactory.getLogger(DiagnosticSnapshotService.class);

    private final ParseResultCache parseResultCache;
    private final ParseSummaryIndexer parseSummaryIndexer;

    public DiagnosticSnapshotService(ParseResultCache parseResultCache,
                                     ParseSummaryIndexer parseSummaryIndexer) {
        this.parseResultCache = parseResultCache;
        this.parseSummaryIndexer = parseSummaryIndexer;
    }

    public String triggerSnapshotRebuild() {
        String taskId = UUID.randomUUID().toString();
        log.info("提交诊断快照任务: taskId={}, cacheEntries={}", taskId, parseResultCache.size());
        parseSummaryIndexer.scheduleRebuild(taskId, parseResultCache);
        return taskId;
    }
}
