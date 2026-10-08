package com.demo.leak.order;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 商品库存服务。
 */
@Service
public class ProductStockService {

    private static final Logger log = LoggerFactory.getLogger(ProductStockService.class);

    private static final int INITIAL_STOCK = 100;

    private int stock = INITIAL_STOCK;
    private int successCount = 0;
    private int failCount = 0;

    public BuyResult buyOne() {
        if (stock > 0) {
            stock = stock - 1;
            successCount++;
            log.debug("购买成功, 剩余库存={}", stock);
            return BuyResult.success(stock);
        }
        failCount++;
        log.debug("购买失败, 库存不足");
        return BuyResult.fail(stock);
    }

    public int getStock() {
        return stock;
    }

    public int getSuccessCount() {
        return successCount;
    }

    public int getFailCount() {
        return failCount;
    }

    public int getInitialStock() {
        return INITIAL_STOCK;
    }

    public void reset() {
        stock = INITIAL_STOCK;
        successCount = 0;
        failCount = 0;
        log.info("库存已重置: stock={}", stock);
    }
}
