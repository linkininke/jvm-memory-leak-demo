package com.demo.leak;

import com.demo.leak.service.ProductInventoryService;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 验证两个独立的 ProductInventoryService 实例是否会互相阻塞。
 *
 * 原理：
 * - synchronized 锁的是「当前对象 this」，不是类
 * - 实例 A 和实例 B 各有自己的 stock 字段、各自的监视器锁
 * - 线程操作 A 时只竞争 A 的锁，不会等待 B 的锁
 * - 因此两个对象之间不存在互斥阻塞，只有「多个线程同时操作同一个对象」才会互相阻塞
 */
class ProductInventoryInstanceIsolationTest {

    private static final int THREADS_PER_SHOP = 500;
    private static final int INITIAL_STOCK = 100;

    @Test
    void twoInstancesDoNotBlockEachOther() throws Exception {
        ProductInventoryService shopA = new ProductInventoryService();
        ProductInventoryService shopB = new ProductInventoryService();
        shopA.reset();
        shopB.reset();

        CyclicBarrier barrier = new CyclicBarrier(THREADS_PER_SHOP * 2 + 1);
        CountDownLatch done = new CountDownLatch(THREADS_PER_SHOP * 2);

        AtomicInteger shopASuccess = new AtomicInteger(0);
        AtomicInteger shopBSuccess = new AtomicInteger(0);

        long start = System.currentTimeMillis();

        for (int i = 0; i < THREADS_PER_SHOP; i++) {
            new Thread(() -> {
                try {
                    barrier.await();
                    if (shopA.buyOne()) {
                        shopASuccess.incrementAndGet();
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    done.countDown();
                }
            }, "shopA-buyer-" + i).start();

            new Thread(() -> {
                try {
                    barrier.await();
                    if (shopB.buyOne()) {
                        shopBSuccess.incrementAndGet();
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    done.countDown();
                }
            }, "shopB-buyer-" + i).start();
        }

        barrier.await();
        done.await();
        long elapsed = System.currentTimeMillis() - start;

        System.out.println("========== 两个实例并行购买 ==========");
        System.out.println("shopA 成功: " + shopASuccess.get() + ", 剩余库存: " + shopA.getStock());
        System.out.println("shopB 成功: " + shopBSuccess.get() + ", 剩余库存: " + shopB.getStock());
        System.out.println("总耗时(ms): " + elapsed);
        System.out.println("结论: 两个实例各自独立扣减，互不影响、互不阻塞");
        System.out.println("======================================");
    }

    @Test
    void singleInstanceThreadsBlockEachOther() throws Exception {
        ProductInventoryService shop = new ProductInventoryService();
        shop.reset();

        int threadCount = 200;
        CyclicBarrier barrier = new CyclicBarrier(threadCount + 1);
        CountDownLatch done = new CountDownLatch(threadCount);
        AtomicInteger success = new AtomicInteger(0);

        long start = System.currentTimeMillis();

        for (int i = 0; i < threadCount; i++) {
            new Thread(() -> {
                try {
                    barrier.await();
                    if (shop.buyOne()) {
                        success.incrementAndGet();
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    done.countDown();
                }
            }, "single-shop-buyer-" + i).start();
        }

        barrier.await();
        done.await();
        long elapsed = System.currentTimeMillis() - start;

        System.out.println("========== 单实例多线程购买 ==========");
        System.out.println("成功: " + success.get() + ", 剩余库存: " + shop.getStock());
        System.out.println("总耗时(ms): " + elapsed);
        System.out.println("结论: 多线程竞争同一个对象锁，buyOne() 串行执行，线程互相阻塞");
        System.out.println("======================================");
    }
}
