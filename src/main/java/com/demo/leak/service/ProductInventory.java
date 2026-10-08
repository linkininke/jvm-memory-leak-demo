package com.demo.leak.service;

/**
 * 商品库存操作接口。
 */
public interface ProductInventory {

    boolean buyOne();

    int getStock();

    int getSoldCount();

    int getInitialStock();

    void reset();
}
