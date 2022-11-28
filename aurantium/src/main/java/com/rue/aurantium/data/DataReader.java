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
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import static com.rue.aurantium.data.Scheduler.TASK_SCHEDULER;
import static java.lang.Long.parseLong;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Takes a folder as an input. Read its files line by line and publish them with 1 sec delay.
 * Simulates the data produced from one building.
* */

public class DataReader {

    public static final long PUBLISH_DELAY = parseLong(System.getProperty("publish.delay", "0"));

    private static final Logger LOG = LoggerFactory.getLogger(DataReader.class);

    public static boolean EOF;

    private static int buildingId = 0;

    private ScheduledExecutorService executorService;

    private ApplicationContext context;

    private Deque<Path> queue;

    private String filePath;

    public DataReader(String filePath) {

//        this.filePath = System.getProperty("filePath");
        this.filePath = filePath;
        start();

    }

    protected void start() {

        try {

            DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(filePath));

            queue = new ArrayDeque<>();
            for (Path path : directoryStream) queue.add(path);

            directoryStream.close();

        } catch (IOException e) { throw new RuntimeException(e); }

        context = new AnnotationConfigApplicationContext(Scheduler.class);
        executorService = (ScheduledExecutorService) context.getBean(TASK_SCHEDULER);

        for (Path path : queue) {

            LOG.info("Handling file [{}].", path);

            try {

                BufferedReader reader = new BufferedReader(new FileReader(path.toFile()));
                EOF = false;

                DataParser dataParser = new DataParser(buildingId++);
                String line = reader.readLine();
                ScheduledFuture<?> future = schedulePublish(line, dataParser);

                while (!EOF) {

                    if (future == null || future.isDone()) {

                        line = reader.readLine();
                        if (line == null) EOF = true;
                        else future = schedulePublish(line, dataParser);

                    }

                }

                LOG.info("File [{}] handled. Closing reader.", path);
                reader.close();

            } catch (IOException e) { throw new RuntimeException(e); }

        }

        LOG.info("Shutting down executor.");
        executorService.shutdown();

    }

    private ScheduledFuture<?> schedulePublish(String line, DataParser dataParser) {

        Building building = dataParser.parseData(line);
        if (building == null) return null;

        Runnable publishData = publish(building);
        return executorService.schedule(publishData, PUBLISH_DELAY, SECONDS);

    }

    private Runnable publish(Building building) {

        return () -> {

            // TODO: Implement business logic.
            System.out.println(building);

        };

    }

}
