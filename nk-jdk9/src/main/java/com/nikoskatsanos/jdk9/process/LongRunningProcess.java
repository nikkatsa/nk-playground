package com.nikoskatsanos.jdk9.process;

import com.nikoskatsanos.nkjutils.yalf.YalfLogger;

import java.time.LocalTime;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author nikkatsa
 */
public class LongRunningProcess implements Runnable {

    private static final YalfLogger log = YalfLogger.getLogger(LongRunningProcess.class);

    @Override
    public void run() {
        while (true) {
            log.info("PID: %d, IsAlive: %b, Time: %s", ProcessHandle.current().getPid(), ProcessHandle.current()
                    .isAlive(), LocalTime.now().toString());
            try {
                TimeUnit.MILLISECONDS.sleep(5000L);
            } catch (final InterruptedException e) {
                // swallow
            }
        }
    }

    public static void main(final String... args) {
        Executors.newSingleThreadExecutor().submit(new LongRunningProcess());
    }
}
