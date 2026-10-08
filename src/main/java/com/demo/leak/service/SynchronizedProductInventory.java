package com.demo.leak.service;

/**
 * synchronized 版本库存服务：用于与 AtomicInteger 版本对比性能和正确性。
 */
public class SynchronizedProductInventory implements ProductInventory {

    private static final int INITIAL_STOCK = 100;

    private int stock = INITIAL_STOCK;
    private int soldCount = 0;

    @Override
    public synchronized boolean buyOne() {
        if (stock <= 0) {
            return false;
        }
        stock--;
        soldCount++;
        return true;
    }

    @Override
    public synchronized int getStock() {
        return stock;
    }

    @Override
    public synchronized int getSoldCount() {
        return soldCount;
    }

    @Override
    public int getInitialStock() {
        return INITIAL_STOCK;
    }

    @Override
    public synchronized void reset() {
        stock = INITIAL_STOCK;
        soldCount = 0;
    }
}
