package com.rue.aurantium.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import protobuf.Machine;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;

import static com.rue.aurantium.data.Scheduler.TASK_SCHEDULER;
import static com.rue.aurantium.mqtt.MachineTopic.getMachineTopic;
import static java.lang.Long.parseLong;
import static java.util.concurrent.CompletableFuture.delayedExecutor;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Takes a folder as an input. Read its files line by line and publish them with 1 sec delay.
 * Simulates the data produced from one building.
 * */

abstract public class DataPublisher {

    public static final String PUBLISH_DELAY = System.getenv("publish.delay");

    private static final String MACHINE_ID = System.getenv("machine.id");

    private static final Logger LOG = LoggerFactory.getLogger(DataPublisher.class);

    public static boolean EOF;

    protected static boolean shuttingDown = false;

    private ScheduledExecutorService executor;

    private final String filePath;

    public DataPublisher(String filePath) { this.filePath = filePath; }

    protected void start() {

        Deque<Path> queue;
        try {

            DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(filePath));

            queue = new ArrayDeque<>();
            for (Path path : directoryStream) queue.add(path);

            directoryStream.close();

        } catch (IOException e) {

            LOG.error("Path was not a directory.");
            shutdown();
            throw new RuntimeException(e);

        }

        ApplicationContext context = new AnnotationConfigApplicationContext(Scheduler.class);
        executor = (ScheduledExecutorService) context.getBean(TASK_SCHEDULER);

        LOG.info("Starting publishing to topic [{}].", getMachineTopic());

        BufferedReader reader = null;
        while (!queue.isEmpty()) {

            Path path = queue.pollLast();

            if (shuttingDown) {

                LOG.info("Skipping other files.");
                break;

            }

            LOG.info("Handling file [{}].", path);

            try {

                reader = new BufferedReader(new FileReader(path.toFile()));

                EOF = false;

                DataParser machineDataParser = new DataParser(MACHINE_ID);
                String line = reader.readLine();
                CompletableFuture<?> future = schedulePublish(line, machineDataParser);

                while (!EOF && !shuttingDown) {

                    if (future == null || future.isDone()) {

                        line = reader.readLine();
                        if (line == null) {

                            /* Will loop the file forever. If you only want one iteration of the
                            * file, keep only the EOF = true;
                            */
                            EOF = true;
                            queue.add(path);

                        } else future = schedulePublish(line, machineDataParser);

                    }

                }

                LOG.info("File [{}] handled.", path);

            } catch (IOException e) { throw new RuntimeException(e); }

        }

        if (reader != null) {

            try {

                LOG.info("Closing reader.");
                reader.close();

            }
            catch (IOException e) { throw new RuntimeException(e); }

        }

        LOG.info("Shutting down executor.");
        executor.shutdown();
        shutdown();

    }

    private CompletableFuture<?> schedulePublish(String line, DataParser machineDataParser) {

        Machine machine = machineDataParser.parseData(line);
        if (machine == null) return null;

        publish(machine);
        Executor delayedExecutor = delayedExecutor(parseLong(PUBLISH_DELAY), SECONDS, executor);

        return CompletableFuture.runAsync(() -> {}, delayedExecutor); // Dummy Delay

    }

    abstract protected void publish(Machine machine);

    abstract protected void shutdown();

}
