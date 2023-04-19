package com.rue.pomelo;

import com.rue.pomelo.kafka.PomeloClient;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.Properties;

import static org.apache.kafka.clients.consumer.ConsumerConfig.*;
import static org.apache.kafka.streams.StreamsConfig.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.streams.StreamsConfig.*;

@Service("pomelo")
public class Pomelo {

    public static final Properties PROPS;

    public static final String KAFKA_TOPIC_PREFIX = "kafka.topic";

    public static final String APPLICATION_ID = "pomelo-stream-client";

    public static final String STATE_STORE_CACHE_MAX_BYTES = "0";

    private static final String OPTIMIZATION_ENV = System.getenv("topology.optimize");

    private static final String NUM_THREADS_ENV = System.getenv("num.threads");

    private static final String FETCH_MAX_BYTES_ENV = System.getenv("fetch.max.bytes");

    private static final String MAX_PARTITION_FETCH_BYTES_ENV =
            System.getenv("max.partition.fetch.bytes");

    private static final String FETCH_MAX_WAIT_MS_ENV = System.getenv("fetch.max.wait.ms");

    static {

        PROPS = new Properties();
        PROPS.setProperty(BOOTSTRAP_SERVERS_CONFIG, System.getenv("kafka.bootstrap.servers"));
        PROPS.setProperty(GROUP_ID_CONFIG, System.getenv("kafka.group.id"));

        PROPS.setProperty(APPLICATION_ID_CONFIG, APPLICATION_ID);
        PROPS.setProperty(STATESTORE_CACHE_MAX_BYTES_CONFIG, STATE_STORE_CACHE_MAX_BYTES);

        if (OPTIMIZATION_ENV != null)
            PROPS.setProperty(TOPOLOGY_OPTIMIZATION_CONFIG, OPTIMIZATION_ENV);

        if (NUM_THREADS_ENV != null) PROPS.setProperty(NUM_STREAM_THREADS_CONFIG, NUM_THREADS_ENV);

        /* Consumer Configurations */
        PROPS.setProperty(AUTO_OFFSET_RESET_CONFIG, "earliest");
        PROPS.setProperty(ENABLE_AUTO_COMMIT_CONFIG, "true");
        PROPS.setProperty(AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");

        if (FETCH_MAX_BYTES_ENV != null)
            PROPS.setProperty(FETCH_MAX_BYTES_CONFIG, FETCH_MAX_BYTES_ENV);

        if (MAX_PARTITION_FETCH_BYTES_ENV != null)
            PROPS.setProperty(MAX_PARTITION_FETCH_BYTES_CONFIG, MAX_PARTITION_FETCH_BYTES_ENV);

        if (FETCH_MAX_WAIT_MS_ENV != null)
            PROPS.setProperty(FETCH_MAX_WAIT_MS_CONFIG, FETCH_MAX_WAIT_MS_ENV);

        PROPS.setProperty(KAFKA_TOPIC_PREFIX, System.getenv("kafka.topic"));

    }

    public Pomelo() { }

    @Bean(destroyMethod = "shutdown")
    private PomeloClient getPomeloClient() { return new PomeloClient(PROPS); }

}
