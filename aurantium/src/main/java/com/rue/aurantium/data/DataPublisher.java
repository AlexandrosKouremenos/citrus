package com.rue.aurantium.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import protobuf.Building;

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
import static com.rue.aurantium.mqtt.BuildingTopic.getBuildingTopic;
import static java.lang.Long.parseLong;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Takes a folder as an input. Read its files line by line and publish them with 1 sec delay.
 * Simulates the data produced from one building.
* */

abstract public class DataPublisher {

    public static final long PUBLISH_DELAY = parseLong(System.getProperty("publish.delay", "1"));

    private static final Logger LOG = LoggerFactory.getLogger(DataPublisher.class);

    private static final int BUILDING_ID = Integer.parseInt(System.getProperty("building.id"));

    public static boolean EOF;

    protected static boolean shuttingDown = false;

    private ScheduledExecutorService executor;

    private BufferedReader reader;

    private ApplicationContext context;

    private Deque<Path> queue;

    private final String filePath;

    public DataPublisher(String filePath) { this.filePath = filePath; }

    public void start() {

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

        context = new AnnotationConfigApplicationContext(Scheduler.class);
        executor = (ScheduledExecutorService) context.getBean(TASK_SCHEDULER);

        LOG.info("Starting publishing to topic [{}].", getBuildingTopic());

        for (Path path : queue) {

            if (shuttingDown) {
                LOG.info("Skipping other files.");
                break;
            }

            LOG.info("Handling file [{}].", path);

            try {

                reader = new BufferedReader(new FileReader(path.toFile()));

                EOF = false;

                DataParser dataParser = new DataParser(BUILDING_ID);
                String line = reader.readLine();
                CompletableFuture<?> future = schedulePublish(line, dataParser);

                while (!EOF && !shuttingDown) {

                    if (future == null || future.isDone()) {

                        line = reader.readLine();
                        if (line == null) EOF = true;
                        else future = schedulePublish(line, dataParser);

                    }

                }

                LOG.info("File [{}] handled.", path);
                LOG.info("Closing reader.");
                reader.close();

            } catch (IOException e) { throw new RuntimeException(e); }

        }

        LOG.info("Shutting down executor.");
        executor.shutdown();
        shutdown();

    }

    private CompletableFuture<?> schedulePublish(String line, DataParser dataParser) {

        Building building = dataParser.parseData(line);
        if (building == null) return null;

        publish(building);
        Executor delayedExecutor = CompletableFuture.delayedExecutor(PUBLISH_DELAY, SECONDS, executor);

        return CompletableFuture.runAsync(() -> {}, delayedExecutor); // Dummy Delay

    }

    abstract protected void publish(Building building);

    abstract protected void shutdown();

}
