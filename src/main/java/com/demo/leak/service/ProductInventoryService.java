package com.demo.leak.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * AtomicInteger 版本库存服务：无 synchronized，CAS 保证线程安全。
 */
@Service
public class ProductInventoryService implements ProductInventory {

    private static final Logger log = LoggerFactory.getLogger(ProductInventoryService.class);
    private static final int INITIAL_STOCK = 100;

    private final AtomicInteger stock = new AtomicInteger(INITIAL_STOCK);
    private final AtomicInteger soldCount = new AtomicInteger(0);

    @Override
    public boolean buyOne() {
        while (true) {
            int current = stock.get();
            if (current <= 0) {
                log.debug("购买失败，库存不足");
                return false;
            }
            if (stock.compareAndSet(current, current - 1)) {
                soldCount.incrementAndGet();
                log.debug("购买成功，剩余库存={}", current - 1);
                return true;
            }
        }
    }

    @Override
    public int getStock() {
        return stock.get();
    }

    @Override
    public int getSoldCount() {
        return soldCount.get();
    }

    @Override
    public int getInitialStock() {
        return INITIAL_STOCK;
    }

    @Override
    public void reset() {
        stock.set(INITIAL_STOCK);
        soldCount.set(0);
        log.info("商品库存已重置为 {}", INITIAL_STOCK);
    }
}
