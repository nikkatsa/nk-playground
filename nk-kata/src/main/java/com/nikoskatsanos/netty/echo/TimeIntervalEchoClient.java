package com.nikoskatsanos.netty.echo;

import com.nikoskatsanos.jutils.core.threading.NamedThreadFactory;
import com.nikoskatsanos.nkjutils.yalf.YalfLogger;

import java.time.LocalTime;
import java.time.Period;
import java.util.Timer;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author nikkatsa
 */
public class TimeIntervalEchoClient extends EchoClient {

    private static final YalfLogger log = YalfLogger.getLogger(TimeIntervalEchoClient.class);

    private final long interval;
    private final TimeUnit timeUnit;
    private final ScheduledExecutorService intervalExecutor;

    public TimeIntervalEchoClient() {
        this(1L, TimeUnit.SECONDS);
    }

    public TimeIntervalEchoClient(final long interval, final TimeUnit timeUnit) {
        this(8080, interval, timeUnit);
    }

    public TimeIntervalEchoClient(final int port, final long interval, final TimeUnit timeUnit) {
        super(port);
        this.interval = interval;
        this.timeUnit = timeUnit;

        this.intervalExecutor = new ScheduledThreadPoolExecutor(1, new NamedThreadFactory("IntervalExecutor", true));
    }

    @Override
    protected void echoClientStarted() {
        this.intervalExecutor.scheduleAtFixedRate(() -> sendTimeInterval(), 0L, this.interval, this.timeUnit);
    }

    @Override
    protected void echoClientStopped() {
        if (!this.intervalExecutor.isTerminated()) {
            try {
                this.intervalExecutor.awaitTermination(0L, TimeUnit.MILLISECONDS);
            } catch (final InterruptedException e) {
            }
        }

        if (!this.intervalExecutor.isShutdown()) {
            this.intervalExecutor.shutdownNow();
        }
    }

    private void sendTimeInterval() {
        this.echoClientHandler.send(LocalTime.now().toString());
    }

    public static void main(final String... args) throws InterruptedException {
        new TimeIntervalEchoClient(1L, TimeUnit.MILLISECONDS).start();
    }
}
