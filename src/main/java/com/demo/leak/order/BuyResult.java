package com.demo.leak.order;

public record BuyResult(boolean success, int remainingStock, String message) {

    public static BuyResult success(int remainingStock) {
        return new BuyResult(true, remainingStock, "购买成功");
    }

    public static BuyResult fail(int remainingStock) {
        return new BuyResult(false, remainingStock, "库存不足");
    }
}
