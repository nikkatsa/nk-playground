package com.nikoskatsanos.jdk9.process;

import com.nikoskatsanos.nkjutils.yalf.YalfLogger;

import java.time.LocalTime;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author nikkatsa
 */
public class ShortRunningProcess implements Runnable {

    private static final YalfLogger log = YalfLogger.getLogger(ShortRunningProcess.class);

    @Override
    public void run() {
        int count = 0;
        while (count++ < 5) {
            log.info("PID: %d, IsAlive: %b, Time: %s", ProcessHandle.current().getPid(), ProcessHandle.current()
                    .isAlive(), LocalTime.now().toString());
            try {
                TimeUnit.MILLISECONDS.sleep(1000L);
            } catch (final InterruptedException e) {
                // swallow
            }
        }

        System.exit(0);
    }

    public static void main(final String... args) {
        Executors.newSingleThreadExecutor().submit(new ShortRunningProcess());
    }
}
