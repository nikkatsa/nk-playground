package com.nikoskatsanos.spinningthread;

import com.nikoskatsanos.jutils.core.threading.NamedThreadFactory;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class SpinningThread {

    private volatile double random;

    public void spin() {
        while (true) {
            this.random = ThreadLocalRandom.current().nextDouble();
        }
    }

    public double getRandom() {
        return this.random;
    }

    public static void main(final String... args) {
        final SpinningThread spinningThread = new SpinningThread();

        final ExecutorService spinner = Executors.newSingleThreadExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(final Runnable r) {
                final Thread t = new Thread(r, "Spinner");
                t.setDaemon(true);
                return t;
            }
        });
        spinner.execute(spinningThread::spin);

        while (true) {
            System.out.println(spinningThread.getRandom());
            try {
                TimeUnit.MILLISECONDS.sleep(10_000L);
            } catch (final InterruptedException e) {
                System.exit(0);
            }
        }
    }
}
