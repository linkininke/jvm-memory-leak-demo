package com.demo.leak.controller;

import com.demo.leak.service.GcPressureService;
import com.demo.leak.service.GcPressureService.GcTarget;
import com.demo.leak.support.GcRetentionStore;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping
public class GcTestController {

    private final GcPressureService gcPressureService;
    private final GcRetentionStore retentionStore;

    public GcTestController(GcPressureService gcPressureService, GcRetentionStore retentionStore) {
        this.gcPressureService = gcPressureService;
        this.retentionStore = retentionStore;
    }

    /**
     * @param target mixed=Young+晋升（默认）, young=仅 Young GC, full=快速填满老年代触发 FGC
     */
    @GetMapping("/gc/test")
    public ResponseEntity<Map<String, Object>> gcTest(
            @RequestParam(defaultValue = "mixed") String target) {

        GcTarget gcTarget = parseTarget(target);
        GcPressureService.GcPressureResult result = gcPressureService.simulateGcPressure(gcTarget);

        return ResponseEntity.ok(Map.of(
                "status", "ok",
                "target", result.target(),
                "ephemeralBytesAllocated", result.ephemeralBytesAllocated(),
                "survivorPromotedBytes", result.survivorPromotedBytes(),
                "promotedBytesAllocated", result.promotedBytesAllocated(),
                "tempChunks", result.tempChunks(),
                "promotedObjects", result.promotedObjects(),
                "totalRetainedObjects", result.totalRetainedObjects(),
                "totalRetainedBytes", result.totalRetainedBytes(),
                "message", messageFor(gcTarget)
        ));
    }

    @GetMapping("/gc/stats")
    public Map<String, Object> gcStats() {
        return Map.of(
                "retainedObjects", retentionStore.promotedCount(),
                "retainedBytes", retentionStore.promotedBytes()
        );
    }

    private GcTarget parseTarget(String target) {
        return switch (target.toLowerCase(Locale.ROOT)) {
            case "young" -> GcTarget.YOUNG;
            case "full" -> GcTarget.FULL;
            case "mixed" -> GcTarget.MIXED;
            default -> throw new IllegalArgumentException("target 仅支持 mixed / young / full");
        };
    }

    private String messageFor(GcTarget target) {
        return switch (target) {
            case YOUNG -> "已制造 Young GC 压力（不写入长期持有区）";
            case FULL -> "已向老年代快速写入大对象，连续请求可触发 Full GC";
            case MIXED -> "已制造 Young GC 压力，并晋升部分对象至老年代";
        };
    }
}
