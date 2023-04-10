package com.rue.pomelo.kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.streams.StreamsConfig;

import java.util.Properties;
import java.util.Scanner;

import static com.rue.pomelo.Pomelo.KAFKA_TOPIC_PREFIX;
import static org.apache.kafka.clients.consumer.ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG;
import static org.apache.kafka.streams.StreamsConfig.NUM_STREAM_THREADS_CONFIG;

public class StreamsDemo {

    private static final String KAFKA_TOPIC = "machine";

    private static final String OUTPUT_TOPIC = "out.0";

    private static final Properties PROPS;

    static {

        PROPS = new Properties();
        PROPS.put(StreamsConfig.APPLICATION_ID_CONFIG, "streams-demo");
        PROPS.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        PROPS.setProperty(NUM_STREAM_THREADS_CONFIG, "2");

        PROPS.setProperty(StreamsConfig.STATESTORE_CACHE_MAX_BYTES_CONFIG, "0");
        PROPS.setProperty(ENABLE_AUTO_COMMIT_CONFIG, "true");
        PROPS.putIfAbsent(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        PROPS.setProperty(KAFKA_TOPIC_PREFIX, KAFKA_TOPIC);

    }

    public static void main(String[] args) {

        PomeloClient pomeloClient = new PomeloClient(PROPS);

        Runtime.getRuntime().addShutdownHook(new Thread(pomeloClient::shutdown));

        Scanner scanner = new Scanner(System.in);
        boolean r = true;
        while (r) {

            if (scanner.hasNext()) {

                String in = scanner.next();

                if (in.equals("q")) r = false;

            }

        }

        Runtime.getRuntime().exit(0);

    }

}
