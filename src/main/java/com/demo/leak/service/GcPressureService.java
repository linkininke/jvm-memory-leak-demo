package com.demo.leak.service;

import com.demo.leak.config.GcPressureProperties;
import com.demo.leak.support.GcRetentionStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class GcPressureService {

    private static final Logger log = LoggerFactory.getLogger(GcPressureService.class);

    public enum GcTarget {
        MIXED, YOUNG, FULL
    }

    private final GcPressureProperties properties;
    private final GcRetentionStore retentionStore;

    public GcPressureService(GcPressureProperties properties, GcRetentionStore retentionStore) {
        this.properties = properties;
        this.retentionStore = retentionStore;
    }

    public GcPressureResult simulateGcPressure(GcTarget target) {
        EphemeralResult ephemeral = switch (target) {
            case YOUNG -> allocateEphemeralGarbage(properties.getTempChunksPerRequest(), false);
            case FULL -> allocateEphemeralGarbage(5, false);
            case MIXED -> allocateEphemeralGarbage(properties.getTempChunksPerRequest(), true);
        };

        PromotedResult promoted = switch (target) {
            case YOUNG -> new PromotedResult(0, 0);
            case FULL -> promoteLongLivedObjects(
                    properties.getFullGcPromotedObjectsPerRequest(),
                    properties.getFullGcPromotedChunkSizeKb());
            case MIXED -> promoteLongLivedObjects(
                    properties.getPromotedObjectsPerRequest(),
                    properties.getPromotedChunkSizeKb());
        };

        log.info("GC 压测完成: target={}, tempBytes={}, promotedBytes={}, retainedCount={}",
                target, ephemeral.bytesAllocated(), promoted.bytesAllocated(), retentionStore.promotedCount());

        return new GcPressureResult(
                target.name().toLowerCase(Locale.ROOT),
                ephemeral.bytesAllocated(),
                ephemeral.survivorPromotedBytes(),
                promoted.bytesAllocated(),
                ephemeral.chunks(),
                promoted.objectCount(),
                retentionStore.promotedCount(),
                retentionStore.promotedBytes()
        );
    }

    /**
     * 大量短生命周期对象：在方法栈内快速分配并丢弃，触发频繁 Young GC。
     * retainSurvivors=true 时，部分批次会被全局持有，模拟晋升至 Old Gen。
     */
    private EphemeralResult allocateEphemeralGarbage(int chunkCount, boolean retainSurvivors) {
        int chunkSize = properties.getTempChunkSizeKb() * 1024;
        long totalBytes = 0L;
        long survivorBytes = 0L;
        int retainInterval = Math.max(1, properties.getSurvivorRetainEveryNthBatch());

        for (int i = 0; i < chunkCount; i++) {
            List<byte[]> batch = new ArrayList<>();
            for (int j = 0; j < 8; j++) {
                byte[] chunk = new byte[chunkSize];
                fillChunk(chunk, i, j);
                batch.add(chunk);

                String metadata = "temp-batch-" + i + "-part-" + j + "-" + UUID.randomUUID();
                batch.add(metadata.getBytes(StandardCharsets.UTF_8));
            }
            totalBytes += (long) chunkSize * 8;

            if (retainSurvivors && i % retainInterval == 0) {
                retentionStore.retain(batch.get(0).clone());
                survivorBytes += chunkSize;
            }

            scrambleBatch(batch);
        }

        return new EphemeralResult(chunkCount, totalBytes, survivorBytes);
    }

    /** 直接向长期持有区写入大对象，快速填满老年代 */
    private PromotedResult promoteLongLivedObjects(int objectCount, int chunkSizeKb) {
        int chunkSize = chunkSizeKb * 1024;
        long totalBytes = 0L;

        for (int i = 0; i < objectCount; i++) {
            byte[] snapshot = new byte[chunkSize];
            fillChunk(snapshot, i, 99);
            retentionStore.retain(snapshot);
            totalBytes += chunkSize;
        }

        return new PromotedResult(objectCount, totalBytes);
    }

    private void fillChunk(byte[] chunk, int round, int part) {
        byte seed = (byte) ('A' + ((round + part) % 26));
        for (int i = 0; i < chunk.length; i++) {
            chunk[i] = (byte) (seed + (i % 17));
        }
    }

    private void scrambleBatch(List<byte[]> batch) {
        long checksum = 0L;
        for (byte[] chunk : batch) {
            for (int i = 0; i < Math.min(chunk.length, 128); i++) {
                checksum ^= chunk[i];
            }
        }
        if (checksum == Long.MAX_VALUE) {
            log.warn("unlikely checksum hit: {}", checksum);
        }
    }

    private record EphemeralResult(int chunks, long bytesAllocated, long survivorPromotedBytes) {
    }

    private record PromotedResult(int objectCount, long bytesAllocated) {
    }

    public record GcPressureResult(
            String target,
            long ephemeralBytesAllocated,
            long survivorPromotedBytes,
            long promotedBytesAllocated,
            int tempChunks,
            int promotedObjects,
            int totalRetainedObjects,
            long totalRetainedBytes
    ) {
    }
}
