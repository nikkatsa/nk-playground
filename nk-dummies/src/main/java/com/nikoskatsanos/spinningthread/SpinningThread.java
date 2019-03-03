package com.nikoskatsanos.spinningthread;

import com.nikoskatsanos.jutils.core.threading.NamedThreadFactory;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * Simple java program that intends to generate a high CPU utilication thread. Made for blog post purposes <a href="https://nikoskatsanos.com/blog/2019/03/03/identifying-a-high-cpu-java-thread/">Identifying
 * A High CPU Java Thread</a>
 */
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

        final ExecutorService spinner = Executors.newSingleThreadExecutor(new NamedThreadFactory("Spinner", true));
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
