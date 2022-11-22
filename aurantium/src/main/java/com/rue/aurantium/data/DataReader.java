package com.rue.aurantium.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.Queue;
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

    public static final long PUBLISH_DELAY = parseLong(System.getProperty("publish.delay", "1"));

    private static final Logger LOG = LoggerFactory.getLogger(DataReader.class);

    private static boolean EOF;

    private final ScheduledExecutorService executorService;

    private final ApplicationContext context;

    private final Queue<Path> queue;

    public DataReader(String filePath) {

        try {

            DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(filePath));

            queue = new ArrayDeque<>();
            for (Path path : directoryStream) queue.add(path);

            directoryStream.close();

        } catch (IOException e) { throw new RuntimeException(e); }

        context = new AnnotationConfigApplicationContext(Scheduler.class);
        executorService = (ScheduledExecutorService) context.getBean(TASK_SCHEDULER);

        while (!queue.isEmpty()) {

            Path path = queue.poll();
            LOG.info("Handling file [{}].", path);

            try {

                BufferedReader reader = new BufferedReader(new FileReader(path.toFile()));
                EOF = false;

                String line = reader.readLine();
                ScheduledFuture<?> future = schedulePublish(line);

                while (!EOF) {

                    if (future.isDone()) {

                        line = reader.readLine();
                        if (line == null) EOF = true;
                        else future = schedulePublish(line);

                    }

                }

                LOG.info("File [{}] handled. Closing reader.", path);
                reader.close();

            } catch (IOException e) { throw new RuntimeException(e); }

        }

        LOG.info("Shutting down executor.");
        executorService.shutdown();

    }

    private ScheduledFuture<?> schedulePublish(String line) {

        Runnable readData = publish(line);
        return executorService.schedule(readData, PUBLISH_DELAY, SECONDS);

    }

    private static Runnable publish(String line) {

        return () -> {

            // TODO: Implement business logic
            System.out.println(line);

        };


    }

//    public static void main(String[] args) throws IOException {
//
//        DataReader dataReader = new DataReader("/home/alex/Repos/citrus/aurantium/src/main/resources/iot-data/building0-short");
//
//    }

}
