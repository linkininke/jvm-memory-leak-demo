package com.demo.leak.controller;

import com.demo.leak.service.ProductInventoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/order")
public class OrderController {

    private final ProductInventoryService inventoryService;

    public OrderController(ProductInventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping("/buy")
    public ResponseEntity<Map<String, Object>> buy() {
        boolean success = inventoryService.buyOne();
        return ResponseEntity.ok(Map.of(
                "success", success,
                "stock", inventoryService.getStock(),
                "soldCount", inventoryService.getSoldCount(),
                "message", success ? "购买成功" : "库存不足"
        ));
    }

    @GetMapping("/stock")
    public Map<String, Object> stock() {
        return Map.of(
                "initialStock", inventoryService.getInitialStock(),
                "stock", inventoryService.getStock(),
                "soldCount", inventoryService.getSoldCount()
        );
    }

    @PostMapping("/reset")
    public Map<String, Object> reset() {
        inventoryService.reset();
        return Map.of(
                "status", "ok",
                "stock", inventoryService.getStock(),
                "message", "库存已重置为 100"
        );
    }
}
