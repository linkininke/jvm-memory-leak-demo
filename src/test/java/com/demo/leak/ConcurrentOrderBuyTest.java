package com.demo.leak;

import com.demo.leak.service.ProductInventory;
import com.demo.leak.service.ProductInventoryService;
import com.demo.leak.service.SynchronizedProductInventory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * AtomicInteger 版本：1000 并发购买正确性验证。
 */
@SpringBootTest
class ConcurrentOrderBuyTest {

    private static final int THREAD_COUNT = 1000;
    private static final int INITIAL_STOCK = 100;

    @Autowired
    private ProductInventoryService inventoryService;

    @Test
    void atomicVersion1000ConcurrentBuysShouldStayConsistent() throws Exception {
        runAndAssert(inventoryService, "AtomicInteger");
    }

    @Test
    void synchronizedVersion1000ConcurrentBuysShouldStayConsistent() throws Exception {
        runAndAssert(new SynchronizedProductInventory(), "synchronized");
    }

    private void runAndAssert(ProductInventory inventory, String label) throws Exception {
        inventory.reset();
        CyclicBarrier barrier = new CyclicBarrier(THREAD_COUNT + 1);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        List<Thread> threads = new ArrayList<>();

        for (int i = 0; i < THREAD_COUNT; i++) {
            Thread thread = new Thread(() -> {
                try {
                    barrier.await();
                    if (inventory.buyOne()) {
                        successCount.incrementAndGet();
                    } else {
                        failCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }, label + "-buyer-" + i);
            threads.add(thread);
            thread.start();
        }

        barrier.await();
        for (Thread thread : threads) {
            thread.join();
        }

        int stock = inventory.getStock();
        int soldCount = inventory.getSoldCount();

        System.out.println("========== " + label + " 并发购买结果 ==========");
        System.out.println("购买成功: " + successCount.get());
        System.out.println("购买失败: " + failCount.get());
        System.out.println("soldCount: " + soldCount);
        System.out.println("stock: " + stock);
        System.out.println("soldCount + stock = " + (soldCount + stock));
        System.out.println("============================================");

        assertEquals(INITIAL_STOCK, successCount.get());
        assertEquals(THREAD_COUNT - INITIAL_STOCK, failCount.get());
        assertEquals(0, stock);
        assertEquals(INITIAL_STOCK, soldCount);
        assertEquals(INITIAL_STOCK, soldCount + stock);
    }
}
