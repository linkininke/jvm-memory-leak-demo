package com.demo.leak.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.gc")
public class GcPressureProperties {

    /** 每次请求创建的临时对象块数（触发 Young GC） */
    private int tempChunksPerRequest = 80;

    /** 每个临时块大小（KB） */
    private int tempChunkSizeKb = 256;

    /** mixed 模式：每次请求晋升到长期存活区的对象数 */
    private int promotedObjectsPerRequest = 40;

    /** mixed 模式：每个晋升对象大小（KB） */
    private int promotedChunkSizeKb = 2048;

    /** full 模式：每次请求晋升对象数（专用于填满老年代、触发 FGC） */
    private int fullGcPromotedObjectsPerRequest = 60;

    /** full 模式：每个晋升对象大小（KB） */
    private int fullGcPromotedChunkSizeKb = 4096;

    /** 每 N 批临时对象保留 1 批，模拟 Survivor 区晋升至 Old Gen */
    private int survivorRetainEveryNthBatch = 10;

    public int getTempChunksPerRequest() {
        return tempChunksPerRequest;
    }

    public void setTempChunksPerRequest(int tempChunksPerRequest) {
        this.tempChunksPerRequest = tempChunksPerRequest;
    }

    public int getTempChunkSizeKb() {
        return tempChunkSizeKb;
    }

    public void setTempChunkSizeKb(int tempChunkSizeKb) {
        this.tempChunkSizeKb = tempChunkSizeKb;
    }

    public int getPromotedObjectsPerRequest() {
        return promotedObjectsPerRequest;
    }

    public void setPromotedObjectsPerRequest(int promotedObjectsPerRequest) {
        this.promotedObjectsPerRequest = promotedObjectsPerRequest;
    }

    public int getPromotedChunkSizeKb() {
        return promotedChunkSizeKb;
    }

    public void setPromotedChunkSizeKb(int promotedChunkSizeKb) {
        this.promotedChunkSizeKb = promotedChunkSizeKb;
    }

    public int getFullGcPromotedObjectsPerRequest() {
        return fullGcPromotedObjectsPerRequest;
    }

    public void setFullGcPromotedObjectsPerRequest(int fullGcPromotedObjectsPerRequest) {
        this.fullGcPromotedObjectsPerRequest = fullGcPromotedObjectsPerRequest;
    }

    public int getFullGcPromotedChunkSizeKb() {
        return fullGcPromotedChunkSizeKb;
    }

    public void setFullGcPromotedChunkSizeKb(int fullGcPromotedChunkSizeKb) {
        this.fullGcPromotedChunkSizeKb = fullGcPromotedChunkSizeKb;
    }

    public int getSurvivorRetainEveryNthBatch() {
        return survivorRetainEveryNthBatch;
    }

    public void setSurvivorRetainEveryNthBatch(int survivorRetainEveryNthBatch) {
        this.survivorRetainEveryNthBatch = survivorRetainEveryNthBatch;
    }
}
