package com.demo.leak.controller;

import com.demo.leak.service.VisibilityDemoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/visibility")
public class VisibilityController {

    private final VisibilityDemoService visibilityDemoService;

    public VisibilityController(VisibilityDemoService visibilityDemoService) {
        this.visibilityDemoService = visibilityDemoService;
    }

    @PostMapping("/start")
    public ResponseEntity<Map<String, Object>> start() {
        visibilityDemoService.startWorker();
        VisibilityDemoService.VisibilityStatus status = visibilityDemoService.getStatus();
        return ResponseEntity.ok(Map.of(
                "status", "ok",
                "mode", status.mode(),
                "useVolatile", status.useVolatile(),
                "workerAlive", status.workerAlive(),
                "message", "工作线程已启动，正在 while(flag) 循环等待"
        ));
    }

    @PostMapping("/stop")
    public ResponseEntity<Map<String, Object>> stop() {
        visibilityDemoService.setFlagFalse();
        VisibilityDemoService.VisibilityStatus status = visibilityDemoService.getStatus();
        return ResponseEntity.ok(Map.of(
                "status", "ok",
                "mode", status.mode(),
                "flag", status.flag(),
                "workerAlive", status.workerAlive(),
                "workerState", status.workerState(),
                "message", "已将 flag 设为 false，观察 worker 是否退出"
        ));
    }

    @GetMapping("/status")
    public VisibilityDemoService.VisibilityStatus status() {
        return visibilityDemoService.getStatus();
    }

    @PostMapping("/reset")
    public Map<String, Object> reset() {
        visibilityDemoService.reset();
        return Map.of(
                "status", "ok",
                "message", "演示已重置，可重新 /visibility/start"
        );
    }
}
