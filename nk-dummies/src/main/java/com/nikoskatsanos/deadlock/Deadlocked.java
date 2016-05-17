package com.nikoskatsanos.deadlock;

import com.nikoskatsanos.jutils.core.threading.NamedThreadFactory;
import com.nikoskatsanos.jutils.core.threading.ThreadUtils;
import com.nikoskatsanos.nkjutils.yalf.YalfLogger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * <p>Example of a deadlock</p>
 *
 * @author nikkatsa
 */
public class Deadlocked {
    private static final YalfLogger log = YalfLogger.getLogger(Deadlocked.class);

    public Deadlocked() {
    }

    public void start() {
        final ExecutorService left = Executors.newSingleThreadExecutor(new NamedThreadFactory("Left", true));
        final Object leftObject = new Object();
        final ExecutorService right = Executors.newSingleThreadExecutor(new NamedThreadFactory("Right", false));
        final Object rightObject = new Object();

        left.execute(() -> {
            synchronized (leftObject) {
                ThreadUtils.sleepWithoutInterruption(3000L, TimeUnit.MILLISECONDS);
                log.info("Got left object, trying to acquire right now");
                synchronized (rightObject) {
                    log.info("Got right object");
                    throw new RuntimeException("Shouldn't get both objects");
                }
            }
        });

        right.execute(() -> {
            synchronized (rightObject) {
                ThreadUtils.sleepWithoutInterruption(3000L, TimeUnit.MILLISECONDS);
                log.info("Got right object, trying to acquire left now");
                synchronized (leftObject) {
                    log.info("Got left object");
                    throw new RuntimeException("Shouldn't get both objects");
                }
            }
        });

        try {
            Thread.currentThread().join();
        } catch (final InterruptedException e) {
        }
    }

    public static void main(String... args) {
        new Deadlocked().start();
    }
}
