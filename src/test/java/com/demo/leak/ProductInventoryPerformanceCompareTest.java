package com.demo.leak;

import com.demo.leak.service.ProductInventory;
import com.demo.leak.service.ProductInventoryService;
import com.demo.leak.service.SynchronizedProductInventory;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * AtomicInteger vs synchronized 性能对比（1000 并发购买）。
 */
class ProductInventoryPerformanceCompareTest {

    private static final int THREAD_COUNT = 1000;
    private static final int WARMUP_ROUNDS = 3;
    private static final int BENCHMARK_ROUNDS = 5;

    @Test
    void compareAtomicAndSynchronizedPerformance() throws Exception {
        ProductInventory atomicInventory = new ProductInventoryService();
        ProductInventory synchronizedInventory = new SynchronizedProductInventory();

        System.out.println("========== 性能对比（" + THREAD_COUNT + " 并发）==========");
        System.out.println("预热 " + WARMUP_ROUNDS + " 轮...");

        for (int i = 0; i < WARMUP_ROUNDS; i++) {
            runConcurrentBuys(atomicInventory);
            runConcurrentBuys(synchronizedInventory);
        }

        long atomicBenchmarkTotal = 0;
        long synchronizedBenchmarkTotal = 0;

        for (int i = 0; i < BENCHMARK_ROUNDS; i++) {
            atomicBenchmarkTotal += runConcurrentBuys(atomicInventory);
            synchronizedBenchmarkTotal += runConcurrentBuys(synchronizedInventory);
        }

        double atomicAvg = atomicBenchmarkTotal / (double) BENCHMARK_ROUNDS;
        double synchronizedAvg = synchronizedBenchmarkTotal / (double) BENCHMARK_ROUNDS;

        System.out.println();
        System.out.println("---------- 总耗时（" + BENCHMARK_ROUNDS + " 轮合计）----------");
        System.out.println("AtomicInteger  总耗时: " + atomicBenchmarkTotal + " ms");
        System.out.println("synchronized   总耗时: " + synchronizedBenchmarkTotal + " ms");
        System.out.println();
        System.out.println("---------- 平均耗时（每轮 " + THREAD_COUNT + " 并发）----------");
        System.out.println("AtomicInteger  平均耗时: " + formatMs(atomicAvg));
        System.out.println("synchronized   平均耗时: " + formatMs(synchronizedAvg));
        System.out.println();

        long totalDiff = Math.abs(atomicBenchmarkTotal - synchronizedBenchmarkTotal);
        if (atomicBenchmarkTotal < synchronizedBenchmarkTotal) {
            double improvement = (synchronizedBenchmarkTotal - atomicBenchmarkTotal) * 100.0 / synchronizedBenchmarkTotal;
            System.out.printf("总耗时：AtomicInteger 比 synchronized 快 %d ms（约 %.1f%%）%n",
                    totalDiff, improvement);
            System.out.printf("平均耗时：AtomicInteger 比 synchronized 快约 %.1f%%%n",
                    (synchronizedAvg - atomicAvg) / synchronizedAvg * 100);
        } else if (atomicBenchmarkTotal > synchronizedBenchmarkTotal) {
            double improvement = (atomicBenchmarkTotal - synchronizedBenchmarkTotal) * 100.0 / atomicBenchmarkTotal;
            System.out.printf("总耗时：synchronized 比 AtomicInteger 快 %d ms（约 %.1f%%）%n",
                    totalDiff, improvement);
            System.out.printf("平均耗时：synchronized 比 AtomicInteger 快约 %.1f%%%n",
                    (atomicAvg - synchronizedAvg) / atomicAvg * 100);
        } else {
            System.out.println("总耗时与平均耗时：两种方式持平");
        }
        System.out.println("====================================================");
    }

    private long runConcurrentBuys(ProductInventory inventory) throws Exception {
        inventory.reset();
        CyclicBarrier barrier = new CyclicBarrier(THREAD_COUNT + 1);
        AtomicInteger successCount = new AtomicInteger(0);
        List<Thread> threads = new ArrayList<>();

        long start = System.nanoTime();

        for (int i = 0; i < THREAD_COUNT; i++) {
            Thread thread = new Thread(() -> {
                try {
                    barrier.await();
                    if (inventory.buyOne()) {
                        successCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            threads.add(thread);
            thread.start();
        }

        barrier.await();
        for (Thread thread : threads) {
            thread.join();
        }

        long elapsedMs = (System.nanoTime() - start) / 1_000_000;

        assertEquals(100, successCount.get());
        assertEquals(100, inventory.getSoldCount() + inventory.getStock());

        return elapsedMs;
    }

    private String formatMs(double ms) {
        return String.format("%.2f ms", ms);
    }
}
