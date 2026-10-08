package com.demo.leak.service;

import com.demo.leak.config.VisibilityProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * volatile 可见性演示：
 * <ul>
 *   <li>useVolatile=false：工作线程可能永远看不到 flag 变为 false</li>
 *   <li>useVolatile=true ：工作线程能立即看到 flag 变化并退出循环</li>
 * </ul>
 */
@Service
public class VisibilityDemoService {

    private static final Logger log = LoggerFactory.getLogger(VisibilityDemoService.class);

    private final VisibilityProperties properties;

    /** 无 volatile：主线程修改后，工作线程可能看不到 */
    private boolean flagPlain = true;

    /** 有 volatile：保证跨线程可见 */
    private volatile boolean flagVolatile = true;

    private Thread workerThread;
    private volatile String workerState = "IDLE";

    public VisibilityDemoService(VisibilityProperties properties) {
        this.properties = properties;
    }

    public synchronized void startWorker() {
        if (workerThread != null && workerThread.isAlive()) {
            log.warn("工作线程已在运行");
            return;
        }

        resetFlags();
        workerState = "WAITING";

        workerThread = new Thread(this::waitForFlagChange, "visibility-worker");
        workerThread.start();
        log.info("工作线程已启动，模式={}", currentMode());
    }

    private void waitForFlagChange() {
        log.info("工作线程进入 while 循环，等待 flag 变为 false，模式={}", currentMode());
        while (readFlag()) {
            Thread.onSpinWait();
        }
        workerState = "STOPPED";
        log.info("工作线程已退出循环，模式={}", currentMode());
    }

    public void setFlagFalse() {
        if (properties.isUseVolatile()) {
            flagVolatile = false;
            log.info("主线程已将 flagVolatile 设为 false");
        } else {
            flagPlain = false;
            log.info("主线程已将 flagPlain 设为 false");
        }
    }

    public synchronized void reset() {
        if (workerThread != null && workerThread.isAlive()) {
            workerThread.interrupt();
        }
        workerThread = null;
        resetFlags();
        workerState = "IDLE";
        log.info("可见性演示已重置");
    }

    private void resetFlags() {
        flagPlain = true;
        flagVolatile = true;
    }

    private boolean readFlag() {
        return properties.isUseVolatile() ? flagVolatile : flagPlain;
    }

    public VisibilityStatus getStatus() {
        boolean flagValue = readFlag();
        boolean alive = workerThread != null && workerThread.isAlive();
        return new VisibilityStatus(
                currentMode(),
                properties.isUseVolatile(),
                flagValue,
                workerState,
                alive,
                workerThread != null ? workerThread.getName() : null
        );
    }

    private String currentMode() {
        return properties.isUseVolatile() ? "volatile" : "plain";
    }

    public record VisibilityStatus(
            String mode,
            boolean useVolatile,
            boolean flag,
            String workerState,
            boolean workerAlive,
            String workerThreadName
    ) {
    }
}
