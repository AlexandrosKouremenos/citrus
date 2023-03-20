package com.rue.pomelo;

import com.rue.pomelo.kafka.PomeloClient;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.Properties;

import static org.apache.kafka.clients.consumer.ConsumerConfig.*;

@Service("pomelo")
public class Pomelo {

    public static final Properties PROPS;

    public static final String GROUP_ID = System.getenv("kafka.group.id");

    public static final String SERVERS = System.getenv("kafka.bootstrap.servers");

    public static final String KAFKA_TOPIC = "kafka.topic";

    private static final String KAFKA_TOPIC_PROP = System.getenv("kafka.topic");

    static {

        PROPS = new Properties();
        PROPS.setProperty(BOOTSTRAP_SERVERS_CONFIG, SERVERS);
        PROPS.setProperty(GROUP_ID_CONFIG, GROUP_ID);

        PROPS.setProperty(KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        PROPS.setProperty(VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class.getName());

        PROPS.setProperty(AUTO_OFFSET_RESET_CONFIG, "earliest");
        PROPS.setProperty(ENABLE_AUTO_COMMIT_CONFIG, "true");
        PROPS.setProperty(AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");

        PROPS.setProperty(KAFKA_TOPIC, KAFKA_TOPIC_PROP);

    }

    private PomeloClient pomeloClient;

    public Pomelo() { }

    @Bean
    public SimpleAsyncTaskExecutor taskExecutor() { return new SimpleAsyncTaskExecutor(); }

    @Bean
    public CommandLineRunner schedulingRunner(SimpleAsyncTaskExecutor executor) {

        return args -> {

            executor.execute(pomeloClient = new PomeloClient(PROPS));
            Runtime.getRuntime().addShutdownHook(new Thread(() -> pomeloClient.shutdown()));

        };

    }

}
