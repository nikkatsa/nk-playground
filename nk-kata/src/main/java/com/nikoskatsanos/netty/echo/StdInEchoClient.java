package com.nikoskatsanos.netty.echo;

import com.nikoskatsanos.jutils.core.threading.NamedThreadFactory;
import com.nikoskatsanos.nkjutils.yalf.YalfLogger;

import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author nikkatsa
 */
public class StdInEchoClient extends EchoClient {

    private static final YalfLogger log = YalfLogger.getLogger(StdInEchoClient.class);

    private final ExecutorService stdInReader = Executors.newSingleThreadExecutor(new NamedThreadFactory
            ("StdInReader", true));

    public StdInEchoClient() {
        this(8080);
    }

    public StdInEchoClient(final int port) {
        super(port);
    }

    @Override
    protected void echoClientStarted() {
        stdInReader.submit(() -> acceptStdIn());
    }

    @Override
    protected void echoClientStopped() {
        if (!this.stdInReader.isTerminated()) {
            try {
                this.stdInReader.awaitTermination(0, TimeUnit.MILLISECONDS);
            } catch (final InterruptedException e) {
            }
        }

        if (!this.stdInReader.isShutdown()) {
            this.stdInReader.shutdownNow();
        }
    }

    private void acceptStdIn() {
        log.info("Listening to StdIn");

        final Scanner in = new Scanner(System.in);
        String line;
        do {
            line = in.nextLine().replace("\n", "");

            this.echoClientHandler.send(line);
        } while (!"EXIT".equalsIgnoreCase(line));
    }

    public static void main(final String... args) {
        try {
            new StdInEchoClient().start();
        } catch (final InterruptedException e) {
            log.error(e.getMessage(), e);
        }
    }
}
