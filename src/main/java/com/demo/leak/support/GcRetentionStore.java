package com.demo.leak.support;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 长期存活对象持有区：模拟报表快照、会话缓存等会晋升至老年代的堆内数据。
 */
@Component
public class GcRetentionStore {

    private final List<byte[]> promotedSnapshots = new ArrayList<>();
    private final AtomicLong totalPromotedBytes = new AtomicLong();

    public void retain(byte[] payload) {
        promotedSnapshots.add(payload);
        totalPromotedBytes.addAndGet(payload.length);
    }

    public int promotedCount() {
        return promotedSnapshots.size();
    }

    public long promotedBytes() {
        return totalPromotedBytes.get();
    }

    public void clear() {
        promotedSnapshots.clear();
        totalPromotedBytes.set(0);
    }
}
