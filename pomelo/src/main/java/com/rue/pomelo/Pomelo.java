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

    static {

        PROPS = new Properties();
        PROPS.setProperty(BOOTSTRAP_SERVERS_CONFIG, System.getenv("kafka.bootstrap.servers"));
        PROPS.setProperty(GROUP_ID_CONFIG, System.getenv("kafka.group.id"));

        PROPS.setProperty(APPLICATION_ID_CONFIG, APPLICATION_ID);
        PROPS.setProperty(STATESTORE_CACHE_MAX_BYTES_CONFIG, STATE_STORE_CACHE_MAX_BYTES);
        PROPS.setProperty(NUM_STREAM_THREADS_CONFIG, System.getenv("num.threads"));

        PROPS.setProperty(AUTO_OFFSET_RESET_CONFIG, "earliest");
        PROPS.setProperty(ENABLE_AUTO_COMMIT_CONFIG, "true");
        PROPS.setProperty(AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");

        PROPS.setProperty(KAFKA_TOPIC_PREFIX, System.getenv("kafka.topic"));

    }

    public Pomelo() { }

    @Bean(destroyMethod = "shutdown")
    private PomeloClient getPomeloClient() { return new PomeloClient(PROPS); }

}
