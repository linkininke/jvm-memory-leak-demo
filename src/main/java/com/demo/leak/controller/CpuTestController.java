package com.demo.leak.controller;

import com.demo.leak.service.DiagnosticSnapshotService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping
public class CpuTestController {

    private final DiagnosticSnapshotService diagnosticSnapshotService;

    public CpuTestController(DiagnosticSnapshotService diagnosticSnapshotService) {
        this.diagnosticSnapshotService = diagnosticSnapshotService;
    }

    @GetMapping("/cpu/test")
    public ResponseEntity<Map<String, Object>> cpuTest() {
        String taskId = diagnosticSnapshotService.triggerSnapshotRebuild();
        return ResponseEntity.ok(Map.of(
                "status", "accepted",
                "taskId", taskId,
                "message", "诊断快照任务已提交，请稍后通过运维面板查看结果"
        ));
    }
}
