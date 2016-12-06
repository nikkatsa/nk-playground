package com.nikoskatsanos.jdk9.process;

import com.nikoskatsanos.jutils.core.threading.NamedThreadFactory;
import com.nikoskatsanos.nkjutils.yalf.YalfLogger;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * <p>Simple example on Java 9 {@link java.lang.ProcessHandle} API features.</p>
 * <p>A caller can do the following:<ul>
 * <li>Spawn sub processes either short running or long running</li>
 * <li>An {@code onExit} event is scheduled for each process to notify the parent</li>
 * <li>List all sub processes</li>
 * <li>Kill all sub processes</li>
 * </ul></p>
 *
 * @author nikkatsa
 */
public class ProcessOrchestrator implements Runnable {

    private static final YalfLogger log = YalfLogger.getLogger(ProcessOrchestrator.class);

    private final Scanner input;
    private boolean keepReading = true;

    private final ExecutorService procsMonitor;

    public ProcessOrchestrator() {
        this.input = new Scanner(System.in);
        log.info(new File("").getAbsolutePath().toString());
        log.info(ProcessHandle.current().info().toString());
        this.procsMonitor = Executors.newCachedThreadPool(new NamedThreadFactory("LongRunningProcMonitor"));
    }

    private final String getOptions() {
        final StringBuilder optionsBuilder = new StringBuilder("What would you like to do?").append('\n');
        optionsBuilder.append("----------------").append('\n');
        optionsBuilder.append("1. Spawn a long running sub process").append('\n');
        optionsBuilder.append("2. Spawn a short running sub process").append('\n');
        optionsBuilder.append("3. List sub processes").append('\n');
        optionsBuilder.append("4. Kill all sub processes").append('\n');
        optionsBuilder.append("5. Exit").append('\n');
        optionsBuilder.append("----------------").append('\n');
        return optionsBuilder.toString();
    }

    private final void doAction(final int choice) {
        switch (choice) {
            case 1:
                this.startLongRunningProcess();
                break;
            case 2:
                this.startShortRunningProcess();
                break;
            case 3:
                this.listSubProcesses();
                break;
            case 4:
                this.killSubProcesses();
                break;
            case 5:
                this.keepReading = false;
                break;
            default:
                log.warn("No action mapped to choice %d. Please enter a number.", choice);
        }
    }

    private final void startShortRunningProcess() {
        this.createAndStartProcess(ShortRunningProcess.class.getName());
    }

    private final void startLongRunningProcess() {
        this.createAndStartProcess(LongRunningProcess.class.getName());
    }

    /**
     * <p>Kills all sub processes by retrieving a handle to the children processes from {@link java.lang.ProcessHandle}
     * API</p>
     */
    private void killSubProcesses() {
        log.warn("Destroying %d processes", ProcessHandle.current().children().count());
        ProcessHandle.current().children().forEach(ProcessHandle::destroy);
    }

    /**
     * <p>Lists all sub processes by retrieving a handle to them from {@link ProcessHandle} API</p>
     */
    private final void listSubProcesses() {
        final StringBuilder builder = new StringBuilder();
        builder.append("Parent PID: ").append(ProcessHandle.current().getPid()).append("\n");
        final String children = ProcessHandle.current().children().map(ProcessHandle::getPid).map(String::valueOf)
                .collect(Collectors.joining(", "));
        builder.append("Children: ").append(children).append("\n");

        log.info(builder.toString());
    }

    /**
     * <p>Starts a {@link java.lang.Process} and adds an {@code onExit} event on it</p>
     *
     * @param mainClassName The java process' main class
     */
    private final void createAndStartProcess(final String mainClassName) {
        log.info("Starting process %s", mainClassName);

        final String javaCmd = this.getJavaCmdFromParent();
        final String classpath = this.getClassPathFromParent();

        try {
            final Process proc = new ProcessBuilder(javaCmd, "-cp", classpath, mainClassName).start();
            proc.onExit().whenComplete((p, e) -> {
                log.warn("Process %d has terminated", p.getPid());
            });

            final InputStream in = proc.getInputStream();
            this.monitorIO(in);

            final InputStream err = proc.getErrorStream();
            this.monitorIO(err);

        } catch (final IOException e) {
            log.error("", e);
        }
    }

    private final void monitorIO(final InputStream io) {
        this.procsMonitor.submit(() -> {
            try (final BufferedReader inReader = new BufferedReader(new InputStreamReader(io))) {
                String line = null;
                while ((line = inReader.readLine()) != null) {
                    log.info(line);
                }
            } catch (final IOException e) {
                log.error("", e);
            }
        });
    }

    @Override
    public void run() {
        final String options = this.getOptions();
        log.info(options);

        do {
            try {
                int choice = Integer.parseInt(this.input.nextLine());
                this.doAction(choice);
            } catch (final NumberFormatException ex) {
                log.warn("Please enter the number representing your choice");
            }
        } while (keepReading);

        log.info("Exiting...");
        this.input.close();

        this.procsMonitor.shutdownNow();
    }

    public final String getClassPathFromParent() {
        return System.getProperty("java.class.path", "./*");
    }

    public final String getJavaCmdFromParent() {
        return Objects.isNull(System.getProperty("java.home")) ? "java" : String.format("%s%sbin%sjava", System
                .getProperty("java.home"), File.separator, File.separator);
    }

    public static void main(final String... args) {
        final ExecutorService optionReaderLoop = Executors.newSingleThreadExecutor(new NamedThreadFactory
                ("OptionReaderLoop", true));
        optionReaderLoop.submit(new ProcessOrchestrator());

        optionReaderLoop.shutdown();
        do {
            try {
                optionReaderLoop.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
            } catch (final InterruptedException e) {
                log.warn(e.getMessage(), e);
            }
        } while (!optionReaderLoop.isTerminated());
    }

}
